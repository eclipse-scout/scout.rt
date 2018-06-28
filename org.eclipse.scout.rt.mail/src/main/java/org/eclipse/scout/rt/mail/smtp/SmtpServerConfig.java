/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mail.smtp;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Configuration object for SMTP server used by
 * {@link SmtpHelper#sendMessage(SmtpServerConfig, javax.mail.internet.MimeMessage)}.
 */
@Bean
public class SmtpServerConfig {

  private String m_host;
  private Integer m_port;
  private String m_username;
  private String m_password;

  private boolean m_useAuthentication;
  private boolean m_useSmtps;
  private boolean m_useStartTls;
  private String m_sslProtocols;

  public String getHost() {
    return m_host;
  }

  /**
   * @param host
   *          SMTP server host name.
   */
  public SmtpServerConfig withHost(String host) {
    m_host = host;
    return this;
  }

  public Integer getPort() {
    return m_port;
  }

  /**
   * @param port
   *          The port to connect to the server.
   */
  public SmtpServerConfig withPort(Integer port) {
    m_port = port;
    return this;
  }

  public String getUsername() {
    return m_username;
  }

  /**
   * @param username
   *          SMTP server username.
   */
  public SmtpServerConfig withUsername(String username) {
    m_username = username;
    return this;
  }

  public String getPassword() {
    return m_password;
  }

  /**
   * @param password
   *          SMTP server password.
   */
  public SmtpServerConfig withPassword(String password) {
    m_password = password;
    return this;
  }

  public boolean isUseAuthentication() {
    return m_useAuthentication;
  }

  /**
   * A {@link #getUsername()} must be set, otherwise this setting has no effect.
   *
   * @param useAuthentication
   *          If <code>true</code>, attempt to authenticate the user using the AUTH command.
   */
  public SmtpServerConfig withUseAuthentication(boolean useAuthentication) {
    m_useAuthentication = useAuthentication;
    return this;
  }

  public boolean isUseSmtps() {
    return m_useSmtps;
  }

  /**
   * @param useSmtps
   *          Specifies if a secure connection should be used.
   */
  public SmtpServerConfig withUseSmtps(boolean useSmtps) {
    m_useSmtps = useSmtps;
    return this;
  }

  public boolean isUseStartTls() {
    return m_useStartTls;
  }

  /**
   * @param useStartTls
   *          Enables STARTTLS support.
   */
  public SmtpServerConfig withUseStartTls(boolean useStartTls) {
    m_useStartTls = useStartTls;
    return this;
  }

  public String getSslProtocols() {
    return m_sslProtocols;
  }

  /**
   * @param sslProtocols
   *          Specifies the SSL protocols that will be enabled for SSL connections. The property value is a whitespace
   *          separated list of tokens acceptable to the {@link javax.net.ssl.SSLSocket#setEnabledProtocols(String[])}
   *          method.
   */
  public SmtpServerConfig withSslProtocols(String sslProtocols) {
    m_sslProtocols = sslProtocols;
    return this;
  }
}
