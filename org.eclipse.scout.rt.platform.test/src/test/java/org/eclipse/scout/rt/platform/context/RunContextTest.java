/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(PlatformTestRunner.class)
public class RunContextTest {

  private static final ThreadLocal<String> TESTEE = new ThreadLocal<>();

  @After
  public void after() {
    NlsLocale.CURRENT.remove();
    RunMonitor.CURRENT.remove();
  }

  @Test
  public void testEmpty() {
    RunContext runContext = RunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getLocale());
    assertTrue(toSet(runContext.getPropertyMap().iterator()).isEmpty());
    assertNotNull(runContext.getRunMonitor());
    assertEquals(TransactionScope.REQUIRED, runContext.getTransactionScope());
  }

  @Test
  public void testCopy() {
    RunContext runContext = RunContexts.empty();
    runContext.getPropertyMap().put("A", "B");
    runContext.withSubject(new Subject());
    runContext.withLocale(Locale.CANADA_FRENCH);
    runContext.withRunMonitor(new RunMonitor());

    RunContext copy = runContext.copy();

    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertSame(runContext.getRunMonitor(), copy.getRunMonitor());
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
    assertSame(subject, runContext.getSubject());

    runContext = Subject.doAs(null, new PrivilegedAction<RunContext>() {

      @Override
      public RunContext run() {
        return RunContexts.copyCurrent();
      }
    });
    assertNull(runContext.getSubject());
  }

  @Test
  public void testCopyCurrent_PropertyMap() {
    PropertyMap propertyMap = new PropertyMap();
    propertyMap.put("prop", "value");

    // No context on ThreadLocal
    PropertyMap.CURRENT.remove();
    assertNotNull(RunContexts.copyCurrent());
    assertTrue(toSet(RunContexts.copyCurrent().getPropertyMap().iterator()).isEmpty());

    // Context on ThreadLocal
    PropertyMap.CURRENT.set(propertyMap);
    assertNotSame(propertyMap, RunContexts.copyCurrent().getPropertyMap()); // test for copy
    assertEquals(toSet(propertyMap.iterator()), toSet(RunContexts.copyCurrent().getPropertyMap().iterator()));
  }

  @Test
  public void testCopyCurrent_Locale() {
    NlsLocale.CURRENT.remove();
    assertNull(RunContexts.copyCurrent().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.CANADA_FRENCH, RunContexts.copyCurrent().getLocale());

    NlsLocale.CURRENT.set(Locale.CANADA_FRENCH);
    assertEquals(Locale.KOREAN, RunContexts.copyCurrent().withLocale(Locale.KOREAN).getLocale());
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
  public void testCopyCurrent_RunMonitor1() {
    RunMonitor.CURRENT.remove();

    RunContext runContext = RunContexts.copyCurrent();
    assertNotNull(runContext.getRunMonitor());

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertNotNull(RunMonitor.CURRENT.get());
      }
    });

    assertNull(RunMonitor.CURRENT.get());
    assertNotNull(runContext.getRunMonitor());
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
  public void testCopyCurrent_RunMonitor2() {
    RunMonitor.CURRENT.remove();

    final RunMonitor explicitRunMonitor = new RunMonitor();
    RunContext runContext = RunContexts.copyCurrent();

    runContext.withRunMonitor(explicitRunMonitor); // explicit RunMonitor
    assertSame(explicitRunMonitor, runContext.getRunMonitor());

    assertSame(explicitRunMonitor, runContext.getRunMonitor());

    runContext.run(new IRunnable() {

      @Override
      public void run() throws Exception {
        assertSame(explicitRunMonitor, RunMonitor.CURRENT.get());
      }
    });

    assertNull(RunMonitor.CURRENT.get());
    assertSame(explicitRunMonitor, runContext.getRunMonitor());
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
  public void testCopyCurrent_RunMonitor3() {
    final RunMonitor currentRunMonitor = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor);

    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.getRunMonitor();

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
    assertSame(newRunMonitor, runContext.getRunMonitor());
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
  public void testCopyCurrent_RunMonitor4() {
    final RunMonitor currentRunMonitor = new RunMonitor();
    final RunMonitor explicitRunMonitor = new RunMonitor();

    RunMonitor.CURRENT.set(currentRunMonitor);
    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor defaultRunMonitor = runContext.getRunMonitor();

    runContext.withRunMonitor(explicitRunMonitor); // explicit RunMonitor
    assertSame(explicitRunMonitor, runContext.getRunMonitor());

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
    assertSame(explicitRunMonitor, runContext.getRunMonitor());
    assertTrue("No deregistration of default RunMonitor", currentRunMonitor.getCancellables().contains(defaultRunMonitor));
    assertFalse("Explicit RunMonitor should NOT be automatically registered within current RunMonitor (by default)", currentRunMonitor.getCancellables().contains(explicitRunMonitor));
  }

  @Test(expected = AssertionException.class)
  public void testCopyCurrentWithNullRunMonitor() {
    RunContexts.copyCurrent().withRunMonitor(null);
  }

  @Test(expected = AssertionException.class)
  public void testEmptyWithNullRunMonitor() {
    RunContexts.empty().withRunMonitor(null);
  }

  @Test
  public void testCurrentTransactionScope() {
    assertEquals(TransactionScope.REQUIRED, RunContexts.copyCurrent().getTransactionScope());
  }

  @Test
  public void testTransactionMember_TxRequiresNew() {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("abc");
    when(txMember.needsCommit()).thenReturn(true);
    when(txMember.commitPhase1()).thenReturn(true);

    RunContexts.empty()
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withTransactionMember(txMember)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            assertSame(txMember, ITransaction.CURRENT.get().getMember("abc"));
          }
        });

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, times(1)).commitPhase1();
    inOrder.verify(txMember, times(1)).commitPhase2();
    inOrder.verify(txMember, never()).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @Test(expected = AssertionException.class)
  public void testTransactionMember_TxRequired_TxPresent() {
    final ITransactionMember txMember = mock(ITransactionMember.class);

    RunContexts.empty()
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            RunContexts.copyCurrent()
                .withTransactionScope(TransactionScope.REQUIRED)
                .withTransactionMember(txMember)
                .run(mock(IRunnable.class));
          }
        });
  }

  @Test
  public void testTransactionMember_TxRequired_TxNotPresent() {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("abc");
    when(txMember.needsCommit()).thenReturn(true);
    when(txMember.commitPhase1()).thenReturn(true);

    RunContexts.empty()
        .withTransactionScope(TransactionScope.REQUIRED)
        .withTransactionMember(txMember)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            assertSame(txMember, ITransaction.CURRENT.get().getMember("abc"));
          }
        });

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, times(1)).commitPhase1();
    inOrder.verify(txMember, times(1)).commitPhase2();
    inOrder.verify(txMember, never()).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @Test
  public void testThreadLocal() {
    RunContexts.empty()
        .withThreadLocal(TESTEE, "some value")
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            assertEquals("some value", TESTEE.get());
          }
        });
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }
}
