package model

case class ApplicationException(message: String) extends Exception(message)
