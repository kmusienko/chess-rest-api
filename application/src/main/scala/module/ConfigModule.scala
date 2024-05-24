package module

import com.google.inject.{Provides, Singleton}
import com.typesafe.config.{Config, ConfigFactory}
import model.config.KafkaConfig
import net.codingwell.scalaguice.ScalaModule
import pureconfig.{ConfigObjectSource, ConfigSource}
import pureconfig.generic.auto._

class ConfigModule extends ScalaModule {

  private val config: Config = ConfigFactory
    .systemEnvironment()
    .withFallback(ConfigFactory.systemProperties())
    .withFallback(ConfigFactory.defaultApplication())
    .resolve()

  override def configure(): Unit = {
    val configSource: ConfigObjectSource = ConfigSource.fromConfig(config.getConfig("kafka"))
    val kafkaConfig                      = configSource.loadOrThrow[KafkaConfig]

    bind[KafkaConfig].toInstance(kafkaConfig)
  }

  @Provides
  @Singleton
  def provideConfig(): Config = config
}
