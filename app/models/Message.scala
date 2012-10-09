package models

import java.util.Date

sealed trait Message

case class StartSearch(searchRequest:SearchRequest) extends Message
case object Pull extends Message
