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

import org.eclipse.scout.rt.security.PermissionLevel;

public final class TestPermissionLevels {

  private TestPermissionLevels() {
  }

  public static final int LEVEL_GRANTED = 52;
  public static final int LEVEL_DENIED = 53;

  public static final PermissionLevel GRANTED = PermissionLevel.register(LEVEL_GRANTED, "GRANTED", true, () -> "GRANTED");
  public static final PermissionLevel DENIED = PermissionLevel.register(LEVEL_DENIED, "DENIED", true, () -> "DENIED");
}
