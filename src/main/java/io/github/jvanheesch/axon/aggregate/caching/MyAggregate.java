package io.github.jvanheesch.axon.aggregate.caching;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(cache = "axonCache")
public class MyAggregate {
    @AggregateIdentifier
    private String id;
    private int version;

    @CommandHandler
    public MyAggregate(Commands.CreateMyAggregate createMyAggregate) {
        apply(new Events.MyAggregateCreated(createMyAggregate.myAggregateId()));
    }

    // required by axon framework
    MyAggregate() {
    }

    @CommandHandler
    public void handle(Commands.UpdateMyAggregate command) {
        if (command.version() == version) {
            apply(new Events.MyAggregateUpdated(id, version + 1));
        } else {
            throw new MyOptimisticLockingException(command.version(), version);
        }
    }

    @EventSourcingHandler
    public void handle(Events.MyAggregateCreated event) {
        id = event.myAggregateId();
        version = 0;
    }

    @EventSourcingHandler
    public void handle(Events.MyAggregateUpdated event) {
        version = event.version();
    }
}
