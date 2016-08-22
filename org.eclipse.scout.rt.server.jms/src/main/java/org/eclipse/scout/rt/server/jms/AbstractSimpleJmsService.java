/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.jms.context.JmsRunContexts;
import org.eclipse.scout.rt.server.jms.transactional.AbstractTransactionalJmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static Logger LOG = LoggerFactory.getLogger(AbstractSimpleJmsService.class);

  private volatile JmsMessageConsumer m_messageConsumer;

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
      LOG.warn("Tried to send messages on inactive or disabled JMS message service {}", getClass().getSimpleName());
      return;
    }
    Session session = null;
    MessageProducer producer = null;
    try {
      session = createSession(connection);
      producer = session.createProducer(getDestination());
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
    return m_messageConsumer != null;
  }

  protected synchronized void startMessageConsumer() {
    LOG.debug("starting message consumer for {}", getClass().getSimpleName(), LOG.isTraceEnabled() ? new Exception("stack trace") : null);
    stopMessageConsumer();
    JmsMessageConsumer messageConsumer = createMessageConsumer();
    try {
      messageConsumer.subscribe();
      m_messageConsumer = messageConsumer;
    }
    catch (JMSException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e);
    }
  }

  protected synchronized void stopMessageConsumer() {
    if (m_messageConsumer != null) {
      try {
        m_messageConsumer.unsubscribe();
      }
      catch (JMSException e) {
        LOG.warn("Could not stop JMS message listener", e);
      }
      finally {
        m_messageConsumer = null;
      }
    }
  }

  protected JmsMessageConsumer createMessageConsumer() {
    return new JmsMessageConsumer(getConnection(), getDestination(), createMessageSerializer());
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
   * This class wraps a {@link MessageListener}, keeps track of the JMS subscription and delegates any messages to
   * {@link AbstractSimpleJmsService#execOnMessage(Object, Session)}.
   */
  protected class JmsMessageConsumer {

    private final Connection m_connection;
    private final IJmsMessageSerializer<T> m_messageSerializer;

    private final Session m_session;
    private final MessageConsumer m_consumer;

    public JmsMessageConsumer(final Connection connection, final Destination destination, final IJmsMessageSerializer<T> messageSerializer) {
      m_connection = Assertions.assertNotNull(connection, "JMS-connection must not be null");
      m_messageSerializer = Assertions.assertNotNull(messageSerializer, "Message serializer must not be null");

      try {
        m_session = createSession(connection);
        m_consumer = Assertions.assertNotNull(m_session.createConsumer(Assertions.assertNotNull(destination, "JMS-destination must not be null")), "Message consumer must not be null");
      }
      catch (JMSException e) {
        throw new ProcessingException("Failed to initialize JMS-session", e);
      }
    }

    protected final IJmsMessageSerializer<T> getMessageSerializer() {
      return m_messageSerializer;
    }

    protected final Session getSession() {
      return m_session;
    }

    public void subscribe() throws JMSException {
      m_consumer.setMessageListener(createMessageListener());
      m_connection.start();
      LOG.info("JMS message consumer started for {}.", AbstractSimpleJmsService.this.getClass().getSimpleName());
    }

    public void unsubscribe() throws JMSException {
      try {
        m_connection.stop();
      }
      catch (Exception e) {
        LOG.info("Unable to stop connection, possibly because of running in J2EE container", LOG.isTraceEnabled() ? e : null);
      }
      m_session.close(); // only need to close session, producer must then not be closed.
      LOG.info("JMS message consumer stopped for {}.", AbstractSimpleJmsService.this.getClass().getSimpleName());
    }

    protected MessageListener createMessageListener() {
      return new MessageListener() {
        @Override
        public void onMessage(final Message jmsMessage) {
          try {
            JmsRunContexts.empty()
                .withJmsMessage(jmsMessage)
                .withCorrelationId(jmsMessage.getJMSCorrelationID())
                .run(new IRunnable() {
              @Override
              public void run() throws Exception {
                final T message = getMessageSerializer().extractMessage(jmsMessage);
                if (message != null) {
                  execOnMessage(message, getSession());
                }
              }
            });
          }
          catch (RuntimeException | JMSException e) {
            LOG.warn("Could not process JMS message for {}", AbstractSimpleJmsService.this.getClass().getSimpleName(), e);
          }
        }
      };
    }
  }
}
