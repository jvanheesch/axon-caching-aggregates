# Caching aggregates with Axon Framework and Spring Boot 2

This requires the following setup:

- `@EnableCaching` annotation
- `org.axonframework.common.caching.Cache` bean
    - we use an implementation of type `org.axonframework.common.caching.EhCacheAdapter`. This requires:
        - `net.sf.ehcache:ehcache` dependency (EhCache v2)
        - `ehcache.xml`
- `@Aggregate(cache = "axonCache")` with `axonCache` the name of the `org.axonframework.common.caching.Cache` bean
    - `SpringAggregateLookup.postProcessBeanFactory()` uses this to configure the `BeanDefinition` used to
      instantiate `CachingEventSourcingRepository`

`ExampleIT` demonstrates that:

1. caching aggregates may result in stale aggregates when writes are done by multiple nodes (`SimpleCommandBus`)
    1. aggregates may reject valid commands (`testStaleAggregateRefusesValidCommand()`)
    2. aggregates may accept invalid commands and publish events, that are in turn rejected by axon
       server (`testAxonServerRefusesEventFromStaleAggregate()`)
2. caching aggregates never results in stale aggregates when using the single writer principle (`AxonServerCommandBus` =
   default command bus when using axon server), as demonstrated by `testNoStaleAggregatesWithAxonServerCommandBus()`.
   Axon reference states that:
   ```
   Commands are always sent to exactly one application. 
   Commands for the same aggregate are always sent to the same application instance, 
   to avoid problems with concurrent updates of the aggregate.
   ```
   https://docs.axoniq.io/reference-guide/v/4.5/axon-server-introduction#message-patterns


