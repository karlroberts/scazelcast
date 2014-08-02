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

  /**
   * Wraps the Hazelcast IMap.get(key) method to return an Option[V].
   * If the key is null Hazelcst would throw an exception, this is caught and None returned.
   * @param hazelcast
   * @param mapName
   * @param key
   * @tparam K key
   * @tparam V value
   * @return Option[V], ie Some[V] or None
   */
  def get[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K): Option[V] = {
    try
    {
      val x: JMap[K, V] = hazelcast.getMap(mapName)
      val y: MMap[K, V] = x
      y.get(key)
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case npe: NullPointerException => None
      case _ => None
    }
  }


  /**
   * Wraps Hazelcast IMap.Put(K,V) method to return the Option of previous Value.
   * Exceptions are caught and None returned in that case.
   * @param hazelcast
   * @param mapName
   * @param key
   * @param value
   * @tparam K
   * @tparam V
   * @return
   */
  def put[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K, value: V): Option[V] = {
    try
    {
      import java.util.{Map => JMap}
      val x: JMap[K, V] = hazelcast.getMap(mapName)
      val ret = x.put(key, value)
      Option(ret)
    } catch {
      case npe: NullPointerException => None
      case _ => None
    }
  }

}
