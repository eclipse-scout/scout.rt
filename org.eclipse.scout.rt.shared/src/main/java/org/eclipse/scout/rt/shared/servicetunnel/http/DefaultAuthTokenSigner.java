/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.JwtPrincipal;
import org.eclipse.scout.rt.platform.security.SamlPrincipal;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenPrivateKeyProperty;
import org.eclipse.scout.rt.shared.SharedConfigProperties.AuthTokenTimeToLiveProperty;

/**
 * Used to sign authentication tokens and may fill default values.
 */
@ApplicationScoped
public class DefaultAuthTokenSigner {
  public static final String JWT_IDENTIFIER = "jwt";
  public static final String SAML_IDENTIFIER = "saml";

  protected long getTokenTimeToLive() {
    return CONFIG.getPropertyValue(AuthTokenTimeToLiveProperty.class);
  }

  protected byte[] getPrivateKey() {
    return CONFIG.getPropertyValue(AuthTokenPrivateKeyProperty.class);
  }

  protected String getDefaultUserId() {
    return BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
  }

  /**
   * @return true if enabled
   */
  public boolean isEnabled() {
    return getPrivateKey() != null;
  }

  /**
   * Creates new token, sets {@link DefaultAuthToken#getUserId()} to user id of current subject and signs token.
   *
   * @param tokenClazz
   *     not null
   * @return null if not enabled or no userId could be determined else serialized token
   */
  public <T extends DefaultAuthToken> T createDefaultSignedToken(Class<T> tokenClazz) {
    if (!isEnabled()) {
      return null;
    }
    String userId = getDefaultUserId();
    if (StringUtility.isNullOrEmpty(userId)) {
      return null;
    }
    T token = BEANS.get(tokenClazz);
    token.withUserId(userId);
    appendCustomArgs(token);
    return sign(token);
  }

  /**
   * Add principal specific params to the token. By default the {@link JwtPrincipal} and {@link SamlPrincipal} are
   * detected. The custom param starts with the type of principal. {@link #JWT_IDENTIFIER} or {@link #SAML_IDENTIFIER}
   * are implemented by default.
   *
   * @param token
   *     the token in creation
   * @since 10.0
   */
  protected void appendCustomArgs(DefaultAuthToken token) {
    Principal principal = selectUserPrincipal();
    if (principal instanceof JwtPrincipal) {
      JwtPrincipal jwt = (JwtPrincipal) principal;
      List<String> args = CollectionUtility.arrayList(JWT_IDENTIFIER, jwt.getJwtTokenString(), jwt.getAccessToken(), jwt.getRefreshToken(), jwt.getOid());
      token.withCustomArgs(args);
    }
    else if (principal instanceof SamlPrincipal) {
      SamlPrincipal saml = (SamlPrincipal) principal;
      token.withCustomArgs(SAML_IDENTIFIER, saml.getSessionIndex());
    }
  }

  /**
   * @return the primary principal by default is the first principal
   * @since 10.0
   */
  protected Principal selectUserPrincipal() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return null;
    }
    return subject.getPrincipals().stream()
        .findFirst()
        .orElse(null);
  }

  /**
   * Sets {@link DefaultAuthToken#getValidUntil()} and signs token.
   *
   * @param token
   *     to be signed
   * @return token for method chaining
   */
  public <T extends DefaultAuthToken> T sign(T token) {
    token.withValidUntil(System.currentTimeMillis() + getTokenTimeToLive());

    if (isEnabled()) {
      token.withSignature(signature(token));
    }
    return token;
  }

  protected byte[] signature(DefaultAuthToken token) {
    return SecurityUtility.createSignature(getPrivateKey(), token.write(false).getBytes(StandardCharsets.UTF_8));
  }
}
