package org.eclipse.scout.rt.mom.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.IDestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.IResolveMethod;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * Message oriented middleware (MOM) for sending and receiving messages between distributed systems. This class is for
 * convenience purpose, and consists of static methods with most of them delegate to {@link IMom}.
 * <p>
 * A MOM provides publish/subscribe (topic-based) or point-to-point (queue-based) messaging. A topic allows to publish a
 * message to multiple subscribers, meaning that the message is transported to all consumers which currently are
 * subscribed. A queue differs from the topic distribution mechanism that the message is transported to exactly one
 * consumer. If there is no subscription the message will be kept until a consumer subscribes for the queue. However, if
 * there are multiple subscriptions for the queue, the message is load balanced to a single consumer.
 * <p>
 * Besides, a MOM provides 'request-reply' messaging, which is kind of synchronous communication between two parties.
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
 * @see IMom
 * @since 6.1
 */
public final class MOM {

  private MOM() {
  }

  /**
   * Creates a destination for asynchronous <i>publish/subscribe</i> messaging.
   * <p>
   * The destination returned is a lightweight object with no physical messaging resources allocated, and which can be
   * constructed even if not connected to the network or broker, i.e. in a static initializer block.
   * <p>
   * Two destinations with the same <i>name</i> are considered 'equals'.
   *
   * @param <DTO>
   *          the type of the transfer object which is sent or received over this destination.
   * @param name
   *          the symbolic name for the destination
   * @param destinationType
   *          the type of the resource that this destination represents, e.g. {@link DestinationType#QUEUE}
   * @param resolveMethod
   *          the method how to resolve the actual destination, e.g. {@link ResolveMethod#JNDI}
   * @param properties
   *          optional map of additional properties used to resolve the destination (may be set to <code>null</code> if
   *          no properties are required)
   * @throws AssertionException
   *           if one of <code>name</code>, <code>type</code> or <code>resolveMethod</code> is <code>null</code> or
   *           empty
   */
  public static <DTO> IDestination<DTO> newDestination(final String name, final IDestinationType destinationType, final IResolveMethod resolveMethod, final Map<String, String> properties) {
    return new Destination<DTO, Void>(name, destinationType, resolveMethod, properties);
  }

  /**
   * Creates a destination for synchronous <i>request-reply</i> messaging, where the requester sends a request for some
   * data and the replier responds to the request.
   * <p>
   * The destination returned is a lightweight object with no physical messaging resources allocated, and which can be
   * constructed even if not connected to the network or broker, i.e. in a static initializer block.
   * <p>
   * Two destinations with the same <i>name</i> are considered 'equals'.
   *
   * @param <REQUEST>
   *          the type of the request object which is sent or received over this destination.
   * @param <REPLY>
   *          the type of the reply object which is sent or received over this destination.
   * @param name
   *          the symbolic name for the destination
   * @param destinationType
   *          the type of the resource that this destination represents, e.g. {@link DestinationType#QUEUE}
   * @param resolveMethod
   *          the method how to resolve the actual destination, e.g. {@link ResolveMethod#JNDI}
   * @param properties
   *          optional map of additional properties used to resolve the destination (may be set to <code>null</code> if
   *          no properties are required)
   * @throws AssertionException
   *           if one of <code>name</code>, <code>type</code> or <code>resolveMethod</code> is <code>null</code> or
   *           empty
   */
  public static <REQUEST, REPLY> IBiDestination<REQUEST, REPLY> newBiDestination(final String name, final IDestinationType destinationType, final IResolveMethod resolveMethod, final Map<String, String> properties) {
    return new Destination<>(name, destinationType, resolveMethod, properties);
  }

  /**
   * Creates an input to control how to publish a message. The input returned specifies normal delivery priority,
   * persistent delivery mode, and without expiration.
   */
  public static PublishInput newPublishInput() {
    return BEANS.get(PublishInput.class);
  }

  /**
   * Creates an input to control how to subscribe for messages. The input returned specifies
   * {@link SubscribeInput#ACKNOWLEDGE_AUTO}.
   */
  public static SubscribeInput newSubscribeInput() {
    return BEANS.get(SubscribeInput.class);
  }

  /**
   * Publishes the given message to the given destination.
   * <p>
   * The message is published with default messaging settings, meaning with normal priority, with persistent delivery
   * mode and without expiration.
   *
   * @param transport
   *          specifies the MOM used as transport to publish the message, e.g. {@link ClusterMom}.
   * @param destination
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @param <DTO>
   *          the type of the transfer object to be published.
   * @see #newQueue(String)
   * @see #newTopic(String)
   * @see #subscribe(IDestination, IMessageListener, RunContext)
   */
  public static <DTO> void publish(final Class<? extends IMomTransport> transport, final IDestination<DTO> destination, final DTO transferObject) {
    publish(transport, destination, transferObject, null);
  }

