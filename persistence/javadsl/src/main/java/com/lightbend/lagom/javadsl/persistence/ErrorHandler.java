package com.lightbend.lagom.javadsl.persistence;

import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.cluster.Cluster;

import akka.pattern.AskTimeoutException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ErrorHandler {

    private final ActorSystem actorSystem;
    private final PersistenceEntityTracingConfig tracingConfig;
    private final Cluster cluster;
    private final String entityId;

    public ErrorHandler(ActorSystem actorSystem, PersistenceEntityTracingConfig tracingConfig, String entityId) {
        this.actorSystem = actorSystem;
        this.tracingConfig = tracingConfig;
        this.cluster =  Cluster.get(actorSystem);
        this.entityId = entityId;
    }


    public <Reply,Cmd extends Object & PersistentEntity.ReplyType<Reply>> CompletionStage<Reply> handleAskFailure(Throwable failure, Cmd command){
        if(failure instanceof AskTimeoutException){
            if(this.tracingConfig.logClusterStateOnTimeout){
                String detailedMessage = ""
                if(this.tracingConfig.logCommandsPayloadOnTimeout){
                    detailedMessage = " with payload: " + command.toString();
                }
                actorSystem.log().error(failure, "Ask timeout when sending command to  " + entityId  + detailedMessage + " cluster state :" + cluster.state());
            }
            if(this.tracingConfig.logCommandsPayloadOnTimeout){
                String message = failure.getMessage() + " with payload" + command;
                return asFailedReply(new AskTimeoutException(message,failure.getCause()));
            }

        }
        else{
            return asFailedReply(failure);
        }
    }

    private <Reply> CompletionStage<Reply> asFailedReply(Throwable failure){
        CompletableFuture<Reply> failed = new CompletableFuture<>();
        failed.completeExceptionally(failure);
        return failed;
    }

}
