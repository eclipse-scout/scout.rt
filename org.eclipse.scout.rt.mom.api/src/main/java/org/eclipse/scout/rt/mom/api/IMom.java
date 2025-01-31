/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonDataObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * Message oriented middleware (MOM) for sending and receiving messages between distributed systems.
 * <p>
 * This MOM provides publish/subscribe (topic-based) or point-to-point (queue-based) messaging. A topic allows to
 * publish a message to multiple subscribers, meaning that the message is transported to all consumers which currently
 * are subscribed. A queue differs from the topic distribution mechanism that the message is transported to exactly one
 * consumer. If there is no subscription the message will be kept until a consumer subscribes for the queue. However, if
 * there are multiple subscriptions for the queue, the message is load balanced to a single consumer.
 * <p>
 * Besides, this MOM provides 'request-reply' messaging, which is kind of synchronous communication between two parties.
 * The publisher of a message blocks until the reply is received. This mode is still based on P2P or pub/sub messaging,
 * meaning there is no open connection for the time of blocking.
 * <p>
 * Message addressing is based on destinations (queues or topics), which additionally allow to register for a
 * {@link IMarshaller}. A marshaller is used to transform the transfer object into its transport representation, like
 * text in JSON format, or bytes for the object's serialization data.
 * <p>
 * End-to-end security may be achieved by using a secure communication protocol, or by encrypting the messages in the
 * {@link IMarshaller}. However, if relying on a secure transport layer, messages may temporarily be stored in
 * clear-text like when being delivered to queues.
 *
 * @since 6.1
 */
public interface IMom {

  /**
   * Indicates the order of the MOM's {@link IPlatformListener} to shutdown itself upon entering platform state
   * {@link State#PlatformStopping}. Any listener depending on MOM facility must be configured with an order less than
   * this value.
   */
  long DESTROY_ORDER = 5_700;

  /**
   * @return <b>volatile</b> identifier of this MOM
   */
  String getId();

  /**
   * @return symbolic name of this MOM
   */
  String getName();

  /**
   * @return new mutable non-null list of all subscriptions
   */
  List<ISubscription> getSubscriptions();

  /**
   * Publishes the given message to the given destination.
   *
   * @param destination
   *     specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *     documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *     specifies the transfer object to be sent to the destination.<br>
   *     The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *     that destination or the default marshaller specified by {@link DefaultMarshallerProperty}.
   * @param input
   *     specifies how to publish the message.
   * @param <DTO>
   *     the type of the transfer object to be published.
   * @see IMom#subscribe(IDestination, IMessageListener, SubscribeInput)
   */
  <DTO> void publish(IDestination<DTO> destination, DTO transferObject, PublishInput input);

  /**
   * Subscribes the given listener to receive messages sent to the given destination.
   *
   * @param destination
   *     specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *     {@link IMom} documentation for more information about the difference between topic and queue based
   *     messaging.
   * @param listener
   *     specifies the listener to receive messages.
   * @param input
   *     specifies how to subscribe for messages.
   * @param <DTO>
   *     the type of the transfer object a subscription is created for.
   * @return subscription handle to unsubscribe from the destination.
   * @see IMom#publish(IDestination, Object, PublishInput)
   */
  <DTO> ISubscription subscribe(IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input);

