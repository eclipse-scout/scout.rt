/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.context;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.transaction.BasicTransaction;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@RunWith(PlatformTestRunner.class)
public class RunContextTest {
  private static final Logger LOG = LoggerFactory.getLogger(RunContextTest.class);
  private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

  @Test
  public void testEmpty() {
    RunContext runContext = RunContexts.empty();
    assertNull(runContext.getSubject());
    assertNull(runContext.getLocale());
    assertTrue(toSet(runContext.getPropertyMap().iterator()).isEmpty());
    assertNull(runContext.getCorrelationId());
    //noinspection deprecation
    assertNull(runContext.getTransaction());
    assertEquals(TransactionScope.REQUIRED, runContext.getTransactionScope());

    assertNotNull(runContext.getRunMonitor());
    RunContexts.empty().run(() -> {
      RunMonitor currentMonitor = RunMonitor.CURRENT.get();
      RunMonitor newMonitor = RunContexts.empty().getRunMonitor();
      assertNotSame(currentMonitor, newMonitor);
      assertFalse(currentMonitor.getCancellables().contains(newMonitor));
    });
  }

  @Test
  public void testCopy() {
    final Subject subject = newSubject("john");
    final ITransaction tx = mock(ITransaction.class);
    final ITransactionMember txMember1 = mock(ITransactionMember.class);
    when(txMember1.getMemberId()).thenReturn("txMember1");
    final ITransactionMember txMember2 = mock(ITransactionMember.class);
    when(txMember2.getMemberId()).thenReturn("txMember2");
    final RunMonitor monitor = new RunMonitor();

    // prepare the RunContext to be copied
    RunContext runContext = RunContexts.empty()
        .withProperty("key", "value")
        .withSubject(subject)
        .withLocale(Locale.CANADA_FRENCH)
        .withRunMonitor(monitor)
        .withCorrelationId("cid")
        .withTransaction(tx)
        .withTransactionScope(TransactionScope.MANDATORY)
        .withThreadLocal(THREAD_LOCAL, "thread-local");

    // test
    RunContext copy = runContext.copy();

    // verify
    assertEquals(toSet(runContext.getPropertyMap().iterator()), toSet(copy.getPropertyMap().iterator()));
    assertSame(runContext.getSubject(), copy.getSubject());
    assertSame(runContext.getLocale(), copy.getLocale());
    assertSame(runContext.getRunMonitor(), copy.getRunMonitor());
    assertEquals("cid", runContext.getCorrelationId());
    //noinspection deprecation
    assertSame(tx, runContext.getTransaction());
    assertEquals("thread-local", runContext.getThreadLocal(THREAD_LOCAL));

    // test running on the copy
    copy
        .withTransactionScope(TransactionScope.REQUIRED)
        .run(() -> {
          assertEquals("value", PropertyMap.CURRENT.get().get("key"));
          assertSame(subject, Subject.getSubject(AccessController.getContext()));
          assertEquals(Locale.CANADA_FRENCH, NlsLocale.CURRENT.get());
          assertSame(monitor, RunMonitor.CURRENT.get());
          assertEquals("cid", CorrelationId.CURRENT.get());
          assertSame(tx, ITransaction.CURRENT.get());
          assertEquals("thread-local", THREAD_LOCAL.get());

          RunContexts.copyCurrent().run(() -> {
            assertEquals("value", PropertyMap.CURRENT.get().get("key"));
            assertSame(subject, Subject.getSubject(AccessController.getContext()));
            assertEquals(Locale.CANADA_FRENCH, NlsLocale.CURRENT.get());
            assertNotSame(monitor, RunMonitor.CURRENT.get());
            assertEquals("cid", CorrelationId.CURRENT.get());
            assertSame(tx, ITransaction.CURRENT.get());
            assertEquals("thread-local", THREAD_LOCAL.get());
          });

          RunContexts.empty().run(() -> {
            assertNull(PropertyMap.CURRENT.get().get("key"));
            assertNull(Subject.getSubject(AccessController.getContext()));
            assertNull(NlsLocale.CURRENT.get());
            assertNotSame(monitor, RunMonitor.CURRENT.get());
            assertNull(CorrelationId.CURRENT.get());
            assertNotSame(tx, ITransaction.CURRENT.get());
            assertEquals("thread-local", THREAD_LOCAL.get());
          });
        });

    // test copy of transaction members
    runContext
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withTransactionMember(txMember1)
        .withTransactionMember(txMember2)
        .copy()
        .run(() -> {
          assertSame(txMember1, ITransaction.CURRENT.get().getMember("txMember1"));
          assertSame(txMember2, ITransaction.CURRENT.get().getMember("txMember2"));
        });
  }

