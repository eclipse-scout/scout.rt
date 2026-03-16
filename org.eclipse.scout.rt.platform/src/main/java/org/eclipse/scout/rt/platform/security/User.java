/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;

@Bean
public class User implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  public static final ThreadLocal<User> CURRENT = new ThreadLocal<>();

  protected static final String USER_ID = "userId";

  private Map<String, Serializable> m_data = new HashMap<>();
  private volatile boolean m_readOnly;

  /**
   * {@link User} which is currently associated to the current {@link RunContext}.
   */
  public static User current() {
    return CURRENT.get();
  }

  /**
   * {@code userId} which is currently associated to the current {@link RunContext}.
   */
  public static String currentUserId() {
    return current() != null ? current().getUserId() : null;
  }

  public String getUserId() {
    return getData(USER_ID);
  }

  public User withUserId(String userId) {
    setData(USER_ID, userId);
    return this;
  }

  protected <T> T getData(String property) {
    //noinspection unchecked
    return (T) m_data.get(property);
  }

  protected void setData(String property, Serializable value) {
    if (value == null) {
      return;
    }
    if (m_readOnly) {
      throw new IllegalStateException("trying to set property " + property + " on a read-only user");
    }
    m_data.put(property, value);
  }

  public boolean isReadOnly() {
    return m_readOnly;
  }

  public User setReadOnly() {
    m_readOnly = true;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;
    return m_data.equals(user.m_data);
  }

  @Override
  public int hashCode() {
    return m_data.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [data=" + m_data + "]";
  }
}
