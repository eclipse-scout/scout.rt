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
package org.eclipse.scout.rt.server.jms.transactional;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.jms.AbstractJmsService;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * Base class for a JMS scout service that receives messages synchronously. Use this class if you require a
 * transactional request - response behavior.
 * <p>
 * A services extending this class <strong>must be registered</strong> with a session based service factory.
 * <p>
 * Before you can use any of the send or receive methods you must call {@link #setupConnection()}.
 */
@SuppressWarnings("restriction")
public abstract class AbstractTransactionalJmsService<T> extends AbstractJmsService<T> {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTransactionalJmsService.class);

  private final String m_transactionId;

  public AbstractTransactionalJmsService(String transactionId) {
    super();
    m_transactionId = transactionId;
  }

  public final String getTransactionId() {
    return m_transactionId;
  }

  @Override
  protected synchronized void setupConnection() throws ProcessingException {
    super.setupConnection();
    // directly start connection
    try {
      getConnection().start();
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed starting JMS connection", e);
    }
  }

  protected Session createSession(Connection connection) throws ProcessingException {
    try {
      return connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
    }
    catch (JMSException e) {
      throw new ProcessingException("Error creating session", e);
    }
  }

  protected JmsTransactionMember<T> getTransaction() throws ProcessingException {
    ITransaction t = ThreadContext.getTransaction();
    if (t == null) {
      throw new IllegalStateException("not inside a scout transaction (ServerJob.schedule)");
    }
    @SuppressWarnings("unchecked")
    JmsTransactionMember<T> m = (JmsTransactionMember<T>) t.getMember(getTransactionId());
    if (m == null) {
      Connection connection = getConnection();
      m = new JmsTransactionMember<T>(getTransactionId(), connection, createSession(connection), lookupDestination(), createMessageSerializer());
      t.registerMember(m);
    }
    return m;
  }

  protected void send(T message) throws ProcessingException {
    JmsTransactionMember<T> transactionMember = getTransaction();
    transactionMember.send(message);
  }

  protected T receive(long timeoutMillis) throws ProcessingException {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(timeoutMillis);
  }

  protected T receive() throws ProcessingException {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(-1);
  }

  protected T receiveNoWait() throws ProcessingException {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(0);
  }
}
