/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
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
   * {@link #DESTROY_ORDER}.
   */
  long DESTROY_ORDER = 5_700;

  /**
   * Publishes the given message to the given destination.
   *
   * @param destination
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @param input
   *          specifies how to publish the message.
   * @param <DTO>
   *          the type of the transfer object to be published.
   * @see #newQueue(String)
   * @see #newTopic(String)
   * @see #subscribe(IDestination, IMessageListener, RunContext)
   */
  <DTO> void publish(IDestination<DTO> destination, DTO transferObject, PublishInput input);

  /**
   * Subscribes the given listener to receive messages sent to the given destination.
   *
   * @param destination
   *          specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *          {@link IMom} documentation for more information about the difference between topic and queue based
   *          messaging.
   * @param listener
   *          specifies the listener to receive messages.
   * @param input
   *          specifies how to subscribe for messages.
   * @return subscription handle to unsubscribe from the destination.
   * @param <DTO>
   *          the type of the transfer object a subscription is created for.
   * @see #publish(IDestination, Object)
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
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @param input
   *          specifies how to publish the message. Transacted publish of the request is not supported.
   * @return the reply of the consumer.
   * @throws ThreadInterruptedError
   *           if interrupted while waiting for the reply to receive. If interrupted, an interruption request is sent to
   *           the consumer(s).
   * @throws TimedOutError
   *           if the timeout specified via {@link PublishInput#withRequestReplyTimeout(long, TimeUnit)} elapsed. If
   *           elapsed, an interruption request is sent to the consumer(s).
   * @throws RuntimeException
   *           if the request failed because the replier threw an exception. If threw a {@link RuntimeException}, it is
   *           that exception which is thrown.
   * @param <REQUEST>
   *          the type of the request object
   * @param <REPLY>
   *          the type of the reply object
   * @see #reply(IDestination, IRequestListener, RunContext)
   */
  <REQUEST, REPLY> REPLY request(IBiDestination<REQUEST, REPLY> destination, REQUEST requestObject, PublishInput input);

  /**
   * Subscribes the given listener to receive messages from 'request-reply' communication sent to the given destination.
   *
   * @param destination
   *          specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *          {@link IMom} documentation for more information about the difference between topic and queue based
   *          messaging.
   * @param listener
   *          specifies the listener to receive messages.
   * @param input
   *          specifies how to subscribe for messages.
   * @return subscription handle to unsubscribe from the destination.
   * @param <REQUEST>
   *          the type of the request object
   * @param <REPLY>
   *          the type of the reply object
   * @see #request(IDestination, Object)
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
   *          The same name that was used to create the durable subscription (see
   *          {@link SubscribeInput#withDurableSubscription(String)}).
   */
  void cancelDurableSubscription(String durableSubscriptionName);

  /**
   * Registers a marshaller for transfer objects sent to the given destination, or which are received from the given
   * destination.
   * <p>
   * A marshaller transforms a transfer object into its transport representation to be sent across the network.
   * <p>
   * By default, if a destination does not specify a marshaller, {@link JsonMarshaller} is used.
   *
   * @return registration handle to unregister the marshaller from the destination.
   * @see TextMarshaller
   * @see BytesMarshaller
   * @see JsonMarshaller
   * @see ObjectMarshaller
   */
  IRegistrationHandle registerMarshaller(IDestination<?> destination, IMarshaller marshaller);

  /**
   * Destroys this MOM and releases all associated resources.
   */
  void destroy();

  /**
   * Specifies the default {@link IMarshaller} to use if no marshaller is specified for a MOM or a destination.
   * <p>
   * By default, {@link JsonMarshaller} is used.
   */
  class DefaultMarshallerProperty extends AbstractClassConfigProperty<IMarshaller> {

    @Override
    public String getKey() {
      return "scout.mom.marshaller";
    }

    @Override
    protected Class<? extends IMarshaller> getDefaultValue() {
      return JsonMarshaller.class;
    }
  }

  /**
   * Specifies if 'request-reply' messaging is enabled by default. This value can also be configured individually per
   * MOM (see {@link IMomImplementor#REQUEST_REPLY_ENABLED}).
   * <p>
   * The default value is <code>true</code>.
   */
  class RequestReplyEnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.requestreply.enabled";
    }

    @Override
    protected Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }

  /**
   * Specifies the default topic to receive cancellation request for 'request-reply' communication.
   * <p>
   * By default, a defined topic with the name <code>scout.mom.requestreply.cancellation</code> is used.
   */
  class RequestReplyCancellationTopicProperty extends AbstractDestinationConfigProperty<String> {

    @Override
    public String getKey() {
      return "scout.mom.requestreply.cancellation.topic";
    }

    @Override
    protected IDestination<String> getDefaultValue() {
      return MOM.newDestination("scout.mom.requestreply.cancellation", getType(), ResolveMethod.DEFINE, null);
    }

    @Override
    protected IDestinationType getType() {
      return DestinationType.TOPIC;
    }
  }
}
