package $package$.stub_services

import $package$.domain.User
import $package$.domain.User.{CreateUser, UserWithPassword}
import $package$.domain.custom.refinements.EmailAddress
import $package$.domain.types.EncryptedPassword
import $package$.services.Users

class UsersStub[F[_]] extends Users[F] {
  override def find(email: EmailAddress): F[Option[UserWithPassword]]              = ???
  override def create(userParam: CreateUser, password: EncryptedPassword): F[User] = ???
}
