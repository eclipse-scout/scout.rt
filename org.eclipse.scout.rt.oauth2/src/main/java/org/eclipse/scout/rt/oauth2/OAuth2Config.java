/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.oauth2;

import org.eclipse.scout.rt.platform.Bean;

@Bean
public class OAuth2Config {
  private String m_clientId;
  private String m_clientSecret;
  private String m_tokenEndpoint;
  private String m_authorizationEndpoint;
  private String m_scope;
  private String m_id;

  // TODO avd Doc
  public OAuth2Config withClientId(String clientId) {
    m_clientId = clientId;
    return this;
  }

  public String getClientId() {
    return m_clientId;
  }

  public OAuth2Config withClientSecret(String clientSecret) {
    m_clientSecret = clientSecret;
    return this;
  }

  public String getClientSecret() {
    return m_clientSecret;
  }

  public OAuth2Config withTokenEndpoint(String tokenEndpoint) {
    m_tokenEndpoint = tokenEndpoint;
    return this;
  }

  public String getTokenEndpoint() {
    return m_tokenEndpoint;
  }

  public OAuth2Config withAuthorizationEndpoint(String authorizationEndpoint) {
    m_authorizationEndpoint = authorizationEndpoint;
    return this;
  }

  public String getAuthorizationEndpoint() {
    return m_authorizationEndpoint;
  }

  public OAuth2Config withScope(String scope) {
    m_scope = scope;
    return this;
  }

  public String getScope() {
    return m_scope;
  }

  public OAuth2Config withId(String id) {
    m_id = id;
    return this;
  }

  public String getId() {
    return m_id;
  }
}
