/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.oauth2;

import java.util.Objects;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public class OAuth2Config {

  private String m_clientId;
  private String m_clientSecret;
  private String m_tokenEndpoint;
  private String m_authorizationEndpoint;
  private String m_scope;

  /**
   * @param clientId
   *     Identification value for the resource registered with OAuth2. The clientId is sometimes also called AppId.
   */
  public OAuth2Config withClientId(String clientId) {
    m_clientId = clientId;
    return this;
  }

  public String getClientId() {
    return m_clientId;
  }

  /**
   * @param clientSecret
   *     Used by the client to authenticate with the token endpoint ({@link #withTokenEndpoint(String)}).
   */
  public OAuth2Config withClientSecret(String clientSecret) {
    m_clientSecret = clientSecret;
    return this;
  }

  public String getClientSecret() {
    return m_clientSecret;
  }

  /**
   * @param tokenEndpoint
   *     Server against which the client authenticates and which responds with an access token. For Exchange Online
   *     365 the token endpoint currently looks like
   *     https://login.microsoftonline.com/_my_tenant_id_/oauth2/v2.0/token
   */
  public OAuth2Config withTokenEndpoint(String tokenEndpoint) {
    m_tokenEndpoint = tokenEndpoint;
    return this;
  }

  public String getTokenEndpoint() {
    return m_tokenEndpoint;
  }

  /**
   * @param authorizationEndpoint
   *     Not used for the client credentials flow, but some implementations require it. For Exchange Online 365 the
   *     authorization endpoint currently looks like
   *     https://login.microsoftonline.com/_my_tenant_id_/oauth2/v2.0/authorize
   */
  public OAuth2Config withAuthorizationEndpoint(String authorizationEndpoint) {
    m_authorizationEndpoint = authorizationEndpoint;
    return this;
  }

  public String getAuthorizationEndpoint() {
    return m_authorizationEndpoint;
  }

  /**
   * @param scope
   *     The scope defines which attributes a client requests from the server. When using multiple scopes, separate
   *     them by a space. For Exchange Online 365 currently use: https://ps.outlook.com/.default
   */
  public OAuth2Config withScope(String scope) {
    m_scope = scope;
    return this;
  }

  public String getScope() {
    return m_scope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuth2Config that = (OAuth2Config) o;
    return Objects.equals(m_clientId, that.m_clientId)
        && Objects.equals(m_clientSecret, that.m_clientSecret)
        && Objects.equals(m_tokenEndpoint, that.m_tokenEndpoint)
        && Objects.equals(m_authorizationEndpoint, that.m_authorizationEndpoint)
        && Objects.equals(m_scope, that.m_scope);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_clientId, m_clientSecret, m_tokenEndpoint, m_authorizationEndpoint, m_scope);
  }
}
