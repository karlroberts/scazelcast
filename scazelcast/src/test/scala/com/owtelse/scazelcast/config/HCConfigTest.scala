package com.owtelse.scazelcast.config

import org.specs2.Specification
import com.hazelcast.config._

/**
 * Created by robertk on 22/10/14.
 */
class HCConfingSpec extends HCConfigTests {
  override def is = s2"""
  Fluent API for Hazelcast Config
   *   enableMulticast works correctly $e1
   *   fluentAPI chains correctly $e2
        """
}

trait HCConfigTests extends Specification {
  def e1 = {
    import HCConfig._
    val config: Config = new Config()
    val c2: Config = config.enableMulticast(false)


    c2.getNetworkConfig.getJoin.getMulticastConfig.isEnabled must_== false
  }

  def e2 = {
    import scalaz.Lens
    val plus1 = (i: Int) => {i + 1}
    println( plus1(5) )

    val config: Config = new Config()
    val mName = "testmap"
    val mapConfigLens  = Lens.lensu[Config, MapConfig] (
      (c, mc) => c.addMapConfig(mc), _.getMapConfig(mName)
    )

    //NB setBackupCout can throw an illegal argument exception! boo!
    val partialupdate = mapConfigLens =>= {mc => mc.setBackupCount(3)}
    val cc = partialupdate(config)

    val prop1 = cc.getMapConfig(mName).getBackupCount must_== 3
    val prop2 = config.getMapConfig(mName).getBackupCount must_== 3
    prop1 and prop2
  }
}
