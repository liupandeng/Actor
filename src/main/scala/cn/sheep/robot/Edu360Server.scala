package cn.sheep.robot

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class Edu360Server extends Actor{
    // 用来接受客户端发送过来的问题的
    override def receive: Receive = {
        case "start" => println("老娘已就绪 ！")

        case ClientMessage(msg) => {
            println(s"收到客户端消息：$msg")
            msg match {
                case "你叫啥" => sender() ! ServerMessage("铁扇公主")
                case "你是男是女" => sender() ! ServerMessage("老娘是男的")
                case "你有男票吗" => sender() ! ServerMessage("没有")
                case _ => sender() ! ServerMessage("What you say ?") //sender()发送端的代理对象， 发送到客户端的mailbox中 -> 客户端的receive
            }
        }

    }
}

object Edu360Server {

    def main(args: Array[String]): Unit = {

        val host = "127.0.0.1"
        val port = 8088

        val config = ConfigFactory.parseString(
            s"""
               |akka.actor.provider="akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname=$host
               |akka.remote.netty.tcp.port=$port
        """.stripMargin)

        // 指定IP 和 端口
        val actorSystem = ActorSystem("Server", config)

        val serverActorRef = actorSystem.actorOf(Props[Edu360Server], "shanshan")
        serverActorRef ! "start" // 到自己的mailbox -》 receive方法


    }
}
