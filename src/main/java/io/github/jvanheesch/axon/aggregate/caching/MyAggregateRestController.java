package io.github.jvanheesch.axon.aggregate.caching;

import io.github.jvanheesch.axon.aggregate.caching.Commands.UpdateMyAggregate;
import org.axonframework.axonserver.connector.command.AxonServerCommandBus;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.callbacks.FutureCallback;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static io.github.jvanheesch.axon.aggregate.caching.Commands.CreateMyAggregate;

@RestController
public class MyAggregateRestController {
    private final SimpleCommandBus simpleCommandBus;
    private final AxonServerCommandBus axonServerCommandBus;

    public MyAggregateRestController(SimpleCommandBus simpleCommandBus, AxonServerCommandBus axonServerCommandBus) {
        this.simpleCommandBus = simpleCommandBus;
        this.axonServerCommandBus = axonServerCommandBus;
    }

    @PostMapping("/")
    public void create(@RequestParam String id) {
        FutureCallback<Object, String> futureCallback = new FutureCallback<>();
        simpleCommandBus.dispatch(GenericCommandMessage.asCommandMessage(new CreateMyAggregate(id)), futureCallback);
        CommandResultMessage<? extends String> result = futureCallback.getResult(); // block
    }

    @PostMapping("/{id}/simple")
    public ResponseEntity<?> updateSimple(@PathVariable String id, @RequestParam int version) {
        FutureCallback<Object, String> futureCallback = new FutureCallback<>();
        simpleCommandBus.dispatch(GenericCommandMessage.asCommandMessage(new UpdateMyAggregate(id, version)), futureCallback);
        CommandResultMessage<? extends String> result = futureCallback.getResult();
        return result.isExceptional()
                ? ResponseEntity.internalServerError().body(result.exceptionResult().getMessage())
                : ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/axon-server")
    public ResponseEntity<?> updateAxonServer(@PathVariable String id, @RequestParam int version) {
        FutureCallback<Object, String> futureCallback = new FutureCallback<>();
        axonServerCommandBus.dispatch(GenericCommandMessage.asCommandMessage(new UpdateMyAggregate(id, version)), futureCallback);
        CommandResultMessage<? extends String> result = futureCallback.getResult();
        return result.isExceptional()
                ? ResponseEntity.internalServerError().body(result.exceptionResult().getMessage())
                : ResponseEntity.ok().build();
    }
}
