package bitminer
import akka.actor._
import akka.routing.RoundRobinRouter
import com.typesafe.config.ConfigFactory



object Project1 {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      startMasterWorkerSystem(4)
    } else {
      if (checkValidIp(args(0))) {
        startWorkerSystem(args(0))
      } else {
        startMasterWorkerSystem(args(0).toInt)
      }
    }
  }

  def startMasterWorkerSystem(noOfZeros: Int): ActorRef = {

    val nrOfWorkers = 10
/*    val system = ActorSystem("MasterWorkerSystem", ConfigFactory.load(ConfigFactory.parseString("""
		akka {
			actor {
				provider = "akka.remote.RemoteActorRefProvider"
			}
			remote {
				transport = "akka.remote.netty.NettyRemoteTransport"
				netty {
					hostname = "127.0.0.1"
					port = 5150
				}
			}
		}
""")))*/
    
    val system = ActorSystem("MasterWorkerSystem", ConfigFactory.load("master.conf"))

    val master = system.actorOf(Props(new BitMaster(noOfZeros)), name = "Master")
    println("Master Created")
    //log.info("Master Created")
    val masterPath = master.path //"akka://MasterWorkerSystem/user/Master"
    //master.path
    println("Master Path : " + masterPath)
    val path = "akka.tcp://MasterWorkerSystem@127.0.0.1:5150/user/Master"
/*    val workerSystem = ActorSystem("WorkerSystem", ConfigFactory.load(ConfigFactory.parseString("""
	akka {
		actor {
			provider = "akka.remote.RemoteActorRefProvider"
		}
		remote {
			transport = "akka.remote.netty.NettyRemoteTransport"
			netty {
				hostname = "127.0.0.1"
				port = 0
			}
		}
	}
""")))*/
      
    var workerSystem = ActorSystem("WorkerSystem", ConfigFactory.load("worker.conf"))

    var masterRouter = workerSystem.actorOf(Props(new BitWorker(path)).withRouter(RoundRobinRouter(nrOfWorkers)), "masterRouter")

    master
  }

  def startWorkerSystem(masterIP: String) = {

    //val nrOfWorkers = 10 
    val system = ActorSystem("RemoteWorkerSystem", ConfigFactory.load(ConfigFactory.parseString("""
		akka {
			actor {
				provider = "akka.remote.RemoteActorRefProvider"
			}
			remote {
				transport = "akka.remote.netty.NettyRemoteTransport"
				netty {
					hostname = "127.0.0.1"
					port = 0
				}
			}
		}
""")))
	println("RemoteWorkerSystem - Created")
    //val masterPath = ActorPath.fromString("akka://%s:5156/user/%s".format(masterIP, "Master"))
    val masterPath = "akka://MasterWorkerSystem@%s:5150/user/Master".format(masterIP)
    //var workerRouter = system.actorOf(Props[BitWorker].withRouter(RoundRobinRouter(nrOfWorkers)), "workerRouter")
    val arrWorkerRef = new Array[ActorRef](10)

    for(i <- 1 to 10) 
		arrWorkerRef :+ system.actorOf(Props(new BitWorker(masterPath)))
  }

  def checkValidIp(ip: String): Boolean = {
    val tokens = ip.split('.')
    if (tokens.length != 4) false

    tokens forall {
      token =>
        try {
          val num = token.toInt
          num >= 0 && num <= 255
        } catch {
          case _: Throwable => false
        }
    }
  }
}