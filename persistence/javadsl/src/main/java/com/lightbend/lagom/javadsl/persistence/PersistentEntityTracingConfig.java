package com.lightbend.lagom.javadsl.persistence;

public class PersistentEntityTracingConfig {
    public final boolean logClusterStateOnTimeout;
    public final boolean logCommandsPayloadOnTimeout;


    public PersistentEntityTracingConfig(boolean logClusterStateOnTimeout, boolean logCommandsOnTimeout) {
        this.logClusterStateOnTimeout = logClusterStateOnTimeout;
        this.logCommandsPayloadOnTimeout = logCommandsOnTimeout;
    }
}
