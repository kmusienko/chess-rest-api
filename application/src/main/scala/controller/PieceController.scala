package controller

import com.google.inject.{Inject, Singleton}
import controller.PieceEndpoints._
import model.{ApiResultError, ApiResultSuccess}
import service.PieceService
import sttp.tapir.PublicEndpoint
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio.ZIO
import ApiResultSuccess._
import model.piece.{ChessPiece, ChessPieceInput, Field}

@Singleton
class PieceController @Inject() (pieceService: PieceService) {

  val createPieceServerEndpoint: ZServerEndpoint[Any, Any] = piecePost.zServerLogic { chessPieceInput =>
    pieceService
      .add(chessPieceInput)
      .foldZIO(
        error => ZIO.fail(ApiResultError(error.getMessage)),
        result => ZIO.succeed(ApiResultSuccess(result))
      )
  }

  val movePieceServerEndpoint: ZServerEndpoint[Any, Any] = piecePatch.zServerLogic { case (id, chessPieceInput) =>
    pieceService
      .move(id, chessPieceInput)
      .foldZIO(
        error => ZIO.fail(ApiResultError(error.getMessage)),
        result => ZIO.succeed(ApiResultSuccess(result))
      )
  }

  val deletePieceServerEndpoint: ZServerEndpoint[Any, Any] = pieceDelete.zServerLogic { id =>
    pieceService
      .delete(id)
      .foldZIO(
        error => ZIO.fail(ApiResultError(error.getMessage)),
        result => ZIO.succeed(ApiResultSuccess(result))
      )
  }

  val getPiecesServerEndpoint: ZServerEndpoint[Any, Any] = piecesGet.zServerLogic { _ =>
    pieceService
      .getAll()
      .foldZIO(
        error => ZIO.fail(ApiResultError(error.getMessage)),
        result => ZIO.succeed(ApiResultSuccess(result))
      )
  }

}

object PieceEndpoints {

  val PIECE_PATH: String = "pieces"

  val piecePost: PublicEndpoint[ChessPieceInput, ApiResultError, ApiResultSuccess[ChessPiece], Any] = endpoint.post
    .in(PIECE_PATH)
    .in(jsonBody[ChessPieceInput])
    .errorOut(jsonBody[ApiResultError])
    .out(jsonBody[ApiResultSuccess[ChessPiece]])

  val piecePatch: PublicEndpoint[(String, Field), ApiResultError, ApiResultSuccess[ChessPiece], Any] =
    endpoint.patch
      .in(PIECE_PATH)
      .in(path[String]("id"))
      .in(jsonBody[Field])
      .errorOut(jsonBody[ApiResultError])
      .out(jsonBody[ApiResultSuccess[ChessPiece]])

  val pieceDelete: PublicEndpoint[String, ApiResultError, ApiResultSuccess[ChessPiece], Any] = endpoint.delete
    .in(PIECE_PATH)
    .in(path[String]("id"))
    .errorOut(jsonBody[ApiResultError])
    .out(jsonBody[ApiResultSuccess[ChessPiece]])

  val piecesGet: PublicEndpoint[Unit, ApiResultError, ApiResultSuccess[List[ChessPiece]], Any] = endpoint.get
    .in(PIECE_PATH)
    .errorOut(jsonBody[ApiResultError])
    .out(jsonBody[ApiResultSuccess[List[ChessPiece]]])

}
