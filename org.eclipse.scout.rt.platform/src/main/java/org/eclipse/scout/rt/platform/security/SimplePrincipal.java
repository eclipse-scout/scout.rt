/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.security;

import java.io.Serializable;
import java.security.Principal;

public class SimplePrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private String m_name;

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
    String otherFullName = ((SimplePrincipal) other).getName();
    return myFullName.equals(otherFullName);
  }

  @Override
  public String toString() {
    return getName();
  }
}
