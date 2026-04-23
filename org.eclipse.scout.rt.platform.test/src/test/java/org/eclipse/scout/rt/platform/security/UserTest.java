/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.security;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class UserTest {

  protected static final String ALICE_USER_ID = "alice";
  protected static final String BOB_USER_ID = "bob";

  @Test
  public void testEmpty() {
    User user = BEANS.get(User.class);
    assertNull(user.getUserId());
  }

  @Test
  public void testUserId() {
    User user = BEANS.get(User.class).withUserId(ALICE_USER_ID);
    assertEquals(ALICE_USER_ID, user.getUserId());
    user.withUserId(BOB_USER_ID);
    assertEquals(BOB_USER_ID, user.getUserId());
  }

  @Test
  public void testEquals() {
    User user = BEANS.get(User.class).withUserId(ALICE_USER_ID);
    assertEquals(user, BEANS.get(User.class).withUserId(ALICE_USER_ID));
    assertNotEquals(user, BEANS.get(User.class));
    assertNotEquals(user, BEANS.get(User.class).withUserId(BOB_USER_ID));
  }

  @Test
  public void testReadOnly() {
    User user = BEANS.get(User.class).withUserId(ALICE_USER_ID);
    assertFalse(user.isReadOnly());
    user.setReadOnly();
    assertTrue(user.isReadOnly());
    assertThrows(IllegalStateException.class, () -> user.withUserId(BOB_USER_ID));
  }

  @Test
  public void testCurrent() {
    User user = BEANS.get(User.class).withUserId(ALICE_USER_ID).setReadOnly();
    RunContexts.empty()
        .withUser(user)
        .run(() -> {
          assertEquals(user, User.current());
          assertEquals(ALICE_USER_ID, User.currentUserId());
        });
  }

  @Test
  public void testCurrentEmpty() {
    RunContexts.empty()
        .run(() -> {
          assertNull(User.current());
          assertNull(User.currentUserId());
        });
  }
}
