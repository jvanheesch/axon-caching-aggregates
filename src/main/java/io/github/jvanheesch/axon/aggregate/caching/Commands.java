package io.github.jvanheesch.axon.aggregate.caching;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class Commands {
    public record CreateMyAggregate(@TargetAggregateIdentifier String myAggregateId) {}
    public record UpdateMyAggregate(@TargetAggregateIdentifier String myAggregateId, int version) {}
}