  @Test
  public void testCopySubject() {
    RunContexts.empty().run(() -> {
      assertNull(RunContexts.copyCurrent().getSubject());
      assertNull(Subject.getSubject(AccessController.getContext()));
    });

    final Subject john = newSubject("john");
    RunContexts.empty().withSubject(john).run(() -> {
      assertSame(john, RunContexts.copyCurrent().getSubject());
      assertSame(Subject.getSubject(AccessController.getContext()), RunContexts.copyCurrent().getSubject());

      // Change Subject directly
      final Subject anna = newSubject("anna");
      Subject.doAs(anna, (PrivilegedExceptionAction<Void>) () -> {
        // Test copy via 'RunContexts.copyCurrent'
        assertSame(anna, RunContexts.copyCurrent().getSubject());
        RunContexts.copyCurrent().run(() -> {
          assertSame(anna, RunContexts.copyCurrent().getSubject());
          assertSame(anna, Subject.getSubject(AccessController.getContext()));
        });

        RunContext.CURRENT.get().run(() -> {
          assertSame(john, RunContexts.copyCurrent().getSubject());
          assertSame(john, Subject.getSubject(AccessController.getContext()));
        });

        // Test copy via direct 'RunContext.copy'
        assertEquals(john, RunContext.CURRENT.get().copy().getSubject());
        Jobs.schedule(() -> {
          assertSame(john, RunContexts.copyCurrent().getSubject());
          assertSame(john, Subject.getSubject(AccessController.getContext()));
        }, Jobs.newInput()
            .withRunContext(RunContext.CURRENT.get().copy()))
            .awaitDoneAndGet();
        return null;
      });
    });

    RunContexts.empty().withSubject(null).run(() -> {
      assertNull(RunContexts.copyCurrent().getSubject());
      assertNull(Subject.getSubject(AccessController.getContext()));
    });
  }

  @Test
  public void testCopyCurrent_PropertyMap() {
    RunContexts.empty().run(() -> {
      assertNotNull(RunContexts.copyCurrent());
      assertTrue(toSet(RunContexts.copyCurrent().getPropertyMap().iterator()).isEmpty());
    });
    RunContexts.empty().withProperties(Collections.singletonMap("prop", "value")).run(() -> {
      assertNotSame(PropertyMap.CURRENT.get(), RunContexts.copyCurrent().getPropertyMap()); // test for copy
      assertEquals(toSet(PropertyMap.CURRENT.get().iterator()), toSet(RunContexts.copyCurrent().getPropertyMap().iterator()));
    });
  }

  @Test
  public void testCopyLocale() {
    RunContexts.empty().withLocale(null).run(() -> {
      assertNull(RunContexts.copyCurrent().getLocale());
      assertNull(NlsLocale.CURRENT.get());
    });

    RunContexts.empty().withLocale(Locale.CANADA_FRENCH).run(() -> {
      assertEquals(Locale.CANADA_FRENCH, NlsLocale.CURRENT.get());
      assertEquals(Locale.CANADA_FRENCH, RunContexts.copyCurrent().getLocale());
      assertEquals(Locale.KOREAN, RunContexts.copyCurrent().withLocale(Locale.KOREAN).getLocale());

      // Change Locale directly via 'thread-local'
      NlsLocale.CURRENT.set(Locale.ITALY);
      assertEquals(Locale.ITALY, RunContexts.copyCurrent().getLocale());
      RunContexts.copyCurrent().run(() -> assertEquals(Locale.ITALY, NlsLocale.get()));

      // Test copy via 'RunContexts.copyCurrent'
      Jobs.schedule(() -> assertEquals(Locale.ITALY, NlsLocale.get()), Jobs.newInput()
          .withRunContext(RunContexts.copyCurrent()))
          .awaitDoneAndGet();

      // Test copy via direct 'RunContext.copy'
      assertEquals(Locale.CANADA_FRENCH, RunContext.CURRENT.get().copy().getLocale());
      Jobs.schedule(() -> {
        assertEquals(Locale.CANADA_FRENCH, NlsLocale.get());
        assertEquals(Locale.CANADA_FRENCH, RunContexts.copyCurrent().getLocale());
      }, Jobs.newInput()
          .withRunContext(RunContext.CURRENT.get().copy()))
          .awaitDoneAndGet();

    });
  }

