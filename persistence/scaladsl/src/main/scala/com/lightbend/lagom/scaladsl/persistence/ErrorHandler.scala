package com.lightbend.lagom.scaladsl.persistence

import akka.cluster.Cluster
import akka.pattern.AskTimeoutException
import scala.concurrent.Future


class PersistentEntityTracingConfig(val logClusterStateOnTimeout   : Boolean,
                                    val logCommandsPayloadOnTimeout: Boolean)

class ErrorHandler(val cluster      : Cluster,
                   val tracingConfig: PersistentEntityTracingConfig,
                   val entityId     : String) {

  def mapResult[Reply, Cmd <: Object with PersistentEntity.ReplyType[Reply]](result: Future[Any],
                                                                                    command: Cmd):Future[Reply] = {
    result.flatMap {
      case exc: Throwable =>
        // not using akka.actor.Status.Failure because it is using Java serialization
        Future.failed(exc)
      case result => Future.successful(result)
    }.recoverWith{
      case e: Throwable => convertFailure(e,command)
    }.asInstanceOf[Future[Reply]]

  }

  private def convertFailure[Reply,Cmd <: PersistentEntity.ReplyType[_]](failure:Throwable, command:Cmd):Future[command.ReplyType] = {
    if (failure.isInstanceOf[AskTimeoutException]) {
      if (this.tracingConfig.logClusterStateOnTimeout) {
        var detailedMessage  = ""
        if (this.tracingConfig.logCommandsPayloadOnTimeout) detailedMessage = " with payload: " + command.toString
        cluster.system.log.error(failure,
          "Ask timeout when sending command to  " + entityId + detailedMessage + " cluster state :" + cluster.state)
      }
      if (this.tracingConfig.logCommandsPayloadOnTimeout) {
        val message  = failure.getMessage + " with payload" + command
        Future.failed(new AskTimeoutException(message, failure.getCause))
      }
      Future.failed(failure)
    }
    else Future.failed(failure)
  }


}