  /**
   * Initiates a 'request-reply' communication with a replier, and blocks until the reply is received. This type of
   * communication does not support transacted message publishing.
   * <p>
   * This method is for convenience to facilitate synchronous communication between a publisher and a subscriber, and is
   * still based on P2P or pub/sub messaging, meaning that there is no open connection for the time of blocking.
   * <p>
   * Typically, request-reply is used with a queue destination. If using a topic, it is the first reply which is
   * returned.
   * <p>
   * If the current thread is interrupted while waiting for the reply to receive, this method returns with a
   * {@link ThreadInterruptedError} and the interruption is propagated to the consumer(s) as well.
   * <p>
   * If invoked from a semaphore aware job, the job's permit is released and passed to the next competing job while
   * waiting for the reply.
   *
   * @param destination
   *     specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *     documentation for more information about the difference between topic and queue based messaging.
   * @param requestObject
   *     specifies the transfer object to be sent to the destination.<br>
   *     The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *     that destination or the default marshaller specified by {@link DefaultMarshallerProperty}.
   * @param input
   *     specifies how to publish the message. Transacted publish of the request is not supported.
   * @param <REQUEST>
   *     the type of the request object
   * @param <REPLY>
   *     the type of the reply object
   * @return the reply of the consumer.
   * @throws ThreadInterruptedError
   *     if interrupted while waiting for the reply to receive. If interrupted, an interruption request is sent to
   *     the consumer(s).
   * @throws TimedOutError
   *     if the timeout specified via {@link PublishInput#withRequestReplyTimeout(long, TimeUnit)} elapsed. If
   *     elapsed, an interruption request is sent to the consumer(s).
   * @throws RuntimeException
   *     if the request failed because the replier threw an exception. If threw a {@link RuntimeException}, it is
   *     that exception which is thrown.
   * @see IMom#reply(IBiDestination, IRequestListener, SubscribeInput)
   */
  <REQUEST, REPLY> REPLY request(IBiDestination<REQUEST, REPLY> destination, REQUEST requestObject, PublishInput input);

  /**
   * Subscribes the given listener to receive messages from 'request-reply' communication sent to the given destination.
   *
   * @param destination
   *     specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *     {@link IMom} documentation for more information about the difference between topic and queue based
   *     messaging.
   * @param listener
   *     specifies the listener to receive messages.
   * @param input
   *     specifies how to subscribe for messages.
   * @param <REQUEST>
   *     the type of the request object
   * @param <REPLY>
   *     the type of the reply object
   * @return subscription handle to unsubscribe from the destination.
   * @see IMom#request(IBiDestination, Object, PublishInput)
   */
  <REQUEST, REPLY> ISubscription reply(IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input);

  /**
   * Cancels a <i>durable</i> subscription previously created on this MOM. Messages published to the subscription's
   * destination while the subscriber is inactive will then no longer be kept by the network.
   * <p>
   * If the durable subscription has already been established for the current MOM instance, make sure to call
   * {@link ISubscription#dispose()} first. Canceling the durable subscription while the subscriber is still active will
   * result in an exception. An exception is also thrown if there is no durable subscription for the given name on the
   * MOM instance.
   *
   * @param durableSubscriptionName
   *     The same name that was used to create the durable subscription (see
   *     {@link SubscribeInput#withDurableSubscription(String)}).
   */
  void cancelDurableSubscription(String durableSubscriptionName);

  /**
   * Registers a marshaller for transfer objects sent to the given destination, or which are received from the given
   * destination.
   * <p>
   * A marshaller transforms a transfer object into its transport representation to be sent across the network.
   * <p>
   * By default, if a destination does not specify a marshaller, the marshaller specified by
   * {@link DefaultMarshallerProperty} is used.
   *
   * @return registration handle to unregister the marshaller from the destination.
   * @see TextMarshaller
   * @see BytesMarshaller
   * @see JsonDataObjectMarshaller
   * @see JsonMarshaller
   * @see ObjectMarshaller
   */
  IRegistrationHandle registerMarshaller(IDestination<?> destination, IMarshaller marshaller);

  /**
   * Destroys this MOM and releases all associated resources.
   */
  void destroy();

  class DefaultMarshallerProperty extends AbstractClassConfigProperty<IMarshaller> {

    @Override
    public String getKey() {
      return "scout.mom.marshaller";
    }

    @Override
    public String description() {
      return String.format("Specifies the default Marshaller to use if no marshaller is specified for a MOM or a destination. By default the '%s' is used.", JsonDataObjectMarshaller.class.getSimpleName());
    }

    @Override
    public Class<? extends IMarshaller> getDefaultValue() {
      return JsonDataObjectMarshaller.class;
    }
  }

