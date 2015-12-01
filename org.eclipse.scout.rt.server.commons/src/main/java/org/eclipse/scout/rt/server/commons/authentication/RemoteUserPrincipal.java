/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.Serializable;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Principal representing a {@link HttpServletRequest#getRemoteUser()}.
 *
 * @since 5.1
 */
public class RemoteUserPrincipal implements Principal, Serializable {

  private static final long serialVersionUID = 1L;

  private final String m_name;

  public RemoteUserPrincipal(final String name) {
    m_name = Assertions.assertNotNull(name, "name must not be null").toLowerCase();
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public int hashCode() {
    return m_name.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }
    if (other.getClass() != this.getClass()) {
      return false;
    }
    return ((RemoteUserPrincipal) other).m_name.equals(this.m_name);
  }

  @Override
  public String toString() {
    return getName();
  }
}
