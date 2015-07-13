/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.junit.Test;

/**
 * Tests for {@link ClientNotificationRegistry}
 */
public class ClientNotificationRegistryTest {
  private static final String TEST_NOTIFICATION = "testNotification";

  @Test
  public void testNotificationsForAllNodes() throws Exception {
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
    reg.registerSession("testNodeId", "testSessionId", "testUserId");
    reg.putForAllNodes(TEST_NOTIFICATION);
    List<ClientNotificationMessage> consumed = reg.consume("testNodeId", 100, 100, TimeUnit.MILLISECONDS);
    assertFalse(consumed.isEmpty());
    assertEquals(TEST_NOTIFICATION, consumed.get(0).getNotification());
  }

}