  @Test
  public void testCopyRunMonitor() {
    RunContexts.empty().run(() -> {
      final RunMonitor currentMonitor = RunMonitor.CURRENT.get();
      assertNotNull(currentMonitor);

      // Test new run monitor and no registration as child monitor
      RunMonitor childMonitor = RunContexts.copyCurrent().getRunMonitor();
      assertNotSame(currentMonitor, childMonitor);
      assertFalse(currentMonitor.getCancellables().contains(childMonitor));

      // Test specific monitor and no registration as child monitor
      final RunMonitor specificRunMonitor = BEANS.get(RunMonitor.class);
      assertSame(specificRunMonitor, RunContexts.copyCurrent().withRunMonitor(specificRunMonitor).getRunMonitor());
      assertFalse(currentMonitor.getCancellables().contains(RunContexts.copyCurrent().withRunMonitor(specificRunMonitor).getRunMonitor()));
      RunContexts.copyCurrent().withRunMonitor(specificRunMonitor).run(() -> assertSame(specificRunMonitor, RunMonitor.CURRENT.get()));

      // Test new run monitor and no registration as child monitor (empty)
      RunMonitor detachedMonitor = RunContexts.empty().getRunMonitor();
      assertNotSame(currentMonitor, detachedMonitor);
      assertFalse(currentMonitor.getCancellables().contains(detachedMonitor));

      // Change monitor directly via 'thread-local'
      final RunMonitor newMonitor = BEANS.get(RunMonitor.class);
      RunMonitor.CURRENT.set(newMonitor);

      // Test copy via 'RunContexts.copyCurrent'
      assertNotSame(newMonitor, RunContexts.copyCurrent().getRunMonitor());
      assertNotSame(currentMonitor, RunContexts.copyCurrent().getRunMonitor());
      assertFalse(newMonitor.getCancellables().contains(RunContexts.copyCurrent().getRunMonitor()));
      Jobs.schedule(() -> {
        assertNotNull(RunMonitor.CURRENT.get());
        assertNotSame(newMonitor, RunMonitor.CURRENT.get());
        assertNotSame(currentMonitor, RunMonitor.CURRENT.get());
        assertTrue(newMonitor.getCancellables().contains(RunMonitor.CURRENT.get()));
      }, Jobs.newInput()
          .withRunContext(RunContexts.copyCurrent()))
          .awaitDoneAndGet();

      // Test copy via direct 'RunContext.copy'
      assertSame(currentMonitor, RunContext.CURRENT.get().copy().getRunMonitor());
      Jobs.schedule(() -> assertSame(currentMonitor, RunMonitor.CURRENT.get()), Jobs.newInput()
          .withRunContext(RunContext.CURRENT.get().copy()))
          .awaitDoneAndGet();

    });
  }

  @Test(expected = AssertionException.class)
  public void testCopyCurrent_NullRunMonitor() {
    RunContexts.copyCurrent().withRunMonitor(null);
  }

  @Test(expected = AssertionException.class)
  public void testEmpty_NullRunMonitor() {
    RunContexts.empty().withRunMonitor(null);
  }

