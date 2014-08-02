Scazelcast
==========

The intention for scazelcast is to produce a simple wrapper library for the [hazelcast](https://github.com/hazelcast/hazelcast) distributed cache/memory grid.

The wrapper will make it easier to use from a Scala program, ie hide all the messy Java stuff.

The project has a number of sister projects

  scazelcast-akka = transmogrify the scalzelcast API into an Akka Actor for nice non-blocking reactive use
  
  scazelcast-demo = working web-app using Spray-IO and scazelcast-akka to demo and play with usecases.

