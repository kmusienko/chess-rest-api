package service

import model.piece.{ChessPiece, Field, PieceType}
import model.ApplicationException
import zio.{IO, ZIO}

trait PieceValidation {

  private val START_TABLE_INDEX = 0
  private val END_TABLE_INDEX   = 7

  def checkEmptyField(field: Field, pieces: List[ChessPiece]): IO[ApplicationException, Unit] = {
    ZIO.cond(
      predicate = !pieces.exists(piece => piece.field.x == field.x && piece.field.y == field.y),
      result = (),
      error = ApplicationException(s"The field (${field.x}, ${field.y}) is busy.")
    )
  }

  def checkFieldRange(field: Field): IO[ApplicationException, Unit] = {
    ZIO.cond(
      predicate =
        field.x >= START_TABLE_INDEX && field.x <= END_TABLE_INDEX && field.y >= START_TABLE_INDEX && field.y <= END_TABLE_INDEX,
      result = (),
      error =
        ApplicationException(s"The field (${field.x}, ${field.y}) is out of range. Coordinates must be from 0 to 7")
    )
  }

  def checkValidMove(
    pieceType: PieceType.Value,
    currentField: Field,
    wantedField: Field
  ): IO[ApplicationException, Unit] = {
    val predicate = pieceType match {
      case PieceType.Rook =>
        currentField.x == wantedField.x || currentField.y == wantedField.y
      case PieceType.Bishop =>
        Math.abs(currentField.x - wantedField.x) == Math.abs(currentField.y - wantedField.y)
    }

    ZIO.cond(
      predicate = predicate,
      result = (),
      error = ApplicationException(
        s"Invalid move. The $pieceType can't move to (${wantedField.x}, ${wantedField.y}) field"
      )
    )
  }
}
