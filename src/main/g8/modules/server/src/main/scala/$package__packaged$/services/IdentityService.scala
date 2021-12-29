package $package$.services

import cats.data.OptionT
import cats.effect.Sync
import $package$.db.algebras.IdentityProvider
import $package$.domain.custom.refinements.EmailAddress
import tsec.authentication.IdentityStore
import tsec.authentication.credentials.SCryptPasswordStore
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

trait IdentityService[F[_], U] extends IdentityStore[F, EmailAddress, U] {
  def get(id: EmailAddress): OptionT[F, U]
  def credentialStore: SCryptPasswordStore[F, EmailAddress]
}

object LiveIdentityService {
  def apply[F[_], U](
    identityProvider: IdentityProvider[F, U]
  )(implicit F: Sync[F]): F[LiveIdentityService[F, U]] =
    F.delay(
      new LiveIdentityService[F, U](identityProvider)
    )
}

final class LiveIdentityService[F[_], U] private (
  identityProvider: IdentityProvider[F, U]
)(implicit F: Sync[F])
  extends IdentityService[F, U] {
  override def get(id: EmailAddress): OptionT[F, U] =
    OptionT(identityProvider.findByEmail(id))

  override def credentialStore: SCryptPasswordStore[F, EmailAddress] =
    new SCryptPasswordStore[F, EmailAddress] {
      def retrievePass(id: EmailAddress): OptionT[F, PasswordHash[SCrypt]] =
        OptionT(identityProvider.retrievePass(id))
    }

}