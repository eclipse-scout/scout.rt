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

import org.eclipse.scout.rt.security.AbstractPermission;
import org.eclipse.scout.rt.security.IPermission;
import org.eclipse.scout.rt.security.PermissionLevel;

public abstract class AbstractFixturePermission extends AbstractPermission {
  private static final long serialVersionUID = 1L;

  public AbstractFixturePermission(String name) {
    super(name);
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
