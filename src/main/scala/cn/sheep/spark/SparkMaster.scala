package cn.sheep.spark

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

class SparkMaster extends Actor{

    // 存储worker的信息的
    val id2WorkerInfo = collection.mutable.HashMap[String, WorkerInfo]()


//    override def preStart(): Unit = {
//        context.system.scheduler.schedule(0 millis, 6000 millis, self, RemoveTimeOutWorker)
//    }

    override def receive: Receive = {

        // 收到worker注册过来的信息
        case RegisterWorkerInfo(wkId, core, ram) => {
            // 将worker的信息存储起来，存储到HashMap
            if (!id2WorkerInfo.contains(wkId)) {
                val workerInfo = new WorkerInfo(wkId, core, ram)
                id2WorkerInfo += ((wkId, workerInfo))

                // master存储完worker注册的数据之后，要告诉worker说你已经注册成功
                sender() ! RegisteredWorkerInfo // 此时worker会收到注册成功消息
            }
        }

        case HearBeat(wkId) => {
            // master收到worker的心跳消息之后，更新woker的上一次心跳时间
            val workerInfo = id2WorkerInfo(wkId)
            // 更改心跳时间
            val currentTime = System.currentTimeMillis()
            workerInfo.lastHeartBeatTime = currentTime
        }

        case CheckTimeOutWorker => {
            import context.dispatcher // 使用调度器时候必须导入dispatcher
            context.system.scheduler.schedule(0 millis, 6000 millis, self, RemoveTimeOutWorker)
        }

        case RemoveTimeOutWorker => {
            // 将hashMap中的所有的value都拿出来，查看当前时间和上一次心跳时间的差 3000
            val workerInfos = id2WorkerInfo.values
            val currentTime = System.currentTimeMillis()

            //  过滤超时的worker
            workerInfos
              .filter(wkInfo => currentTime - wkInfo.lastHeartBeatTime > 3000)
              .foreach(wk => id2WorkerInfo.remove(wk.id))

            println(s"-----还剩 ${id2WorkerInfo.size} 存活的Worker-----")
        }
    }
}

object SparkMaster {
    private var name = ""
    private val age = 100


    def main(args: Array[String]): Unit = {

        // 检验参数
        if(args.length != 3) {
            println(
                """
                  |请输入参数：<host> <port> <masterName>
                """.stripMargin)
            sys.exit() // 退出程序
        }

        val host = args(0)
        val port = args(1)
        val masterName = args(2)

        val config = ConfigFactory.parseString(
            s"""
               |akka.actor.provider="akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname=$host
               |akka.remote.netty.tcp.port=$port
            """.stripMargin)

        val actorSystem = ActorSystem("sparkMaster", config)

        val masterActorRef = actorSystem.actorOf(Props[SparkMaster], masterName)

        // 自己给自己发送一个消息，去启动一个调度器，定期的检测HashMap中超时的worker
        masterActorRef ! CheckTimeOutWorker
    }
}
