package $package$.db.algebras

case class Algebras[F[_]](
  user: UserAlgebra[F]
)
