package $package$.stub_services

import $package$.domain.auth.{EncryptedPassword, UserId, UserName}
import $package$.http.auth.users.UserWithPassword
import $package$.services.Users

class UsersStub[F[_]] extends Users[F] {
  def find(username: UserName): F[Option[UserWithPassword]] = ???

  def create(username: UserName, password: EncryptedPassword): F[UserId] = ???
}
