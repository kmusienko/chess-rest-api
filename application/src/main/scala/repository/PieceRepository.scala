package repository

import com.google.inject.{Inject, Singleton}
import io.circe.parser._
import io.circe.syntax._
import model.piece.{ChessPiece, ChessPieceInput, Field, PieceStatus}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, ReturnDocument}
import org.mongodb.scala.{Document, MongoCollection, MongoDatabase}
import zio.{Task, ZIO}

@Singleton
class PieceRepository @Inject() (database: MongoDatabase) extends RichObservable {
  private val collectionName                        = "pieces"
  private val collection: MongoCollection[Document] = database.getCollection(collectionName)

  def create(pieceInput: ChessPieceInput): Task[ChessPiece] = {
    for {
      id <- collection
        .insertOne(Document(ChessPiece(pieceInput).asJson.noSpaces))
        .getOrFailZio("Insertion error")
        .map(_.getInsertedId.asString().getValue)
      document <- collection.find(searchFilter(id)).getOrFailZio(s"Piece not found: $id")
      piece    <- ZIO.fromEither(parse(document.toJson()).flatMap(_.as[ChessPiece]))
    } yield piece
  }

  def update(id: String, fieldInput: Field): Task[ChessPiece] = {
    for {
      document <- collection
        .findOneAndUpdate(
          filter = searchFilter(id),
          update = combine(set("field.x", fieldInput.x), set("field.y", fieldInput.y)),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .getOrFailZio(s"Piece not found during update: $id")
      piece <- ZIO.fromEither(parse(document.toJson()).flatMap(_.as[ChessPiece]))
    } yield piece
  }

  def delete(id: String): Task[ChessPiece] = {
    for {
      document <- collection
        .findOneAndUpdate(
          filter = searchFilter(id),
          update = set("status", PieceStatus.Deleted.toString),
          options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )
        .getOrFailZio(s"Piece not found: $id")
      piece <- ZIO.fromEither(parse(document.toJson()).flatMap(_.as[ChessPiece]))
    } yield piece
  }

  def getAll(): Task[List[ChessPiece]] = {
    for {
      documents <- collection.find(Filters.notEqual("status", PieceStatus.Deleted.toString)).collectMany()
      pieces    <- ZIO.foreach(documents)(doc => ZIO.fromEither(parse(doc.toJson()).flatMap(_.as[ChessPiece])))
    } yield pieces
  }

  private def searchFilter(id: String): Bson = {
    Filters.and(
      Filters.eq("_id", id),
      Filters.notEqual("status", PieceStatus.Deleted.toString)
    )
  }
}
