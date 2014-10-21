package com.owtelse.scazelcast

/**
 * Created by robertk on 21/10/14.
 */
case class Predicate[K,V](p: (K, V) => Boolean) {


}