  @Test
  public void testCopyCurrent_TransactionScope() {
    RunContexts.empty().run(() -> assertEquals(TransactionScope.REQUIRED, RunContexts.copyCurrent().getTransactionScope()));
    RunContexts.empty().withTransactionScope(TransactionScope.REQUIRES_NEW).run(() -> assertEquals(TransactionScope.REQUIRED, RunContexts.copyCurrent().getTransactionScope()));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testCopyCurrent_Transaction() {
    final ITransaction tx = mock(ITransaction.class);

    RunContexts.empty().withTransaction(tx).run(() -> assertEquals(tx, RunContexts.copyCurrent().getTransaction()));

    RunContexts.empty().withTransaction(null).run(() -> {
      assertNotNull(ITransaction.CURRENT.get());
      assertNotNull(RunContexts.copyCurrent().getTransaction());
      assertSame(RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());

      RunContexts.copyCurrent().run(() -> {
        assertNotNull(ITransaction.CURRENT.get());
        assertSame(RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
      });
    });

    RunContexts.empty().withTransaction(tx).run(() -> {
      assertSame(tx, ITransaction.CURRENT.get());
      assertSame(tx, RunContexts.copyCurrent().getTransaction());
      assertSame(tx, RunContext.CURRENT.get().getTransaction());

      RunContexts.copyCurrent().run(() -> {
        assertSame(tx, ITransaction.CURRENT.get());
        assertSame(tx, RunContexts.copyCurrent().getTransaction());
        assertSame(tx, RunContext.CURRENT.get().getTransaction());
      });
    });
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testCopyCurrent_newTransactionSupplier() {
    final ITransaction tx = mock(ITransaction.class);
    final Supplier<ITransaction> txSupplier = () -> tx;

    RunContexts.empty().withNewTransactionSupplier(txSupplier).run(() -> {
      assertEquals(txSupplier, RunContext.CURRENT.get().getNewTransactionSupplier());
      assertNull(RunContexts.copyCurrent().getNewTransactionSupplier());
    });

    RunContexts.empty().withNewTransactionSupplier(null).run(() -> {
      assertNotNull(ITransaction.CURRENT.get());
      assertNotNull(RunContexts.copyCurrent().getTransaction());
      assertNull(RunContext.CURRENT.get().getNewTransactionSupplier());

      RunContexts.copyCurrent().run(() -> assertNotNull(ITransaction.CURRENT.get()));
    });

    RunContexts.empty().withNewTransactionSupplier(txSupplier).run(() -> {
      assertSame(tx, ITransaction.CURRENT.get());
      assertSame(tx, RunContexts.copyCurrent().getTransaction());
      assertSame(txSupplier, RunContext.CURRENT.get().getNewTransactionSupplier());

      RunContexts.copyCurrent().run(() -> {
        assertSame(tx, ITransaction.CURRENT.get());
        assertSame(tx, RunContexts.copyCurrent().getTransaction());
        assertNull(RunContext.CURRENT.get().getNewTransactionSupplier());
      });
    });
  }

  @Test
  public void testCopyCurrent_TransactionMember() {
    final ITransactionMember txMember = mock(ITransactionMember.class);
    when(txMember.getMemberId()).thenReturn("txMember");

    RunContexts.empty().withTransactionMember(txMember).run(() -> {
      final ITransaction tx = ITransaction.CURRENT.get();
      assertNotNull(tx);
      assertSame(txMember, ITransaction.CURRENT.get().getMember("txMember"));

      RunContexts.copyCurrent()
          .withTransactionScope(TransactionScope.REQUIRED)
          .run(() -> {
            assertSame(tx, ITransaction.CURRENT.get());
            assertSame(txMember, ITransaction.CURRENT.get().getMember("txMember"));
          });
      RunContexts.copyCurrent()
          .withTransactionScope(TransactionScope.REQUIRES_NEW)
          .run(() -> {
            assertNotSame(tx, ITransaction.CURRENT.get());
            assertNull(ITransaction.CURRENT.get().getMember("txMember"));
          });
      RunContexts.copyCurrent()
          .withTransactionScope(TransactionScope.MANDATORY)
          .run(() -> {
            assertSame(tx, ITransaction.CURRENT.get());
            assertSame(txMember, ITransaction.CURRENT.get().getMember("txMember"));
          });
    });
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
        .run(() -> assertSame(txMember, ITransaction.CURRENT.get().getMember("abc")));

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
        .run(() -> RunContexts.copyCurrent()
            .withTransactionScope(TransactionScope.REQUIRED)
            .withTransactionMember(txMember)
            .run(mock(IRunnable.class)));
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
        .run(() -> assertSame(txMember, ITransaction.CURRENT.get().getMember("abc")));

    InOrder inOrder = Mockito.inOrder(txMember);
    inOrder.verify(txMember, times(1)).commitPhase1();
    inOrder.verify(txMember, times(1)).commitPhase2();
    inOrder.verify(txMember, never()).rollback();
    inOrder.verify(txMember, times(1)).release();
  }

  @Test
  public void testThreadLocal() {
    RunContexts.empty()
        .withThreadLocal(THREAD_LOCAL, "initial_value")
        .run(() -> assertEquals("initial_value", THREAD_LOCAL.get()));
  }

  @Test
  public void testCopyThreadLocal() {
    RunContexts.empty()
        .withThreadLocal(THREAD_LOCAL, "initial_value")
        .run(() -> {
          assertEquals("initial_value", THREAD_LOCAL.get());

          // Change ThreadLocal directly
          THREAD_LOCAL.set("updated_value");

          RunContexts.copyCurrent().run(() -> assertEquals("updated_value", THREAD_LOCAL.get()));

          // Test copy via copyCurrent
          Jobs.schedule(() -> assertEquals("updated_value", THREAD_LOCAL.get()), Jobs.newInput()
              .withRunContext(RunContexts.copyCurrent()))
              .awaitDoneAndGet();

          // Test copy directly
          assertEquals("initial_value", RunContext.CURRENT.get().copy().getThreadLocal(THREAD_LOCAL));
          Jobs.schedule(() -> {
            assertEquals("initial_value", THREAD_LOCAL.get());
            assertEquals("initial_value", RunContext.CURRENT.get().copy().getThreadLocal(THREAD_LOCAL));
          }, Jobs.newInput()
              .withRunContext(RunContext.CURRENT.get().copy()))
              .awaitDoneAndGet();
        });
  }

  @Test
  public void testCurrentRunContext() {
    final RunContext runContext = RunContexts.empty();

    runContext.run(() -> assertSame(runContext, RunContext.CURRENT.get()));
  }

  @Test
  public void testHardCancellation() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    final AtomicBoolean interrupted = new AtomicBoolean();
    final RunMonitor monitor = BEANS.get(RunMonitor.class);

    Jobs.schedule(() -> RunContexts.empty()
        .withRunMonitor(monitor)
        .run(() -> {
          try {
            assertTrue(setupLatch.countDownAndBlock(10, TimeUnit.SECONDS));
          }
          catch (InterruptedException e) {
            interrupted.set(true);
          }
          finally {
            verifyLatch.countDown();
          }
        }), Jobs.newInput());

    assertTrue(setupLatch.await());
    monitor.cancel(true);
    assertTrue(verifyLatch.await());
    assertTrue(interrupted.get());
  }

  @Test
  public void testCopyCurrentOrElseEmpty() {
    Jobs.schedule(() -> {
      try {
        RunContexts.copyCurrent();
        fail("AssertionException expected because not running in a RunContext");
      }
      catch (AssertionException e) {
        // expected
      }

      try {
        RunContexts.copyCurrent(false);
        fail("AssertionException expected because not running in a RunContext");
      }
      catch (AssertionException e) {
        // expected
      }

      assertNotNull(RunContexts.copyCurrent(true));
    }, Jobs.newInput()
        .withRunContext(null))
        .awaitDoneAndGet();
  }

  @Test
  public void testDiagnostics() {
    final ThreadLocal<String> diagnosticThreadLocal1 = new ThreadLocal<>();
    final ThreadLocal<String> diagnosticThreadLocal2 = new ThreadLocal<>();

    IDiagnosticContextValueProvider diagnostic1 = mock(IDiagnosticContextValueProvider.class);
    when(diagnostic1.key()).thenReturn("test-key-1");
    when(diagnostic1.value()).thenAnswer((Answer<String>) invocation -> diagnosticThreadLocal1.get());

    IDiagnosticContextValueProvider diagnostic2 = mock(IDiagnosticContextValueProvider.class);
    when(diagnostic2.key()).thenReturn("test-key-2");
    when(diagnostic2.value()).thenAnswer((Answer<String>) invocation -> diagnosticThreadLocal2.get());

    RunContexts.empty()
        .withDiagnostic(diagnostic1)
        .withDiagnostic(diagnostic2)
        .withThreadLocal(diagnosticThreadLocal1, "value-1")
        .withThreadLocal(diagnosticThreadLocal2, "value-2")
        .run(() -> {
          assertEquals("value-1", MDC.get("test-key-1"));
          assertEquals("value-2", MDC.get("test-key-2"));

          RunContexts.copyCurrent()
              .withThreadLocal(diagnosticThreadLocal2, "ABC")
              .run(() -> {
                assertEquals("value-1", MDC.get("test-key-1"));
                assertEquals("ABC", MDC.get("test-key-2"));
              });

          Jobs.schedule(() -> {
            assertEquals("value-1", MDC.get("test-key-1"));
            assertEquals("value-2", MDC.get("test-key-2"));
          }, Jobs.newInput()
              .withRunContext(RunContext.CURRENT.get()))
              .awaitDoneAndGet();
        });
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>Current RunMonitor available</li>
   * <li>Explicit RunMonitor not set</li>
   * <li>Runnable throws an Exception</li>
   * </ul>
   * Expected: a new RunMonitor is CREATED, registered as child and set as CURRENT for the time of execution.
   */
  @Test
  public void testCopyCurrent_RunMonitorException() {
    final RunMonitor currentRunMonitor = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor);

    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.getRunMonitor();

    assertNotSame("RunMonitor should not be same instance a current RunMonitor", currentRunMonitor, newRunMonitor);

    final Exception exception = new Exception();
    try {
      runContext.run(() -> {
        throw exception;
      });
    }
    catch (PlatformException e) {
      assertEquals(exception, e.getCause());
    }

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());
    assertSame(newRunMonitor, runContext.getRunMonitor());
    assertFalse("RunMonitor should not be registered within current RunMonitor anymore", currentRunMonitor.getCancellables().contains(newRunMonitor));
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>Current RunMonitor available</li>
   * <li>Explicit RunMonitor not set</li>
   * <li>Additional cancellables are registered as new child which are not removed until some time after completion of
   * run context</li>
   * </ul>
   * Expected: a new RunMonitor is CREATED, registered as child and set as CURRENT for the time of execution until the
   * last cancellable is removed.
   */
  @Test
  public void testCopyCurrent_RunMonitorLeftoverCancellables() {
    final RunMonitor currentRunMonitor = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor);

    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.getRunMonitor();

    assertNotSame("Run monitor should not be same instance a current run monitor", currentRunMonitor, newRunMonitor);

    class LocalCancellable implements ICancellable {

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean cancel(boolean interruptIfRunning) {
        return true;
      }
    }
    final ICancellable c1 = new LocalCancellable();
    final ICancellable c2 = new LocalCancellable();

    runContext.run(() -> {
      assertSame(newRunMonitor, RunMonitor.CURRENT.get());
      newRunMonitor.registerCancellable(c1);
      newRunMonitor.registerCancellable(c2);
      assertTrue("Run monitor should be registered within current run monitor", currentRunMonitor.getCancellables().contains(RunMonitor.CURRENT.get()));
    });

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());
    assertSame(newRunMonitor, runContext.getRunMonitor());
    assertEquals(2, newRunMonitor.getCancellables().size());
    assertTrue("New run monitor should be registered within current run monitor as long a cancellables remain", currentRunMonitor.getCancellables().contains(newRunMonitor));

    // Remove cancellable 1/2
    newRunMonitor.unregisterCancellable(c1);
    assertEquals(1, newRunMonitor.getCancellables().size());
    assertTrue("New run monitor should be registered within current run monitor as long a cancellables remain", currentRunMonitor.getCancellables().contains(newRunMonitor));

    // Remove cancellable 2/2
    newRunMonitor.unregisterCancellable(c2);
    assertEquals(0, newRunMonitor.getCancellables().size());
    assertFalse("New run monitor should not be registered within current run monitor anymore", currentRunMonitor.getCancellables().contains(newRunMonitor));
  }

