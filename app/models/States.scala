package models

sealed trait State

case object Idle extends State
case object Active extends State
