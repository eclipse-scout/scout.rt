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
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.Serializable;
import java.security.Principal;

/**
 * Principal representing a service tunnel remote call impersonation
 */
public class ServiceTunnelPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private String m_name;

  public ServiceTunnelPrincipal(String name) {
    if (name == null) {
      throw new IllegalArgumentException("name is null");
    }
    m_name = name.toLowerCase();
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
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (other.getClass() != this.getClass()) {
      return false;
    }
    return ((ServiceTunnelPrincipal) other).m_name.equals(this.m_name);
  }

  @Override
  public String toString() {
    return getName();
  }
}
