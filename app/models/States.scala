package models

sealed trait SearchActorState

case object Idle extends SearchActorState
case object Active extends SearchActorState