  /**
   * Tests RunMonitor functionality with:
   * <ul>
   * <li>Different parent current run monitors available</li>
   * <li>Explicit run monitor not set (e.g. new one created)</li>
   * <li>Additional cancellables are registered as new child which are not removed until some time after completion of
   * run context</li>
   * </ul>
   * Expected: One new run monitor is <b>created</b>, registered as child and set as <b>current</b> for the time of
   * execution until the last cancellable is removed.
   */
  @Test
  public void testCopyCurrent_RunMonitorLeftoverMultipleParent() {
    final RunMonitor currentRunMonitor1 = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor1);

    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.getRunMonitor();

    assertNotSame("Run monitor should not be same instance a current run monitor", currentRunMonitor1, newRunMonitor);

    class LocalCancellable implements ICancellable {

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean cancel(boolean interruptIfRunning) {
        return true;
      }
    }
    final ICancellable c1 = new LocalCancellable();
    final ICancellable c2 = new LocalCancellable();

    runContext.run(() -> {
      assertSame(newRunMonitor, RunMonitor.CURRENT.get());
      newRunMonitor.registerCancellable(c1);
      assertTrue("Run monitor should be registered within current run monitor", currentRunMonitor1.getCancellables().contains(RunMonitor.CURRENT.get()));
    });

