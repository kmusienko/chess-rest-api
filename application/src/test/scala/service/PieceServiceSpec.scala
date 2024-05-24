package service

import model.ApplicationException
import model.piece._
import org.mockito.scalatest.IdiomaticMockito
import org.scalatest.enablers.Messaging.messagingNatureOfThrowable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import repository.PieceRepository
import service.kafka.KafkaService
import zio.{Runtime, Unsafe, ZIO}

class PieceServiceSpec extends AnyFlatSpec with IdiomaticMockito with should.Matchers {
  private val runtime = Runtime.default

  private val pieceRepository: PieceRepository = mock[PieceRepository]
  private val kafkaService: KafkaService       = mock[KafkaService]

  private val service: PieceService = new PieceService(pieceRepository, kafkaService)

  private val pieces = List(
    ChessPiece("id1", Field(0, 0), PieceType.Bishop, PieceStatus.Active),
    ChessPiece("id2", Field(5, 5), PieceType.Rook, PieceStatus.Active)
  )

  pieceRepository.getAll().returns(ZIO.succeed(pieces))
  kafkaService.produceAction(*).returns(ZIO.unit)

  "PieceService" should "add new piece on a free field" in {
    val input = ChessPieceInput(Field(0, 1), PieceType.Bishop)

    pieceRepository.create(input).returns(ZIO.succeed(ChessPiece("id3", input.field, input.`type`, PieceStatus.Active)))

    val result = service.add(input)

    unsafeRun(result)._id shouldBe "id3"
  }

  it should "not add new piece on a busy field" in {
    val input = ChessPieceInput(Field(0, 0), PieceType.Bishop)

    val result = service.add(input)

    the[ApplicationException] thrownBy unsafeRun(result) should have message "The field (0, 0) is busy."
  }

  it should "not add new piece on a non-existing field" in {
    val input = ChessPieceInput(Field(9, 9), PieceType.Bishop)

    val result = service.add(input)

    the[ApplicationException] thrownBy unsafeRun(result) should have message
      "The field (9, 9) is out of range. Coordinates must be from 0 to 7"
  }

  it should "move a piece on a new field" in {
    val field = Field(1, 1)

    pieceRepository
      .update("id1", field)
      .returns(ZIO.succeed(ChessPiece("id1", field, PieceType.Bishop, PieceStatus.Active)))

    val result = unsafeRun(service.move("id1", field))

    result._id shouldBe "id1"
    result.field shouldBe field
  }

  it should "not move a piece on a non-existing field" in {
    val field = Field(8, 8)

    val result = service.move("id1", field)

    the[ApplicationException] thrownBy unsafeRun(result) should have message
      "The field (8, 8) is out of range. Coordinates must be from 0 to 7"
  }

  it should "not move a piece on a busy field" in {
    val field = Field(5, 5)

    val result = service.move("id1", field)

    the[ApplicationException] thrownBy unsafeRun(result) should have message "The field (5, 5) is busy."
  }

  it should "not move a non-existing piece" in {
    val field = Field(5, 5)

    val result = service.move("unknown_id", field)

    the[ApplicationException] thrownBy unsafeRun(result) should have message "Piece with id: unknown_id not found"
  }

  it should "not move a piece on an incorrect field" in {
    val resultBishop = service.move("id1", Field(1, 0))
    val resultRook   = service.move("id2", Field(4, 4))

    the[ApplicationException] thrownBy unsafeRun(resultBishop) should have message
      "Invalid move. The Bishop can't move to (1, 0) field"

    the[ApplicationException] thrownBy unsafeRun(resultRook) should have message
      "Invalid move. The Rook can't move to (4, 4) field"
  }

  private def unsafeRun[E <: Throwable, A](zio: ZIO[Any, E, A]): A = {
    Unsafe.unsafe(implicit unsafeCtx => runtime.unsafe.run(zio).getOrThrow())
  }
}
