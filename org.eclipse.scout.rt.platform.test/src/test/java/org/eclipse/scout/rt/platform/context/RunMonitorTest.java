/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class RunMonitorTest {

  @Mock
  private ICancellable m_cancellable1;

  @Mock
  private ICancellable m_cancellable2;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.openMocks(this).close();
  }

  @Test
  public void testCancel() {
    RunMonitor monitor = new RunMonitor();
    monitor.registerCancellable(m_cancellable1);

    monitor.cancel(false);

    assertTrue(monitor.isCancelled());
    verify(m_cancellable1).cancel(eq(false));
  }

  @Test
  public void testRegisterOnceCancelled() {
    RunMonitor monitor = new RunMonitor();
    monitor.cancel(false);

    monitor.registerCancellable(m_cancellable1);

    assertTrue(monitor.isCancelled());
    verify(m_cancellable1).cancel(eq(true));
  }

  @Test
  public void testCancelMultipleSuccess() {
    RunMonitor monitor = new RunMonitor();
    monitor.registerCancellable(m_cancellable1);
    monitor.registerCancellable(m_cancellable2);

    when(m_cancellable1.cancel(anyBoolean())).thenReturn(true);
    when(m_cancellable2.cancel(anyBoolean())).thenReturn(true);

    assertTrue(monitor.cancel(false));

    verify(m_cancellable1).cancel(eq(false));
    verify(m_cancellable2).cancel(eq(false));

    assertFalse(monitor.cancel(false)); // already cancelled
  }

  @Test
  public void testCancelMultipleFailed() {
    RunMonitor monitor = new RunMonitor();
    monitor.registerCancellable(m_cancellable1);
    monitor.registerCancellable(m_cancellable2);

    when(m_cancellable1.cancel(anyBoolean())).thenReturn(false);
    when(m_cancellable2.cancel(anyBoolean())).thenReturn(true);

    boolean success = monitor.cancel(false);
    assertFalse(success);

    verify(m_cancellable1).cancel(eq(false));
    verify(m_cancellable2).cancel(eq(false));
  }
}
