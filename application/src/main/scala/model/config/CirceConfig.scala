package model.config

import io.circe.generic.extras.Configuration

object CirceConfig {

  implicit val default: Configuration = Configuration.default.withDefaults
}
