package cn.sheep.robot


// 服务端发送给客户端的消息格式
case class ServerMessage(msg: String)

// 客户端发送给服务器端的消息格式
case class ClientMessage(msg: String)
