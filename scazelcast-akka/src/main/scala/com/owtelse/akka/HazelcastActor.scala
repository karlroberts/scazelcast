package com.owtelse.akka

import akka.actor.{Actor, Props}
import scala.concurrent.duration._
import com.hazelcast.config.{ClasspathXmlConfig, Config}
import com.hazelcast.core.{Hazelcast, HazelcastInstance}

object HazelcastMessages {
  sealed trait MapBasedCacheRequest
  sealed trait MapBasedCacheResponse
  case class GetPosRep[K](key: K) extends MapBasedCacheRequest
  //NB PosRep will be stored as a String for marshalling
  case class PutPosRep[K, V](key: K, value: V) extends MapBasedCacheRequest
  //  case class GetResult(posRep: Option[PositionReport]) extends MapBasedCacheResponse
}

object HazelcastActor {


  def props(hostname: String = "", port: Int = 0, confFileName: String = "", timeout: FiniteDuration = 0 seconds) : Props = {
    //    import java.net.InetSocketAddress
    //    val remote = new InetSocketAddress(hostname, port)
    Props(new HazelcastActor(hostname, port, confFileName, timeout))
  }


}

class HazelcastActor(hostname: String , port: Int, confFileName: String , timeout: FiniteDuration) extends Actor with HazelcastService
{
  import com.owtelse.akka.HazelcastMessages._

  val config = if(confFileName.isEmpty) new Config() else new ClasspathXmlConfig(confFileName)

  val hazelcast: HazelcastInstance = Hazelcast.newHazelcastInstance(config)

  def actorRefFactory = context

  def receive = receiveNormal

  import com.owtelse.scazelcast.Map._
  def receiveNormal: Receive = {
    case GetPosRep(key) => {
      sender ! get(hazelcast, "PositionReports")(key) // sender needs to handle None or Some(PositionReport)
    }
    case PutPosRep(key, value) => put(hazelcast, "PositionReports")(key, value)
    case wtf => unhandled(s"Dont know what to do with this ${wtf.toString}")
  }


}

trait HazelcastService { self: Actor =>


}
