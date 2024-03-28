/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.server.services.common.security.PermissionsInvalidationNotificationListener.PermissionsUiNotificationTransactionMember;
import org.junit.Test;

public class PermissionsInvalidationNotificationListenerTest {
  @Test
  public void testComputeReloadDelayWindow() {
    var member = new PermissionsUiNotificationTransactionMember();
    // test lower bounds
    assertEquals(0, member.computeReloadDelayWindow(0, 30));

    assertEquals(4, member.computeReloadDelayWindow(100, 30));
    assertEquals(20, member.computeReloadDelayWindow(100, 5));

    // test upper bounds
    assertEquals(60, member.computeReloadDelayWindow(1000, 5));
  }
}
