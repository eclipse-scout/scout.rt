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
package org.eclipse.scout.rt.server.services.common.smtp;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.commons.BundleContextUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;

@SuppressWarnings("restriction")
public abstract class AbstractSMTPService extends AbstractService implements ISMTPService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSMTPService.class);

  private String m_host;
  private int m_port;
  private String m_username;
  private String m_password;
  private boolean m_useAuthentication;
  private String m_debugReceiverEmail;
  private String m_subjectPrefix;
  public String m_defaultFromEmail;
  private String m_sslProtocols;
  private boolean m_useSmtps;

  public AbstractSMTPService() {
    init();
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

  private void init() {
    setHost(getConfiguredHost());
    setPort(getConfiguredPort());
    setUsername(getConfiguredUsername());
    setPassword(getConfiguredPassword());
    setSubjectPrefix(getConfiguredSubjectPrefix());
    setDefaultFromEmail(getConfiguredDefaultFromEmail());
    setUseSmtps(getConfiguredUseSmtps());
    setSslProtocols(getConfiguredSslProtocols());
  }

  @Override
  public String getHost() {
    return m_host;
  }

  @Override
  public void setHost(String s) {
    m_host = s;
  }

  @Override
  public int getPort() {
    return m_port;
  }

  @Override
  public void setPort(int s) {
    m_port = s;
  }

  @Override
  public boolean isUseAuthentication() {
    return m_useAuthentication;
  }

  @Override
  public void setUseAuthentication(boolean useAuthentication) {
    m_useAuthentication = useAuthentication;
  }

  @Override
  public String getUsername() {
    return m_username;
  }

  @Override
  public void setUsername(String s) {
    m_username = s;
  }

  @Override
  public String getPassword() {
    return m_password;
  }

  @Override
  public void setPassword(String s) {
    m_password = s;
  }

  @Override
  public String getSubjectPrefix() {
    return m_subjectPrefix;
  }

  @Override
  public void setSubjectPrefix(String subjectPrefix) {
    m_subjectPrefix = subjectPrefix;
  }

  @Override
  public String getDefaultFromEmail() {
    return m_defaultFromEmail;
  }

  @Override
  public void setDefaultFromEmail(String defaultFromEmail) {
    m_defaultFromEmail = defaultFromEmail;
  }

  @Override
  public String getDebugReceiverEmail() {
    return m_debugReceiverEmail;
  }

  /**
   * this method is used for debug reasons. If the address is set and not an
   * empty string all emails sent through this service will be sent only to the
   * debug email address.<br>
   * To override the debug email in the config.ini file use the property
   * <code>org.eclipse.scout.rt.server.services.common.smtp.AbstractSMTPService#debugReceiverEmail</code>
   * 
   * @param emailAddress
   *          a valid email address
   *          <p>
   *          Supports ${...} variables resolved by {@link BundleContextUtility#resolve(String)}
   */
  public void setDebugReceiverEmail(String emailAddress) {
    m_debugReceiverEmail = BundleContextUtility.resolve(emailAddress);
  }

  protected String getProtocol() {
    return isUseSmtps() ? "smtps" : "smtp";
  }

  @Override
  public boolean isUseSmtps() {
    return m_useSmtps;
  }

  @Override
  public void setUseSmtps(boolean useSmtps) {
    m_useSmtps = useSmtps;
  }

  @Override
  public String getSslProtocols() {
    return m_sslProtocols;
  }

  @Override
  public void setSslProtocols(String sslProtocols) {
    m_sslProtocols = sslProtocols;
  }

  @Override
  public void sendMessage(MimeMessage message) throws ProcessingException {
    sendMessage(message, null);
  }

  @Override
  public void sendMessage(MimeMessage message, Session session) throws ProcessingException {
    Transport transport = null;
    try {
      if (session == null) {
        session = createSession();
      }
      transport = session.getTransport(getProtocol());
      if (!StringUtility.isNullOrEmpty(getUsername())) {
        if (StringUtility.isNullOrEmpty(getHost())) {
          transport.connect(System.getProperty("mail." + getProtocol() + ".host"), getUsername(), getPassword());
        }
        else {
          transport.connect(getHost(), getUsername(), getPassword());
        }
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
        allRecipients = new Address[]{new InternetAddress(debugReceiverEmail)};
        LOG.debug("SMTP Service: debug receiver email set to: " + debugReceiverEmail);
      }
      // from address
      Address[] fromAddresses = message.getFrom();
      if (fromAddresses == null || fromAddresses.length == 0) {
        String defaultFromEmail = getDefaultFromEmail();
        if (!StringUtility.isNullOrEmpty(defaultFromEmail)) {
          message.setFrom(new InternetAddress(defaultFromEmail));
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
