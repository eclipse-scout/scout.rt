package org.eclipse.scout.rt.server;

import java.security.Principal;

public class VirtualSessionIdPrincipal implements Principal {

  private String m_virtualSessionId;

  public VirtualSessionIdPrincipal(String virtualSessionId) {
    if (virtualSessionId == null || virtualSessionId.length() == 0) {
      throw new IllegalArgumentException("webSessionId must not be null or empty");
    }
    m_virtualSessionId = virtualSessionId;
  }

  @Override
  public String getName() {
    return m_virtualSessionId;
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
    if (!(other instanceof VirtualSessionIdPrincipal)) {
      return false;
    }
    else {
      String myFullName = getName();
      String otherFullName = ((VirtualSessionIdPrincipal) other).getName();
      return myFullName.equals(otherFullName);
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}
