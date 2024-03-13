/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.fixture;

import org.eclipse.scout.rt.api.data.security.PermissionId;
import org.eclipse.scout.rt.security.AbstractPermission;
import org.eclipse.scout.rt.security.IPermission;
import org.eclipse.scout.rt.security.PermissionLevel;

public abstract class AbstractFixturePermission extends AbstractPermission {
  private static final long serialVersionUID = 1L;

  public AbstractFixturePermission(PermissionId permissionId) {
    super(permissionId);
  }

  @Override
  protected boolean evalPermission(IPermission p) {
    switch (getLevel().getValue()) {
      case PermissionLevel.LEVEL_NONE:
        throw new AssertionError("Precondition violated: LEVEL NONE should be handled by #implies");
      case TestPermissionLevels.LEVEL_DENIED:
        return false;
      case PermissionLevel.LEVEL_ALL:
        return true;
      case TestPermissionLevels.LEVEL_GRANTED:
        return true;
      default:
        return super.evalPermission(p);
    }
  }
}
