package org.eclipse.scout.rt.mom.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

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
