package com.owtelse.actors

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


  def props(namePrefix: String = "scazelcast", hostname: String = "", port: Int = 0, confFileName: String = "", timeout: FiniteDuration = 0 seconds) : Props = {
    //    import java.net.InetSocketAddress
    //    val remote = new InetSocketAddress(hostname, port)
    Props(new HazelcastActor(namePrefix, hostname, port, confFileName, timeout))
  }


}

/**
 *
 * @param hostname
 * @param port
 * @param confFileName
 * @param timeout
 * @param namePrefix used to id DistributedObjects for this Actor
 */
class HazelcastActor(namePrefix: String, hostname: String , port: Int, confFileName: String , timeout: FiniteDuration) extends Actor
with HazelcastService
{
  import com.owtelse.actors.HazelcastMessages._

  val config = if(confFileName.isEmpty) new Config() else new ClasspathXmlConfig(confFileName)

  val hazelcast: HazelcastInstance = Hazelcast.newHazelcastInstance(config)

  val mapName = namePrefix + "Map"
  val idGenName = namePrefix + "IdGen"

  def actorRefFactory = context

  def receive = receiveNormal

  import com.owtelse.scazelcast.Map._
  def receiveNormal: Receive = {
    case GetPosRep(key) => {
      sender ! get(hazelcast, mapName)(key) // sender needs to handle None or Some(PositionReport)
    }
    case PutPosRep(key, value) => put(hazelcast, mapName)(key, value)
    case wtf => unhandled(s"Dont know what to do with this ${wtf.toString}")
  }


}

trait HazelcastService {
  val config: Config
  val hazelcast: HazelcastInstance
  val idGenName: String
}

trait IdGen { self: HazelcastService =>
  import com.owtelse.scazelcast.{IdGenerator => Gen}
  def initIDGen(i: Long) = Gen.init(hazelcast, idGenName)(i)
  def newId = Gen.newId(hazelcast, idGenName)
}


