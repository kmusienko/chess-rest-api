import com.google.inject.{Guice, Injector}
import controller.{PieceController, PieceEndpoints}
import module.{ConfigModule, MongoDatabaseModule}
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir._
import zio.http.{HttpApp, Server}
import zio.{Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object App extends ZIOAppDefault {

  val injector: Injector          = Guice.createInjector(new ConfigModule(), new MongoDatabaseModule())
  val controller: PieceController = injector.getInstance(classOf[PieceController])

  val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter().fromEndpoints[Task](
      endpoints =
        List(PieceEndpoints.piecePost, PieceEndpoints.piecePatch, PieceEndpoints.pieceDelete, PieceEndpoints.piecesGet),
      title = "Chess piece API",
      version = "1.0"
    )

  val routes: HttpApp[Any] = ZioHttpInterpreter().toHttp(
    List(
      controller.createPieceServerEndpoint,
      controller.movePieceServerEndpoint,
      controller.deletePieceServerEndpoint,
      controller.getPiecesServerEndpoint
    ) ++ swaggerEndpoints
  )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    Server
      .serve(routes)
      .provide(
        ZLayer.succeed(Server.Config.default.port(8080)),
        Server.live
      )
      .exitCode
  }
}
