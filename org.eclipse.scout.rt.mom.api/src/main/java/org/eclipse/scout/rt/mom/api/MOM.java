package org.eclipse.scout.rt.mom.api;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.encrypter.ClusterEncrypter;
import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;

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
 * {@link IMarshaller}, and optionally for an {@link IEncrypter}. A marshaller is used to transform the transfer object
 * into its transport representation, like text in JSON format, or bytes for the object's serialization data. An
 * encrypter allows end-to-end message encryption, which may be required depending on the messaging topology you choose.
 * However, even if working with a secure transport layer, messages may temporarily be stored like when being delivered
 * to queues - end-to-end encryption ensures confidentiality, integrity, and authenticity of those messages.
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
   * A destination with the same <i>name</i> and <i>type</i> are considered 'equals'.
   *
   * @param <DTO>
   *          the type of the transfer object which is sent or received over this destination.
   * @see IDestination#TOPIC
   * @see IDestination#QUEUE
   * @see IDestination#JNDI_LOOKUP
   */
  public static <DTO> IDestination<DTO> newDestination(final String name, final int destinationType) {
    return new Destination<DTO, Void>(name, destinationType);
  }

  /**
   * Creates a destination for synchronous <i>request-reply</i> messaging, where the requester sends a request for some
   * data and the replier responds to the request.
   * <p>
   * The destination returned is a lightweight object with no physical messaging resources allocated, and which can be
   * constructed even if not connected to the network or broker, i.e. in a static initializer block.
   * <p>
   * A destination with the same <i>name</i> and <i>type</i> are considered 'equals'.
   *
   * @param <REQUEST>
   *          the type of the request object which is sent or received over this destination.
   * @param <REPLY>
   *          the type of the reply object which is sent or received over this destination.
   * @see IDestination#TOPIC
   * @see IDestination#QUEUE
   * @see IDestination#JNDI_LOOKUP
   */
  public static <REQUEST, REPLY> IBiDestination<REQUEST, REPLY> newBiDestination(final String name, final int destinationType) {
    return new Destination<REQUEST, REPLY>(name, destinationType);
  }

  /**
   * Creates an input to control how to publish a message. The input returned specifies normal delivery priority,
   * persistent delivery mode, and without expiration.
   */
  public static PublishInput newPublishInput() {
    return BEANS.get(PublishInput.class);
  }

  /**
   * Publishes the given message to the given destination.
   * <p>
   * The message is published with default messaging settings, meaning with normal priority, with persistent delivery
   * mode and without expiration.
   *
   * @param transport
   *          specifies the MOM used as transport to publish the message, i.e. {@link ClusterMom}.
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
  public static <DTO, TRANSPORT extends IMom & IMomTransport> void publish(final Class<TRANSPORT> transport, final IDestination<DTO> destination, final DTO transferObject) {
    publish(transport, destination, transferObject, newPublishInput());
  }

  /**
   * Publishes the given message to the given destination.
   *
   * @param transport
   *          specifies the MOM used as transport to publish the message, i.e. {@link ClusterMom}.
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
  public static <DTO, TRANSPORT extends IMom & IMomTransport> void publish(final Class<? extends TRANSPORT> transport, final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    BEANS.get(transport).publish(destination, transferObject, input);
  }

  /**
   * Subscribes the given listener to receive messages sent to the given destination.
   * <p>
   * This method complies with {@link IMom#ACKNOWLEDGE_AUTO}, where message are received concurrently and acknowledged
   * automatically.
   *
   * @param transport
   *          specifies the 'transport or network' to subscribe for messages, e.g. {@link ClusterMom}.
   * @param destination
   *          specifies the target to consume messages from, and is either a topic (pub/sub) or queue (P2P). See
   *          {@link IMom} documentation for more information about the difference between topic and queue based
   *          messaging.
   * @param listener
   *          specifies the listener to receive messages.
   * @param runContext
   *          specifies the optional context in which to receive messages. If not specified, an empty context is
   *          created. In either case, the transaction scope is set to {@link TransactionScope#REQUIRES_NEW}.
   * @return subscription handle to unsubscribe from the destination.
   * @param <DTO>
   *          the type of the transfer object a subscription is created for.
   * @see #publish(IDestination, Object)
   */
  public static <DTO, TRANSPORT extends IMom & IMomTransport> ISubscription subscribe(final Class<? extends TRANSPORT> transport, final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext) {
    return subscribe(transport, destination, listener, runContext, IMom.ACKNOWLEDGE_AUTO);
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
   * @param runContext
   *          specifies the optional context in which to receive messages. If not specified, an empty context is
   *          created. In either case, the transaction scope is set to {@link TransactionScope#REQUIRES_NEW}.
   * @param acknowledgementMode
   *          specifies the mode how to acknowledge messages. Supported modes are {@link IMom#ACKNOWLEDGE_AUTO},
   *          {@link IMom#ACKNOWLEDGE_AUTO_SINGLE_THREADED} and {@link IMom#ACKNOWLEDGE_TRANSACTED}.
   * @return subscription handle to unsubscribe from the destination.
   * @param <DTO>
   *          the type of the transfer object a subscription is created for.
   * @see #publish(IDestination, Object)
   */
  public static <DTO, TRANSPORT extends IMom & IMomTransport> ISubscription subscribe(final Class<? extends TRANSPORT> transport, final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext,
      final int acknowledgementMode) {
    return BEANS.get(transport).subscribe(destination, listener, runContext, acknowledgementMode);
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
   * {@link ThreadInterruptedException} and the interruption is propagated to the consumer(s) as well.
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
   * @throws ThreadInterruptedException
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
  public static <REQUEST, REPLY, TRANSPORT extends IMom & IMomTransport> REPLY request(final Class<? extends TRANSPORT> transport, final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject) {
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
   * {@link ThreadInterruptedException} and the interruption is propagated to the consumer(s) as well.
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
   * @throws ThreadInterruptedException
   *           if interrupted while waiting for the reply to receive. If interrupted, an interruption request is sent to
   *           the consumer(s).
   * @throws TimedOutException
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
  public static <REQUEST, REPLY, TRANSPORT extends IMom & IMomTransport> REPLY request(final Class<? extends TRANSPORT> transport, final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    return BEANS.get(transport).request(destination, requestObject, input);
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
   * @param runContext
   *          specifies the optional context in which to receive messages. If not specified, an empty context is
   *          created. In either case, the transaction scope is set to {@link TransactionScope#REQUIRES_NEW}.
   * @return subscription handle to unsubscribe from the destination.
   * @param <REQUEST>
   *          the type of the request object
   * @param <REPLY>
   *          the type of the reply object
   * @see #request(IDestination, Object)
   */
  public static <REQUEST, REPLY, TRANSPORT extends IMom & IMomTransport> ISubscription reply(final Class<? extends TRANSPORT> transport, final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener,
      final RunContext runContext) {
    return BEANS.get(transport).reply(destination, listener, runContext);
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
  public static <TRANSPORT extends IMom & IMomTransport> IRegistrationHandle registerMarshaller(final Class<? extends TRANSPORT> transport, final IDestination<?> destination, final IMarshaller marshaller) {
    return BEANS.get(transport).registerMarshaller(destination, marshaller);
  }

  /**
   * Allows end-to-end encryption for transfer objects sent to the given destination. By default, no encryption is used.
   *
   * @return registration handle to unregister the encrypter from the destination.
   * @see ClusterEncrypter
   */
  public static <TRANSPORT extends IMom & IMomTransport> IRegistrationHandle registerEncrypter(final Class<? extends TRANSPORT> transport, final IDestination<?> destination, final IEncrypter encrypter) {
    return BEANS.get(transport).registerEncrypter(destination, encrypter);
  }
}
