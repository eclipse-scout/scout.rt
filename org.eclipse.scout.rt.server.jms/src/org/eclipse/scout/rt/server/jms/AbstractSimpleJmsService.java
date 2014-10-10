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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.jms.internal.Activator;
import org.eclipse.scout.rt.server.jms.transactional.AbstractTransactionalJmsService;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;

/**
 * Base class for a JMS scout service that receives messages asynchronously. Use this class if you do not need any
 * transactional behavior on messages. If you require more of a transactional request - response behavior use
 * {@link AbstractTransactionalJmsService}.
 * <p>
 * A services extending this class must <strong>not</strong> be registered with a session based service factory.
 * <p>
 * Before one can send messages or start the message consumer you must call {@link #setupConnection()}. After that one
 * can start receiving messages by calling {@link #startMessageConsumerJob()} and stopping by calling
 * {@link #stopMessageConsumerJob()}.
 */
public abstract class AbstractSimpleJmsService<T> extends AbstractJmsService<T> {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSimpleJmsService.class);

  private MessageConsumerJob m_messageConsumerJob;

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

  /**
   * A message consumer job calls this method if it receives any messages. This method is not called within a scout
   * server transaction. Also the method should not throw any exception. Therefore, it is recommended to start in this
   * method a new {@link ServerJob} to handle the message.
   * 
   * @param message
   *          not null
   * @param session
   *          The jms session. In case of a transacted session, one can commit an reject on it.
   * @param monitor
   *          The currents job monitor. If one has a blocking operation within this method, the monitors cancel status
   *          should be checked.
   */
  protected void execOnMessage(T message, Session session, IProgressMonitor monitor) {
  }

  protected synchronized boolean isMessageConsumerJobRunning() {
    return m_messageConsumerJob != null;
  }

  protected synchronized void startMessageConsumerJob() throws ProcessingException {
    stopMessageConsumerJob();
    m_messageConsumerJob = createMessageConsumerJob();
    m_messageConsumerJob.schedule();
  }

  protected synchronized void stopMessageConsumerJob() throws ProcessingException {
    MessageConsumerJob job = m_messageConsumerJob;
    if (job != null) {
      m_messageConsumerJob = null;
      job.cancel();
      // waiting for job to be stopped
      try {
        job.join(job.getReceiveTimeout() * 3);
      }
      catch (InterruptedException e) {
        throw new ProcessingException("Interrupted", e);
      }
      job.throwOnError();
    }
  }

  protected MessageConsumerJob createMessageConsumerJob() throws ProcessingException {
    return new MessageConsumerJob(getConnection());
  }

  /**
   * This is by intend not a ServerJob; we do not want to running a in scout transaction.
   */
  protected class MessageConsumerJob extends JobEx {
    private final IJmsMessageSerializer<T> m_messageSerializer;
    private final Connection m_connection;
    private final Session m_session;
    private final MessageConsumer m_consumer;

    public MessageConsumerJob(Connection connection) throws ProcessingException {
      super("JMS message receiver");
      m_messageSerializer = createMessageSerializer();
      m_connection = connection;
      try {
        m_session = createSession(connection);
        m_consumer = m_session.createConsumer(lookupDestination());
      }
      catch (JMSException e) {
        throw new ProcessingException("Failed initializing MessageConsumerJob", e);
      }
      if (m_messageSerializer == null || m_consumer == null) {
        throw new IllegalArgumentException("Connection nor consumer can be null");
      }
    }

    protected long getReceiveTimeout() {
      return 1000;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      LOG.info("JMS message consumer job started.");
      IStatus result = Status.OK_STATUS;
      try {
        m_connection.start();
        while (!monitor.isCanceled()) {
          onMessage(m_consumer.receive(getReceiveTimeout()), monitor);
        }
      }
      catch (Exception e) {
        LOG.error("Unexpected exception while receiving messages from consumer.", e);
        result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
      }
      finally {
        LOG.trace("Stopping JMS message consumer job...");
        try {
          m_connection.stop();
          // only need to close session, producer must then not be closed.
          m_session.close();
        }
        catch (Exception e) {
          LOG.error("Unexpected exception", e);
          result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
        }
      }
      LOG.info("JMS message consumer job stopped.");
      return result;
    }

    protected void onMessage(Message jmsMessage, IProgressMonitor monitor) {
      if (jmsMessage != null) {
        try {
          T message = m_messageSerializer.extractMessage(jmsMessage);
          if (message != null) {
            execOnMessage(message, m_session, monitor);
          }
        }
        catch (Exception e) {
          LOG.error("Unexpected exception", e);
        }
      }
    }
  }
}
