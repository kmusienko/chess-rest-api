package repository

import model.ApplicationException
import org.mongodb.scala.Observable
import zio.{Task, ZIO}

import scala.concurrent.Future

trait RichObservable {

  implicit class RichObservable[T](observable: Observable[T]) {

    def collectMany(): Task[List[T]] = {
      ZIO.fromFuture(implicit ec => observable.collect().headOption().map(_.fold(List.empty[T])(_.toList)))
    }

    def getOrFailZio(message: String): Task[T] = {
      ZIO.fromFuture(implicit ec =>
        observable.headOption().flatMap {
          case Some(result) => Future.successful(result)
          case _            => Future.failed(ApplicationException(message))
        }
      )
    }
  }
}
