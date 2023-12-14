/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mail.imap;

import java.util.Map;

import jakarta.mail.Session;

import org.eclipse.scout.rt.oauth2.OAuth2Config;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Configuration object for IMAP server used by {@link ImapHelper#connect(ImapServerConfig)}.
 */
@Bean
public class ImapServerConfig {

  private String m_host;
  private Integer m_port;
  private String m_username;
  private String m_password;

  private boolean m_useSsl;
  private String m_sslProtocols;

  private String m_customStoreProtocol;

  private OAuth2Config m_oAuth2Config;

  private Map<String, String> m_additionalSessionProperties;

  public String getHost() {
    return m_host;
  }

  /**
   * @param host
   *          SMTP server host name.
   */
  public ImapServerConfig withHost(String host) {
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
  public ImapServerConfig withPort(Integer port) {
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
  public ImapServerConfig withUsername(String username) {
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
  public ImapServerConfig withPassword(String password) {
    m_password = password;
    return this;
  }

  public boolean isUseSsl() {
    return m_useSsl;
  }

  /**
   * @param useSsl
   *          Specifies if a secure connection should be used.
   */
  public ImapServerConfig withUseSsl(boolean useSsl) {
    m_useSsl = useSsl;
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
  public ImapServerConfig withSslProtocols(String sslProtocols) {
    m_sslProtocols = sslProtocols;
    return this;
  }

  public String getCustomStoreProtocol() {
    return m_customStoreProtocol;
  }

  /**
   * For most IMAP server it's okay to always use 'imap' and enable SSL via 'mail.imap.ssl.enable', but certain IMAP
   * servers seem to required 'imaps' as protocol when retrieving store (e.g. for accessing shared mailbox from
   * Office365).
   *
   * @param customStoreProtocol
   *          Protocol use in {@link Session#getStore(String)}. If none is provided, 'imap' is used.
   */
  public ImapServerConfig withCustomStoreProtocol(String customStoreProtocol) {
    m_customStoreProtocol = customStoreProtocol;
    return this;
  }

  public ImapServerConfig withOAuth2Config(OAuth2Config oAuth2Config) {
    m_oAuth2Config = oAuth2Config;
    return this;
  }

  public OAuth2Config getAuth2Config() {
    return m_oAuth2Config;
  }

  public Map<String, String> getAdditionalSessionProperties() {
    return m_additionalSessionProperties;
  }

  /**
   * These properties are added after the other properties, thus can override predefined properties such as host, port
   * or user.
   *
   * @param additionalSessionProperties
   *          Additional properties used to create {@link Session} for IMAP server connection.
   */
  public ImapServerConfig withAdditionalSessionProperties(Map<String, String> additionalSessionProperties) {
    m_additionalSessionProperties = additionalSessionProperties;
    return this;
  }
}
