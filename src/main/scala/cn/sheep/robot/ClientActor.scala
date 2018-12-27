package cn.sheep.robot

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

class ClientActor(host: String, port: Int) extends Actor{

    var serverActorRef: ActorSelection = _ // 服务端的代理对象

    // 在receive方法之前调用
    override def preStart(): Unit = {
        // akka.tcp://Server@127.0.0.1:8088
        serverActorRef = context.actorSelection(s"akka.tcp://Server@${host}:${port}/user/shanshan")
    }

    // mailbox ->receive
    override def receive: Receive = { // shit
        case "start" => println("牛魔王系列已启动...")
        case msg: String => { // shit
            serverActorRef ! ClientMessage(msg) // 把客户端输入的内容发送给 服务端（actorRef）--》服务端的mailbox中 -> 服务端的receive
        }
        case ServerMessage(msg) => println(s"收到服务端消息：$msg")
    }
}

object ClientActor  {


    def main(args: Array[String]): Unit = {
        val host = "127.0.0.1"
        val port  = 8089

        val serverHost = "127.0.0.1"
        val serverPort = 8088

        val config = ConfigFactory.parseString(
            s"""
               |akka.actor.provider="akka.remote.RemoteActorRefProvider"
               |akka.remote.netty.tcp.hostname=$host
               |akka.remote.netty.tcp.port=$port
        """.stripMargin)

        val clientSystem = ActorSystem("client", config)

        // 创建dispatch | mailbox
        val actorRef = clientSystem.actorOf(Props(new ClientActor(serverHost, serverPort.toInt)), "NMW-002")

        actorRef ! "start" // 自己给自己发送了一条消息 到自己的mailbox => receive

        while (true) {
            val question = StdIn.readLine() // 同步阻塞的， shit
            actorRef ! question // mailbox -> receive
        }
    }






}
