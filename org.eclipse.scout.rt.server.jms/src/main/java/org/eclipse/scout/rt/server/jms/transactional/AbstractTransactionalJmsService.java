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
package org.eclipse.scout.rt.server.jms.transactional;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.jms.AbstractJmsService;

/**
 * Base class for a JMS scout service that receives messages synchronously. Use this class if you require a
 * transactional request - response behavior.
 * <p>
 * A services extending this class <strong>must be registered</strong> with a session based service factory.
 * <p>
 * Before you can use any of the send or receive methods you must call {@link #setupConnection()}.
 */
public abstract class AbstractTransactionalJmsService<T> extends AbstractJmsService<T> {

  protected AbstractTransactionalJmsService() {
  }

  public abstract String getTransactionId();

  @Override
  protected synchronized void setupConnection() {
    super.setupConnection();
    // directly start connection
    try {
      getConnection().start();
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed starting JMS connection", e);
    }
  }

  protected Session createSession(Connection connection) {
    try {
      return connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
    }
    catch (JMSException e) {
      throw new ProcessingException("Error creating session", e);
    }
  }

  protected JmsTransactionMember<T> getTransaction() {
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");
    @SuppressWarnings("unchecked")
    JmsTransactionMember<T> m = (JmsTransactionMember<T>) tx.getMember(getTransactionId());
    if (m == null) {
      Connection connection = getConnection();
      m = new JmsTransactionMember<T>(getTransactionId(), connection, createSession(connection), getDestination(), createMessageSerializer());
      tx.registerMember(m);
    }
    return m;
  }

  protected void send(T message) {
    JmsTransactionMember<T> transactionMember = getTransaction();
    transactionMember.send(message);
  }

  protected T receive(long timeoutMillis) {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(timeoutMillis);
  }

  protected T receive() {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(-1);
  }

  protected T receiveNoWait() {
    JmsTransactionMember<T> transactionMember = getTransaction();
    return transactionMember.receive(0);
  }
}
