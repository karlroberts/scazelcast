package com.owtelse.scazelcast.hazelcast

/**
 * Created by robertk on 21/10/14.
 */
trait HazelcastMultiMap {
  import com.hazelcast.core.HazelcastInstance
  import com.hazelcast.core.{IMap, MultiMap}
  import scala.collection.convert.WrapAsScala._
  import collection.mutable.{Map => MMap}
  import java.util.{Map => JMap}


  /**
   * Wraps the Hazelcast MultiMap.get(key) method
   * If the key is null Hazelcst would throw an exception, this is caught and None returned.
   * @param hazelcast
   * @param mapName
   * @param key
   * @tparam K key
   * @tparam V value
   * @return The list of V associated with the key
   */
  def get[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K): List[V] = {
    try
    {
      import java.util.Map
      import scala.collection.mutable.Map
      import com.hazelcast.core.MultiMap
      val x: MultiMap[K, V] = hazelcast.getMultiMap(mapName)

      val ret = x.get(key)
      if (ret != null) ret.toList else List()
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case wtf : Exception => {
        // TODO fix logging
        wtf.printStackTrace()
        List()
      }
    }
  }


  /**
   * Wraps Hazelcast MultiMap.Put(K,V) method to return true if successful.
   * Exceptions are caught and None returned in that case.
   * @param hazelcast
   * @param mapName
   * @param key
   * @param value
   * @tparam K
   * @tparam V
   * @return true if successful
   */
  def put[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K, value: V): Boolean = {
    try
    {
      if (value == null) false
      else {
        val x: MultiMap[K, V] = hazelcast.getMultiMap(mapName)

        x.put(key, value);
      }
    } catch {
      // TODO fix logging
      case wtf: Exception => {
        wtf.printStackTrace()
        false
      }
    }
  }

  /**
   * Wraps the Hazelcast MutliMap.remove(key) method
   * If the key is null Hazelcast would throw an exception, this is caught and None returned.
   * @param hazelcast
   * @param mapName
   * @param key
   * @return The list of items deleted
   */
  def deleteAll[K,V](hazelcast: HazelcastInstance, mapName: String)(key: K): List[V] = {
    try
    {
      val x: MultiMap[K, V] = hazelcast.getMultiMap(mapName)

      val ret = x.remove(key)
      if (ret != null) ret.toList else List()
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case wtf : Exception => {
        // TODO fix logging
        wtf.printStackTrace()
        List()
      }
    }
  }

  /**
   * Wraps the Hazelcast MultiMap.remove(key,value)
   * @param hazelcast
   * @param mapName
   * @param key
   * @param value
   * @tparam K
   * @tparam V
   * @return true if successfully removed from map
   */
  def delete[K,V](hazelcast: HazelcastInstance, mapName: String)(key:K, value: V): Boolean = {
    try
    {
      val x: MultiMap[K, V] = hazelcast.getMultiMap(mapName)

      x.remove(key,value)
    } catch {
      // TODO should I return a Validation or Either rather than Option?
      case wtf : Exception => {
        // TODO fix logging
        wtf.printStackTrace()
        false
      }
    }
  }

}
