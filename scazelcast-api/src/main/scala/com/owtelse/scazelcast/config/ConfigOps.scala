package com.owtelse.scazelcast.config

import com.hazelcast.config._
import scalaz.Lens
import java.util.{Collection => JCollection, Map => JMap, List => JList}
import java.lang.{Integer => JInt}/**

 * Created by robertk on 23/10/14.
 */
class ConfigOps {

}


/**
 * Note that the Config is actually mutated by the Hazelcast API. So the lens is not Pure in some cases.
 */
trait ConfigLenz {
  val groupConfigL = Lens.lensu[Config, GroupConfig]((c,v) => c.setGroupConfig(v), _.getGroupConfig)
  
  val executorConfigsL = Lens.lensu[Config, JMap[String,ExecutorConfig ]](
    (c,v) => c.setExecutorConfigs(v) , _.getExecutorConfigs)

  //Mutation of Config needed in setter
  val executorConfigL = (eName: String) => Lens.lensu[Config, ExecutorConfig] (
    (c,v) => {c.getExecutorConfigs.put(eName, v); c}, _.getExecutorConfig(eName))

  val jobConfigsL = Lens.lensu[Config,  JMap[String, JobTrackerConfig]](
    (c,v) => c.setJobTrackerConfigs(v), _.getJobTrackerConfigs)

  //Mutation of Config needed in setter
  val jobConfigL = (name: String) => Lens.lensu[Config, JobTrackerConfig](
    (c,v) => {c.getJobTrackerConfigs.put(name, v); c}, _.getJobTrackerConfig(name)
  )

//  val listConfigL = Lens.lensu[Config,  ListConfig ]((c,v) => ???, ???)
//  val listenerConfigL = Lens.lensu[Config,  ListenerConfig ]((c,v) => ???, ???)
//  val manCenterConfigL = Lens.lensu[Config,  ManagementCenterConfig ]((c,v) => ???, ???)
  val mapConfigsL = Lens.lensu[Config,  JMap[String, MapConfig] ]((c,v) => c.setMapConfigs(v), _.getMapConfigs)
  val mapConfigL  = (mapName:String) => Lens.lensu[Config, MapConfig] (
    (c, mc) => c.addMapConfig(mc), _.getMapConfig(mapName)
  )

//    def doMapConfig(name: String)(f: MapConfig => MapConfig): Config => Config =
//      hzConf.doIt(_.getMapConfig(name))((c: Config, a: MapConfig) => c.addMapConfig(a))(f)
//
//  val memberAttrConfigL = Lens.lensu[Config,  MemberAttributeConfig ]((c,v) => ???, ???)
//  val multiMapConfigL = Lens.lensu[Config,  MultiMapConfig ]((c,v) => ???, ???)
  val networkConfigL = Lens.lensu[Config,  NetworkConfig ]((c,v) => c.setNetworkConfig(v), _.getNetworkConfig)

//    def doNetworkConfig(f: NetworkConfig => NetworkConfig): Config => Config =
//      hzConf.doIt(_.getNetworkConfig)(_.setNetworkConfig(_))(f)

//  val partitionGroupConfigL = Lens.lensu[Config,  PartitionGroupConfig ]((c,v) => ???, ???)
//  val queueConfigL = Lens.lensu[Config,  QueueConfig ]((c,v) => ???, ???)
//  val securityConfigL = Lens.lensu[Config,  SecurityConfig ]((c,v) => ???, ???)
//  val semaphoreConfigL = Lens.lensu[Config,  SemaphoreConfig ]((c,v) => ???, ???)
//  val serialisationConfigL = Lens.lensu[Config,  SerializationConfig ]((c,v) => ???, ???)
//  val servicesConfigL = Lens.lensu[Config,  ServicesConfig ]((c,v) => ???, ???)
//  val setConfigL = Lens.lensu[Config,  SetConfig ]((c,v) => ???, ???)
//  val topicConfigL = Lens.lensu[Config,  TopicConfig ]((c,v) => ???, ???)
//  val wanRepConfigL = Lens.lensu[Config,  WanReplicationConfig ]((c,v) => ???, ???)
}

