/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.mom;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.mom.encryptor.DefaultAesEncryptor;
import org.eclipse.scout.rt.mom.encryptor.IEncryptor;
import org.eclipse.scout.rt.mom.marshaller.BytesMarshaller;
import org.eclipse.scout.rt.mom.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.mom.marshaller.ObjectMarshaller;
import org.eclipse.scout.rt.mom.marshaller.StringMarshaller;
import org.eclipse.scout.rt.platform.config.AbstractClassConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
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
 * {@link IMarshaller}, and optionally for an {@link IEncryptor}. A marshaller is used to transform the transfer object
 * into its transport representation, like text in JSON format, or bytes for the object's serialization data. An
 * encryptor allows end-to-end message encryption, which may be required depending on the messaging topology you choose.
 * However, even if working with a secure transport layer, messages may temporarily be stored like when being delivered
 * to queues - end-to-end encryption ensures confidentiality, integrity, and authenticity of those messages.
 *
 * @since 6.1
 */
public interface IMom {

  int AUTO_ACKNOWLEDGE_MODE = 1;
  int TRANSACTED_ACKNOWLEDGE_MODE = 2;

  /**
   * Publishes the given message to the given destination.
   * <p>
   * The message is published with default messaging settings, meaning with normal priority, with persistent delivery
   * mode and without expiration.
   *
   * @param destination
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @see #newQueue(String)
   * @see #newTopic(String)
   * @see #subscribe(IDestination, IMessageListener, RunContext)
   */
  void publish(IDestination destination, Object transferObject);

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
   * @see #newQueue(String)
   * @see #newTopic(String)
   * @see #subscribe(IDestination, IMessageListener, RunContext)
   */
  void publish(IDestination destination, Object transferObject, MessageInput input);

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
   *          specifies the context in which to receive and process the messages.
   * @return subscription handle to unsubscribe from the destination.
   * @see #publish(IDestination, Object)
   */
  <TRANSFER_OBJECT> ISubscription subscribe(IDestination destination, IMessageListener<TRANSFER_OBJECT> listener, RunContext runContext);

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
   *          specifies the context in which to receive and process the messages.
   * @param acknowledgementMode
   *          XXX dwi
   * @return subscription handle to unsubscribe from the destination.
   * @see #publish(IDestination, Object)
   */
  <TRANSFER_OBJECT> ISubscription subscribe(IDestination destination, IMessageListener<TRANSFER_OBJECT> listener, RunContext runContext, int acknowledgementMode);

  /**
   * Initiates a 'request-reply' communication with a consumer, and blocks until the reply is received.
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
   * @param destination
   *          specifies the target of the message, and is either a queue (P2P) or topic (pub/sub). See {@link IMom}
   *          documentation for more information about the difference between topic and queue based messaging.
   * @param transferObject
   *          specifies the transfer object to be sent to the destination.<br>
   *          The object is marshalled into its transport representation using the {@link IMarshaller} registered for
   *          that destination. By default, {@link JsonMarshaller} is used.
   * @return the reply of the consumer.
   * @throws ThreadInterruptedException
   *           if interrupted while waiting for the reply to receive.
   * @see {@link #reply(IDestination, IRequestListener, RunContext)}
   */
  <REPLY_OBJECT, REQUEST_OBJECT> REPLY_OBJECT request(IDestination destination, REQUEST_OBJECT transferObject);

  /**
   * Initiates a 'request-reply' communication with a consumer, and blocks until the reply is received.
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
   *          specifies how to publish the message.
   * @return the reply of the consumer.
   * @throws ThreadInterruptedException
   *           if interrupted while waiting for the reply to receive. If interrupted, an interruption request is sent to
   *           the consumer(s).
   * @throws TimedOutException
   *           if the timeout specified via {@link MessageInput#withRequestReplyTimeout(long, TimeUnit)} elapsed. If
   *           elapsed, an interruption request is sent to the consumer(s).
   * @see #reply(IDestination, IRequestListener, RunContext)
   */

  <REPLY_OBJECT> REPLY_OBJECT request(IDestination destination, Object transferObject, MessageInput input);

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
   *          specifies the context in which to receive and process the messages.
   * @return subscription handle to unsubscribe from the destination.
   * @see #request(IDestination, Object)
   */
  <REQUEST_TRANSFER_OBJECT, REPLY_TRANSFER_OBJECT> ISubscription reply(IDestination destination, IRequestListener<REQUEST_TRANSFER_OBJECT, REPLY_TRANSFER_OBJECT> listener, RunContext runContext);

  /**
   * Creates a new topic for publish/subscribe messaging.
   */
  IDestination newTopic(String topic);

  /**
   * Creates a new queue for point-to-point messaging.
   */
  IDestination newQueue(String queue);

  /**
   * Looks up the given destination using JNDI.
   */
  IDestination lookupDestination(String destination);

  /**
   * Creates a message input to control how to publish a message. The {@link MessageInput} returned specifies normal
   * delivery priority, persistent delivery mode and without expiration.
   */
  MessageInput newMessageInput();

  /**
   * Registers a marshaller for transfer objects sent to the given destination, or which are received from the given
   * destination.
   * <p>
   * A marshaller transforms a transfer object into its transport representation to be sent across the network.
   * <p>
   * By default, if a destination does not specify a marshaller, {@link JsonMarshaller} is used.
   *
   * @return registration handle to unregister the marshaller from the destination.
   * @see StringMarshaller
   * @see BytesMarshaller
   * @see JsonMarshaller
   * @see ObjectMarshaller
   */
  IRegistrationHandle registerMarshaller(IDestination destination, IMarshaller<?> marshaller);

  /**
   * Allows end-to-end encryption for transfer objects sent to the given destination. By default, no encryption is used.
   *
   * @return registration handle to unregister the encryptor from the destination.
   * @see DefaultAesEncryptor
   */
  IRegistrationHandle registerEncryptor(IDestination destination, IEncryptor encryptor);

  /**
   * Destroys this MOM and releases all associated resources.
   */
  void destroy();

  /**
   * Specifies the default {@link IEncryptor} to use if no encryptor is specified for a destination.
   * <p>
   * By default, no encryptor is used.
   */
  class EncryptorProperty extends AbstractClassConfigProperty<IEncryptor> {

    @Override
    public String getKey() {
      return "scout.mom.encryptor";
    }
  }

  /**
   * Specifies the default {@link IMarshaller} to use if no marshaller is specified for a destination.
   * <p>
   * By default, {@link JsonMarshaller} is used.
   */
  class MarshallerProperty extends AbstractClassConfigProperty<IMarshaller<?>> {

    @Override
    public String getKey() {
      return "scout.mom.marshaller";
    }

    @Override
    protected Class<? extends IMarshaller<?>> getDefaultValue() {
      return JsonMarshaller.class;
    }
  }
}
