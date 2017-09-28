/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.security;

import java.security.Permission;
import java.util.regex.Pattern;

/**
 * Permission to grant remote access to a service interface from gui to server.
 * <p>
 * Checked at central access control location in scout server's BusinessOperationDispatcher.
 * <p>
 * Use this permission together with the application specific AccessControlService.
 */
public class RemoteServiceAccessPermission extends Permission {
  private static final long serialVersionUID = 1L;

  private transient Pattern m_pattern;

  /**
   * Permission granting access to remote service call
   * <p>
   * pattern may contain multiple * as wildcards
   */
  public RemoteServiceAccessPermission(String interfaceTypeName, String methodName) {
    super(interfaceTypeName.replace('$', '.') + "#" + methodName);
  }

  @Override
  public boolean implies(Permission p) {
    if ((p == null) || (p.getClass() != getClass())) {
      return false;
    }
    if (m_pattern == null) {
      m_pattern = Pattern.compile(this.getName().replace(".", "[.]").replace("*", ".*"));
    }
    RemoteServiceAccessPermission other = (RemoteServiceAccessPermission) p;
    return m_pattern.matcher(other.getName()).matches();
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return ((Permission) obj).getName().equals(this.getName());
  }

  @Override
  public String getActions() {
    return null;
  }

}
