/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom.api;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractStringConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;

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
 * {@link IMarshaller}, and optionally for an {@link IEncrypter}. A marshaller is used to transform the transfer object
 * into its transport representation, like text in JSON format, or bytes for the object's serialization data. An
 * encrypter allows end-to-end message encryption, which may be required depending on the messaging topology you choose.
 * However, even if working with a secure transport layer, messages may temporarily be stored like when being delivered
 * to queues - end-to-end encryption ensures confidentiality, integrity, and authenticity of those messages.
 *
 * @since 6.1
 */
public interface IMom {

  /**
   * Subscription mode to acknowledge a message automatically upon its receipt. This mode dispatches message processing
   * to a separate thread to allow concurrent message consumption.
   * <p>
   * This is the default acknowledgment mode.
   */
  int ACKNOWLEDGE_AUTO = 1;

  /**
   * Subscription mode to acknowledge a message automatically upon its receipt. This mode uses the <i>message receiving
   * thread</i> to process the message, meaning the subscription does not receive any other messages for the time of
   * processing a message.
   */
  int ACKNOWLEDGE_AUTO_SINGLE_THREADED = 2;

  /**
   * Subscription mode to acknowledge a message upon successful commit of the receiving transaction. This mode uses the
   * <i>message receiving thread</i> to process the message, meaning the subscription does not receive any other
   * messages for the time of processing a message.
   */
  int ACKNOWLEDGE_TRANSACTED = 3;

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
  <DTO> ISubscription subscribe(IDestination<DTO> destination, IMessageListener<DTO> listener, RunContext runContext, int acknowledgementMode);

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
  <REQUEST, REPLY> ISubscription reply(IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, RunContext runContext);

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
   * Allows end-to-end encryption for transfer objects sent to the given destination. By default, no encryption is used.
   *
   * @return registration handle to unregister the encrypter from the destination.
   * @see ClusterEncrypter
   */
  IRegistrationHandle registerEncrypter(IDestination<?> destination, IEncrypter encrypter);

  /**
   * Destroys this MOM and releases all associated resources.
   */
  void destroy();

  /**
   * Specifies the default {@link IEncrypter} to use if no encrypter is specified for a destination.
   * <p>
   * By default, no encrypter is used.
   */
  class EncrypterProperty extends AbstractClassConfigProperty<IEncrypter> {

    @Override
    public String getKey() {
      return "scout.mom.encrypter";
    }
  }

  /**
   * Specifies the default {@link IMarshaller} to use if no marshaller is specified for a destination.
   * <p>
   * By default, {@link JsonMarshaller} is used.
   */
  class MarshallerProperty extends AbstractClassConfigProperty<IMarshaller> {

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
   * Specifies the topic to receive cancellation request for request-reply communication.
   * <p>
   * By default, the topic 'scout.mom.requestreply.cancellation' is used.
   */
  class RequestReplyCancellationTopicProperty extends AbstractStringConfigProperty {

    @Override
    public String getKey() {
      return "scout.mom.requestreply.cancellation.topic";
    }

    @Override
    protected String getDefaultValue() {
      return "scout.mom.requestreply.cancellation";
    }
  }
}
