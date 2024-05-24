package model

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import model.config.CirceConfig._

sealed trait ApiResult

case class ApiResultSuccess[T](data: T) extends ApiResult

object ApiResultSuccess {

  implicit def optionalApiResultSuccessEncoder[T: Encoder]: Encoder[ApiResultSuccess[T]] =
    deriveConfiguredEncoder[ApiResultSuccess[T]]

  implicit def apiResultSuccessDecoder[T: Decoder]: Decoder[ApiResultSuccess[T]] =
    deriveConfiguredDecoder[ApiResultSuccess[T]]
}

case class ApiResultError(message: String) extends ApiResult

object ApiResultError {
  implicit val encoder: Encoder[ApiResultError] = deriveConfiguredEncoder[ApiResultError]
  implicit val decoder: Decoder[ApiResultError] = deriveConfiguredDecoder[ApiResultError]
}
