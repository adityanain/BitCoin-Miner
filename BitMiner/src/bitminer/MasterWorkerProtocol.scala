package bitminer

import akka.actor._

sealed trait MasterWorkerProtocol

//Worker Messages
case class Work(prefixString : String, lenght : Int) extends MasterWorkerProtocol
case object NoWorkToBeDone

//Master Messages
case class Result(inp : String, result : String) extends MasterWorkerProtocol
case class WorkerIsAvailable( workerRef : ActorRef) extends MasterWorkerProtocol
case object WorkRequested extends MasterWorkerProtocol
case class WorkerTerminated(workerRef : ActorRef) extends MasterWorkerProtocol
case object WorkCompleted extends MasterWorkerProtocol
