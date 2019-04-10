/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.PublishInput;
import org.eclipse.scout.rt.mom.jms.internal.JmsConnectionWrapper;

/**
 * Implementation of 'instance-scoped' {@link IMom} based on JMS (Java Messaging Standard).
 * <p>
 * This class is a specialized version of {@link JmsMomImplementor} which follows the JMS specification rules for JMS in
 * a J2EE environment.
 * <p>
 * Applied rules:
 * <ul>
 * <li>Each {@link Session} requires its own {@link Connection}
 * <li>ClientId should not be set on {@link Connection}
 * <li>ExceptionListener should not be set with {@link Connection#setExceptionListener(javax.jms.ExceptionListener)}
 * <li>Async send (CompletionListener) is not supported
 * <li>Async receive (MessageListener) is not supported
 * </ul>
 *
 * @since 6.1
 */
public class J2eeJmsMomImplementor extends JmsMomImplementor {

  @Override
  protected JmsConnectionWrapper createConnectionWrapper(final Map<Object, Object> properties) {
    return super.createConnectionWrapper(properties)
        .withConnectionRetryCount(0);
  }

  @Override
  public IJmsSessionProvider createSessionProvider(IDestination<?> destination, boolean transacted) throws JMSException {
    // Each session requires its own connection
    Connection connection = createConnection();
    // start connection
    connection.start();

    Session session = transacted ? connection.createSession(true, Session.SESSION_TRANSACTED) : connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination jmsDestination = resolveJmsDestination(destination, session);
    return new JmsSessionProviderWithConnection(connection, session, jmsDestination);
  }

  @Override
  protected <DTO> void publishNonTransactional(final IDestination<DTO> destination, final DTO transferObject, final PublishInput input) throws JMSException {
    // this publish method uses the shared connection
    synchronized (m_connectionWrapper) {
      // use lock on this connection until session is closed
      IJmsSessionProvider sessionProvider = super.createSessionProvider(destination, false);
      try {
        send(sessionProvider, destination, transferObject, input);
      }
      finally {
        sessionProvider.close();
      }
    }
  }

  @Override
  protected void postCreateConnection(Connection connection) throws JMSException {
    // We do not set client id nor exception handler and  we do NOT start the shared connection.
    // As a result the shared connection for publishing message is not started.
    // For only publishing messages, a connection must not be started.
  }
}