// ---- Sub Config Lenses
trait ExecutorConfigLenz {/*TODO*/}
trait JobTrackerConfigLenz {/*TODO*/}
trait ListConfigLenz {/*TODO*/}
trait ListenerConfigLenz {/*TODO*/}
trait ManagementCenterConfigLenz {/*TODO*/}
trait MemberAttributeConfigLenz {/*TODO*/}
trait MultiMapConfigLenz {/*TODO*/}
trait NetworkConfigLenz {
  val outBoundPortDefinitionsL = Lens.lensu[NetworkConfig, JCollection[String]]((n,v) => n.setOutboundPortDefinitions(v), _.getOutboundPortDefinitions)
  val outBoundPortL = Lens.lensu[NetworkConfig, JCollection[JInt]]((n,v) => n.setOutboundPorts(v), _.getOutboundPorts)
  val interfaceConfigL = Lens.lensu[NetworkConfig, InterfacesConfig]((n,v) => n.setInterfaces(v), _.getInterfaces)
  val joinConfigL = Lens.lensu[NetworkConfig, JoinConfig]((n,v) => n.setJoin(v), _.getJoin)
  val symetricEncryptionConfigL = Lens.lensu[NetworkConfig, SymmetricEncryptionConfig]((n,v) => n.setSymmetricEncryptionConfig(v), _.getSymmetricEncryptionConfig)
  val socketInterceptorConfigL = Lens.lensu[NetworkConfig, SocketInterceptorConfig]((n,v) => n.setSocketInterceptorConfig(v), _.getSocketInterceptorConfig)
  val SSLConfigL = Lens.lensu[NetworkConfig, SSLConfig]((n,v) => n.setSSLConfig(v), _.getSSLConfig)
}
trait PartitionGroupConfigLenz {/*TODO*/}
trait QueueConfigLenz {/*TODO*/}
trait SecurityConfigLenz {/*TODO*/}
trait SemaphoreConfigLenz {/*TODO*/}
trait SerializationConfigLenz {/*TODO*/}
trait ServicesConfigLenz {/*TODO*/}
trait SetConfigLenz {/*TODO*/}
trait TopicConfigLenz {/*TODO*/}
trait WanReplicationConfigLenz {/*TODO*/}


// sub NetworkConfig
trait InterfacesConfigLenz {/*TODO*/}
trait JoinConfigLenz {
  var multicastConfigL = Lens.lensu[JoinConfig, MulticastConfig]((j,v) => j.setMulticastConfig(v), _.getMulticastConfig)
  var tcpIpConfigL = Lens.lensu[JoinConfig, TcpIpConfig]((j,v)=> j.setTcpIpConfig(v), _.getTcpIpConfig)
  var awsConfigL = Lens.lensu[JoinConfig, AwsConfig]((j,v) => j.setAwsConfig(v), _.getAwsConfig)
}
trait SymmetricEncryptionConfigLenz {/*TODO*/}
trait SocketInterceptorConfigLenz {/*TODO*/}
trait SSLConfigLenz {/*TODO*/}

//sub MapConfig
trait MapConfigLenz {
  val mapStoreConfigL = Lens.lensu[MapConfig, MapStoreConfig](
    (mc,v) => mc.setMapStoreConfig(v), _.getMapStoreConfig)
  val maxSizeConfigL = Lens.lensu[MapConfig, MaxSizeConfig](
    (mc,v) => mc.setMaxSizeConfig(v), _.getMaxSizeConfig)
  val nearCacheConfigL = Lens.lensu[MapConfig, NearCacheConfig](
    (mc,v) => mc.setNearCacheConfig(v), _.getNearCacheConfig)
  val entryListenerConfigsL = Lens.lensu[MapConfig, JList[EntryListenerConfig]](
    (mc,v) => mc.setEntryListenerConfigs(v), _.getEntryListenerConfigs)
  // cant get a specific EntryListener but stll may be useful to add one???
  val entryListenerConfigL = Lens.lensu[MapConfig, EntryListenerConfig](
    (mc,v) => {mc.addEntryListenerConfig(v)}, ???)
  val mapIndexConfigsL = Lens.lensu[MapConfig, JList[MapIndexConfig]](
    (mc,v) => mc.setMapIndexConfigs(v), _.getMapIndexConfigs)
  val partitionStrategyConfigL = Lens.lensu[MapConfig, PartitioningStrategyConfig](
    (mc,v) => mc.setPartitioningStrategyConfig(v), _.getPartitioningStrategyConfig)
}




object ConfigOps extends ConfigLenz with NetworkConfigLenz with JoinConfigLenz with MapConfigLenz {
  // TODO maybe move this to the config trait?
}
