package com.owtelse.scazelcast
import scala.concurrent.duration._
import scala.collection.convert.WrapAsScala._
import collection.mutable.{Map => MMap}
import java.util.{Map => JMap}
import com.hazelcast.config.{ClasspathXmlConfig, Config}
import com.hazelcast.core.{Hazelcast, HazelcastInstance}

/**
 * Created by robertk on 21/06/14.
 */
object Map
{

  def getFromCache[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K): Option[V] = {
    val x:JMap[K, V] = hazelcast.getMap(mapName)
    val y:MMap[K, V] = x
    y.get(key)
  }

  def putInCache[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K, value: V) = {
    import java.util.{Map => JMap}
    val x:JMap[K, V] = hazelcast.getMap(mapName)
    x.put(key, value)
  }

}
