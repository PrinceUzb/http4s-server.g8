package $package$.security

import cats.data._
import cats.effect._
import $package$.domain.custom.refinements.EmailAddress
import org.http4s._
import tsec.authentication._
import tsec.cipher.symmetric.jca._

import scala.collection.mutable

object AuthHelper {
  type TokenSecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, TSecBearerToken[EmailAddress]]
  type SecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, AuthEncryptedCookie[AES128GCM, EmailAddress]]

  type TokenSecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, TSecBearerToken[EmailAddress]], F[Response[F]]]

  type SecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, AuthEncryptedCookie[AES128GCM, EmailAddress]], F[Response[F]]]

  def dummyBackingStore[F[_], I, V](
    getId: V => I
  )(implicit F: Sync[F]): BackingStore[F, I, V] = new BackingStore[F, I, V] {
    private val storageMap = mutable.HashMap.empty[I, V]

    def put(elem: V): F[V] = {
      val map = storageMap.put(getId(elem), elem)
      if (map.isEmpty)
        F.pure(elem)
      else
        F.raiseError(new IllegalArgumentException)
    }

    def get(id: I): OptionT[F, V] =
      OptionT.fromOption[F](storageMap.get(id))

    def update(v: V): F[V] = {
      storageMap.update(getId(v), v)
      F.pure(v)
    }

    def delete(id: I): F[Unit] =
      storageMap.remove(id) match {
        case Some(_) => F.unit
        case None    => F.raiseError(new IllegalArgumentException)
      }
  }
}