  class RequestReplyEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.requestreply.enabled";
    }

    @Override
    public String description() {
      return "Specifies if 'request-reply' messaging is enabled by default. This value can also be configured individually per MOM. The default value is true.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  class RequestReplyCancellationTopicProperty extends AbstractDestinationConfigProperty<String> {

    @Override
    public String getKey() {
      return "scout.mom.requestreply.cancellationTopic";
    }

    @Override
    public String description() {
      return "Specifies the default topic to receive cancellation request for 'request-reply' communication. By default, a defined topic with the name 'scout.mom.requestreply.cancellation' is used.";
    }

    @Override
    public IDestination<String> getDefaultValue() {
      return MOM.newDestination("scout.mom.requestreply.cancellation", getType(), ResolveMethod.DEFINE, null);
    }

    @Override
    protected IDestinationType getType() {
      return DestinationType.TOPIC;
    }
  }

  /**
   * Property to enable or disable connection failover in the scout mom wrapper. The default retry count is
   * <code>15</code> times. A value of 0 disables scout level connection failover.
   * <p>
   * If the property is not set, the value of {@link ConnectionRetryCountProperty} is used. <b>Value type:</b>
   * {@link Integer} or {@link String}
   * <p>
   * Assume the connection dropped. A call to a method of the scout mom wrapper is then blocked until a reconnect
   * succeeded or all retry attempts failed. The maximum blocking time is
   * <code>connectionRetryCount*connectionRetryInterval + sessionRetryInterval + connectionRetryCount*connectionRetryInterval</code>.
   * After connectionRetryCount unsuccessful connection retries the caller code in the session wrapper gets an error
   * back and does one retry after {@link SessionRetryIntervalMillisProperty}. This results in a second set of
   * connectionRetryCount attempts to restore the connection. If also this second phase of retries fails, the call
   * results in an exception thrown.
   * <p>
   * Scout mom subscriber listeners however support failover even when such retry phases fail. The consumer loop is not
   * terminated when an exception occurs. It continues looping and tries again and again to get the next message until
   * the connection is restored or the subscription is closed.
   * <p>
   * Setting this property to a large value increases the time a caller of jms code is potentially blocked until the jms
   * connection is restored.
   * <p>
   *
   * @since 6.1
   */
  class ConnectionRetryCountProperty extends AbstractIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.failover.connectionRetryCount";
    }

    @Override
    public String description() {
      return "Specifies the connection retry count for connection failover. Default is 15. The value 0 disables connection failover.";
    }

    @Override
    public Integer getDefaultValue() {
      return 15;
    }
  }

  /**
   * If {@link IMom.ConnectionRetryCountProperty} is set then this property sets the interval in milliseconds between
   * connection attempts. The default retry interval is <code>2</code> seconds.
   * <p>
   * If the property is not set, the value of {@link ConnectionRetryIntervalMillisProperty} is used. <b>Value type:</b>
   * {@link Integer} or {@link String}
   *
   * @since 6.1
   */
  class ConnectionRetryIntervalMillisProperty extends AbstractIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.failover.connectionRetryIntervalMillis";
    }

    @Override
    public String description() {
      return "Specifies the connection retry interval in milliseconds. Default is 2000ms.";
    }

    @Override
    public Integer getDefaultValue() {
      return 2000;
    }
  }

  /**
   * If {@link IMom.ConnectionRetryCountProperty} is set then every call to a method in the scout mom wrapper is tried a
   * second time after an Exception. This is the interval to wait in-between these two attempts.
   * <p>
   * The default interval is <code>5</code> seconds.
   * <p>
   * If the property is not set, the value of {@link SessionRetryIntervalMillisProperty} is used. <b>Value type:</b>
   * {@link Integer} or {@link String}
   * <p>
   * Choosing the right value for this property depends on the jms driver. When the jms driver detects a connection
   * loss, it calls the handler set by Connection.setExceptionListener(). The latency between the detection and that
   * call to the listener is relevant in defining the value for this property. A good value is double the expected
   * latency time.
   * <p>
   * Example: A subscriber calls Consumer.receive. The connection has just dropped. This leads to an exception in the
   * driver code. The driver code throws an exception to the scout jms wrapper code and <em>after some latency time</em>
   * informs the listener set by Connection.setExceptionListener(). Therefore the scout jms wrapper code should wait
   * before its second attempt to get a new connection until the old connection in the connection wrapper was marked
   * invalid. This is the wait time set in {@link SessionRetryIntervalMillisProperty}.
   *
   * @since 6.1
   */
  class SessionRetryIntervalMillisProperty extends AbstractIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.failover.sessionRetryIntervalMillis";
    }

    @Override
    public String description() {
      return "Specifies the session retry interval in milliseconds. Default is 5000ms.";
    }

    @Override
    public Integer getDefaultValue() {
      return 5000;
    }
  }
}