  /**
   * Publishes the given message to the given destination.
   *
   * @param transport
   *          specifies the MOM used as transport to publish the message, e.g. {@link ClusterMom}.
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
  public static <DTO> void publish(final Class<? extends IMomTransport> transport, final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    BEANS.get(transport).publish(destination, transferObject, input != null ? input : newPublishInput());
  }

  /**
   * Subscribes the given listener to receive messages sent to the given destination.
   * <p>
   * This method complies with {@link SubscribeInput#ACKNOWLEDGE_AUTO}, where message are received concurrently and
   * acknowledged automatically.
   *
   * @param transport
   *          specifies the 'transport or network' to subscribe for messages, e.g. {@link ClusterMom}.
   * @param destination
   *          specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *          {@link IMom} documentation for more information about the difference between topic and queue based
   *          messaging.
   * @param listener
   *          specifies the listener to receive messages.
   * @return subscription handle to unsubscribe from the destination.
   * @param <DTO>
   *          the type of the transfer object a subscription is created for.
   * @see #publish(IDestination, Object)
   */
  public static <DTO> ISubscription subscribe(final Class<? extends IMomTransport> transport, final IDestination<DTO> destination, final IMessageListener<DTO> listener) {
    return subscribe(transport, destination, listener, null);
  }

  /**
   * Subscribes the given listener to receive messages sent to the given destination.
   *
   * @param transport
   *          specifies the 'transport or network' to subscribe for messages, e.g. {@link ClusterMom}.
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
  public static <DTO> ISubscription subscribe(final Class<? extends IMomTransport> transport, final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) {
    return BEANS.get(transport).subscribe(destination, listener, input != null ? input : newSubscribeInput());
  }

  /**
   * Cancels a <i>durable</i> subscription previously created on the specified MOM. Messages published to the
   * subscription's destination while the subscriber is inactive will then no longer be kept by the network.
   * <p>
   * If the durable subscription has already been established for the MOM instance, make sure to call
   * {@link ISubscription#dispose()} first. Canceling the durable subscription while the subscriber is still active will
   * result in an exception. An exception is also thrown if there is no durable subscription for the given name on the
   * MOM instance.
   *
   * @param transport
   *          specifies the 'transport' or 'network' for which the durable subscription has been made, e.g.
   *          {@link ClusterMom}.
   * @param durableSubscriptionName
   *          The same name that was used to create the durable subscription (see
   *          {@link SubscribeInput#withDurableSubscription(String)}).
   */
  public static void cancelDurableSubscription(final Class<? extends IMomTransport> transport, final String durableSubscriptionName) {
    BEANS.get(transport).cancelDurableSubscription(durableSubscriptionName);
  }

  /**
   * Initiates a 'request-reply' communication with a replier, and blocks until the reply is received (synchronous).
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
   * <p>
   * The message is published with default messaging settings, meaning with normal priority, with persistent delivery
   * mode and without expiration.
   *
   * @param transport
   *          specifies the 'transport or network' where to initiate a 'request-reply' communication, e.g.
   *          {@link ClusterMom}.
   * @param destination
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param requestObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @return the reply of the consumer.
   * @throws ThreadInterruptedError
   *           if interrupted while waiting for the reply to receive.
   * @throws RuntimeException
   *           if the request failed because the replier threw an exception. If threw a {@link RuntimeException}, it is
   *           that exception which is thrown.
   * @param <REQUEST>
   *          the type of the request object
   * @param <REPLY>
   *          the type of the reply object
   * @see {@link #reply(IDestination, IRequestListener, RunContext)}
   */
  public static <REQUEST, REPLY> REPLY request(final Class<? extends IMomTransport> transport, final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject) {
    return BEANS.get(transport).request(destination, requestObject, newPublishInput());
  }

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
   * @param transport
   *          specifies the 'transport or network' where to initiate a 'request-reply' communication, e.g.
   *          {@link ClusterMom}.
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
  public static <REQUEST, REPLY> REPLY request(final Class<? extends IMomTransport> transport, final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return BEANS.get(transport).request(destination, requestObject, input);
  }

  /**
   * Subscribes the given listener to receive messages from 'request-reply' communication sent to the given destination.
   * <p>
   * This method complies with {@link SubscribeInput#ACKNOWLEDGE_AUTO}, where message are received concurrently and
   * acknowledged automatically.
   *
   * @param transport
   *          specifies the 'transport or network' where to reply to requests of 'request-reply' communication, e.g.
   *          {@link ClusterMom}.
   * @param destination
   *          specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *          {@link IMom} documentation for more information about the difference between topic and queue based
   *          messaging.
   * @param listener
   *          specifies the listener to receive messages.
   * @return subscription handle to unsubscribe from the destination.
   * @param <REQUEST>
   *          the type of the request object
   * @param <REPLY>
   *          the type of the reply object
   * @see #request(IDestination, Object)
   */
  public static <REQUEST, REPLY> ISubscription reply(final Class<? extends IMomTransport> transport, final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener) {
    return reply(transport, destination, listener, null);
  }

  /**
   * Subscribes the given listener to receive messages from 'request-reply' communication sent to the given destination.
   *
   * @param transport
   *          specifies the 'transport or network' where to reply to requests of 'request-reply' communication, e.g.
   *          {@link ClusterMom}.
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
  public static <REQUEST, REPLY> ISubscription reply(final Class<? extends IMomTransport> transport, final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener,
      final SubscribeInput input) {
    return BEANS.get(transport).reply(destination, listener, input != null ? input : newSubscribeInput());
  }

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
  public static IRegistrationHandle registerMarshaller(final Class<? extends IMomTransport> transport, final IDestination<?> destination, final IMarshaller marshaller) {
    return BEANS.get(transport).registerMarshaller(destination, marshaller);
  }
}
