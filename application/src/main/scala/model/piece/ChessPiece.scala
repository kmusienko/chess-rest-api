package model.piece

import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}
import org.bson.types.ObjectId
import model.config.CirceConfig._

final case class ChessPiece(_id: String, field: Field, `type`: PieceType.Value, status: PieceStatus.Value)

final case class ChessPieceInput(field: Field, `type`: PieceType.Value)

final case class Field(x: Int, y: Int)

object Field {
  implicit val encoder: Encoder[Field] = deriveConfiguredEncoder[Field]
  implicit val decoder: Decoder[Field] = deriveConfiguredDecoder[Field]
}

object ChessPieceInput {

  implicit val encoder: Encoder[ChessPieceInput] = deriveConfiguredEncoder[ChessPieceInput]
  implicit val decoder: Decoder[ChessPieceInput] = deriveConfiguredDecoder[ChessPieceInput]
}

object ChessPiece {

  def apply(chessPieceInput: ChessPieceInput): ChessPiece = ChessPiece(
    _id = ObjectId.get().toHexString,
    field = chessPieceInput.field,
    `type` = chessPieceInput.`type`,
    status = PieceStatus.Active
  )

  implicit val encoder: Encoder[ChessPiece] = deriveConfiguredEncoder[ChessPiece]
  implicit val decoder: Decoder[ChessPiece] = deriveConfiguredDecoder[ChessPiece]
}
