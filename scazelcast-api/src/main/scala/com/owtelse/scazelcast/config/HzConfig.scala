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

    import scalaz.Lens

    def doIt[A](g: Config => A)(h: (Config,A) => Config)(f: A => A): Config => Config

    //squashes down a bit when I use Lens instead of Doit
    def lensMod[A](configLens: Lens[Config, A])(f: (A) => A): (Config) => Config = configLens =>= f
  }
}

/**
 * Cake pattern: use the low level dependency HzConfig to build more more fluent API that will be exposed
 */
trait HzConfig {
  this: HzConfigComponent =>
  import ConfigOps._
  def enableMulticast(enable: Boolean): Config => Config = (networkConfigL >=> joinConfigL >=> multicastConfigL) =>=
    { _.setEnabled(enable) }

  def enableMapWriteBehind(delaySec: Int) = (mapName:String) =>
    (mapConfigL(mapName) >=> mapStoreConfigL) =>= { msc =>
      msc.setEnabled(true)
      msc.setWriteDelaySeconds(delaySec) // 0 seconds makes it writeThrough not writeBehind
    }

  def enableWriteThrough = (mapName: String) => enableMapWriteBehind(0)(mapName)

  def withStore(storeClassName: String) = (mapName:String) => (mapConfigL(mapName) >=> mapStoreConfigL) =>= {
    _.setClassName(storeClassName)
  }



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
   * NB don't really need to run s because f on g(c) mutates c, eg below but this way I could go immutable too??
   *         val x = f(c.getMapConfig(name))
             config mutatated by f, so don't really need to add it.
             c.addMapConfig(x)
   * TODO experiment to see what happens if multiple nodes have different config??? chaos?
   */
  val hzConf = new HzConfigCombinator {
    override def doIt[A](g: Config => A)(s: (Config,A) => Config)(f: A => A): Config => Config =
      c =>  s(c,f(g(c)))
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

//trait HCMapConfig[MC] {
//  def usingStore(storeClassName: String)(mc: MC): MC
//}
//object HCMapConfig {
//  implicit object HazelcastMapConfig extends HCMapConfig[MapConfig] {
//    override def usingStore(storeClassName: String)(mc: MapConfig): MapConfig = ???
//  }
//}

/**
 * Typeclass exposing a Distributed Cache Config API, allowing instances to conform to this interface.
 * @tparam C
 */
trait DistCacheConfig[C] {
  def withMulticast(enable: Boolean)(config: C): C
  def withWriteThrough(config: C): String => C
//  def withMapWriteThrough[MC: HCMapConfig](mapname: String)(config: C): MC
  def onMap(mapName: String)(f: String => C): C = f(mapName)
//  def map[B: DistCacheConfig](f: C => B)(config: C): DistCacheConfig[B] = { f(config); implicitly[DistCacheConfig[B]] }
}


object DistCacheConfig extends DistCacheInstances {


}

trait DistCacheInstances {
  //HzConfig instances
  implicit object HazelcastConfigInstance extends DistCacheConfig[Config] with HzConfig with  HzConfigCombinatorMutatingComponentImpl  {
    def withMulticast( enable: Boolean)(config: Config): Config = {
      enableMulticast(enable)(config)
    }

    /*    //need implicit in scope
        import HCMapConfig._
        def withMapWriteThrough[MC: HCMapConfig](mapname: String)(config: Config)  = {
          val c = enableMapWriteThrough(mapname)
          val x = c andThen(_.getMapConfig(mapname))
          x(config)
        } */
    override def withWriteThrough(config: Config): String => Config = (mapName: String) => enableWriteThrough(mapName)(config)

  }


  //
  //implicit def ConfigToHzConfig[Config: DistCacheConfig](c: Config) = implicitly[DistCacheConfig[Config]]


}

/**
 * Allows a more fluent API by pimping instances of HCConfig.
 * e.g. withMulticast
 */

object FluentApi {

  implicit class HzConfigFluentApi[C : DistCacheConfig](conf: C) {
    //    implicit class HzConfigFluentApi[C](conf: C)(implicit evidence: C =:= Config)  {


    //could use implicit conversion func ConfigToHzConfig like this
    // conf.withMulticast(true)(conf) ;
    //
    //or just be honest about it like this
    //      adaptor.withMulticast(enable)(conf) }

    val adaptor = implicitly[DistCacheConfig[C]]


    //HazelcastConfigInstance
    def enableMulticast(enable: Boolean): C = adaptor.withMulticast(enable)(conf)
//    def enableMulticast(enable: Boolean)(implicit adaptor: DistCacheConfig[Config]): Config = adaptor.withMulticast(enable)(conf)

//    def withWriteThrough(implicit adaptor: DistCacheConfig[Config]): String => Config = {
//      adaptor.withWriteThrough(conf)
//    }
def withWriteThrough: String => C = { adaptor.withWriteThrough(conf) }

    def doMulticast(b: Boolean) = adaptor.withMulticast(b) _
//    def doMulticast(b: Boolean)(implicit adaptor: DistCacheConfig[Config]) = implicitly[DistCacheConfig[Config]].withMulticast(b) _

    def wibbleWirteThrough = adaptor.withWriteThrough _
//    def wibbleWirteThrough(implicit adaptor: DistCacheConfig[Config]) = implicitly[DistCacheConfig[Config]].withWriteThrough _

    /**
     * Just returns the name of the map to allow english style """ withWriteThrough onMap "mapName" """
     */
//    def onMap(mapName: String)(implicit adaptor: DistCacheConfig[Config]) = adaptor.onMap(mapName) _
    def onMap(mapName: String) = adaptor.onMap(mapName) _

    def map[B: DistCacheConfig](f: C => B): HzConfigFluentApi[B] = new HzConfigFluentApi[B](f(conf))
//    def map(f: Config => Config): HzConfigFluentApi = new HzConfigFluentApi(f(conf))


    def flatMap[B: DistCacheConfig](f: C => HzConfigFluentApi[B]): HzConfigFluentApi[B] = f(conf)
//    def flatMap(f: Config => HzConfigFluentApi): HzConfigFluentApi = f(conf)
  }

  object HzConfigFluentApiInstances {

    import scalaz.Monad

//    def apply[Config: DistCacheConfig](conf: Config) = new HzConfigFluentApi(conf)
    def apply(conf: Config) = new HzConfigFluentApi(conf)

    // HzConfigFluentAPI instances
//      implicit def hzConfigFluentApiMonad[C](implicit adaptor: DistCacheConfig[C]): Monad[HzConfigFluentApi] = new HzFluentApiMonad[C] {
//      implicit def _conf = adaptor
//    }
//
//    private trait HzFluentApiMonad[C] extends Monad[HzConfigFluentApi] {
//      implicit def _conf : DistCacheConfig[C]
//
//      override def point[A](a: => A): HzConfigFluentApi[A] = new HzConfigFluentApi(a)
//
//      override def bind[A, B ](fa: HzConfigFluentApi[A])(f: A => HzConfigFluentApi[B]): HzConfigFluentApi[B] = fa.flatMap(f)
//    }
  }

    // TODO would this be simpler? this way give nice syntax i can do config.withMulticast(true)... but is implicit conversion
    // TODO danger i think? prefer typeclass, how do I get prety syntax?
    //    implicit class HZConfigWrapper(config: Config) {
    //      def withMulticast(enable: Boolean): Config = {
    //        HazelcastConfig.withMulticast(enable)(config)
    //      }
    //    }


}