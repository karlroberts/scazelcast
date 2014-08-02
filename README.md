Scazelcast
==========

The intention for Scazelcast is to produce a simple wrapper library for the
[hazelcast](https://github.com/hazelcast/hazelcast) distributed cache/memory grid.

The wrapper will make it easier to use from a Scala program, ie hide all the messy Java stuff.

IN addition there are some higher level libraries, such as scazelcast-akka that create an Akka Actor abstraction to the
underlying hazelcast grid. This allows us to use Hazelcast from Akka in a non blocking reactive manner

## Jars

The project is broken into a number of separate .jar files that depend on each other hierarchically

* scazelcast-api
  - Pimps and wrappers for Hazelcast.

* scazelcast-types
  - Additional distributed data structures built on top of hazelcast low level API, more suited to functional programming.

* scazelcast-akka
  - Depends on scazelcast-api.
  - Transmogrify the scalzelcast API into an Akka Actor for nice non-blocking reactive use.
  
* scazelcast-demo
  - Depends on scazelcast-akka.
  - Working web-app using Spray-IO and scazelcast-akka to demo and play with use cases.

## Features implemented

### scazelcast-api
#### Map
* Get and Put return an Option[V] and trap exceptions to produce None

### scalzelcast-types

### scalzelcast-akka
* Basic Actor for asynchronously getting and putting from a hazelcast IMap.

## TODO



