/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

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
