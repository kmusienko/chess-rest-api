package service.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import util.CirceSerde
import zio.{URIO, ZIO, ZLayer}
import zio.kafka.producer.{Producer, ProducerSettings}
import KafkaService._
import com.google.inject.{Inject, Singleton}
import model.config.KafkaConfig
import model.piece.PieceAction
import zio.kafka.serde.{Serde, Serializer}

@Singleton
class KafkaService @Inject() (config: KafkaConfig) {

  private val producerSettings: ProducerSettings = ProducerSettings(List(config.address))

  private val producerLayer = ZLayer.scoped(Producer.make(producerSettings))

  def produceAction(action: PieceAction): URIO[Any, Unit] = {
    val record = new ProducerRecord(config.topic, action)
    Producer
      .produce(
        record = record,
        keySerializer = Serde.long,
        valueSerializer = pieceActionSerializer
      )
      .provideLayer(producerLayer)
      .tapError(error => ZIO.logError(s"Failed to produce action to Kafka. Reason: $error"))
      .ignore
  }

}

object KafkaService {

  private val pieceActionSerializer: Serializer[Any, PieceAction] = CirceSerde.serialize[PieceAction]
}
