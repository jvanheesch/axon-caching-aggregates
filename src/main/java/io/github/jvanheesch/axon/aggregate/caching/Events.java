package io.github.jvanheesch.axon.aggregate.caching;

public class Events {
    public record MyAggregateCreated(String myAggregateId) {}
    public record MyAggregateUpdated(String myAggregateId, int version) {}
}
