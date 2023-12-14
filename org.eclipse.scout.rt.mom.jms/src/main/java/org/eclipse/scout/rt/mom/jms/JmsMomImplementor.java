/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_REPLY_ID;
import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.mom.api.DestinationConfigPropertyParser;
import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IDestination.DestinationType;
import org.eclipse.scout.rt.mom.api.IDestination.ResolveMethod;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.mom.api.PublishInput;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.TextMarshaller;
import org.eclipse.scout.rt.mom.jms.internal.JmsConnectionWrapper;
import org.eclipse.scout.rt.mom.jms.internal.JmsSessionProviderWrapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of 'instance-scoped' {@link IMom} based on JMS (Java Messaging Standard).
 * <p>
 * This class expects a JMS implementor to be available at runtime.
 *
 * @since 6.1
 */
public class JmsMomImplementor implements IMomImplementor {

  private static final Logger LOG = LoggerFactory.getLogger(JmsMomImplementor.class);

  /**
   * Key to explicitly set the JMS client ID. If omitted, the client ID is computed automatically.
   */
  public static final String JMS_CLIENT_ID = "scout.mom.jms.clientId";

  /**
   * Property to specify the JMS message handler of a JMS MOM. If the property is not set, the
   * {@link LogJmsMessageHandler} is used.
   * <p>
   * <b>Value type:</b> {@link IJmsMessageHandler} or {@link String} (interpreted as the class name of a {@link Bean} of
   * type {@link IJmsMessageHandler})
   */
  public static final String JMS_MESSAGE_HANDLER = "scout.mom.jms.messageHandler";

  /**
   * Key to set {@link #m_messageConsumerJobReceiveTimeout}
   */
  public static final String JMS_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT = "scout.mom.jms.messageConsumerJobReceiveTimeout";

  /**
   * Key to set {@link #m_replyMessageConsumerJobReceiveTimeout}
   */
  public static final String JMS_REPLY_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT = "scout.mom.jms.replyMessageConsumerJobReceiveTimeout";

  /**
   * Key to set {@link #m_requestCancellationMessageConsumerJobReceiveTimeout}
   */
  public static final String JMS_REQUEST_CANCELLATION_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT = "scout.mom.jms.requestCancellationMessageConsumerJobReceiveTimeout";

  /**
   * Key to set {@link #m_subscriptionAwaitStartedSeconds}, if value is not set {@link #WAIT_TIME_INFINITE} is used as a
   * default to wait infinitely (the {@link #WAIT_TIME_INFINITE} value may also be used to configure an infinite wait
   * time)
   */
  public static final String JMS_SUBSCRIPTION_AWAIT_STARTED_TIMEOUT = "scout.mom.jms.subscriptionAwaitStartedTimeout";

  /**
   * Constant for {@link #JMS_SUBSCRIPTION_AWAIT_STARTED_TIMEOUT} to indicate an infinite wait time.
   */
  public static final int WAIT_TIME_INFINITE = -1;

  protected final String m_momUid = UUID.randomUUID().toString();

  // init -> thread-safety: only set in init method
  protected String m_symbolicName;
  @SuppressWarnings("squid:S1149")
  protected Hashtable<Object, Object> m_contextEnvironment;
  protected ConnectionFactory m_connectionFactory;
  protected String m_clientId;

  protected JmsConnectionWrapper m_connectionWrapper;
  protected boolean m_requestReplyEnabled;
  protected IDestination<?> m_requestReplyCancellationTopic;
  protected IMarshaller m_defaultMarshaller;
  protected IJmsMessageHandler m_messageHandler;
  // end init

  protected ISubscription m_requestCancellationSubscription;
  protected int m_subscriptionAwaitStartedSeconds = WAIT_TIME_INFINITE;
  protected final List<ISubscription> m_subscriptions = Collections.synchronizedList(new ArrayList<>());

  protected final Map<IDestination, Destination> m_jmsDestinations = new ConcurrentHashMap<>();
  protected final Map<IDestination, IMarshaller> m_marshallers = new ConcurrentHashMap<>();

