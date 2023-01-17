/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
