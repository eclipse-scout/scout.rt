package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.PROP_REPLY_ID;

import java.security.GeneralSecurityException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.IMomInitializer;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.PublishInput;
import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationNameProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.ApplicationVersionProperty;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.IFunction;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of 'instance-scoped' {@link IMom} based on JMS (Java Messaging Standard).
 * <p>
 * This class expects a JMS implementor to be available at runtime.
 *
 * @since 6.1
 */
@Bean
public class JmsMom implements IMom, IMomInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(JmsMom.class);

  protected final String m_momUid = UUID.randomUUID().toString();
  protected volatile Context m_context;
  protected volatile Connection m_connection;

  protected volatile Session m_defaultSession; // single-threaded
  protected volatile MessageProducer m_defaultProducer;

  protected volatile TemporaryQueue m_replyQueue;
  protected volatile Topic m_requestReplyCancellationTopic;
  protected final Map<String, ReplyFuture> m_replyFutureMap = new ConcurrentHashMap<>();

  protected final Map<IDestination, IMarshaller> m_marshallers = new ConcurrentHashMap<>();
  protected final Map<IDestination, IEncrypter> m_encrypters = new ConcurrentHashMap<>();

  protected final Map<Integer, ISubscriptionStrategy> m_subscriptionStrategies = new ConcurrentHashMap<>();

  @Override
  public void init(final Map<Object, Object> properties) throws Exception {
    m_context = createContext(properties);
    m_connection = createConnection(m_context, properties);
    m_defaultSession = m_connection.createSession(false /* non-transacted */, Session.AUTO_ACKNOWLEDGE);
    m_defaultProducer = m_defaultSession.createProducer(null /* all destinations */);

    m_subscriptionStrategies.put(ACKNOWLEDGE_AUTO, BEANS.get(AutoAcknowledgeStrategy.class).init(this));
    m_subscriptionStrategies.put(ACKNOWLEDGE_TRANSACTED, BEANS.get(TransactedStrategy.class).init(this));

    // Register consumer to dispatch replies of 'request-reply' messaging to the requester.
    m_replyQueue = m_defaultSession.createTemporaryQueue();
    m_defaultSession
        .createConsumer(m_replyQueue)
        .setMessageListener(new JmsMessageListener() {

          @Override
          public void onJmsMessage(final Message message) throws JMSException {
            final String replyId = message.getStringProperty(PROP_REPLY_ID);
            final ReplyFuture replyFuture = m_replyFutureMap.remove(replyId);
            if (replyFuture != null) {
              replyFuture.set(message);
            }
          }
        });

    // Register consumer to handle cancellation requests for 'request-reply' communication.
    m_requestReplyCancellationTopic = m_defaultSession.createTopic(CONFIG.getPropertyValue(RequestReplyCancellationTopicProperty.class));
    m_defaultSession
        .createConsumer(m_requestReplyCancellationTopic)
        .setMessageListener(new JmsMessageListener() {

          @Override
          public void onJmsMessage(final Message message) throws JMSException {
            Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
                .andMatchExecutionHint(message.getStringProperty(PROP_REPLY_ID))
                .toFilter(), true);
          }
        });

    m_connection.start();
  }

  @Override
  public void publish(final IDestination destination, final Object transferObject) {
    publish(destination, transferObject, newPublishInput());
  }

  @Override
  public void publish(final IDestination destination, final Object transferObject, final PublishInput input) {
    Assertions.assertNotNull(destination, "destination not specified");
    Assertions.assertNotNull(input, "publishInput not specified");

    try {
      if (input.isTransactional()) {
        publishTransactional(destination, transferObject, input);
      }
      else {
        publishNonTransactional(destination, transferObject, input);
      }
    }
    catch (final JMSException | GeneralSecurityException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  protected void publishNonTransactional(final IDestination destination, final Object transferObject, final PublishInput input) throws JMSException, GeneralSecurityException {
    final IMarshaller marshaller = lookupMarshaller(destination);
    final IEncrypter encrypter = lookupEncrypter(destination);

    final Message message = JmsMessageWriter.newInstance(m_defaultSession, marshaller, encrypter)
        .writeTransferObject(transferObject)
        .writeProperties(input.getProperties(), true)
        .writeCorrelationId(CorrelationId.CURRENT.get())
        .build();
    send(m_defaultProducer, toJmsDestination(destination), message, toJmsDeliveryMode(input), toJmsPriority(input), toJmsTimeToLive(input));
  }

  protected void publishTransactional(final IDestination destination, final Object transferObject, final PublishInput input) throws JMSException, GeneralSecurityException {
    final ITransaction currentTransaction = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required for transactional messaging");
    final IMarshaller marshaller = lookupMarshaller(destination);
    final IEncrypter encrypter = lookupEncrypter(destination);

    // Register transaction member for transacted publishing.
    final JmsTransactionMember txMember = currentTransaction.registerMemberIfAbsent(m_momUid, new IFunction<String, JmsTransactionMember>() {

      @Override
      public JmsTransactionMember apply(final String memberId) {
        try {
          final Session transactedSession = m_connection.createSession(true, Session.SESSION_TRANSACTED);
          return BEANS.get(JmsTransactionMember.class)
              .withMemberId(memberId)
              .withTransactedSession(transactedSession)
              .withTransactedProducer(transactedSession.createProducer(null))
              .withAutoClose(true); // close upon transaction end
        }
        catch (final JMSException e) {
          throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
        }
      }
    });

    // Publish the message
    final Message message = JmsMessageWriter.newInstance(txMember.getTransactedSession(), marshaller, encrypter)
        .writeTransferObject(transferObject)
        .writeProperties(input.getProperties(), true)
        .writeCorrelationId(CorrelationId.CURRENT.get())
        .build();
    send(txMember.getTransactedProducer(), toJmsDestination(destination), message, toJmsDeliveryMode(input), toJmsPriority(input), toJmsTimeToLive(input));
  }

  @Override
  public <TRANSFER_OBJECT> ISubscription subscribe(final IDestination destination, final IMessageListener<TRANSFER_OBJECT> listener, final RunContext runContext) {
    return subscribe(destination, listener, runContext, ACKNOWLEDGE_AUTO);
  }

  @Override
  public <TRANSFER_OBJECT> ISubscription subscribe(final IDestination destination, final IMessageListener<TRANSFER_OBJECT> listener, final RunContext runContext, final int acknowledgementMode) {
    Assertions.assertNotNull(destination, "destination not specified");
    Assertions.assertNotNull(listener, "messageListener not specified");

    try {
      final ISubscriptionStrategy strategy = Assertions.assertNotNull(m_subscriptionStrategies.get(acknowledgementMode), "Acknowledgement mode not supported [mode={}]", acknowledgementMode);
      return strategy.subscribe(destination, listener, runContext != null ? runContext.copy() : RunContexts.empty());
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public <REPLY_OBJECT, REQUEST_OBJECT> REPLY_OBJECT request(final IDestination destination, final REQUEST_OBJECT transferObject) {
    return request(destination, transferObject, newPublishInput());
  }

  @Override
  public <REPLY_OBJECT> REPLY_OBJECT request(final IDestination destination, final Object transferObject, final PublishInput input) {
    Assertions.assertNotNull(destination, "destination not specified");
    Assertions.assertNotNull(input, "publishInput not specified");
    Assertions.assertFalse(input.isTransactional(), "transactional mode not supported for 'request-reply' communication");

    final IMarshaller marshaller = lookupMarshaller(destination);
    final IEncrypter encrypter = lookupEncrypter(destination);

    // Prepare to receive the reply message
    final ReplyFuture<REPLY_OBJECT> replyFuture = new ReplyFuture<>(marshaller, encrypter);
    final String replyId = String.format("scout.mom.requestreply.uid-%s", UUID.randomUUID()); // JMS message ID not applicable because unknown until sent

    m_replyFutureMap.put(replyId, replyFuture);
    try {
      // Prepare the request message
      final Message message = JmsMessageWriter.newInstance(m_defaultSession, marshaller, encrypter)
          .writeReplyTo(m_replyQueue)
          .writeReplyId(replyId)
          .writeProperties(input.getProperties(), true)
          .writeCorrelationId(CorrelationId.CURRENT.get())
          .writeTransferObject(transferObject)
          .build();
      send(m_defaultProducer, toJmsDestination(destination), message, toJmsDeliveryMode(input), toJmsPriority(input), toJmsTimeToLive(input));

      // Wait until the reply is received
      try {
        return replyFuture.awaitDoneAndGet(input.getRequestReplyTimeout());
      }
      catch (final ExecutionException e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
      catch (ThreadInterruptedException | TimedOutException e) {
        final Message cancellationMessage = JmsMessageWriter.newInstance(m_defaultSession, marshaller, encrypter)
            .writeReplyId(replyId)
            .writeCorrelationId(CorrelationId.CURRENT.get())
            .build();
        send(m_defaultProducer, m_requestReplyCancellationTopic, cancellationMessage, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        throw e;
      }
    }
    catch (final JMSException | GeneralSecurityException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    finally {
      m_replyFutureMap.remove(replyId);
    }
  }

  @Override
  public <REQUEST_OBJECT, REPLY_OBJECT> ISubscription reply(final IDestination destination, final IRequestListener<REQUEST_OBJECT, REPLY_OBJECT> listener, final RunContext runContext) {
    Assertions.assertNotNull(destination, "Destination not specified");
    Assertions.assertNotNull(listener, "MessageListener not specified");

    final IMarshaller marshaller = lookupMarshaller(destination);
    final IEncrypter encrypter = lookupEncrypter(destination);
    try {
      final MessageConsumer consumer = m_defaultSession.createConsumer(toJmsDestination(destination));
      consumer.setMessageListener(new JmsMessageListener() {

        @Override
        public void onJmsMessage(final Message jmsRequest) throws JMSException, GeneralSecurityException {
          final JmsMessageReader<REQUEST_OBJECT> requestReader = JmsMessageReader.newInstance(jmsRequest, marshaller, encrypter);
          final IMessage<REQUEST_OBJECT> request = requestReader.readMessage();
          final String replyId = requestReader.readReplyId();
          final Destination replyTopic = requestReader.readReplyTo();

          Jobs.schedule(new IRunnable() {

            @Override
            public void run() throws Exception {
              Message replyMessage;
              try {
                final REPLY_OBJECT reply = listener.onRequest(request);
                replyMessage = JmsMessageWriter.newInstance(m_defaultSession, marshaller, encrypter)
                    .writeTransferObject(reply)
                    .writeRequestReplySuccess(true)
                    .writeReplyId(replyId)
                    .writeCorrelationId(CorrelationId.CURRENT.get())
                    .build();
              }
              catch (final Exception e) { // NOSONAR
                BEANS.get(ExceptionHandler.class).handle(e);
                replyMessage = JmsMessageWriter.newInstance(m_defaultSession, marshaller, encrypter)
                    .writeTransferObject(interceptRequestReplyException(e))
                    .writeRequestReplySuccess(false)
                    .writeReplyId(replyId)
                    .writeCorrelationId(CorrelationId.CURRENT.get())
                    .build();
              }
              send(m_defaultProducer, replyTopic, replyMessage, jmsRequest.getJMSDeliveryMode(), jmsRequest.getJMSPriority(), Message.DEFAULT_TIME_TO_LIVE);
            }
          }, Jobs.newInput()
              .withName("Receiving JMS request [msg={}]", jmsRequest)
              .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true)
              .withExecutionHint(replyId) // Register for cancellation
              .withRunContext((runContext != null ? runContext.copy() : RunContexts.empty())
                  .withThreadLocal(IMessage.CURRENT, request)
                  .withCorrelationId(requestReader.readCorrelationId())));

        }
      });

      return new JmsSubscription(consumer, destination);
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public IDestination newTopic(final String topic) {
    try {
      return BEANS.get(JmsTopicDestination.class).init(m_defaultSession.createTopic(topic));
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public IDestination newQueue(final String queue) {
    try {
      return BEANS.get(JmsQueueDestination.class).init(m_defaultSession.createQueue(queue));
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public IDestination lookupDestination(final String name) {
    try {
      final Destination destination = (Destination) m_context.lookup(name);
      if (destination instanceof Queue) {
        return BEANS.get(JmsQueueDestination.class).init((Queue) destination);
      }
      else if (destination instanceof Topic) {
        return BEANS.get(JmsTopicDestination.class).init((Topic) destination);
      }
      else {
        throw new PlatformException("Destination lookup failed: must be a topic or queue [name={}, destination={}]", name, destination);
      }
    }
    catch (final NamingException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public PublishInput newPublishInput() {
    return BEANS.get(PublishInput.class);
  }

  @Override
  public void destroy() {
    try {
      m_connection.close();
    }
    catch (final JMSException e) {
      LOG.error("Failed to destory MOM", e);
    }
  }

  @Override
  public IRegistrationHandle registerMarshaller(final IDestination destination, final IMarshaller marshaller) {
    m_marshallers.put(destination, marshaller);
    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        m_marshallers.remove(destination);
      }
    };
  }

  @Override
  public IRegistrationHandle registerEncrypter(final IDestination destination, final IEncrypter encrypter) {
    m_encrypters.put(destination, encrypter);
    return new IRegistrationHandle() {

      @Override
      public void dispose() {
        m_encrypters.remove(encrypter);
      }
    };
  }

  /**
   * Registers the given {@link ISubscriptionStrategy} to receive message.
   *
   * @see AutoAcknowledgeStrategy
   * @see TransactedStrategy
   */
  public void registerSubscriptionStrategy(final int acknowledgementMode, final ISubscriptionStrategy strategy) {
    m_subscriptionStrategies.put(acknowledgementMode, strategy);
  }

  /**
   * Returns the {@link IMarshaller} registered for the given destination, and is never <code>null</code>.
   */
  public IMarshaller lookupMarshaller(final IDestination destination) {
    final IMarshaller marshaller = m_marshallers.get(destination);
    if (marshaller != null) {
      return marshaller;
    }
    return BEANS.get(CONFIG.getPropertyValue(MarshallerProperty.class));
  }

  /**
   * Returns the {@link IEncrypter} registered for the given destination, or <code>null</code> if not set.
   */
  public IEncrypter lookupEncrypter(final IDestination destination) {
    final IEncrypter encrypter = m_encrypters.get(destination);
    if (encrypter != null) {
      return encrypter;
    }

    final Class<? extends IEncrypter> clazz = CONFIG.getPropertyValue(EncrypterProperty.class);
    return (clazz != null ? BEANS.get(clazz) : null);
  }

  /**
   * Returns the {@link Connection} to the JMS broker.
   */
  public Connection getConnection() {
    return m_connection;
  }

  /**
   * Returns the default {@link Session} of this MOM to receive and send messages. This session is not transacted, and
   * configured for auto-acknowledge messages.
   * <p>
   * Please note: A JMS session is single-threaded, so while processing a message, not other message can be received,
   * nor a message published.
   */
  public Session getDefaultSession() {
    return m_defaultSession;
  }

  /**
   * Returns the default {@link MessageProducer} of this MOM to send messages. There is not destination associated with
   * this producer.
   */
  public MessageProducer getDefaultProducer() {
    return m_defaultProducer;
  }

  /**
   * Sends the message to the given destination.
   */
  protected void send(final MessageProducer producer, final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {
    final IRestorer interruption = ThreadInterruption.clear(); // Temporarily clear the thread's interrupted status while sending a message.
    try {
      LOG.debug("Sending JMS message [destination={}, message={}]", destination, message);
      producer.send(destination, message, deliveryMode, priority, timeToLive);
    }
    finally {
      interruption.restore();
    }
  }

  protected Context createContext(final Map<Object, Object> properties) throws NamingException {
    return new InitialContext(new Hashtable<>(properties));
  }

  protected Connection createConnection(final Context context, final Map<Object, Object> properties) throws NamingException, JMSException {
    final String connectionFactoryName = (String) Assertions.assertNotNull(properties.get(CONNECTION_FACTORY), "Property {} not specified to lookup connection factory", CONNECTION_FACTORY);
    final ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);
    final Connection connection = connectionFactory.createConnection();
    connection.setClientID(computeConnectionIdentifier());
    connection.setExceptionListener(new ExceptionListener() {

      @Override
      public void onException(final JMSException exception) {
        BEANS.get(MomExceptionHandler.class).handle(exception);
      }
    });
    return connection;
  }

  public Destination toJmsDestination(final IDestination destination) {
    return Assertions.assertNotNull(destination.getAdapter(Destination.class), "JMS destination cannot be resolved");
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

  protected Throwable interceptRequestReplyException(final Throwable t) {
    // Unset cause and stracktrace (security)
    if (t.getCause() == t) {
      t.initCause(null);
    }
    t.setStackTrace(new StackTraceElement[0]);

    // Break recursion
    if (t instanceof ProcessingException) {
      ((ProcessingStatus) ((ProcessingException) t).getStatus()).setException(null);
    }

    return t;
  }

  /**
   * Returns the identifier to name the {@link Connection}.
   */
  protected String computeConnectionIdentifier() {
    final String applicationName = CONFIG.getPropertyValue(ApplicationNameProperty.class);
    final String applicationVersion = CONFIG.getPropertyValue(ApplicationVersionProperty.class);
    final String nodeId = BEANS.get(NodeIdentifier.class).get();
    return String.format("%s:%s [mom=%s nodeId=%s]", applicationName, applicationVersion, getClass().getSimpleName(), nodeId);
  }

  /**
   * Future to wait for a reply to receive.
   */
  protected static class ReplyFuture<TYPE> {

    protected final IBlockingCondition m_condition = Jobs.newBlockingCondition(true);
    private final IMarshaller m_marshaller;
    private final IEncrypter m_encrypter;

    protected volatile Message m_reply;

    public ReplyFuture(final IMarshaller marshaller, final IEncrypter encrypter) {
      m_marshaller = marshaller;
      m_encrypter = encrypter;
    }

    /**
     * Sets the reply.
     */
    public void set(final Message reply) {
      m_reply = reply;
      m_condition.setBlocking(false);
    }

    /**
     * Waits until a reply is received.
     *
     * @param timeoutMillis
     *          the maximal time to wait for the job to complete, or {@link PublishInput#INFINITELY} to wait infinitely.
     * @throws ThreadInterruptedException
     *           if interrupted while waiting.
     * @throws TimedOutException
     *           if the wait timed out.
     */
    @SuppressWarnings("unchecked")
    public TYPE awaitDoneAndGet(final long timeoutMillis) throws JMSException, GeneralSecurityException, ExecutionException {
      // Wait until the reply is received
      if (timeoutMillis == PublishInput.INFINITELY) {
        m_condition.waitFor();
      }
      else {
        m_condition.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
      }

      // Evaluate the reply
      final JmsMessageReader reader = JmsMessageReader.newInstance(m_reply, m_marshaller, m_encrypter);
      final Object reply = reader.readMessage().getTransferObject();
      if (reader.readRequestReplySuccess()) {
        return (TYPE) reply;
      }
      final Throwable cause = reply instanceof Throwable ? (Throwable) reply : new ProcessingException("Request-Reply failed");
      throw new ExecutionException(cause);
    }
  }

  public static class MomExceptionHandler extends ExceptionHandler {

    @Override
    public void handle(final Throwable t) {
      if (t instanceof GeneralSecurityException) {
        LOG.warn("Decryption failed, either because of a bad authenticity token or a cipher problem. Message is discarded.", t);
      }
      else {
        super.handle(t);
      }
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this MOM upon platform shutdown.
   */
  @Order(IMom.DESTROY_ORDER)
  public static class MomPlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (State.PlatformStopping.equals(event.getState())) {
        BEANS.get(JmsMom.class).destroy();
      }
    }
  }
}
