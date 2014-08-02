TODO
====

placeholder for ideas yet to be implemented

### scazelcast-api

* pimps - implicit conversions and type-classes for Hazelcast types to make them more scala idiomatic
  - eg: convert Maps and Lists etc into Scala mutable Maps and Lists so all the scala collections API can be used.
    - NB will need to think about what it means wrt distributed nature of hazelcast.
* sanitize methods to handle failure as Options or Validations
* config DSL - create a builder style DSL to simplify programatic hazelcast configuration.

### scazelcast-types

* want some Algebraic Data Types(ADT) so the category theory laws work.
* Immutable List - what does this mean for a distributed structure?
  - can it be rebalanced across nodes?
  - or are elements locked to the same Node for access performance reasons?
  - can we use it like a stream to prevent fetching the full list from distributed nodes until needed?

* Tree?


### scazelcast-akka

* ???
