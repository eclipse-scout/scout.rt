package org.eclipse.scout.rt.ui.html.officeonline.wopi;

import java.io.Serializable;
import java.security.Principal;

public class WopiPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = 1L;

  private String m_name;

  public WopiPrincipal(String name) {
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
    if (!(other instanceof WopiPrincipal)) {
      return false;
    }
    else {
      return getName().equals(((WopiPrincipal) other).getName());
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}
