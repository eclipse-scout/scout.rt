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

public class SimplePrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private final String m_name;

  public SimplePrincipal(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name must not be null");
    }
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    String myFullName = getName();
    String otherFullName = ((Principal) other).getName();
    return myFullName.equals(otherFullName);
  }

  @Override
  public String toString() {
    return getName();
  }
}
