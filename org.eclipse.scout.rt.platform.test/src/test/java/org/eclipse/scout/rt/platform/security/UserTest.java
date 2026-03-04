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
import org.junit.Test;

public class UserTest {

  @Test
  public void testEmpty() {
    User user = BEANS.get(User.class);
    assertNull(user.getUserId());
  }

  @Test
  public void testUserId() {
    User user = BEANS.get(User.class).withUserId("alice");
    assertEquals("alice", user.getUserId());
    user.withUserId("bob");
    assertEquals("bob", user.getUserId());
  }

  @Test
  public void testEquals() {
    User user = BEANS.get(User.class).withUserId("alice");
    assertEquals(user, BEANS.get(User.class).withUserId("alice"));
    assertNotEquals(user, BEANS.get(User.class));
    assertNotEquals(user, BEANS.get(User.class).withUserId("bob"));
  }

  @Test
  public void testReadOnly() {
    User user = BEANS.get(User.class).withUserId("alice");
    assertFalse(user.isReadOnly());
    user.setReadOnly();
    assertTrue(user.isReadOnly());
    assertThrows(IllegalStateException.class, () -> user.withUserId("bob"));
  }

  @Test
  public void testCurrent() {
    User user = BEANS.get(User.class).withUserId("alice").setReadOnly();
    RunContexts.empty()
        .withUser(user)
        .run(() -> {
          assertEquals(user, User.current());
          assertEquals("alice", User.currentUserId());
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
