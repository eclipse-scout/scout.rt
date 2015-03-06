/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.jms.transactional.AbstractTransactionalJmsService;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;

/**
 * Base class for a JMS scout service that receives messages asynchronously. Use this class if you do not need any
 * transactional behavior on messages. If you require more of a transactional request - response behavior use
 * {@link AbstractTransactionalJmsService}.
 * <p>
 * A services extending this class must <strong>not</strong> be registered with a session based service factory.
 * <p>
 * Before one can send messages or start the message consumer you must call {@link #setupConnection()}. After that one
 * can start receiving messages by calling {@link #startMessageConsumer()} and stopping by calling
 * {@link #stopMessageConsumer()}.
 */
public abstract class AbstractSimpleJmsService<T> extends AbstractJmsService<T> {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSimpleJmsService.class);

  private volatile IFuture<Void> m_messageConsumerFuture;

  private static final String PROP_REQUEST_TIMEOUT = String.format("%s#requestTimeout", AbstractSimpleJmsService.class.getName());
  private final long m_receiveTimeout = ConfigIniUtility.getPropertyLong(PROP_REQUEST_TIMEOUT, TimeUnit.SECONDS.toMillis(1));

  protected Session createSession(Connection connection) throws JMSException {
    return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  /**
   * Sends messages to the JMS destination. If the service is not enabled or active a warning is logged and the request
   * is ignored.
   * <p>
   * This method must not be called in a scout server transaction. Any errors are caught and logged.
   * <p>
   * It is recommended to collect some messages before sending them. In some cases a {@link ITransactionMember} would be
   * a good place.
   */
  protected void send(List<T> messages) {
    if (messages == null || messages.isEmpty()) {
      return;
    }
    Connection connection = getConnection();
    if (!isEnabled() || connection == null) {
      LOG.warn("Tried to send messages on a not active or enabled JMS message service");
      return;
    }
    Session session = null;
    MessageProducer producer = null;
    try {
      session = createSession(connection);
      producer = session.createProducer(lookupDestination());
      IJmsMessageSerializer<T> serializer = createMessageSerializer();
      for (T message : messages) {
        producer.send(serializer.createMessage(message, session));
      }
    }
    catch (Exception e) {
      LOG.error("Failed sending messages", e);
    }
    finally {
      // only need to close session, publisher must then not be closed.
      if (session != null) {
        try {
          session.close();
        }
        catch (JMSException e) {
          LOG.error("Failed closing", e);
        }
      }
    }
  }

  protected boolean isMessageConsumerRunning() {
    IFuture<Void> future = m_messageConsumerFuture;
    return future != null && !future.isCancelled();
  }

  protected synchronized void startMessageConsumer() throws ProcessingException {
    stopMessageConsumer();
    m_messageConsumerFuture = OBJ.one(IServerJobManager.class).schedule(createMessageConsumerRunnable(), ServerJobInput.empty().transactional(false));
  }

  protected synchronized void stopMessageConsumer() throws ProcessingException {
    if (m_messageConsumerFuture != null) {
      m_messageConsumerFuture.cancel(true);

      // Wait for the consumer to be stopped.
      try {
        OBJ.one(IServerJobManager.class).waitUntilDone(new FutureFilter(m_messageConsumerFuture), m_receiveTimeout * 3, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {
        // NOOP
      }
      finally {
        m_messageConsumerFuture = null;
      }
    }
  }

  protected MessageConsumerRunnable createMessageConsumerRunnable() throws ProcessingException {
    return new MessageConsumerRunnable(getConnection(), lookupDestination(), createMessageSerializer(), m_receiveTimeout);
  }

  /**
   * Method invoked to handle a message received from a subscribed channel. This method is not called within a
   * transaction.
   *
   * @param message
   *          message received; is is not <code>null</code>.
   * @param session
   *          The JMS-session; in case of a transacted JMS-session, one can commit an reject on it.
   */
  protected void execOnMessage(T message, Session session) {
  }

  /**
   * Runnable to wait for incoming JMS-messages.
   */
  protected class MessageConsumerRunnable implements IRunnable {

    private final Connection m_connection;
    private final IJmsMessageSerializer<T> m_messageSerializer;
    private final long m_receiveTimeout;

    private final Session m_session;
    private final MessageConsumer m_consumer;

    public MessageConsumerRunnable(final Connection connection, final Destination destination, final IJmsMessageSerializer<T> messageSerializer, final long receiveTimeout) throws ProcessingException {
      m_connection = Assertions.assertNotNull(connection, "JMS-connection must not be null");
      m_messageSerializer = Assertions.assertNotNull(messageSerializer, "Message serializer must not be null");
      m_receiveTimeout = receiveTimeout;

      try {
        m_session = createSession(connection);
        m_consumer = Assertions.assertNotNull(m_session.createConsumer(Assertions.assertNotNull(destination, "JMS-destination must not be null")), "Message consumer must not be null");
      }
      catch (JMSException e) {
        throw new ProcessingException("Failed to initialize JMS-session", e);
      }
    }

    @Override
    public void run() throws Exception {
      LOG.info("JMS message consumer started.");

      m_connection.start();
      try {
        while (!IProgressMonitor.CURRENT.get().isCancelled()) {
          try {
            final Message jmsMessage = m_consumer.receive(m_receiveTimeout);
            if (jmsMessage != null) {
              onMessage(jmsMessage);
            }
          }
          catch (final Exception e) {
            LOG.error("Failed to process JMS-message", e);
          }
        }
      }
      finally {
        m_connection.stop();
        m_session.close(); // only need to close session, producer must then not be closed.
      }
      LOG.info("JMS message consumer stopped.");
    }

    /**
     * Method invoked once a JMS-message is received.
     *
     * @param jmsMessage
     *          JMS-message; is not <code>null</code>.
     */
    protected void onMessage(final Message jmsMessage) throws Exception {
      final T message = m_messageSerializer.extractMessage(jmsMessage);
      if (message != null) {
        execOnMessage(message, m_session);
      }
    }
  }
}
