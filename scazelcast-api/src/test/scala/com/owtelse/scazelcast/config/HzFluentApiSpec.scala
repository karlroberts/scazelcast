package com.owtelse.scazelcast.config

import org.specs2.Specification
import com.hazelcast.config._

/**
 * Created by robertk on 17/12/14.
 */
class HzFluentApiSpec extends HzFluentApiTests {
  override def is = s2"""
  Fluent API for Hazelcast Config
   *   enableMulticast works correctly $e1
           """
}

trait HzFluentApiTests extends Specification {
  def e1 = {
    import com.owtelse.scazelcast.config.FluentApi._
    //test OnMap

    val c1: Config = new Config()
    val blah = (c1.withWriteThrough)
    val cc1: Config = c1.onMap("foo") (blah)

    val prop1 = cc1.getMapConfig("foo").getMapStoreConfig.getWriteDelaySeconds must_== 0
    prop1
  }
}
