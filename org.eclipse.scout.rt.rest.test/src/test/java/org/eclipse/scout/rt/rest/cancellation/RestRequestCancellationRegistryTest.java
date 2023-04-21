/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.holders.ObjectHolder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RestRequestCancellationRegistryTest {

  private RestRequestCancellationRegistry m_registry;
  private RunMonitor m_runMonitor;

  @Before
  public void before() {
    m_registry = new RestRequestCancellationRegistry();
    m_runMonitor = BEANS.get(RunMonitor.class);
  }

  @Test
  public void testRegisterInvalid() {
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, null, null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register("1", null, null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, "1", null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, null, m_runMonitor));

    m_registry.register("1", null, m_runMonitor);
  }

  @Test
  public void testCancel() {
    assertFalse(m_registry.cancel(null, null));
    assertFalse(m_registry.cancel("1", null));
  }

  @Test
  public void testRegisterAndCancel() {
    assertFalse(m_runMonitor.isCancelled());
    assertNotNull(m_registry.register("1", null, m_runMonitor));

    assertTrue(m_registry.cancel("1", null));
    assertTrue(m_runMonitor.isCancelled());
  }

  @Test
  public void testRegisterSameRequestIdMultipleTimes() {
    assertFalse(m_runMonitor.isCancelled());
    assertNotNull(m_registry.register("1", null, m_runMonitor));
    assertNull(m_registry.register("1", "alice", m_runMonitor));
  }

  @Test
  public void testCancelWithDifferentUser() {
    assertFalse(m_runMonitor.isCancelled());
    assertNotNull(m_registry.register("1", "alice", m_runMonitor));

    Assert.assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", null));
    assertFalse(m_runMonitor.isCancelled());

    Assert.assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", "eve"));
    assertFalse(m_runMonitor.isCancelled());

    assertTrue(m_registry.cancel("1", "alice"));
    assertTrue(m_runMonitor.isCancelled());

    RunMonitor otherRunMonitor = BEANS.get(RunMonitor.class);
    assertFalse(otherRunMonitor.isCancelled());
    assertNotNull(m_registry.register("2", null, otherRunMonitor));

    assertTrue(m_registry.cancel("2", null));
    assertTrue(otherRunMonitor.isCancelled());

    otherRunMonitor = BEANS.get(RunMonitor.class);
    assertFalse(otherRunMonitor.isCancelled());
    assertNotNull(m_registry.register("3", null, otherRunMonitor));

    assertTrue(m_registry.cancel("3", "bob"));
    assertTrue(otherRunMonitor.isCancelled());
  }

  @Test
  public void testRegistrationHandle() {
    assertFalse(m_runMonitor.isCancelled());
    IRegistrationHandle handle = m_registry.register("1", null, m_runMonitor);
    assertNotNull(handle);

    handle.dispose();
    assertFalse(m_registry.cancel("1", null));
    assertFalse(m_runMonitor.isCancelled());

    assertNotNull(m_registry.register("1", null, m_runMonitor));
    assertTrue(m_registry.cancel("1", null));
    assertTrue(m_runMonitor.isCancelled());
  }

  @Test
  public void testCancellationInfoNotExistsHandler() {
    assertFalse(m_runMonitor.isCancelled());

    StringHolder requestIdHolder = new StringHolder();
    ObjectHolder userIdHolder = new ObjectHolder();
    final String expectedRequestId = "requestId";
    final String expectedUserId = "userId";

    // handler that returns false;
    assertFalse(m_registry.cancel(expectedRequestId, expectedUserId, (requestId, userId) -> {
      requestIdHolder.setValue(requestId);
      userIdHolder.setValue(userId);
      return false;
    }));
    assertEquals(expectedRequestId, requestIdHolder.getValue());
    assertEquals(expectedUserId, userIdHolder.getValue());

    // handler that returns true;
    assertTrue(m_registry.cancel(expectedRequestId, expectedUserId, (requestId, userId) -> {
      requestIdHolder.setValue(requestId);
      userIdHolder.setValue(userId);
      return true;
    }));

    assertEquals(expectedRequestId, requestIdHolder.getValue());
    assertEquals(expectedUserId, userIdHolder.getValue());

    assertFalse(m_runMonitor.isCancelled());
  }
}
