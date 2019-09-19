package org.eclipse.scout.rt.mail.smtp;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;

@Bean
public class LeasedSmtpConnection implements AutoCloseable {

  protected SmtpConnectionPool m_connectionPool;
  protected SmtpConnectionPoolEntry m_connectionPoolEntry;
  protected Transport m_transport;
  protected boolean m_failed;

  private boolean m_closed;

  public void sendMessage(MimeMessage message, Address[] recipients) throws MessagingException {
    Assertions.assertFalse(m_closed, "{} must not be used after it has been closed.", LeasedSmtpConnection.class.getSimpleName());
    try {
      m_transport.sendMessage(message, recipients);
      m_connectionPoolEntry.incrementMessagesSent();
    }
    catch (MessagingException e) {
      m_failed = true;
      throw e;
    }
  }

  @Override
  public void close() {
    m_closed = true;
    m_connectionPool.releaseConnection(this);
  }

  protected LeasedSmtpConnection withConnectionPool(SmtpConnectionPool connectionPool) {
    m_connectionPool = connectionPool;
    return this;
  }

  protected LeasedSmtpConnection withConnectionPoolEntry(SmtpConnectionPoolEntry connectionPoolEntry) {
    m_connectionPoolEntry = connectionPoolEntry;
    return this;
  }

  protected LeasedSmtpConnection withTransport(Transport transport) {
    m_transport = transport;
    return this;
  }

  protected Transport getTransport() {
    return m_transport;
  }

  public boolean isFailed() {
    return m_failed;
  }
}
