/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.cancellation;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
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
    assertThrows(AssertionException.class, () -> m_registry.register(null, null, null));
    assertThrows(AssertionException.class, () -> m_registry.register("1", null, null));
    assertThrows(AssertionException.class, () -> m_registry.register(null, "1", null));
    assertThrows(AssertionException.class, () -> m_registry.register(null, null, m_runMonitor));

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

    assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", null));
    assertFalse(m_runMonitor.isCancelled());

    assertThrows(AccessForbiddenException.class, () -> m_registry.cancel("1", "eve"));
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
}
