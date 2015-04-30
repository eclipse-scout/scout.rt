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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.commons.ICallable;
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
    final RunMonitor currentMonitor = new RunMonitor();
    final RunMonitor explicitMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentMonitor);
    new RunMonitorCallable<>(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        return null;
      }
    }, explicitMonitor).call();
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndExplicitMonitor() throws Exception {
    final RunMonitor explicitMonitor = new RunMonitor();

    RunMonitor.CURRENT.remove();
    new RunMonitorCallable<>(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        assertSame(explicitMonitor, RunMonitor.CURRENT.get());
        return null;
      }
    }, explicitMonitor).call();
    assertNull(RunMonitor.CURRENT.get());
  }

  @Test
  public void testCurrentAndNoExplicitMonitor() throws Exception {
    final RunMonitor currentMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentMonitor);
    new RunMonitorCallable<>(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        assertSame(currentMonitor, RunMonitor.CURRENT.get());
        return null;
      }
    }, null).call();
    assertSame(currentMonitor, RunMonitor.CURRENT.get());
  }

  @Test
  public void testNoCurrentAndNoExplicitMonitor() throws Exception {
    RunMonitor.CURRENT.remove();
    new RunMonitorCallable<>(new ICallable<Void>() {

      @Override
      public Void call() throws Exception {
        assertNotNull(RunMonitor.CURRENT.get());
        return null;
      }
    }, null).call();
    assertNull(RunMonitor.CURRENT.get());
  }
}
