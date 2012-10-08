package models

import java.util.Date

sealed trait State

case class StartSearch(searchRequest:SearchRequest) extends State
