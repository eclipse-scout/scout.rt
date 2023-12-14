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

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;

public class JmsSessionProviderWithConnection extends JmsSessionProvider {

  protected final Connection m_connection;

  public JmsSessionProviderWithConnection(Connection connection, Session session, Destination destination) {
    super(session, destination);
    m_connection = connection;
  }

  @Override
  public void deleteTemporaryQueue() throws JMSException {
    // do nothing as temporary queue gets deleted as soon as connection is closed
  }

  @Override
  protected void closeImpl() throws JMSException {
    // close connection instead of session
    if (m_connection != null) {
      m_connection.close();
    }
  }
}
