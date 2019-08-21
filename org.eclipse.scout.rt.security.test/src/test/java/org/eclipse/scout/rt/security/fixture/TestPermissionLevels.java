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

import org.eclipse.scout.rt.security.PermissionLevel;

public final class TestPermissionLevels {

  private TestPermissionLevels() {
  }

  public static final int LEVEL_GRANTED = 52;
  public static final int LEVEL_DENIED = 53;

  public static final PermissionLevel GRANTED = PermissionLevel.register(LEVEL_GRANTED, "GRANTED", true, () -> "GRANTED");
  public static final PermissionLevel DENIED = PermissionLevel.register(LEVEL_DENIED, "DENIED", true, () -> "DENIED");

}
