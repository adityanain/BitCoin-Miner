package bitminer
import akka.actor._

sealed trait MasterWorkerProtocol

//Worker Messages
case class Work(prefixString : String, length : Int, nZerosToFind : Int, noOfHashesToFind : Long) extends MasterWorkerProtocol
case object NoWorkToBeDone extends MasterWorkerProtocol
case object TerminateYourself extends MasterWorkerProtocol

//Master Messages
case class Result(inp : String, result : String) extends MasterWorkerProtocol
case class WorkerIsAvailable( workerRef : ActorRef) extends MasterWorkerProtocol
case object WorkRequested extends MasterWorkerProtocol
case class WorkerTerminated(workerRef : ActorRef) extends MasterWorkerProtocol
case class WorkCompleted(stringProcessed : String, actorRef : ActorRef) extends MasterWorkerProtocol

case class Shutdown()
