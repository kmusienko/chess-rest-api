package module

import com.google.inject.{Inject, Provider, Singleton}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{MongoClient, MongoDatabase}
import pureconfig.ConfigSource

class MongoDatabaseModule extends ScalaModule {

  override def configure(): Unit = {
    bind[MongoClient].toProvider[MongoClientProvider].asEagerSingleton()
    bind[MongoDatabase].toProvider[MongoDbProvider].asEagerSingleton()
  }

}

@Singleton
class MongoClientProvider @Inject() (globalConfig: Config) extends Provider[MongoClient] {

  private val globalPureConfig = ConfigSource.fromConfig(globalConfig)

  lazy val get: MongoClient = provideMongoClient()

  private def provideMongoClient(): MongoClient = {
    val uri    = globalPureConfig.at("mongodb.uri").loadOrThrow[String]
    val client = MongoClient(uri)
    client
  }
}

@Singleton
class MongoDbProvider @Inject() (
  globalConfig: Config,
  mongoClient: MongoClient
) extends Provider[MongoDatabase] {

  private val globalPureConfig = ConfigSource.fromConfig(globalConfig)

  lazy val get: MongoDatabase = provideMongoDb()

  private def provideMongoDb(): MongoDatabase = {
    val dbName = globalPureConfig.at("mongodb.dbname").loadOrThrow[String]
    mongoClient.getDatabase(dbName).withCodecRegistry(DEFAULT_CODEC_REGISTRY)
  }
}
