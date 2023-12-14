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

import java.util.Map;

import jakarta.mail.Session;

import org.eclipse.scout.rt.platform.Bean;

/**
 * Configuration object for SMTP server used by
 * {@link SmtpHelper#sendMessage(SmtpServerConfig, jakarta.mail.internet.MimeMessage)}.
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

  private Map<String, String> m_additionalSessionProperties;

  private int m_poolSize = 0;
  private int m_maxMessagesPerConnection = 0;

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

  public Map<String, String> getAdditionalSessionProperties() {
    return m_additionalSessionProperties;
  }

  /**
   * These properties are added after the other properties, thus can override predefined properties such as host, port
   * or user.
   *
   * @param additionalSessionProperties
   *          Additional properties used to create {@link Session} for SMTP server connection.
   */
  public SmtpServerConfig withAdditionalSessionProperties(Map<String, String> additionalSessionProperties) {
    m_additionalSessionProperties = additionalSessionProperties;
    return this;
  }

  /**
   * @return Returns the size of the connection pool to use with this {@link SmtpServerConfig}. If 0, smtp connection
   *         pooling is disabled.
   */
  public int getPoolSize() {
    return m_poolSize;
  }

  /**
   * @param poolSize
   *          Specifies the size of the connection pool to use with this {@link SmtpServerConfig#}. If 0, smtp
   *          connection pooling is disabled.
   */
  public SmtpServerConfig withPoolSize(int poolSize) {
    m_poolSize = poolSize;
    return this;
  }

  /**
   * @return Returns the maximum number of messages to send per connection with this {@link SmtpServerConfig}. If the
   *         limit is reached the connection will not be returned to the pool to be used again. If 0, no limit will be
   *         applied on the number of messages sent per connection. Only applies when {@link #withPoolSize(int)} > 0.
   */
  public int getMaxMessagesPerConnection() {
    return m_maxMessagesPerConnection;
  }

  /**
   * @param maxMessagesPerConnection
   *          The maximum number of messages sent per connection. If the limit is reached, the connection will not be
   *          returned to the pool to be used again. If the value of this property is 0, no limit will be applied on the
   *          number of messages sent per connection. Only applies {@link #withPoolSize(int)} > 0.
   */
  public SmtpServerConfig withMaxMessagesPerConnection(int maxMessagesPerConnection) {
    m_maxMessagesPerConnection = maxMessagesPerConnection;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_additionalSessionProperties == null) ? 0 : m_additionalSessionProperties.hashCode());
    result = prime * result + ((m_host == null) ? 0 : m_host.hashCode());
    result = prime * result + m_maxMessagesPerConnection;
    result = prime * result + ((m_password == null) ? 0 : m_password.hashCode());
    result = prime * result + m_poolSize;
    result = prime * result + ((m_port == null) ? 0 : m_port.hashCode());
    result = prime * result + ((m_sslProtocols == null) ? 0 : m_sslProtocols.hashCode());
    result = prime * result + (m_useAuthentication ? 1231 : 1237);
    result = prime * result + (m_useSmtps ? 1231 : 1237);
    result = prime * result + (m_useStartTls ? 1231 : 1237);
    result = prime * result + ((m_username == null) ? 0 : m_username.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SmtpServerConfig other = (SmtpServerConfig) obj;
    if (m_additionalSessionProperties == null) {
      if (other.m_additionalSessionProperties != null) {
        return false;
      }
    }
    else if (!m_additionalSessionProperties.equals(other.m_additionalSessionProperties)) {
      return false;
    }
    if (m_host == null) {
      if (other.m_host != null) {
        return false;
      }
    }
    else if (!m_host.equals(other.m_host)) {
      return false;
    }
    if (m_maxMessagesPerConnection != other.m_maxMessagesPerConnection) {
      return false;
    }
    if (m_password == null) {
      if (other.m_password != null) {
        return false;
      }
    }
    else if (!m_password.equals(other.m_password)) {
      return false;
    }
    if (m_poolSize != other.m_poolSize) {
      return false;
    }
    if (m_port == null) {
      if (other.m_port != null) {
        return false;
      }
    }
    else if (!m_port.equals(other.m_port)) {
      return false;
    }
    if (m_sslProtocols == null) {
      if (other.m_sslProtocols != null) {
        return false;
      }
    }
    else if (!m_sslProtocols.equals(other.m_sslProtocols)) {
      return false;
    }
    if (m_useAuthentication != other.m_useAuthentication) {
      return false;
    }
    if (m_useSmtps != other.m_useSmtps) {
      return false;
    }
    if (m_useStartTls != other.m_useStartTls) {
      return false;
    }
    if (m_username == null) {
      if (other.m_username != null) {
        return false;
      }
    }
    else if (!m_username.equals(other.m_username)) {
      return false;
    }
    return true;
  }
}
