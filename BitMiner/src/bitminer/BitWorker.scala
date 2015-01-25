package bitminer

import akka.actor._
import org.apache.commons.codec.digest._
import scala.concurrent.duration._

class BitWorker(masterLocation: String) extends Actor with ActorLogging {

  println("Worker Created")
  val path = masterLocation.toString()
  println("Master Path : " + path)

  val master = context.actorFor(masterLocation)
  println("My master is : " + master)

  override def preStart = { 
    master ! WorkerIsAvailable(self) 
    master ! WorkRequested
  }
  
  def receive = {
    case Work(prefix: String, lengthTogenerate: Int) => {
      //hashInp = hashInp concat prefix
      //println("Worker : Got Work, prefix string is : " + prefix)
      //println("Length to generate is : "+ lengthTogenerate)

      //log.info("Worker : Got Work, prefix string is : " + prefix)
      //log.info("Length to generate is : "+ lengthTogenerate)

      doWork(prefix, lengthTogenerate, sender)
    }
  }

  def doWork(inp: String, length: Int, master: ActorRef): Any = {
    var str = "anain" concat inp
    //println("str : " + str)
    log.info("str : " + str)
    //var hashStr = ""
    genStrings(str, length)
  }

  def genStrings(prefixStr: String, maxLen: Int) = {

    val lenToGen: Int = maxLen - prefixStr.length
    var resultCounter: Int = 0
    var counter = 0

    for (i <- 1 to lenToGen; if counter <= 5) {
      genStringsLenKAndCalHash(prefixStr, i, resultCounter)
      //if(counter == 5){}

    }

    def genStringsLenKAndCalHash(prefix: String, k: Int, resultCounter: Int): String = {

      if (k == 0) {
        //println(prefix)
        return prefix
      }
      counter = resultCounter
      for (i <- 33 until 127) {
        // var newPrefix = prefix + i.toChar 
        var etaString: String = genStringsLenKAndCalHash(prefix + i.toChar, k - 1, counter) //.toString
        //println("String : " + etaString)
        //log.info("String : " + etaString)
        var hashString = ComputeHash(etaString)
        //println(hashString)
        //log.info(hashString)
        if (hashString startsWith "0000") {
          //println("Found a match " + " sending result : "+ etaString + " : " + hashString)
          //println("Found a match " + " sending result : "+ etaString + " : " + hashString)
          sender ! Result(etaString, hashString)
          counter += 1
          if (counter == 5) {
            println("Job Done")
            sender ! WorkCompleted
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