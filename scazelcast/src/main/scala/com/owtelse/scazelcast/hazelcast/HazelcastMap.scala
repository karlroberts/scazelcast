package com.owtelse.scazelcast.hazelcast

import scala.collection.convert.WrapAsScala._
import collection.mutable.{Map => MMap}
import java.util.{Map => JMap}
import collection.mutable.{Set => MSet}
import java.util.{Set => JSet}
import com.hazelcast.config.Config
import com.hazelcast.core.HazelcastInstance

/**
 * Created by robertk on 21/06/14.
 */
trait HazelcastMap
{

  import com.hazelcast.query.Predicate

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

  //TODO remove Predicate from the API make it a closure
  def getByPredicate[K,V,Y,Z](hazelcast: HazelcastInstance, mapName: String)(p: Predicate[Y,Z]): Set[V] = {
    try
    {
      import com.hazelcast.core.IMap
      import scala.Predef.Set
      val x: IMap[K,V] = hazelcast.getMap(mapName)
      val entries: MSet[java.util.Map.Entry[K,V]] = x.entrySet(p)

      if (entries != null) {
        entries.map(_.getValue).toSet
      }
      else {
        Set()
      }
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case wtf : Exception => {
        // TODO fix logging
        wtf.printStackTrace()
        Set()
      }
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
      val x: JMap[K, V] = hazelcast.getMap(mapName)
      val ret = x.put(key, value)
      Option(ret)
    } catch {
      case npe: NullPointerException => None
      case _ => None
    }
  }

  /**
   * Wraps the Hazelcast IMap.remove(key) method to return an Option[V].
   * If the key is null Hazelcst would throw an exception, this is caught and None returned.
   * @param hazelcast
   * @param mapName
   * @param key
   * @return Option[V], ie Some[V] or None
   */
  def delete[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K): Option[V] = {
    try
    {
      val x: JMap[K, V] = hazelcast.getMap(mapName)
      val y: MMap[K, V] = x
      y.remove(key)
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case wtf : Exception => {
        // TODO fix logging
        wtf.printStackTrace()
        None
      }
    }
  }

}