  protected long m_messageConsumerJobReceiveTimeout = 0L;
  protected long m_replyMessageConsumerJobReceiveTimeout = 0L;
  protected long m_requestCancellationMessageConsumerJobReceiveTimeout = 0L;

  @Override
  public void init(final Map<Object, Object> properties) throws Exception {
    m_symbolicName = Objects.toString(properties.get(SYMBOLIC_NAME), StringUtility.join(" ", CONFIG.getPropertyValue(ApplicationNameProperty.class), "MOM"));
    if (Platform.get().inDevelopmentMode()) {
      LOG.info("{} configuration: {}", m_symbolicName, properties);
    }
    try {
      m_messageConsumerJobReceiveTimeout = NumberUtility.nvl(TypeCastUtility.castValue(properties.get(JMS_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT), Long.class), 0L);
      m_replyMessageConsumerJobReceiveTimeout = NumberUtility.nvl(TypeCastUtility.castValue(properties.get(JMS_REPLY_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT), Long.class), 0L);
      m_requestCancellationMessageConsumerJobReceiveTimeout = NumberUtility.nvl(TypeCastUtility.castValue(properties.get(JMS_REQUEST_CANCELLATION_MESSAGE_CONSUMER_JOB_RECEIVE_TIMEOUT), Long.class), 0L);
      m_subscriptionAwaitStartedSeconds = NumberUtility.nvl(TypeCastUtility.castValue(properties.get(JMS_SUBSCRIPTION_AWAIT_STARTED_TIMEOUT), Integer.class), WAIT_TIME_INFINITE);
      m_contextEnvironment = createContextEnvironment(properties);
      m_connectionFactory = createConnectionFactory(properties);
      m_clientId = computeClientId(properties);
      m_connectionWrapper = createConnectionWrapper(properties);

      m_defaultMarshaller = createDefaultMarshaller(properties);
      m_messageHandler = createMessageHandler(properties);
      Assertions.assertNotNull(m_messageHandler);

      initRequestReply(properties);

      LOG.info("{} initialized: {}", m_symbolicName, m_connectionWrapper);
    }
    catch (Exception e) {
      try {
        destroy();
      }
      catch (RuntimeException re) {
        e.addSuppressed(re);
      }
      throw e;
    }
  }

  protected JmsConnectionWrapper createConnectionWrapper(final Map<Object, Object> properties) {
    return new JmsConnectionWrapper(properties)
        .withConnectionFunction(this::createConnection);
  }

