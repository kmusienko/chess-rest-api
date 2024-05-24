package model.piece

import io.circe.Codec

object PieceType extends Enumeration {

  val Rook: PieceType.Value   = Value
  val Bishop: PieceType.Value = Value

  implicit val pieceTypeCodec: Codec[PieceType.Value] = Codec.codecForEnumeration(PieceType)

}
