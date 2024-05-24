package util

import java.nio.charset.StandardCharsets

import io.circe._
import io.circe.syntax.EncoderOps
import org.apache.kafka.common.header.Headers
import zio._
import zio.kafka.serde._

object CirceSerde {

  class CirceSerializer[A](implicit encoder: Encoder[A]) extends Serializer[Any, A] {

    override def serialize(
        topic: String,
        headers: Headers,
        value: A
    ): RIO[Any, Array[Byte]] = {
      ZIO.attempt(Printer.noSpaces.print(value.asJson).getBytes(StandardCharsets.UTF_8))
    }

  }

  def serialize[A: Encoder]: Serializer[Any, A] = new CirceSerializer[A]
}
