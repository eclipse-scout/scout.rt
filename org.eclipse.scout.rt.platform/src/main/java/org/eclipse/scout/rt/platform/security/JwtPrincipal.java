/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.security;

import java.security.AccessController;

import javax.security.auth.Subject;

/**
 * JSON web token principal
 *
 * @since 10.0
 */
public class JwtPrincipal extends SimplePrincipal {

  private static final long serialVersionUID = 1L;
  private final String m_jwtTokenString;

  /**
   * @param name
   *          is the username or userId
   * @param jwtTokenString
   *          saml index or oicd token string
   */
  public JwtPrincipal(String name, String jwtTokenString) {
    super(name);
    m_jwtTokenString = jwtTokenString;
  }

  /**
   * @return String based representation of a JSON web token, saml index or oicd token string
   */
  public String getJwtTokenString() {
    return m_jwtTokenString;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_jwtTokenString == null) ? 0 : m_jwtTokenString.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    JwtPrincipal other = (JwtPrincipal) obj;
    if (m_jwtTokenString == null) {
      return other.m_jwtTokenString == null;
    }
    else return m_jwtTokenString.equals(other.m_jwtTokenString);
  }

  /**
   * @return the JSON web token for the current subject if it contains a {@link JwtPrincipal}
   */
  public static String jwtTokenStringOfCurrentSubject() {
    return jwtTokenString(Subject.getSubject(AccessController.getContext()));
  }

  /**
   * @param subject
   *          is optional
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
}
