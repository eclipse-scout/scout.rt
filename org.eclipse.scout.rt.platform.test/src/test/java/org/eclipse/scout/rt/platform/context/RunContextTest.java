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
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.job.PropertyMap;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class RunContextTest {

  @After
  public void after() {
    NlsLocale.CURRENT.remove();
    RunMonitor.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    RunContext runContext = RunContexts.empty();
    assertNull(runContext.subject());
    assertNull(runContext.locale());
    assertTrue(toSet(runContext.propertyMap().iterator()).isEmpty());
    assertNotNull(runContext.runMonitor());
  }

  @Test
  public void testCopy() {
    RunContext runContext = RunContexts.empty();
    runContext.propertyMap().put("A", "B");
    runContext.subject(new Subject());
    runContext.locale(Locale.CANADA_FRENCH);
    runContext.runMonitor(new RunMonitor());

    RunContext copy = runContext.copy();

    assertEquals(toSet(runContext.propertyMap().iterator()), toSet(copy.propertyMap().iterator()));
    assertSame(runContext.subject(), copy.subject());
    assertSame(runContext.locale(), copy.locale());
    assertSame(runContext.runMonitor(), copy.runMonitor());
  }

  @Test
  public void testCopyCurrent_Subject() {
    Subject subject = new Subject();
    RunContext runContext = Subject.doAs(subject, new PrivilegedAction<RunContext>() {

      @Override
      public RunContext run() {
        return RunContexts.copyCurrent();
      }
    });
    assertSame(subject, runContext.subject());

    runContext = Subject.doAs(null, new PrivilegedAction<RunContext>() {

      @Override
      public RunContext run() {
        return RunContexts.copyCurrent();
      }
    });
    assertNull(runContext.subject());
  }

  @Test
  public void testCopyCurrent_PropertyMap() {
    PropertyMap propertyMap = new PropertyMap();
    propertyMap.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(RunContexts.copyCurrent());
    assertTrue(toSet(RunContexts.copyCurrent().propertyMap().iterator()).isEmpty());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, RunContexts.copyCurrent().propertyMap()); // test for copy
    assertEquals(toSet(propertyMap.iterator()), toSet(RunContexts.copyCurrent().propertyMap().iterator()));
  }

  @Test
  public void testCopyCurrent_Locale() {
    NlsLocale.CURRENT.remove();
    assertNull(RunContexts.copyCurrent().locale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, RunContexts.copyCurrent().locale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.KOREAN, RunContexts.copyCurrent().locale(Locale.KOREAN).locale());
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>No current RunMonitor available</li>
   * <li>No explicit RunMonitor set</li>
   * </ul>
   * Expected: a new RunMonitor is CREATED and set as CURRENT for the time of execution.
   */
  @Test
  public void testCopyCurrent_RunMonitor1() throws ProcessingException {
    RunMonitor.CURRENT.remove();

    RunContext runContext = RunContexts.copyCurrent();
    assertNotNull(runContext.runMonitor());

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNotNull(RunMonitor.CURRENT.get());
      }
    });

    assertNull(RunMonitor.CURRENT.get());
    assertNotNull(runContext.runMonitor());
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>No current RunMonitor available</li>
   * <li>Explicit RunMonitor set</li>
   * </ul>
   * Expected: explicit RunMonitor is set as CURRENT for the time of execution.
   */
  @Test
  public void testCopyCurrent_RunMonitor2() throws ProcessingException {
    RunMonitor.CURRENT.remove();

    final RunMonitor explicitRunMonitor = new RunMonitor();
    RunContext runContext = RunContexts.copyCurrent();

    runContext.runMonitor(explicitRunMonitor); // explicit RunMonitor
    assertSame(explicitRunMonitor, runContext.runMonitor());

    assertSame(explicitRunMonitor, runContext.runMonitor());

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(explicitRunMonitor, RunMonitor.CURRENT.get());
      }
    });

    assertNull(RunMonitor.CURRENT.get());
    assertSame(explicitRunMonitor, runContext.runMonitor());
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>Current RunMonitor available</li>
   * <li>Explicit RunMonitor not set</li>
   * </ul>
   * Expected: a new RunMonitor is CREATED, registered as child and set as CURRENT for the time of execution.
   */
  @Test
  public void testCopyCurrent_RunMonitor3() throws ProcessingException {
    final RunMonitor currentRunMonitor = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor);

    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.runMonitor();

    assertNotSame("RunMonitor should not be same instance a current RunMonitor", currentRunMonitor, newRunMonitor);
    assertTrue("RunMonitor should be registered within current RunMonitor", currentRunMonitor.getCancellables().contains(newRunMonitor));

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(newRunMonitor, RunMonitor.CURRENT.get());
        assertTrue("RunMonitor should be registered within current RunMonitor", currentRunMonitor.getCancellables().contains(RunMonitor.CURRENT.get()));
      }
    });

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());
    assertSame(newRunMonitor, runContext.runMonitor());
    assertTrue("RunMonitor should still be registered within current RunMonitor (by default)", currentRunMonitor.getCancellables().contains(newRunMonitor));
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>Current RunMonitor available</li>
   * <li>Explicit RunMonitor set</li>
   * </ul>
   * Expected: explicit RunMonitor is used as CURRENT for the time of execution, but NOT registered as child.
   */
  @Test
  public void testCopyCurrent_RunMonitor4() throws ProcessingException {
    final RunMonitor currentRunMonitor = new RunMonitor();
    final RunMonitor explicitRunMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentRunMonitor);
    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor defaultRunMonitor = runContext.runMonitor();

    runContext.runMonitor(explicitRunMonitor); // explicit RunMonitor
    assertSame(explicitRunMonitor, runContext.runMonitor());

    assertTrue("No deregistration of default RunMonitor", currentRunMonitor.getCancellables().contains(defaultRunMonitor));
    assertFalse("Explicit RunMonitor should NOT be automatically registered within current RunMonitor (by default)", currentRunMonitor.getCancellables().contains(explicitRunMonitor));

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(explicitRunMonitor, RunMonitor.CURRENT.get());
        assertTrue("No deregistration of default RunMonitor", currentRunMonitor.getCancellables().contains(defaultRunMonitor));
        assertFalse("Explicit RunMonitor should NOT be automatically registered within current RunMonitor (by default)", currentRunMonitor.getCancellables().contains(RunMonitor.CURRENT.get()));
      }
    });

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());
    assertSame(explicitRunMonitor, runContext.runMonitor());
    assertTrue("No deregistration of default RunMonitor", currentRunMonitor.getCancellables().contains(defaultRunMonitor));
    assertFalse("Explicit RunMonitor should NOT be automatically registered within current RunMonitor (by default)", currentRunMonitor.getCancellables().contains(explicitRunMonitor));
  }

  @Test(expected = AssertionError.class)
  public void testCopyCurrentWithNullRunMonitor() {
    RunContexts.copyCurrent().runMonitor(null);
  }

  @Test(expected = AssertionError.class)
  public void testEmptyWithNullRunMonitor() {
    RunContexts.empty().runMonitor(null);
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
