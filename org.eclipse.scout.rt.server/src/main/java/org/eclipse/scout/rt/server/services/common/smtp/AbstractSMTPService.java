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
package org.eclipse.scout.rt.server.services.common.smtp;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpDebugReceiverEmailProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpDefaultFromEmailProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpHostProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpPasswordProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpPortProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpSslProtocolsProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpSubjectPrefixProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUseAuthenticationProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUseSmtpsProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUsernameProperty;
import org.eclipse.scout.rt.shared.mail.MailUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSMTPService implements ISMTPService {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSMTPService.class);

  private final String m_host;
  private final int m_port;
  private final String m_username;
  private final String m_password;
  private final boolean m_useAuthentication;
  private String m_debugReceiverEmail;
  private final String m_subjectPrefix;
  public final String m_defaultFromEmail;
  private final String m_sslProtocols;
  private final boolean m_useSmtps;

  public AbstractSMTPService() {
    m_host = getPropertyValue(SmtpHostProperty.class, getConfiguredHost());
    m_port = getPropertyValue(SmtpPortProperty.class, getConfiguredPort());
    m_username = getPropertyValue(SmtpUsernameProperty.class, getConfiguredUsername());
    m_password = getPropertyValue(SmtpPasswordProperty.class, getConfiguredPassword());
    m_useAuthentication = getPropertyValue(SmtpUseAuthenticationProperty.class, getConfiguredUseAuthentication());
    m_debugReceiverEmail = getPropertyValue(SmtpDebugReceiverEmailProperty.class, getConfiguredDefaultFromEmail());
    m_subjectPrefix = getPropertyValue(SmtpSubjectPrefixProperty.class, getConfiguredSubjectPrefix());
    m_defaultFromEmail = getPropertyValue(SmtpDefaultFromEmailProperty.class, getConfiguredDefaultFromEmail());
    m_sslProtocols = getPropertyValue(SmtpSslProtocolsProperty.class, getConfiguredSslProtocols());
    m_useSmtps = getPropertyValue(SmtpUseSmtpsProperty.class, getConfiguredUseSmtps());
  }

  protected <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz, DATA_TYPE defaultValue) {
    DATA_TYPE value = CONFIG.getPropertyValue(clazz);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  // configuration

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredHost() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(20)
  protected int getConfiguredPort() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredUseAuthentication() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredUsername() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredPassword() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  protected String getConfiguredSubjectPrefix() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredDefaultFromEmail() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredUseSmtps() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(90)
  protected String getConfiguredSslProtocols() {
    return null;
  }

  @Override
  public String getHost() {
    return m_host;
  }

  @Override
  public int getPort() {
    return m_port;
  }

  @Override
  public boolean isUseAuthentication() {
    return m_useAuthentication;
  }

  @Override
  public String getUsername() {
    return m_username;
  }

  @Override
  public String getPassword() {
    return m_password;
  }

  @Override
  public String getSubjectPrefix() {
    return m_subjectPrefix;
  }

  @Override
  public String getDefaultFromEmail() {
    return m_defaultFromEmail;
  }

  public void setDebugReceiverEmail(String debugReceiverEmail) {
    m_debugReceiverEmail = debugReceiverEmail;
  }

  @Override
  public String getDebugReceiverEmail() {
    return m_debugReceiverEmail;
  }

  public String getProtocol() {
    return isUseSmtps() ? "smtps" : "smtp";
  }

  @Override
  public boolean isUseSmtps() {
    return m_useSmtps;
  }

  @Override
  public String getSslProtocols() {
    return m_sslProtocols;
  }

  @Override
  public void sendMessage(MimeMessage message) {
    sendMessage(message, null);
  }

  @Override
  public void sendMessage(MimeMessage message, Session session) {
    Transport transport = null;
    try {
      if (session == null) {
        session = createSession();
      }
      transport = session.getTransport(getProtocol());
      if (!StringUtility.isNullOrEmpty(getUsername())) {
        transport.connect(getHost(), getUsername(), getPassword());
      }
      else {
        transport.connect();
      }
      // subject prefix
      String subjectPrefix = getSubjectPrefix();
      if (!StringUtility.isNullOrEmpty(subjectPrefix)) {
        String subject = subjectPrefix + ((message.getSubject() != null) ? (message.getSubject()) : (""));
        message.setSubject(subject);
      }

      Address[] allRecipients = message.getAllRecipients();
      // check debug receiver
      String debugReceiverEmail = getDebugReceiverEmail();
      if (debugReceiverEmail != null) {
        allRecipients = new Address[]{MailUtility.createInternetAddress(debugReceiverEmail)};
        LOG.debug("SMTP Service: debug receiver email set to: {}", debugReceiverEmail);
      }
      // from address
      Address[] fromAddresses = message.getFrom();
      if (fromAddresses == null || fromAddresses.length == 0) {
        String defaultFromEmail = getDefaultFromEmail();
        if (!StringUtility.isNullOrEmpty(defaultFromEmail)) {
          message.setFrom(MailUtility.createInternetAddress(defaultFromEmail));
        }
      }
      if (allRecipients != null && allRecipients.length > 0) {
        transport.sendMessage(message, allRecipients);
        transport.close();
        transport = null;
      }
    }
    catch (Exception e) {
      throw new ProcessingException("cannot send Mime Message.", e);
    }
    finally {
      try {
        if (transport != null) {
          transport.close();
        }
      }
      catch (Exception e) {
        LOG.warn("Could not close transport", e);
      }
    }
  }

  protected Session createSession() {
    Properties props = new Properties();
    props.setProperty("mail.transport.protocol", getProtocol());
    props.setProperty("mail." + getProtocol() + ".quitwait", "false");
    if (!StringUtility.isNullOrEmpty(m_host)) {
      props.setProperty("mail." + getProtocol() + ".host", m_host);
    }
    if (m_port > 0) {
      props.setProperty("mail." + getProtocol() + ".port", "" + m_port);
    }
    if (!StringUtility.isNullOrEmpty(m_username)) {
      props.setProperty("mail." + getProtocol() + ".user", m_username);
      props.setProperty("mail." + getProtocol() + ".auth", "" + m_useAuthentication);
    }
    if (!StringUtility.isNullOrEmpty(getSslProtocols())) {
      props.setProperty("mail." + getProtocol() + ".ssl.protocols", getSslProtocols());
    }
    return Session.getInstance(props, null);
  }
}
