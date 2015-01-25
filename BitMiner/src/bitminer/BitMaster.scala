package bitminer

import akka.actor._
import scala.collection.mutable.{ Map, Queue }

//import MasterWorkerProtocol

class BitMaster(zerosToMatch : Int ) extends Actor with ActorLogging {
  
  val charList = Array(' ', '!', '"', '#', '$', '%', '&', ''', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2',
    '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?', '@', 'A', 'B', 'C', 'D', 'E',
    'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
    'Y', 'Z', '[', '\\', ']', '^', '_', '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
    'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}', '~')

  val charListLength = charList.length
  println("Master : " + charListLength)
  val workers = Map.empty[ActorRef, String]
  val resultList = Map.empty[String, String]
  
  var i = 33
  var j = 33
  val length = 15
  
  def getWorkString : String = {
    if(j == 128) { i += 1 ; j = 33 }
    var str: String = "" + i.toChar + j.toChar
    println("Work String : " + str)
    j += 1
    str
    }
  
  def receive = {

    case WorkerIsAvailable(actorRef: ActorRef) => {
      println("Worker is Available")
      context.watch(actorRef)
      workers += (actorRef -> null)
      //notifyWorkers()
    }

    case Result(inpString: String, hashString: String) =>
      {
        resultList += (inpString -> hashString)
        println("Input String : %s , SHA 256 Hash String : %s".format(inpString, hashString))
        //log.info("Input String : %s , SHA 256 Hash String : %s".format(inpString, hashString))
      }

    case WorkRequested => {
    	println("Work Requested")
    	if(i < 127) sender ! (Work(getWorkString, length))
    	else sender ! NoWorkToBeDone	
    }

    case Terminated(worker: ActorRef) => {
      if (workers.contains(worker) && (workers(worker) != None)) {
        val workAssigned = workers.get(worker)
      }

    }
  }
}

/*object Main extends App{
  val system = ActorSystem("MasterSystem")
  val master = system.actorOf(Props[BitMaster])
}
*/

