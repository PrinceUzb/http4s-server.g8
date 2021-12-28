package $package$.security

import cats.effect.{Async, Sync}
import cats.implicits._
import $package$.domain.Credentials
import $package$.domain.custom.refinements.EmailAddress
import $package$.implicits.PartOps
import $package$.security.AuthHelper._
import $package$.services.IdentityService
import eu.timepit.refined.auto.autoUnwrap
import io.circe.refined._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.multipart.Multipart
import org.http4s.{HttpRoutes, Request, Response, Status, Uri}
import tsec.authentication._
import tsec.authentication.credentials.RawCredentials
import tsec.cipher.symmetric.jca.{AES128GCM, SecretKey}
import tsec.cipher.symmetric.{AADEncryptor, IvGen}
import tsec.common.SecureRandomId

import scala.concurrent.duration.DurationInt

trait AuthService[F[_], U] {
  def authorizer(request: Multipart[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]

  def authorizer(request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]

  def securedRoutes: TokenSecHttpRoutes[F, U] => HttpRoutes[F]

  def securedRoutesWithCookie: SecHttpRoutes[F, U] => HttpRoutes[F]

  def discard(authenticator: TSecBearerToken[EmailAddress])(implicit dsl: Http4sDsl[F]): F[Response[F]]
}

object LiveAuthService {
  def apply[F[_]: Async, U](
    identityService: IdentityService[F, U]
  )(implicit F: Sync[F]): F[AuthService[F, U]] =
    F.delay(
      new LiveAuthService[F, U](identityService)
    )
}

final class LiveAuthService[F[_]: Async, U] private (
                                                      identityService: IdentityService[F, U]
                                                    )(implicit F: Sync[F])
  extends AuthService[F, U] {

  implicit val encryptor: AADEncryptor[F, AES128GCM, SecretKey] = AES128GCM.genEncryptor[F]
  implicit val gcmStrategy: IvGen[F, AES128GCM] = AES128GCM.defaultIvStrategy[F]

  private[this] val bearerTokenStore =
    dummyBackingStore[F, SecureRandomId, TSecBearerToken[EmailAddress]](s => SecureRandomId.coerce(s.id))

  private[this] val settings: TSecTokenSettings =
    TSecTokenSettings(
      expiryDuration = 30.minutes,
      maxIdle = None
    )

  private[this] val cookieSetting: TSecCookieSettings =
    TSecCookieSettings(
      secure = true,
      expiryDuration = 60.minutes, // Absolute expiration time
      maxIdle = None               // Rolling window expiration. Set this to a FiniteDuration if you intend to have one
    )

  private[this] val key: SecretKey[AES128GCM] = AES128GCM.unsafeGenerateKey //Our encryption key

  private[this] def bearerTokenAuth: BearerTokenAuthenticator[F, EmailAddress, U] =
    BearerTokenAuthenticator(
      bearerTokenStore,
      identityService,
      settings
    )

  private[this] def stateless
  : StatelessECAuthenticator[F, EmailAddress, U, AES128GCM] = //Instantiate a stateless authenticator
    EncryptedCookieAuthenticator.stateless(
      cookieSetting,
      identityService,
      key
    )

  private[this] def authWithToken: TokenSecReqHandler[F, U] = SecuredRequestHandler(bearerTokenAuth)

  private[this] def auth: SecReqHandler[F, U] = SecuredRequestHandler(stateless)

  private[this] def verify(Credentials: Credentials): F[Boolean] =
    identityService.credentialStore.isAuthenticated(RawCredentials(Credentials.email, Credentials.password))

  private[this] def createSession(credentials: Credentials): F[Response[F]] = {
    authWithToken.authenticator
      .create(credentials.email)
      .map(authWithToken.authenticator.embed(Response(Status.NoContent), _))
  }

  override def authorizer(request: Multipart[F])(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    for {
      credentials <- request.parts.convert[Credentials]
      isAuthed    <- verify(credentials)
      response <-
        if (isAuthed)
          createSession(credentials)
        else
          Forbidden("Email or password isn't correct")
    } yield response
  }

  override def authorizer(request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    for {
      credentials <- request.as[Credentials]
      isAuthed    <- verify(credentials)
      response <-
        if (isAuthed)
          createSession(credentials)
        else
          Forbidden("Email or password isn't correct")
    } yield response
  }

  override def securedRoutes: TokenSecHttpRoutes[F, U] => HttpRoutes[F] = pf =>
    authWithToken.liftService(TSecAuthService(pf))

  override def securedRoutesWithCookie: SecHttpRoutes[F, U] => HttpRoutes[F] = pf =>
    auth.liftService(TSecAuthService(pf))

  override def discard(authenticator: TSecBearerToken[EmailAddress])(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
    import dsl._
    authWithToken.authenticator.discard(authenticator).flatMap { _ =>
      SeeOther(Location(Uri.unsafeFromString("/login")))
    }
  }
}