    // set new current run monitor
    assertSame(currentRunMonitor1, RunMonitor.CURRENT.get());
    final RunMonitor currentRunMonitor2 = new RunMonitor();
    RunMonitor.CURRENT.set(currentRunMonitor2);

    runContext.withParentRunMonitor(currentRunMonitor2).run(() -> {
      assertSame(newRunMonitor, RunMonitor.CURRENT.get());
      newRunMonitor.registerCancellable(c2);
      assertTrue("Run monitor should be registered within current run monitor", currentRunMonitor1.getCancellables().contains(RunMonitor.CURRENT.get()));
    });

    assertSame(currentRunMonitor2, RunMonitor.CURRENT.get());
    assertSame(newRunMonitor, runContext.getRunMonitor());
    assertEquals(2, newRunMonitor.getCancellables().size());

    assertTrue("New run monitor should be registered within parent run monitor as long a cancellables remain", currentRunMonitor1.getCancellables().contains(newRunMonitor));
    assertTrue("New run monitor should be registered within parent run monitor as long a cancellables remain", currentRunMonitor2.getCancellables().contains(newRunMonitor));

    // Remove cancellable 1/2
    newRunMonitor.unregisterCancellable(c1);
    assertTrue("New run monitor should be registered within parent run monitor as long a cancellables remain", currentRunMonitor1.getCancellables().contains(newRunMonitor));
    assertTrue("New run monitor should be registered within parent run monitor as long a cancellables remain", currentRunMonitor2.getCancellables().contains(newRunMonitor));

