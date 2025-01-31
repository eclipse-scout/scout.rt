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
import java.security.Principal;
import java.util.Objects;

/**
 * SAML token principal
 *
 * @since 10.0
 */
public class SamlPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_name;
  private final String m_sessionIndex;

  /**
   * @param name
   *     is the username or userId
   * @param sessionIndex
   *     saml session index
   */
  public SamlPrincipal(String name, String sessionIndex) {
    if (name == null) {
      throw new IllegalArgumentException("name must not be null");
    }
    m_name = name;
    m_sessionIndex = sessionIndex;
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
   * @return the saml session index
   */
  public String getSessionIndex() {
    return m_sessionIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SamlPrincipal that = (SamlPrincipal) o;
    return m_name.equals(that.m_name) &&
        Objects.equals(m_sessionIndex, that.m_sessionIndex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_name, m_sessionIndex);
  }
}
