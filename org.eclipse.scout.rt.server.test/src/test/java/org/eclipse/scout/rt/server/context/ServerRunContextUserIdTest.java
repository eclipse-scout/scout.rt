/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import static org.eclipse.scout.rt.platform.util.Assertions.assertEquals;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.shared.user.UserId;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ServerTestRunner.class)
@RunWithSubject("john")
@RunWithServerSession(IServerSession.class)
public class ServerRunContextUserIdTest {

  @Test
  public void testBasic() {
    assertUserId("john");
  }

  @Test
  public void testNested() {
    ServerRunContexts.copyCurrent()
        .run(() -> assertUserId("john"));
  }

  @Test
  public void testChangingUser() {
    String otherUserId = "anna";
    ServerRunContexts.copyCurrent()
        .withThreadLocal(UserId.CURRENT, otherUserId)
        .run(() -> assertUserId(otherUserId));
  }

  @Test
  public void testChangingUserNested() {
    String otherUserId = "anna";
    ServerRunContexts.copyCurrent()
        .withThreadLocal(UserId.CURRENT, otherUserId)
        .run(() -> ServerRunContexts.copyCurrent()
            .run(() -> assertUserId(otherUserId)));
  }

  protected void assertUserId(String expectedUserId) {
    assertEquals(expectedUserId, UserId.CURRENT.get());
  }
}
