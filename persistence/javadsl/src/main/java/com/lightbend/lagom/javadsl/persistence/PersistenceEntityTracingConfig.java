package com.lightbend.lagom.javadsl.persistence;

public class PersistenceEntityTracingConfig {
    public final boolean logClusterStateOnTimeout;
    public final boolean logCommandsPayloadOnTimeout;


    public PersistenceEntityTracingConfig(boolean logClusterStateOnTimeout, boolean logCommandsOnTimeout) {
        this.logClusterStateOnTimeout = logClusterStateOnTimeout;
        this.logCommandsPayloadOnTimeout = logCommandsOnTimeout;
    }
}
