package com.owtelse.scazelcast.config

import com.hazelcast.config._

//TODO create a Config case class and lenses for modification then I can create a pure version,
// NB from the Config we must contruct the equivilent Hazelcast Config at the end... possible useing the
// HzConfigCombinatorMutatingComponentImpl below

/** Cake pattern: allows injection of Hazelcast combinator functions
  * so I can swap Pure and mutating config implementations.
  * TODO may never implement an Pure version, underlying hazelcast just doesn't work that way.
  * but I'll make it as funtional as possible so we always return a value of Config so we have a chance of Purity
  */
trait HzConfigComponent {
  def hzConf: HzConfigCombinator
  
  // low level primative trait for combinators, smallest possible so easier to change
  //TODO FIXME cn't help think these should be Lenses
  trait HzConfigCombinator {
    def doIt[A](g: Config => A)(h: (Config,A) => Config)(f: A => A): Config => Config
  }
}

/**
 * low level Config API
 */
trait HzConfigLow {
  this: HzConfigComponent =>

    def doExecutorConfig(f: ExecutorConfig => ExecutorConfig): Config => Config = ???
    def doGroupConfig(f: GroupConfig => GroupConfig): Config => Config = ???
    def doJobTrackerConfig(f: JobTrackerConfig => JobTrackerConfig): Config => Config = ???
    def doListConfig(f: ListConfig => ListConfig): Config => Config = ???
    def doListenerConfig(f: ListenerConfig => ListenerConfig): Config => Config = ???
    def doManagementCenterConfig(f: ManagementCenterConfig => ManagementCenterConfig): Config => Config = ???

    def doMapConfig(name: String)(f: MapConfig => MapConfig): Config => Config =
      hzConf.doIt(_.getMapConfig(name))((c: Config, a: MapConfig) => c.addMapConfig(a))(f)

    def doMemberAttributeConfig(f: MemberAttributeConfig => MemberAttributeConfig): Config => Config = ???
    def doMultiMapConfig(f: MultiMapConfig => MultiMapConfig): Config => Config = ???

    def doNetworkConfig(f: NetworkConfig => NetworkConfig): Config => Config =
      hzConf.doIt(_.getNetworkConfig)(_.setNetworkConfig(_))(f)

    def doPartitionGroupConfig(f: PartitionGroupConfig => PartitionGroupConfig): Config => Config = ???
    def doQueueConfig(f: QueueConfig => QueueConfig): Config => Config = ???
    def doSecurityConfig(f: SecurityConfig => SecurityConfig): Config => Config = ???
    def doSemaphoreConfig(f: SemaphoreConfig => SemaphoreConfig): Config => Config = ???
    def doSerializationConfig(f: SerializationConfig => SerializationConfig): Config => Config = ???
    def doServicesConfig(f: ServicesConfig => ServicesConfig): Config => Config = ???
    def doSetConfig(f: SetConfig => SetConfig): Config => Config = ???
    def doTopicConfig(f: TopicConfig => TopicConfig): Config => Config = ???
    def doWanReplicationConfig(f: WanReplicationConfig => WanReplicationConfig): Config => Config = ???
}

/**
 * Cake pattern: use the low level dependency HzConfig to build more more fluent API that will be exposed
 */
trait HzConfig {
  this: HzConfigLow =>

  def enableMulticast(enable: Boolean): Config => Config = doNetworkConfig(nc => {
    nc.getJoin.getMulticastConfig.setEnabled(enable) //FIXME create Lens
    //nc mutated
    nc
  })

  def enableMapWriteThrough(mapname: String): Config => Config  = doMapConfig(mapname)(mc => {
    val m = mc.getMapStoreConfig.setClassName(mapname)
    m.setEnabled(true)
    m.setWriteDelaySeconds(0); //makes it write-through not writebehind
    mc
  })

}

/**
 * implementation trait of combinator functions that mutate hazelcast Config objects
 * TODO can I do  pure version that builds a new config copy without modifying the origional?
 * TODO Will Reader[Config, A] be enough?
 */
trait HzConfigCombinatorMutatingComponentImpl extends  HzConfigComponent  {

  /*
   * primative combinators for modyfying or decorating the Config programatically.
   * NB Hazelcast does not allow modifcation of Config after it is applied to an instance.
   *
   * NB don't really need to run h because f on g(c) mutates c, eg below but this way I could go immutable too??
   *         val x = f(c.getMapConfig(name))
             config mutatated by f, so don't really need to add it.
             c.addMapConfig(x)
   * TODO experiment to see what happens if multiple nodes have different config??? chaos?
   */
  val hzConf = new HzConfigCombinator {
    override def doIt[A](g: Config => A)(h: (Config,A) => Config)(f: A => A): Config => Config =
      c =>  h(c,f(g(c)))
  }
}

trait HzConfigCombinatorImmutatableComponentImpl extends HzConfigComponent {
  val hzConf = new HzConfigCombinator {
    override def doIt[A](g: (Config) => A)(h: (Config, A) => Config)(f: (A) => A): (Config) => Config = c => {
      val cIm = new Config()
      // cIM need to copy all values from c copy Contructor would be nice
      val ret = h(cIm, f(g(c)))
      ??? // TODO need to implement Config copy to be immutable
    }
  }
}

trait HCMapConfig[MC] {
  def usingStore(storeClassName: String)(mc: MC): MC
}
object HCMapConfig {
  implicit object HazelcastMapConfig extends HCMapConfig[MapConfig] {
    override def usingStore(storeClassName: String)(mc: MapConfig): MapConfig = ???
  }
}

trait HCConfig[C] {
  def withMulticast(enable: Boolean)(config: C): C
//  def withMapWriteThrough[MC: HCMapConfig](mapname: String)(config: C): MC
}


object HCConfig extends HzConfig with HzConfigLow with HzConfigCombinatorMutatingComponentImpl {

  //HzConfig instances
  implicit object HazelcastConfigInstance extends HCConfig[Config] {
    def withMulticast( enable: Boolean)(config: Config): Config = {
      val x  = enableMulticast(enable)(config)
      x
    }

/*    //need implicit in scope
    import HCMapConfig._
    def withMapWriteThrough[MC: HCMapConfig](mapname: String)(config: Config)  = {
      val c = enableMapWriteThrough(mapname)
      val x = c andThen(_.getMapConfig(mapname))
      x(config)
    } */
  }


  //
  implicit def ConfigToHzConfig[Config: HCConfig](c: Config) = implicitly[HCConfig[Config]]

  /**
   * Allows a more fluent API by pimping instances of HCConfig.
   * e.g. withMulticast
   * @tparam C
   */
  implicit class HzConfigFluentApi[C: HCConfig](conf: C)  {
    val adaptor = implicitly[HCConfig[C]]
    def enableMulticast(enable: Boolean): C = { adaptor.withMulticast(enable)(conf) }
  }

  // TODO would this be simpler? this way give nice syntax i can do config.withMulticast(true)... but is implicit conversion
  // TODO danger i think? prefer typeclass, how do I get prety syntax?
  //  implicit class HZConfigWrapper(config: Config) {
  //    def withMulticast(enable: Boolean): Config = {
  //      HazelcastConfig.withMulticast(enable)(config)
  //    }
  //  }
}
