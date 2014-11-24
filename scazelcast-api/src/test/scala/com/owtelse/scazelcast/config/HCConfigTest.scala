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
   *   composing lens on config $e2
   *   composing lens on config $e3
   *   composing a MapLens on MapConfig $e4
           """
}

trait HCConfigTests extends Specification {

  import scalaz.Functor

  def e1 = {
    import DistCacheConfig._
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

    val testmapMapCfgL = ConfigOps.mapConfigL("testmap")
    val modevictionP = testmapMapCfgL =>= {_.setEvictionPercentage(33)}

    val combinedEffects = partialupdate.andThen( modevictionP )
    val cc = combinedEffects(config)

    val prop1 = cc.getMapConfig(mName).getBackupCount must_== 3
    val prop2 = config.getMapConfig(mName).getBackupCount must_== 3
    val prop3 = cc.getMapConfig(mName).getEvictionPercentage must_== 33



    prop1 and prop2 and prop3
  }

  def e3 = {
    val config: Config = new Config()
    val config2: Config = new Config()
    import ConfigOps._
    val modJoinL = networkConfigL >- { (nc) => nc.setPort(5555)  }

    val modMC = multicastConfigL >- { (mc) => mc.setEnabled(true); mc.setMulticastGroup("224.0.33.44")  }

    val modMulticast = networkConfigL >=> joinConfigL >=> multicastConfigL

    def modMulticastandport = for {
      x <- networkConfigL %= { _.setPort(5555) }
      y <- networkConfigL %= { _.setPort(9995) }
      z <- networkConfigL %= { (b) => b.setPortAutoIncrement(false)}
      //jc <- joinConfigL(y)
      // _ <- multicastConfigL %= { (mc) => mc.setEnabled(true); mc.setMulticastGroup("224.0.33.44")   }
     // zz <- multicastConfigL(jc)
    } yield x // (y,z)

    val c1 = modMulticastandport exec (config)

    val prop1 = c1.getNetworkConfig.getPort must_== 9995

    val combinedSet = ( networkConfigL =>= (_.setPort(9996)) ) andThen ( modMulticast =>= { (mc) => mc.setEnabled(false); mc.setMulticastGroup("224.0.33.44")  })

    val c2: Config = combinedSet(config2)

    val prop2 = c2.getNetworkConfig.getPort must_== 9996
    val prop3 = c2.getNetworkConfig.getJoin.getMulticastConfig.isEnabled must_== false
    val prop4 = c2.getNetworkConfig.getJoin.getMulticastConfig.getMulticastGroup must_== "224.0.33.44"

    prop1 and prop2 and prop3 and prop4
  }

  def e4 = {
    import ConfigOps._
    val c1: Config = new Config()

    //set unknow Key
    val mc1 = mapConfigL("k1").get(c1)
    val cc1 = mapConfigL("k1").set(c1, mc1.setBackupCount(3))
    val prop1 = cc1.getMapConfig("k1").getBackupCount must_== 3

    //modify unknown key

    //get unknown key

    prop1
  }





}