    // Remove cancellable 2/2
    newRunMonitor.unregisterCancellable(c2);
    assertFalse("New run monitor should not be registered within parent run monitor anymore", currentRunMonitor1.getCancellables().contains(newRunMonitor));
    assertFalse("New run monitor should not be registered within parent run monitor anymore", currentRunMonitor2.getCancellables().contains(newRunMonitor));
  }

  /**
   * <p>
   * Tests RunMonitor functionality with:
   * </p>
   * <ul>
   * <li>Current RunMonitor available</li>
   * <li>Explicit RunMonitor not set</li>
   * <li>Run context is used to start two {@link Jobs} in new threads</li>
   * </ul>
   * <p>
   * Expected: exactly one new RunMonitor is created, registered as child and set as current for the time of execution,
   * must be removed after the last job finishes.
   * </p>
   */
  @Test
  public void testCopyCurrent_RunMonitorTwoConcurrentJobs() {
    final RunMonitorEx currentRunMonitor = new RunMonitorEx();
    RunMonitor.CURRENT.set(currentRunMonitor);
    RunContext runContext = RunContexts.copyCurrent();
    final RunMonitor newRunMonitor = runContext.getRunMonitor();
    assertNotNull(newRunMonitor);
    assertNotSame(currentRunMonitor, newRunMonitor);
    assertSame(currentRunMonitor, runContext.getParentRunMonitor());

    final IBlockingCondition startedCondition1 = Jobs.newBlockingCondition(true);
    final IBlockingCondition startedCondition2 = Jobs.newBlockingCondition(true);
    final IBlockingCondition blockingCondition1 = Jobs.newBlockingCondition(true);
    final IBlockingCondition blockingCondition2 = Jobs.newBlockingCondition(true);

    assertFalse("New run monitor must not be registered with current run monitor", currentRunMonitor.containsCancellable(newRunMonitor));

    IFuture<Void> future1 = Jobs.schedule(() -> {
      startedCondition1.setBlocking(false);
      blockingCondition1.waitFor();
      assertSame(newRunMonitor, RunMonitor.CURRENT.get());
      assertTrue("New run monitor must be registered with current run monitor", currentRunMonitor.containsCancellable(newRunMonitor));
    }, Jobs.newInput().withRunContext(runContext));

    IFuture<Void> future2 = Jobs.schedule(() -> {
      startedCondition2.setBlocking(false);
      blockingCondition2.waitFor();
      assertSame(newRunMonitor, RunMonitor.CURRENT.get());
      assertTrue("New run monitor must be registered with current run monitor", currentRunMonitor.containsCancellable(newRunMonitor));
    }, Jobs.newInput().withRunContext(runContext));

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());

    // Wait for jobs to be started
    startedCondition1.waitFor();
    startedCondition2.waitFor();

    // Stop latter job first
    blockingCondition2.setBlocking(false);
    future2.awaitDoneAndGet();

    // Stop first job
    blockingCondition1.setBlocking(false);
    future1.awaitDoneAndGet();

    assertSame(currentRunMonitor, RunMonitor.CURRENT.get());
    assertFalse("New run monitor must not be registered with current run monitor", currentRunMonitor.containsCancellable(newRunMonitor));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testNestedTransactions() {
    AtomicInteger txSeq = new AtomicInteger();
    Supplier<ITransaction> newTx = () -> new BasicTransaction() {
      int id = txSeq.incrementAndGet();

      @Override
      public String toString() {
        return "tx" + id;
      }
    };
    List<String> expected = new ArrayList<>();
    List<String> actual = new ArrayList<>();

    RunContext r1 = RunContexts.empty()
        .withNewTransactionSupplier(newTx)
        .withTransactionScope(TransactionScope.REQUIRES_NEW);
    r1.run(() -> {
      LOG.info("1a {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
      expected.add("1a tx1");
      actual.add("1a " + ITransaction.CURRENT.get());

      RunContext r2 = RunContexts.copyCurrent()
          .withNewTransactionSupplier(newTx)
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
      r2.run(() -> {
        LOG.info("2a {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
        expected.add("2a tx2");
        actual.add("2a " + ITransaction.CURRENT.get());

        RunContext r3 = RunContexts.copyCurrent()
            .withNewTransactionSupplier(newTx)
            .withTransactionScope(TransactionScope.REQUIRES_NEW);
        r3.run(() -> {
          LOG.info("3a {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
          expected.add("3a tx3");
          actual.add("3a " + ITransaction.CURRENT.get());
        });

        r3 = RunContexts.copyCurrent()
            .withNewTransactionSupplier(newTx)
            .withTransactionScope(TransactionScope.REQUIRED);
        r3.run(() -> {
          LOG.info("3b {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
          expected.add("3b tx2");
          actual.add("3b " + ITransaction.CURRENT.get());
        });

        r3 = RunContext.CURRENT.get().copy()// using copy() instead of RunContexts.copyCurrent()
            .withNewTransactionSupplier(newTx)
            .withTransactionScope(TransactionScope.REQUIRED);
        r3.run(() -> {
          LOG.info("3c {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
          expected.add("3c tx2");
          actual.add("3c " + ITransaction.CURRENT.get());
        });

        LOG.info("2b {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
        expected.add("2b tx2");
        actual.add("2b " + ITransaction.CURRENT.get());
      });

      LOG.info("1b {} {}", RunContext.CURRENT.get().getTransaction(), ITransaction.CURRENT.get());
      expected.add("1b tx1");
      actual.add("1b " + ITransaction.CURRENT.get());
    });
    assertEquals(expected, actual);
  }

  private static Set<Object> toSet(Iterator<?> iterator) {
    Set<Object> set = new HashSet<>();
    while (iterator.hasNext()) {
      set.add(iterator.next());
    }
    return set;
  }

  private static Subject newSubject(String principal) {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(principal));
    subject.setReadOnly();
    return subject;
  }

  private static class RunMonitorEx extends RunMonitor {

    public boolean containsCancellable(ICancellable cancellable) {
      return getCancellables().contains(cancellable);
    }
  }
}
