package org.eclipse.scout.rt.mail.smtp;

import javax.mail.Session;
import javax.mail.Transport;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This class models a connection managed by the {@link SmtpConnectionPool}.<br>
 * <br>
 * The properties
 * <ul>
 * <li>{@link #m_name}</li>
 * <li>{@link #m_smtpServerConfig}</li>
 * <li>{@link #m_session}</li>
 * <li>{@link #m_transport}</li>
 * <li>{@link #m_createTime}</li>
 * </ul>
 * are never changed during the lifetime of this {@link SmtpConnectionPoolEntry} object.<br>
 * The {@link #m_idleSince} property is changed constantly by the {@link SmtpConnectionPool} in order to manage this
 * entry's lifecycle.
 */
@Bean
public class SmtpConnectionPoolEntry {

  private String m_name;
  private SmtpServerConfig m_smtpServerConfig;
  private Session m_session;
  private Transport m_transport;
  // creation time of this pool entry object in milliseconds
  private long m_createTime;
  // number of milliseconds this pool entry has been idle
  private long m_idleSince;
  private int m_messagesSent;

  public SmtpConnectionPoolEntry withName(String name) {
    m_name = name;
    return this;
  }

  public SmtpConnectionPoolEntry withSmtpServerConfig(SmtpServerConfig smtpServerConfig) {
    m_smtpServerConfig = smtpServerConfig;
    return this;
  }

  public SmtpConnectionPoolEntry withSession(Session session) {
    m_session = session;
    return this;
  }

  public SmtpConnectionPoolEntry withTransport(Transport transport) {
    m_transport = transport;
    return this;
  }

  public SmtpConnectionPoolEntry withCreateTime(long createTime) {
    m_createTime = createTime;
    return this;
  }

  public SmtpConnectionPoolEntry withIdleSince(long idleSince) {
    m_idleSince = idleSince;
    return this;
  }

  public String getName() {
    return m_name;
  }

  public SmtpServerConfig getSmtpServerConfig() {
    return m_smtpServerConfig;
  }

  public Session getSession() {
    return m_session;
  }

  public Transport getTransport() {
    return m_transport;
  }

  public long getCreateTime() {
    return m_createTime;
  }

  public long getIdleSince() {
    return m_idleSince;
  }

  public void incrementMessagesSent() {
    m_messagesSent++;
  }

  public int getMessagesSent() {
    return m_messagesSent;
  }

  public boolean matchesConfig(SmtpServerConfig smtpServerConfig) {
    return m_smtpServerConfig.equals(smtpServerConfig);
  }

  @Override
  public String toString() {
    return SmtpConnectionPoolEntry.class.getSimpleName() +
        "[name=" + m_name +
        " transport=" + m_transport +
        " created=" + (System.currentTimeMillis() - m_createTime) / 1000d + "s ago" +
        " idle for=" + (System.currentTimeMillis() - m_idleSince) / 1000d + "s" +
        " messages sent=" + m_messagesSent + "]";
  }
}
