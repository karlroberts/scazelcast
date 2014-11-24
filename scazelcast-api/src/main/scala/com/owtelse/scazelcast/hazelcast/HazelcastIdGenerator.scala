package com.owtelse.scazelcast.hazelcast

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.{IdGenerator => HzIdGenerator}


/**
  * Accessor/wrapper of a Hazelcast IdGenerator
 * TODO this should this be a typeclass
 * TODO need to prevent nulls so method is predictable, lift to Option?
 */
object HazelcastIdGenerator {

  /**
   * Try to initialize this IdGenerator instance with given id. The first
   * generated id will be 1 bigger than id.
   *
   * @return true if initialization success. If id is equal or smaller
   *         than 0, then false is returned.
   */
  def init(hz: HazelcastInstance, idGeneratorName: String)(id: Long): Boolean = {
    val idGenerator: HzIdGenerator = hz.getIdGenerator(idGeneratorName);
    idGenerator.init(id); //Optional
  }

  /**
   * Generates and returns cluster-wide unique id.
   * Generated ids are guaranteed to be unique for the entire cluster
   * as long as the cluster is live. If the cluster restarts then
   * id generation will start from 0.
   *
   * @return cluster-wide new unique id
   */
  def newId(hz: HazelcastInstance, idGeneratorName: String): Long = {
   val idGen = hz.getIdGenerator(idGeneratorName)
    idGen.newId()
  }
}
