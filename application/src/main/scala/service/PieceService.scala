package service

import com.google.inject.{Inject, Singleton}
import model.piece.{ChessPiece, ChessPieceInput, Field, PieceCreationAction, PieceDeleteAction, PieceMoveAction}
import model.ApplicationException
import repository.PieceRepository
import service.kafka.KafkaService
import zio.{Task, ZIO}
import util.TimeUtil._

@Singleton
class PieceService @Inject() (repository: PieceRepository, kafkaService: KafkaService) extends PieceValidation {

  def add(pieceInput: ChessPieceInput): Task[ChessPiece] = {
    for {
      _      <- checkFieldRange(pieceInput.field)
      pieces <- repository.getAll()
      _      <- checkEmptyField(pieceInput.field, pieces)
      res    <- repository.create(pieceInput)
      _      <- kafkaService.produceAction(PieceCreationAction(res, getTS()))
    } yield res
  }

  def move(id: String, fieldInput: Field): Task[ChessPiece] = {
    for {
      _      <- checkFieldRange(fieldInput)
      pieces <- repository.getAll()
      pieceWas <- ZIO
        .fromOption(pieces.find(_._id == id))
        .mapError(_ => ApplicationException(s"Piece with id: $id not found"))
      _           <- checkEmptyField(fieldInput, pieces)
      _           <- checkValidMove(pieceWas.`type`, pieceWas.field, fieldInput)
      pieceBecame <- repository.update(id, fieldInput)
      _ <- kafkaService.produceAction(
        PieceMoveAction(pieceWas = pieceWas, pieceBecame = pieceBecame, timestamp = getTS())
      )
    } yield pieceBecame
  }

  def delete(id: String): Task[ChessPiece] = {
    for {
      piece <- repository.delete(id)
      _     <- kafkaService.produceAction(PieceDeleteAction(piece, getTS()))
    } yield piece
  }

  def getAll(): Task[List[ChessPiece]] = {
    repository.getAll()
  }

}
