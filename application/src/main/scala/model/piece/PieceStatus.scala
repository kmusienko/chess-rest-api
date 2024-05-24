package model.piece

import io.circe.Codec

object PieceStatus extends Enumeration {

  val Active: PieceStatus.Value  = Value
  val Deleted: PieceStatus.Value = Value

  implicit val pieceStatusCodec: Codec[PieceStatus.Value] = Codec.codecForEnumeration(PieceStatus)
}
