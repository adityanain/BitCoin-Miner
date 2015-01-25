package bitminer

import akka.actor._
import org.apache.commons.codec.digest._
import scala.concurrent.duration._

class BitWorker(masterLocation: String) extends Actor with ActorLogging {

  //println("Worker Created")
  val path = masterLocation.toString()
  //println("Master Path : " + path)

  //val master = context.actorFor(masterLocation)
  //println("Master is : " + master)

  /* override def preStart = { 
    master ! WorkerIsAvailable(self) 
    master ! WorkRequested
  }*/

  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    context.actorSelection(path) ! Identify(path)
    import context.dispatcher
    context.system.scheduler.scheduleOnce(5.seconds, self, ReceiveTimeout)
    var a = context.system.scheduler.scheduleOnce(2.second, self, ReceiveTimeout)
  }
  
  def receive = identify

  def identify: Actor.Receive = {
    case ActorIdentity(`path`, Some(actor)) => {
      //context.watch(actor)
      //println("Actor Identified")
      actor ! WorkerIsAvailable(self)
      actor ! WorkRequested
      context.become(active(actor))
    }
    case ActorIdentity(`path`, None) => println(s"Master not yet available at: $path")
    case ReceiveTimeout => sendIdentifyRequest()
    case _ => println("System not ready yet")
  }

  def active(actor: ActorRef): Receive = {
    case Work(prefix: String, lengthTogenerate: Int, noOfZerosFind: Int, noOfHashesToFind: Long) => {
      //hashInp = hashInp concat prefix
      //println("Worker : Got Work, prefix string is : " + prefix)
      //println("Length to generate is : "+ lengthTogenerate)

      doWork(prefix, lengthTogenerate, actor, noOfZerosFind, noOfHashesToFind)
    }

    case TerminateYourself => {
      //println("Worker Terminating" + self)
      context stop self
    }

    case ReceiveTimeout => {}
  }

  def doWork(inp: String, length: Int, master: ActorRef, zeros: Int, hashes: Long): Any = {
    var str = "anain" concat inp
    genStrings(str, length, zeros, hashes)
  }

  def genStrings(prefixStr: String, maxLen: Int, zeros: Int, nHash: Long): Any = {

    val lenToGen: Int = maxLen - prefixStr.length
    var resultCounter: Int = 0
    var counter = 0
    //var zString : String = ""
    var sb = new StringBuilder
    for (i <- 1 to zeros) sb += '0'
    for (i <- 1 to lenToGen) {
      genStringsLenKAndCalHash(prefixStr, i, sb.toString)
      if (counter == nHash) {
        return
      }

    }

    def genStringsLenKAndCalHash(prefix: String, k: Int, zString: String): String = {

      if (k == 0) {
        //println(prefix)
        return prefix
      }

      for (i <- 33 until 127; if counter < nHash) {
        var etaString: String = genStringsLenKAndCalHash(prefix + i.toChar, k - 1, zString)
        var hashString = ComputeHash(etaString)
        //println(hashString)

        if (hashString.startsWith(zString)) {
          //println("Found a match " + " sending result : "+ etaString + " : " + hashString)
          sender ! Result(etaString, hashString)
          counter += 1
          if (counter == nHash) {
            sender ! WorkCompleted(etaString, self)
            return prefix
          }
        }
      }

      prefix
    }
  }

  def ComputeHash(string: String): String = {
    var hashString = DigestUtils.sha256Hex(string)
    hashString
  }

}