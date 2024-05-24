package model.piece

import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.syntax._
import io.circe.{Encoder, Json}
import model.piece.PieceAction._
import model.config.CirceConfig._

sealed trait PieceAction {
  def typename: String
  def timestamp: Long
}

object PieceAction {

  final val TYPENAME_FIELD = "typename"

  implicit val pieceActionEncoder: Encoder[PieceAction] = Encoder.instance {
    case action: PieceCreationAction => action.asJson
    case action: PieceMoveAction     => action.asJson
    case action: PieceDeleteAction   => action.asJson
  }
}

final case class PieceCreationAction(piece: ChessPiece, timestamp: Long) extends PieceAction {
  override val typename: String = PieceCreationAction.TYPENAME
}

object PieceCreationAction {
  final val TYPENAME = "piece_creation"

  implicit val encoder: Encoder[PieceCreationAction] = deriveConfiguredEncoder[PieceCreationAction]
    .mapJsonObject(
      _.add(TYPENAME_FIELD, Json.fromString(TYPENAME))
    )
}

final case class PieceMoveAction(pieceWas: ChessPiece, pieceBecame: ChessPiece, timestamp: Long) extends PieceAction {
  override val typename: String = PieceMoveAction.TYPENAME
}

object PieceMoveAction {
  final val TYPENAME = "piece_move"

  implicit val encoder: Encoder[PieceMoveAction] = deriveConfiguredEncoder[PieceMoveAction]
    .mapJsonObject(
      _.add(TYPENAME_FIELD, Json.fromString(TYPENAME))
    )
}

final case class PieceDeleteAction(pieceWas: ChessPiece, timestamp: Long) extends PieceAction {
  override def typename: String = PieceDeleteAction.TYPENAME

}

object PieceDeleteAction {
  final val TYPENAME = "piece_delete"

  implicit val encoder: Encoder[PieceDeleteAction] = deriveConfiguredEncoder[PieceDeleteAction]
    .mapJsonObject(
      _.add(TYPENAME_FIELD, Json.fromString(TYPENAME))
    )
}
