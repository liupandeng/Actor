package cn.sheep.spark


/**
  * worker -> master
  */

// worker向master注册自己（信息）
case class RegisterWorkerInfo(id: String, core: Int, ram: Int)

// worker给master发送心跳信息
case class HearBeat(id: String)


/**
  * master -> worker
  */
// master向worker发送注册成功消息
case object RegisteredWorkerInfo


// worker 发送发送给自己的消息，告诉自己说要开始周期性的向master发送心跳消息
case object SendHeartBeat

//master自己给自己发送一个检查超时worker的信息,并启动一个调度器，周期新检测删除超时worker
case object CheckTimeOutWorker

// master发送给自己的消息，删除超时的worker
case object RemoveTimeOutWorker

// 存储worker信息的类
class WorkerInfo(val id: String, core: Int, ram: Int) {
    var lastHeartBeatTime: Long = _
}