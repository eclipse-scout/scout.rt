/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.Serializable;
import java.security.AccessController;
import java.security.Principal;
import java.util.Objects;

import javax.security.auth.Subject;

/**
 * JSON web token principal
 *
 * @since 10.0
 */
public class JwtPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final String m_jwtTokenString;
  private String m_accessToken;
  private String m_refreshToken;
  private String m_oid;

  /**
   * @param name
   *     is the username or userId
   * @param jwtTokenString
   *     oidc token string
   */
  public JwtPrincipal(String name, String jwtTokenString) {
    if (name == null) {
      throw new IllegalArgumentException("name must not be null");
    }
    m_name = name;
    m_jwtTokenString = jwtTokenString;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * @return String based representation of a JSON web token, resp. oidc token string
   */
  public String getJwtTokenString() {
    return m_jwtTokenString;
  }

  /**
   * @return the JSON web token for the current subject if it contains a {@link JwtPrincipal}
   */
  public static String jwtTokenStringOfCurrentSubject() {
    return jwtTokenString(Subject.getSubject(AccessController.getContext()));
  }

  /**
   * @param subject
   *     is optional
   * @return the JSON web token from the subject if it contains a {@link JwtPrincipal}
   */
  public static String jwtTokenString(Subject subject) {
    if (subject == null) {
      return null;
    }
    return subject.getPrincipals().stream()
        .filter(p -> p instanceof JwtPrincipal)
        .map(f -> ((JwtPrincipal) f).getJwtTokenString())
        .findAny()
        .orElse(null);
  }

  public void setAccessToken(String accessToken) {
    m_accessToken = accessToken;
  }

  public String getAccessToken() {
    return m_accessToken;
  }

  public void setRefreshToken(String refreshToken) {
    m_refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return m_refreshToken;
  }

  public String getOid() {
    return m_oid;
  }

  public void setOid(String oid) {
    m_oid = oid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JwtPrincipal that = (JwtPrincipal) o;
    return m_name.equals(that.m_name) &&
        Objects.equals(m_jwtTokenString, that.m_jwtTokenString) &&
        Objects.equals(m_accessToken, that.m_accessToken) &&
        Objects.equals(m_refreshToken, that.m_refreshToken) &&
        Objects.equals(m_oid, that.m_oid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_name, m_jwtTokenString, m_accessToken, m_refreshToken, m_oid);
  }
}
