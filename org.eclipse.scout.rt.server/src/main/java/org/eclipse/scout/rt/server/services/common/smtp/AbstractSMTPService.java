/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.smtp;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.eclipse.scout.rt.mail.MailHelper;
import org.eclipse.scout.rt.mail.smtp.SmtpHelper;
import org.eclipse.scout.rt.mail.smtp.SmtpHelper.SmtpDebugReceiverEmailProperty;
import org.eclipse.scout.rt.mail.smtp.SmtpServerConfig;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpDefaultFromEmailProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpHostProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpPasswordProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpPortProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpSslProtocolsProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpSubjectPrefixProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUseAuthenticationProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUseSmtpsProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SmtpUsernameProperty;

/**
 * @deprecated See {@link ISMTPService}.
 */
@SuppressWarnings("deprecation")
@Deprecated
public abstract class AbstractSMTPService implements ISMTPService {

  private final String m_subjectPrefix;
  private final String m_defaultFromEmail;
  private final SmtpServerConfig m_smtpServerConfig;

  public AbstractSMTPService() {
    m_subjectPrefix = getPropertyValue(SmtpSubjectPrefixProperty.class, getConfiguredSubjectPrefix());
    m_defaultFromEmail = getPropertyValue(SmtpDefaultFromEmailProperty.class, getConfiguredDefaultFromEmail());

    m_smtpServerConfig = BEANS.get(SmtpServerConfig.class)
        .withHost(getPropertyValue(SmtpHostProperty.class, getConfiguredHost()))
        .withPort(getPropertyValue(SmtpPortProperty.class, getConfiguredPort()))
        .withUsername(getPropertyValue(SmtpUsernameProperty.class, getConfiguredUsername()))
        .withPassword(getPropertyValue(SmtpPasswordProperty.class, getConfiguredPassword()))
        .withUseAuthentication(getPropertyValue(SmtpUseAuthenticationProperty.class, getConfiguredUseAuthentication()))
        .withSslProtocols(getPropertyValue(SmtpSslProtocolsProperty.class, getConfiguredSslProtocols()))
        .withUseSmtps(getPropertyValue(SmtpUseSmtpsProperty.class, getConfiguredUseSmtps()));
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

  public SmtpServerConfig getSmtpServerConfig() {
    return m_smtpServerConfig;
  }

  @Override
  public String getHost() {
    return getSmtpServerConfig().getHost();
  }

  @Override
  public int getPort() {
    return getSmtpServerConfig().getPort();
  }

  @Override
  public boolean isUseAuthentication() {
    return getSmtpServerConfig().isUseAuthentication();
  }

  @Override
  public String getUsername() {
    return getSmtpServerConfig().getUsername();
  }

  @Override
  public String getPassword() {
    return getSmtpServerConfig().getPassword();
  }

  @Override
  public String getSubjectPrefix() {
    return m_subjectPrefix;
  }

  @Override
  public String getDefaultFromEmail() {
    return m_defaultFromEmail;
  }

  @Override
  public String getDebugReceiverEmail() {
    return CONFIG.getPropertyValue(SmtpDebugReceiverEmailProperty.class);
  }

  public String getProtocol() {
    return isUseSmtps() ? "smtps" : "smtp";
  }

  @Override
  public boolean isUseSmtps() {
    return getSmtpServerConfig().isUseSmtps();
  }

  @Override
  public String getSslProtocols() {
    return getSmtpServerConfig().getSslProtocols();
  }

  @Override
  public void sendMessage(MimeMessage message) {
    sendMessage(message, null);
  }

  @Override
  public void sendMessage(MimeMessage message, Session session) {
    MailHelper mailHelper = BEANS.get(MailHelper.class);
    mailHelper.addPrefixToSubject(message, getSubjectPrefix());
    mailHelper.ensureFromAddress(message, getDefaultFromEmail());

    if (session == null) {
      BEANS.get(SmtpHelper.class).sendMessage(getSmtpServerConfig(), message);
    }
    else {
      BEANS.get(SmtpHelper.class).sendMessage(session, getPassword(), message);
    }
  }
}
