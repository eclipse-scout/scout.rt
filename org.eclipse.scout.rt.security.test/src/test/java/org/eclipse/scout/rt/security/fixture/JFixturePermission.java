/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.security.fixture;

import java.security.Permission;

public class JFixturePermission extends Permission {
  private static final long serialVersionUID = 1L;

  public JFixturePermission() {
    super("J");
  }

  @Override
  public boolean implies(Permission permission) {
    return equals(permission);
  }

  @Override
  public String getActions() {
    return "";
  }

  @Override
  public int hashCode() {
    return 7;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && getClass() == obj.getClass();
  }
}
