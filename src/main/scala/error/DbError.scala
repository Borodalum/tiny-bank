package error

sealed trait DbError

final case class UnexpectedDbError(
  message: String
) extends DbError

final case class NotFoundDbError(
  message: String
) extends DbError