/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.holders.ObjectHolder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RestRequestCancellationRegistryTest {

  private RestRequestCancellationRegistry m_registry;
  private RunContext m_runContext;

  @Before
  public void before() {
    m_registry = new RestRequestCancellationRegistry();
    m_runContext = BEANS.get(RunContext.class).withRunMonitor(BEANS.get(RunMonitor.class));
  }

  @Test
  public void testRegisterInvalid() {
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, null, null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register("1", null, null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, "1", null));
    Assert.assertThrows(AssertionException.class, () -> m_registry.register(null, null, m_runContext));

    m_registry.register("1", null, m_runContext);
  }

  @Test
  public void testCancel() {
    assertFalse(m_registry.cancel(null, null));
    assertFalse(m_registry.cancel("1", null));
  }

  @Test
  public void testRegisterAndCancel() {
    assertFalse(m_runContext.getRunMonitor().isCancelled());
    m_registry.register("1", null, m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));

    assertTrue(m_registry.cancel("1", null));
    assertTrue(m_runContext.getRunMonitor().isCancelled());
  }

  @Test
  public void testRegisterSameRequestIdMultipleTimes() {
    assertFalse(m_runContext.getRunMonitor().isCancelled());
    m_registry.register("1", null, m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));

    // register duplicate request, will be ignored
    m_registry.register("1", "alice", m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));
    assertNull(m_registry.getRequestCancellationInfos().get("1").getUserId());
  }

  @Test
  public void testCancelWithDifferentUser() {
    assertFalse(m_runContext.getRunMonitor().isCancelled());
    m_registry.register("1", "alice", m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));

    Assert.assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", null));
    assertFalse(m_runContext.getRunMonitor().isCancelled());

    Assert.assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", "eve"));
    assertFalse(m_runContext.getRunMonitor().isCancelled());

    assertTrue(m_registry.cancel("1", "alice"));
    assertTrue(m_runContext.getRunMonitor().isCancelled());

    RunContext otherRunContext = BEANS.get(RunContext.class);
    assertFalse(otherRunContext.getRunMonitor().isCancelled());
    m_registry.register("2", null, otherRunContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("2"));

    assertTrue(m_registry.cancel("2", null));
    assertTrue(otherRunContext.getRunMonitor().isCancelled());

    otherRunContext = BEANS.get(RunContext.class);
    assertFalse(otherRunContext.getRunMonitor().isCancelled());
    m_registry.register("3", null, otherRunContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("3"));

    assertTrue(m_registry.cancel("3", "bob"));
    assertTrue(otherRunContext.getRunMonitor().isCancelled());
  }

  @Test
  public void testRegistrationHandle() {
    assertFalse(m_runContext.getRunMonitor().isCancelled());
    m_registry.register("1", null, m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));

    m_registry.unregister("1");
    assertFalse(m_registry.cancel("1", null));
    assertFalse(m_runContext.getRunMonitor().isCancelled());

    m_registry.register("1", null, m_runContext);
    assertNotNull(m_registry.getRequestCancellationInfos().get("1"));
    RunContexts.empty().withRunMonitor(m_runContext.getRunMonitor()).run(() -> {
      assertTrue(m_registry.cancel("1", null));
      assertTrue(m_runContext.getRunMonitor().isCancelled());

      // reset interruption state on dispose
      assertTrue(Thread.currentThread().isInterrupted());
      m_registry.unregister("1");
      assertFalse(Thread.currentThread().isInterrupted());
    });
  }

  @Test
  public void testCancellationInfoNotExistsHandler() {
    assertFalse(m_runContext.getRunMonitor().isCancelled());

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

    assertFalse(m_runContext.getRunMonitor().isCancelled());
  }
}
