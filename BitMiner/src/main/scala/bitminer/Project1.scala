package bitminer
import akka.actor._
import akka.routing.RoundRobinRouter
import com.typesafe.config._
import java.net.InetAddress

object Project1 {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startMasterWorkerSystem(4, 50)
    } else if (args.length == 1) {
      if (checkValidIp(args(0))) {
        startWorkerSystem(args(0))
      } else {
        startMasterWorkerSystem(args(0).toInt, 50)
      }
    } else if (args.length == 2) {
      startMasterWorkerSystem(args(0).toInt, args(1).toLong)
    }
  }

  def startMasterWorkerSystem(noOfZeros: Int, noOfHashes: Long): Any = {

    // No of Local Actors
    val nrOfWorkers = 10

    var masterConfig = ConfigFactory.load("master")
    val system = ActorSystem("MasterWorkerSystem", masterConfig)
    val master = system.actorOf(Props(new BitMaster(noOfZeros, noOfHashes)), name = "Master")

    //val masterPath = master.path
    //println("Master Path : " + masterPath)
    /* println(master.path)
    println(master.path.address.host)
    println(master.path.address.port)
	*/

    val hostname = masterConfig.getString("akka.remote.netty.tcp.hostname")
    val port = masterConfig.getString("akka.remote.netty.tcp.port")

    /*println(hostname)
    println(port)*/
    //val IPAddress = InetAddress.getLocalHost.getHostAddress

    val path = "akka.tcp://MasterWorkerSystem@%s:%s/user/Master".format(hostname, port)

    //var workerSystem = ActorSystem("WorkerSystem", ConfigFactory.load("worker"))
    //var masterRouter = workerSystem.actorOf(Props(new BitWorker(path)).withRouter(RoundRobinRouter(nrOfWorkers)), "masterRouter")
    var localWorkers = new Array[ActorRef](nrOfWorkers)
    for (i <- 1 to nrOfWorkers)
      //localWorkers :+ workerSystem.actorOf(Props(new BitWorker(path)))
      localWorkers :+ system.actorOf(Props(new BitWorker(path)))

    //master
  }

  def startWorkerSystem(masterIP: String) = {

    // No. of remote actors
    var nRemoteWorkers = 10

    val system = ActorSystem("RemoteWorkerSystem", ConfigFactory.load("remoteWorker"))
    val masterPath = "akka.tcp://MasterWorkerSystem@%s:5150/user/Master".format(masterIP)

    var arrWorkerRef = new Array[ActorRef](nRemoteWorkers)
    for (i <- 0 until nRemoteWorkers) { arrWorkerRef(i) = system.actorOf(Props(new BitWorker(masterPath))) }

    val listenerActor = system.actorOf(Props(new Listener(arrWorkerRef)), "Listener")
  }

  def checkValidIp(ip: String): Boolean = {
    val tokens = ip.split('.')
    if (tokens.length == 4) {

      tokens forall {
        token =>
          try {
            val num = token.toInt
            num >= 0 && num <= 255
          } catch {
            case _: Throwable => false
          }
      }
    } else false
  }
}

class Listener(workersRef: Array[ActorRef]) extends Actor {
  var nWorkers = workersRef.length
  for (i <- 0 until nWorkers) {
    context.watch(workersRef(i))
  }

  def receive = {
    case Result =>
    case Shutdown => context.system.shutdown
    case Terminated(worker: ActorRef) => {
      nWorkers -= 1
      if (nWorkers == 0){
        context stop self
        context.system.shutdown
      }
    }
  }
}