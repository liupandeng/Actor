package cn.sheep.spark

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._ // 导入时间单位

class SparkWorker(masterUrl: String) extends Actor{

    // master的 actorRef
    var masterProxy: ActorSelection = _
    val workId = UUID.randomUUID().toString

    override def preStart(): Unit = {
        masterProxy = context.actorSelection(masterUrl)
    }

    override def receive: Receive = {

        case "started" => { // 自己已就绪
            // 向master注册自己的信息，id, core, ram
            masterProxy ! RegisterWorkerInfo(workId, 4, 32 * 1024) // 此时master会收到该条信息
        }
        case RegisteredWorkerInfo => { // master发送给自己的注册成功消息
            // worker 启动一个定时器，定时向master 发送心跳
            import context.dispatcher
            context.system.scheduler.schedule(0 millis, 1500 millis, self, SendHeartBeat)
        }
        case SendHeartBeat => {
            // 开始向master发送心跳了
            println(s"------- $workId 发送心跳-------")
            masterProxy ! HearBeat(workId) // 此时master将会收到心跳信息
        }
    }
}


object SparkWorker {

    def main(args: Array[String]): Unit = {

        // 检验参数
        if(args.length != 4) {
            println(
                """
                  |请输入参数：<host> <port> <workName> <masterURL>
                """.stripMargin)
            sys.exit() // 退出程序
        }

        val host = args(0)
        val port = args(1)
        val workName = args(2)
        val masterURL = args(3)

        val config = ConfigFactory.parseString(
            s"""
              |akka.actor.provider="akka.remote.RemoteActorRefProvider"
              |akka.remote.netty.tcp.hostname=$host
              |akka.remote.netty.tcp.port=$port
            """.stripMargin)

        val actorSystem = ActorSystem("sparkWorker", config)

        // 创建自己的actorRef
        val workerActorRef = actorSystem.actorOf(Props(new SparkWorker(masterURL)), workName)

        // 给自己发送一个以启动的消息，标识自己已经就绪了
        workerActorRef ! "started"
    }


}