  @SuppressWarnings("RedundantThrows")
  protected void initRequestReply(final Map<Object, Object> properties) throws JMSException {//NOSONAR
    m_requestReplyEnabled = BooleanUtility.nvl(
        TypeCastUtility.castValue(properties.get(REQUEST_REPLY_ENABLED), Boolean.class),
        CONFIG.getPropertyValue(RequestReplyEnabledProperty.class));
    if (!m_requestReplyEnabled) {
      LOG.info("{}: 'request-reply' messaging is disabled", m_symbolicName);
      return;
    }

    // Register consumer to handle cancellation requests for 'request-reply' communication.
    Object prop = properties.get(IMomImplementor.REQUEST_REPLY_CANCELLATION_TOPIC);
    if (prop instanceof IDestination) {
      m_requestReplyCancellationTopic = (IDestination<?>) prop;
    }
    else {
      final String cancellationTopicName = ObjectUtility.toString(prop);
      if (cancellationTopicName != null) {
        final DestinationConfigPropertyParser p = BEANS.get(DestinationConfigPropertyParser.class).parse(cancellationTopicName);
        m_requestReplyCancellationTopic = MOM.newDestination(p.getDestinationName(), DestinationType.TOPIC, p.getResolveMethod(), p.getParameters());
      }
      else {
        m_requestReplyCancellationTopic = CONFIG.getPropertyValue(RequestReplyCancellationTopicProperty.class);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected IMarshaller createDefaultMarshaller(final Map<Object, Object> properties) {
    Object prop = properties.get(MARSHALLER);
    if (prop instanceof IMarshaller) {
      return (IMarshaller) prop;
    }
    else {
      Class<? extends IMarshaller> marshallerClass;
      String marshallerClassName = ObjectUtility.toString(prop);
      if (marshallerClassName != null) {
        try {
          marshallerClass = (Class<? extends IMarshaller>) Class.forName(marshallerClassName);
        }
        catch (final ClassNotFoundException | ClassCastException e) {
          throw new PlatformException("Failed to load class specified by environment property '{}' [value={}]", MARSHALLER, marshallerClassName, e);
        }
      }
      else {
        marshallerClass = CONFIG.getPropertyValue(DefaultMarshallerProperty.class);
      }
      return BEANS.get(marshallerClass);
    }
  }

  @SuppressWarnings("unchecked")
  protected IJmsMessageHandler createMessageHandler(final Map<Object, Object> properties) {
    Object prop = properties.get(JMS_MESSAGE_HANDLER);
    if (prop instanceof IJmsMessageHandler) {
      return (IJmsMessageHandler) prop;
    }
    else {
      Class<? extends IJmsMessageHandler> messageHandlerClass;
      String messageHandlerClassName = ObjectUtility.toString(prop);
      if (messageHandlerClassName != null) {
        try {
          messageHandlerClass = (Class<? extends IJmsMessageHandler>) Class.forName(messageHandlerClassName);
        }
        catch (final ClassNotFoundException | ClassCastException e) {
          throw new PlatformException("Failed to load class specified by environment property '{}' [value={}]", JMS_MESSAGE_HANDLER, messageHandlerClassName, e);
        }
      }
      else {
        messageHandlerClass = LogJmsMessageHandler.class;
      }
      IJmsMessageHandler handler = BEANS.get(messageHandlerClass);
      handler.init(properties);
      return handler;
    }
  }

  public JobInput newJobInput() {
    return Jobs.newInput()
        .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true)
        .withExecutionHint(m_momUid);
  }

  public IJmsSessionProvider createSessionProvider() throws JMSException {
    return createSessionProvider(null, false);
  }

  public IJmsSessionProvider createSessionProvider(final IDestination<?> destination, boolean transacted) throws JMSException {//NOSONAR
    ICreateJmsSessionProvider providerFunction = session -> new JmsSessionProvider(session, resolveJmsDestination(destination, session));
    return createSessionProviderWrapper(transacted, providerFunction);
  }

  protected JmsSessionProviderWrapper createSessionProviderWrapper(boolean transacted, ICreateJmsSessionProvider providerFunction) {
    return new JmsSessionProviderWrapper(m_connectionWrapper, transacted, providerFunction);
  }

  protected JmsSubscription createJmsSubscription(IDestination<?> destination, IMessageListener<?> listener, IRequestListener<?, ?> requestListener, SubscribeInput input, IJmsSessionProvider sessionProvider, IFuture<?> worker) {
    JmsSubscription subscription = new JmsSubscription(destination, listener, requestListener, input, sessionProvider, worker);
    subscription.awaitStarted(m_subscriptionAwaitStartedSeconds, TimeUnit.SECONDS);
    return subscription;
  }

  @Override
  public String getId() {
    return m_momUid;
  }

  @Override
  public String getName() {
    return m_symbolicName;
  }

  @Override
  public List<ISubscription> getSubscriptions() {
    synchronized (m_subscriptions) {
      m_subscriptions.removeIf(ISubscription::isDisposed);
      return new ArrayList<>(m_subscriptions);
    }
  }

  @Override
  public <DTO> void publish(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) {
    assertNotNull(destination, "destination not specified");
    assertNotNull(input, "publishInput not specified");

    try {
      if (input.isTransactional()) {
        publishTransactional(destination, transferObject, input);
      }
      else {
        publishNonTransactional(destination, transferObject, input);
      }
    }
    catch (JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  protected <DTO> void publishNonTransactional(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) throws JMSException {
    IJmsSessionProvider sessionProvider = createSessionProvider(destination, false);
    try {
      send(sessionProvider, destination, transferObject, input);
    }
    finally {
      sessionProvider.close();
    }
  }

  protected <DTO> void publishTransactional(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) throws JMSException {
    final ITransaction currentTransaction = assertNotNull(ITransaction.CURRENT.get(), "Transaction required for transactional messaging");

    // Register transaction member for transacted publishing.
    final JmsTransactionMember txMember = currentTransaction.registerMemberIfAbsent(m_momUid, memberId -> {
      try {
        return BEANS.get(JmsTransactionMember.class)
            .withMemberId(memberId)
            .withSessionProvider(createSessionProvider(destination, true))
            .withAutoClose(true); // close upon transaction end
      }
      catch (final JMSException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    });

    // Publish the message
    send(txMember.getSessionProvider(), destination, transferObject, input);
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) {
    assertNotNull(destination, "destination not specified");
    assertNotNull(listener, "messageListener not specified");
    assertNotNull(input, "input not specified");
    try {
      return subscribeImpl(destination, listener, input);
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  protected <DTO> ISubscription subscribeImpl(IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) throws JMSException {
    IJmsSessionProvider sessionProvider = createSessionProvider(destination, SubscribeInput.ACKNOWLEDGE_TRANSACTED == input.getAcknowledgementMode());
    IFuture<?> worker = Jobs.schedule(createMessageConsumerJob(sessionProvider, destination, listener, input), newJobInput().withName("JMS subscriber"));
    JmsSubscription subscription = createJmsSubscription(destination, listener, null, input, sessionProvider, worker);
    m_subscriptions.add(subscription);
    return subscription;
  }

  protected <DTO> IRunnable createMessageConsumerJob(IJmsSessionProvider sessionProvider, IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) {
    return new MessageConsumerJob<>(this, sessionProvider, destination, listener, input, m_messageConsumerJobReceiveTimeout);
  }

  @Override
  public <REQUEST, REPLY> REPLY request(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input) {
    assertTrue(m_requestReplyEnabled, "'request-reply' messaging is not enabled for this MOM");
    assertNotNull(destination, "destination not specified");
    assertNotNull(input, "publishInput not specified");
    assertFalse(input.isTransactional(), "transactional mode not supported for 'request-reply' communication");

    final String replyId = String.format("scout.mom.requestreply.uid-%s", UUID.randomUUID()); // JMS message ID not applicable because unknown until sent
    final IBlockingCondition condition = Jobs.newBlockingCondition(true);

    IFuture<Message> requestFuture = Jobs.schedule(() -> {
      try {
        return requestImpl(destination, requestObject, input, replyId);
      }
      finally {
        condition.setBlocking(false);
      }
    }, newJobInput()
        .withName("request on {}", destination.getName())
        .withExceptionHandling(BEANS.get(MomExceptionHandler.class), false)
        .withRunContext(RunContexts.copyCurrent(true)
            .withDiagnostics(BEANS.all(IJmsRunContextDiagnostics.class))));

    try {
      long timeout = input.getRequestReplyTimeout();
      if (timeout == PublishInput.INFINITELY) {
        condition.waitFor();
      }
      else {
        condition.waitFor(timeout, TimeUnit.MILLISECONDS);
      }

      Message responseMessage = requestFuture.awaitDoneAndGet();
      return transform(responseMessage, replyId, resolveMarshaller(destination));
    }
    catch (JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    catch (ThreadInterruptedError | TimedOutError e) {

      // send cancel to replier
      cancelRequest(replyId);

      // cancel request job
      if (requestFuture.cancel(true)) {
        requestFuture.awaitDone();
      }

      throw e;
    }
  }

  protected <REQUEST, REPLY> Message requestImpl(final IBiDestination<REQUEST, REPLY> destination, final REQUEST requestObject, final PublishInput input, String replyId) throws JMSException {
    Message responseMessage;
    IJmsSessionProvider sessionProvider = createSessionProvider(destination, false);
    try {
      TemporaryQueue temporaryQueue = sessionProvider.getTemporaryQueue();
      @SuppressWarnings("resource")
      MessageConsumer responseQueueConsumer = sessionProvider.getSession().createConsumer(temporaryQueue);

      // send request message
      JmsMessageWriter messageWriter = JmsMessageWriter.newInstance(sessionProvider.getSession(), resolveMarshaller(destination))
          .writeReplyTo(temporaryQueue)
          .writeReplyId(replyId)
          .writeProperties(input.getProperties())
          .writeTransferObject(requestObject);
      send(sessionProvider, destination, messageWriter, input);

      // receive response message
      responseMessage = responseQueueConsumer.receive();

      getMessageHandler().handleIncoming(destination, responseMessage, resolveMarshaller(destination));
    }
    catch (JMSException e) {
      if (IFuture.CURRENT.get().isCancelled()) {
        // if job was canceled, we ignore JMSException as these are exceptions because of interruption
        LOG.info("Request job canceled; {}", e.getMessage());
        return null;
      }
      else {
        throw e;
      }
    }
    finally {
      sessionProvider.close();
    }

    // After closing session, delete not required temporary queue. The queue is associated with the connection and exists as long as the connection is alive.
    sessionProvider.deleteTemporaryQueue();

    return responseMessage;
  }

  @SuppressWarnings("unchecked")
  protected <REPLY> REPLY transform(Message message, String replyId, IMarshaller marshaller) throws JMSException {
    assertEqual(replyId, message.getStringProperty(JMS_PROP_REPLY_ID), "expected reply message with id {} but got {}", replyId, message);

    JmsMessageReader reader = JmsMessageReader.newInstance(message, marshaller);
    Object reply = reader.readMessage().getTransferObject();
    if (reader.readRequestReplySuccess()) {
      return (REPLY) reply;
    }
    Throwable cause = reply instanceof Throwable ? (Throwable) reply : new ProcessingException("Request-Reply failed");
    throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(cause);
  }

  protected void cancelRequest(final String replyId) {
    Jobs.schedule(() -> {
      assertNotNull(m_requestReplyCancellationTopic);

      IJmsSessionProvider sessionProvider = createSessionProvider();
      try {
        JmsMessageWriter writer = JmsMessageWriter.newInstance(sessionProvider.getSession(), BEANS.get(TextMarshaller.class))
            .writeReplyId(replyId);

        send(sessionProvider.getProducer(), resolveJmsDestination(m_requestReplyCancellationTopic, sessionProvider.getSession()), writer, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
      }
      finally {
        sessionProvider.close();
      }
    }, newJobInput().withName("JMS publish cancel request for {}", replyId));
  }

  @Override
  public <REQUEST, REPLY> ISubscription reply(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final SubscribeInput input) {
    assertTrue(m_requestReplyEnabled, "'request-reply' messaging is not enabled for this MOM");
    assertNotNull(destination, "destination not specified");
    assertNotNull(listener, "messageListener not specified");
    assertNotNull(input, "input not specified");
    try {
      ensureRequestCancellationSubscription();
      return replyImpl(destination, listener, input);
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  protected <REQUEST, REPLY> ISubscription replyImpl(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final SubscribeInput input) throws JMSException {
    IJmsSessionProvider sessionProvider = createSessionProvider(destination, false);
    IFuture<?> worker = Jobs.schedule(createReplyMessageConsumerJob(sessionProvider, destination, listener, input), newJobInput().withName("JMS subscriber"));
    JmsSubscription subscription = createJmsSubscription(destination, null, listener, input, sessionProvider, worker);
    m_subscriptions.add(subscription);
    return subscription;
  }

  protected <REQUEST, REPLY> IRunnable createReplyMessageConsumerJob(IJmsSessionProvider sessionProvider, IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input) {
    return new ReplyMessageConsumerJob<>(this, sessionProvider, destination, listener, input, m_replyMessageConsumerJobReceiveTimeout);
  }

  protected synchronized void ensureRequestCancellationSubscription() throws JMSException {
    if (m_requestCancellationSubscription == null) {
      m_requestCancellationSubscription = subscribeRequestCancellation(m_requestReplyCancellationTopic);
    }
  }

  protected <DTO> ISubscription subscribeRequestCancellation(IDestination<DTO> cancellationTopic) throws JMSException {
    SubscribeInput input = MOM.newSubscribeInput();
    IJmsSessionProvider sessionProvider = createSessionProvider(cancellationTopic, false);
    final IFuture<?> worker = Jobs.schedule(createRequestCancellationMessageConsumerJob(sessionProvider, cancellationTopic, input), newJobInput().withName("JMS reply cancel message listener"));
    return createJmsSubscription(cancellationTopic, null, null, input, sessionProvider, worker);
  }

  protected <DTO> IRunnable createRequestCancellationMessageConsumerJob(IJmsSessionProvider sessionProvider, final IDestination<DTO> cancellationTopic, final SubscribeInput input) {
    return new RequestCancellationMessageConsumerJob<>(this, sessionProvider, cancellationTopic, input, m_requestCancellationMessageConsumerJobReceiveTimeout);
  }

  @Override
  public void cancelDurableSubscription(final String durableSubscriptionName) {
    try {
      IJmsSessionProvider sessionProvider = createSessionProvider();
      try {
        sessionProvider.getSession().unsubscribe(durableSubscriptionName);
      }
      finally {
        sessionProvider.close();
      }
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public synchronized void destroy() {
    try {
      synchronized (m_subscriptions) {
        m_subscriptions.forEach(ISubscription::dispose);
        m_subscriptions.clear();
      }
      if (m_requestCancellationSubscription != null) {
        m_requestCancellationSubscription.dispose();
      }

      // cancel any still running mom jobs
      Predicate<IFuture<?>> momJobsFilter = Jobs.newFutureFilterBuilder().andMatchExecutionHint(m_momUid).toFilter();
      Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(momJobsFilter);
      if (!futures.isEmpty()) {
        Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
            .andMatchFuture(futures)
            .andMatchNotState(JobState.DONE)
            .toFilter(), false);
      }

      // close connection
      if (m_connectionWrapper != null) {
        try {
          m_connectionWrapper.close();
        }
        finally {
          m_connectionWrapper = null;
        }
      }

      // wait for jobs to finish
      if (!futures.isEmpty()) {
        try {//NOSONAR
          Jobs.getJobManager().awaitDone(momJobsFilter, 10, TimeUnit.SECONDS);
          LOG.debug("All mom jobs have finished.");
        }
        catch (ThreadInterruptedError | TimedOutError e) {
          LOG.warn("Unable to cancel all mom jobs: {}", futures, e);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to destroy MOM", e);
    }
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination<?> destination, final IMarshaller marshaller) {
    m_marshallers.put(destination, marshaller);
    return () -> m_marshallers.remove(destination);
  }

  /**
   * Returns the {@link IMarshaller} registered for the given destination, and is never <code>null</code>.
   */
  public IMarshaller resolveMarshaller(final IDestination<?> destination) {
    IMarshaller marshaller = m_marshallers.get(destination);
    return marshaller != null ? marshaller : m_defaultMarshaller;
  }

  /**
   * @return true if jms is currently connected. Does not block.
   */
  public boolean isConnected() {
    return m_connectionWrapper.isConnected();
  }

  /**
   * @return a shared {@link Connection} to JMS broker. This method may block until a connection is available.
   *         <p>
   *         Do not keep references to this value, it may change after reconnect attempts.
   *         <p>
   *         see {@link #isConnected()} which is not blocking
   */
  public Connection getConnection() {
    try {
      return m_connectionWrapper.getConnection();
    }
    catch (JMSException e) {
      throw new ProcessingException("Cannot open connection", e);
    }
  }

  public <DTO> void send(IJmsSessionProvider sessionProvider, IDestination<DTO> destination, DTO transferObject, PublishInput input) throws JMSException {
    Session session = sessionProvider.getSession();
    JmsMessageWriter messageWriter = JmsMessageWriter.newInstance(session, resolveMarshaller(destination))
        .writeTransferObject(transferObject)
        .writeReplyTo(resolveJmsDestination(input.getReplyTo(), session))
        .writeProperties(input.getProperties());
    send(sessionProvider, destination, messageWriter, input);
  }

  public <DTO> void send(IJmsSessionProvider sessionProvider, IDestination<DTO> destination, JmsMessageWriter messageWriter, PublishInput input) throws JMSException {
    assertNotNull(destination);
    Session session = sessionProvider.getSession();
    send(sessionProvider.getProducer(), resolveJmsDestination(destination, session), messageWriter, input);
  }

  public <DTO> void send(MessageProducer producer, Destination destination, JmsMessageWriter messageWriter, PublishInput input) throws JMSException {
    send(producer, destination, messageWriter, toJmsDeliveryMode(input), toJmsPriority(input), toJmsTimeToLive(input));
  }

  public void send(MessageProducer producer, Destination destination, JmsMessageWriter messageWriter, int deliveryMode, int priority, long timeToLive) throws JMSException {
    Message message = messageWriter
        .writeCorrelationId(CorrelationId.CURRENT.get())
        .build();
    IDestination<?> momDestination = resolveMomDestination(destination);
    getMessageHandler().handleOutgoing(momDestination, message, messageWriter.getMarshaller());
    producer.send(destination, message, deliveryMode, priority, timeToLive);
  }

  protected Context createContext() throws NamingException {
    // calling this constructor on InitialContext will not modify m_contextEnvironment
    return new InitialContext(m_contextEnvironment);
  }

  @SuppressWarnings({"squid:S1149", "RedundantThrows"})
  protected Hashtable<Object, Object> createContextEnvironment(final Map<Object, Object> properties) throws NamingException {//NOSONAR
    Hashtable<Object, Object> env = new Hashtable<>();
    if (properties != null) {
      for (Entry<Object, Object> entry : properties.entrySet()) {
        if (entry.getKey() == null || entry.getValue() == null) {
          LOG.info("ignoring property having null key or value [key={}, value={}]", entry.getKey(), entry.getValue());
        }
        else if (ObjectUtility.isOneOf(entry.getKey(), CONNECTION_FACTORY, SYMBOLIC_NAME, MARSHALLER, REQUEST_REPLY_ENABLED, REQUEST_REPLY_CANCELLATION_TOPIC, JMS_CLIENT_ID)) {
          // Don't pass MOM-specific properties to the initial context to prevent problems with non-standard values.
          // For example, some containers throw an error if it finds an unserializable value.
        }
        else {
          env.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return env;
  }

  protected ConnectionFactory createConnectionFactory(Map<Object, Object> properties) throws NamingException {
    String connectionFactoryName = (String) assertNotNull(properties.get(CONNECTION_FACTORY), "Property {} not specified to lookup connection factory", CONNECTION_FACTORY);
    return (ConnectionFactory) createContext().lookup(connectionFactoryName);
  }

  protected Connection createConnection() throws JMSException {
    Object securityPrincipal = m_contextEnvironment.get(Context.SECURITY_PRINCIPAL);
    Object securityCredentials = m_contextEnvironment.get(Context.SECURITY_CREDENTIALS);
    Connection connection = null;
    boolean connectionValid = false;
    try {
      if (securityPrincipal != null && securityCredentials != null) {
        connection = m_connectionFactory.createConnection(securityPrincipal.toString(), securityCredentials.toString());
      }
      else {
        connection = m_connectionFactory.createConnection();
      }
      postCreateConnection(connection);
      connectionValid = true;
      return connection;
    }
    finally {
      //detect failure
      if (connection != null && !connectionValid) {
        try {
          connection.close();
        }
        catch (JMSException e2) {
          LOG.info("Close invalid connection", e2);
        }
      }
    }
  }

  protected void postCreateConnection(Connection connection) throws JMSException {
    connection.setClientID(m_clientId);
    // we directly start the shared connection
    connection.start();
  }

  public Destination resolveJmsDestination(final IDestination<?> destination, final Session session) {
    if (destination == null) {
      return null;
    }
    Destination jmsDestination = m_jmsDestinations.get(destination);
    if (jmsDestination == null) {
      synchronized (m_jmsDestinations) {
        jmsDestination = m_jmsDestinations.get(destination);
        if (jmsDestination == null) {
          jmsDestination = defineOrLookupJmsDestination(destination, session);
          m_jmsDestinations.put(destination, jmsDestination);
        }
      }
    }
    return jmsDestination;
  }

  protected Destination defineOrLookupJmsDestination(final IDestination<?> destination, final Session session) {
    try {
      if (destination.getResolveMethod() == ResolveMethod.JNDI) {
        return lookupJmsDestination(destination);
      }
      else if (destination.getResolveMethod() == ResolveMethod.DEFINE) {
        return defineJmsDestination(destination, session);
      }
      throw new AssertionException("Unsupported resolve method [{}]", destination);
    }
    catch (final JMSException | NamingException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  /**
   * Looks up a destination via JNDI.
   */
  protected Destination lookupJmsDestination(final IDestination<?> destination) throws NamingException {
    final Object object = Assertions.assertNotNull(createContext().lookup(destination.getName()));
    final Class<?> expectedType = (destination.getType() == DestinationType.QUEUE ? Queue.class : Topic.class);
    Assertions.assertInstance(object, expectedType, "The looked up destination is of type '{}', but expected type '{}' [{}]", object.getClass().getName(), expectedType.getName(), destination);
    return (Destination) object;
  }

  /**
   * Creates a destination ad-hoc.
   */
  protected Destination defineJmsDestination(final IDestination<?> destination, final Session session) throws JMSException {
    if (destination.getType() == DestinationType.QUEUE) {
      return session.createQueue(destination.getName());
    }
    else if (destination.getType() == DestinationType.TOPIC) {
      return session.createTopic(destination.getName());
    }
    throw new AssertionException("Unsupported destination type [{}]", destination);
  }

  protected IDestination<?> resolveMomDestination(final Destination destination) {
    for (Entry<IDestination, Destination> jmsDestination : m_jmsDestinations.entrySet()) {
      if (jmsDestination.getValue() == destination) {
        return jmsDestination.getKey();
      }
    }
    return null;
  }

  public int toJmsPriority(final PublishInput publishInput) {
    return publishInput.getPriority() + Message.DEFAULT_PRIORITY;
  }

  public long toJmsTimeToLive(final PublishInput publishInput) {
    return publishInput.getTimeToLive() == PublishInput.INFINITELY ? Message.DEFAULT_TIME_TO_LIVE : publishInput.getTimeToLive();
  }

  public int toJmsDeliveryMode(final PublishInput publishInput) {
    return publishInput.getDeliveryMode() == PublishInput.DELIVERY_MODE_PERSISTENT ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
  }

  /**
   * @return the identifier to name the {@link Connection}.
   */
  protected String computeClientId(final Map<Object, Object> properties) {
    final String clientId = ObjectUtility.toString(properties.get(JMS_CLIENT_ID));
    if (clientId != null) {
      return clientId;
    }
    final NodeId nodeId = NodeId.current();
    return StringUtility.join(" ", m_symbolicName, StringUtility.box("(", IIds.toString(nodeId), ")"));
  }

  /**
   * @return the associated {@link IJmsMessageHandler} for this JMS MOM implementor. It is never <code>null</code>.
   */
  public IJmsMessageHandler getMessageHandler() {
    return m_messageHandler;
  }

  /**
   * Exception handler used in MOM.
   */
  public static class MomExceptionHandler extends ExceptionHandler {
  }
}
