/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.smtp;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;

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
 * The {@link #m_idleSince} and {@link #m_messagesSent} properties are changed constantly by the
 * {@link SmtpConnectionPool} in order to manage this entry's lifecycle.<br>
 * The {@link #m_failed} property is set to true as soon as a MessagingException occurs while trying to send a message.
 */
@Bean
public class SmtpConnectionPoolEntry {

  protected String m_name;
  protected SmtpServerConfig m_smtpServerConfig;
  protected Session m_session;
  protected Transport m_transport;
  // creation time of this pool entry object in milliseconds
  protected long m_createTime;
  // number of milliseconds this pool entry has been idle
  protected long m_idleSince;
  protected int m_messagesSent;
  protected boolean m_failed;

  public void sendMessage(MimeMessage message, Address[] recipients) throws MessagingException {
    try {
      m_transport.sendMessage(message, recipients);
      m_messagesSent++;
    }
    catch (RuntimeException | MessagingException e) {
      m_failed = true;
      throw e;
    }
  }

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

  public SmtpServerConfig getSmtpServerConfig() {
    return m_smtpServerConfig;
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

  public int getMessagesSent() {
    return m_messagesSent;
  }

  public boolean isFailed() {
    return m_failed;
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
        " messages sent=" + m_messagesSent +
        " failed=" + m_failed + "]";
  }
}
