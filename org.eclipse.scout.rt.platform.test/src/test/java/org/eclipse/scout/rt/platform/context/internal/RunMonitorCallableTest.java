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
package org.eclipse.scout.rt.platform.context.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.context.ICancellable;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunMonitorCallableTest {

  @After
  public void after() {
    RunMonitor.CURRENT.remove();
  }

  @Test
  public void testCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    RunMonitor.CURRENT.set(currentMonitor);
    new RunMonitorCallable<>(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        assertTrue(currentMonitor.containsCancellable(RunMonitor.CURRENT.get()));
        return null;
      }
    }, currentMonitor, explicitMonitor).call();
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();
    final RunMonitorEx explicitMonitor = new RunMonitorEx();

    new RunMonitorCallable<>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        assertFalse(currentMonitor.containsCancellable(RunMonitor.CURRENT.get()));
        return null;
      }
    }, null, explicitMonitor).call();
    assertNull(RunMonitor.CURRENT.get());
  }

  @Test
  public void testCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();

    RunMonitor.CURRENT.set(currentMonitor);
    new RunMonitorCallable<>(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        assertTrue(currentMonitor.containsCancellable(RunMonitor.CURRENT.get()));
        return null;
      }
    }, currentMonitor, null).call();
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitorEx currentMonitor = new RunMonitorEx();

    new RunMonitorCallable<>(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        assertFalse(currentMonitor.containsCancellable(RunMonitor.CURRENT.get()));
        return null;
      }
    }, null, null).call();
    assertNull(RunMonitor.CURRENT.get());
  }

  private static class RunMonitorEx extends RunMonitor {
    public boolean containsCancellable(ICancellable c) {
      return getCancellables().contains(c);
    }
  }
}
