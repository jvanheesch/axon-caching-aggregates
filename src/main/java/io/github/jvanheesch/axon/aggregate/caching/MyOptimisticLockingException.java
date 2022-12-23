package io.github.jvanheesch.axon.aggregate.caching;

public class MyOptimisticLockingException extends RuntimeException {
    public MyOptimisticLockingException(int expectedVersion, int actualVersion) {
        super(String.format("MyOptimisticLockingException: Expected version = %d, actual version = %d", expectedVersion, actualVersion));
    }
}
