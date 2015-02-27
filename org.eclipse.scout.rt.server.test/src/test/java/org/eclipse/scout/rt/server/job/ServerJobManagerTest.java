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
package org.eclipse.scout.rt.server.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
import org.eclipse.scout.rt.testing.commons.UncaughtExceptionRunnable;
import org.eclipse.scout.rt.testing.platform.ScoutPlatformTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(ScoutPlatformTestRunner.class)
public class ServerJobManagerTest {

  private static ExecutorService s_executor;

  @Mock
  private IServerSession m_serverSession1;
  @Mock
  private IServerSession m_serverSession2;

  private ServerJobManager m_jobManager;
  private List<ITransaction> m_transactions = Collections.synchronizedList(new ArrayList<ITransaction>());

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newCachedThreadPool();
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    ISession.CURRENT.set(m_serverSession1);

    m_jobManager = new ServerJobManager() {

      @Override
      protected ITransaction createTransaction() {
        ITransaction tx = mock(ITransaction.class);
        when(tx.cancel()).thenReturn(true);
        m_transactions.add(tx);
        return tx;
      }
    };
  }

  @After
  public void after() {
    m_jobManager.shutdown();

    ISession.CURRENT.remove();
  }

  /**
   * Cancel a 'runNow-job'.
   */
  @Test
  public void testCancel1() throws Throwable {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    UncaughtExceptionRunnable runnable = new UncaughtExceptionRunnable() {

      @Override
      protected void runSafe() throws Exception {
        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("interrupted-job-1");
            }
            finally {
              verifyLatch.countDown();
            }
          }
        }, ServerJobInput.empty().id(1).session(m_serverSession1));
      }

      @Override
      protected void onUncaughtException(Throwable t) {
        setupLatch.release();
      }
    };
    s_executor.execute(runnable);

    assertTrue(setupLatch.await());
    runnable.throwOnError();

    assertEquals(1, m_transactions.size());

    // run the test
    assertTrue(m_jobManager.cancel(1, m_serverSession1));

    assertTrue(verifyLatch.await());

    // verify
    verify(m_transactions.get(0), times(1)).cancel();
    assertEquals(CollectionUtility.arrayList("interrupted-job-1"), protocol);
  }

  /**
   * Cancel a 'scheduled-job'.
   */
  @Test
  public void testCancel2() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("interrupted-job-1");
        }
        finally {
          verifyLatch.countDown();
        }
      }
    }, ServerJobInput.defaults().id(1));

    assertTrue(setupLatch.await());

    assertEquals(1, m_transactions.size());

    // run the test
    assertTrue(m_jobManager.cancel(1, m_serverSession1));

    assertTrue(verifyLatch.await());

    // verify
    verify(m_transactions.get(0), times(1)).cancel();
    assertEquals(CollectionUtility.arrayList("interrupted-job-1"), protocol);
  }

  /**
   * Cancel a 'runNow-job' that has nested jobs.
   */
  @Test
  public void testCancelCascade1() throws Throwable {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    UncaughtExceptionRunnable runnable = new UncaughtExceptionRunnable() {

      @Override
      protected void runSafe() throws Exception {
        // The outermost job is 'runNow'.
        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                final BlockingCountDownLatch latch3 = new BlockingCountDownLatch(1);
                m_jobManager.schedule(new IRunnable() {

                  @Override
                  public void run() throws Exception {
                    try {
                      latch3.countDownAndBlock();
                    }
                    catch (InterruptedException e) {
                      protocol.add("job-3-interrupted");
                    }
                  }
                }, ServerJobInput.empty().id(3).session(m_serverSession1));
                latch3.await();

                m_jobManager.runNow(new IRunnable() {

                  @Override
                  public void run() throws Exception {
                    // NOOP
                  }
                }, ServerJobInput.empty().id(4).session(m_serverSession1));

                m_jobManager.runNow(new IRunnable() {

                  @Override
                  public void run() throws Exception {
                    try {
                      setupLatch.countDownAndBlock();
                    }
                    catch (InterruptedException e) {
                      protocol.add("job-5-interrupted");
                    }

                    if (IProgressMonitor.CURRENT.get().isCancelled()) {
                      protocol.add("job-5-cancelled");
                    }
                    verifyLatch.countDown();
                  }
                }, ServerJobInput.empty().id(5).session(m_serverSession1));

                if (IProgressMonitor.CURRENT.get().isCancelled()) {
                  protocol.add("job-2-cancelled");
                }
                verifyLatch.countDown();
              }
            }, ServerJobInput.empty().id(2).session(m_serverSession2));

            if (IProgressMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-1-cancelled");
            }
            verifyLatch.countDown();
          }
        }, ServerJobInput.empty().id(1).session(m_serverSession1));
      }

      @Override
      protected void onUncaughtException(Throwable t) {
        setupLatch.release();
      }
    };
    s_executor.execute(runnable);

    assertTrue(setupLatch.await());
    runnable.throwOnError();

    assertEquals(5, m_transactions.size());

    // test cancel of inner jobs (expected=no effect)
    assertFalse(m_jobManager.cancel(5, m_serverSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.
    assertFalse(m_jobManager.cancel(4, m_serverSession1)); // already finished
    assertFalse(m_jobManager.cancel(2, m_serverSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.

    // test cancel with wrong session (expected=no effect)
    assertFalse(m_jobManager.cancel(1, m_serverSession2));
    verify(m_transactions.get(0), never()).cancel();
    verify(m_transactions.get(1), never()).cancel();
    verify(m_transactions.get(2), never()).cancel();
    verify(m_transactions.get(3), never()).cancel();
    verify(m_transactions.get(4), never()).cancel();

    // test cancel
    assertTrue(m_jobManager.cancel(1, m_serverSession1));

    assertTrue(verifyLatch.await());

    // verify
    verify(m_transactions.get(0), times(1)).cancel(); // job-1
    verify(m_transactions.get(1), times(1)).cancel(); // job-2
    verify(m_transactions.get(2), never()).cancel(); // job-3 (scheduled job)
    verify(m_transactions.get(3), never()).cancel(); // job-4: already committed
    verify(m_transactions.get(4), times(1)).cancel(); // job-5
    assertEquals(CollectionUtility.arrayList("job-5-interrupted", "job-5-cancelled", "job-2-cancelled", "job-1-cancelled"), protocol);
  }

  /**
   * Cancel a 'scheduled-job' that has nested jobs.
   */
  @Test
  public void testCancelCascade2() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    // The outermost job is scheduled.
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            final BlockingCountDownLatch latch3 = new BlockingCountDownLatch(1);
            m_jobManager.schedule(new IRunnable() {

              @Override
              public void run() throws Exception {
                try {
                  latch3.countDownAndBlock();
                }
                catch (InterruptedException e) {
                  protocol.add("job-3-interrupted");
                }
              }
            }, ServerJobInput.defaults().id(3));
            latch3.await();

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                // NOOP
              }
            }, ServerJobInput.defaults().id(4));

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                try {
                  setupLatch.countDownAndBlock();
                }
                catch (InterruptedException e) {
                  protocol.add("job-5-interrupted");
                }

                if (IProgressMonitor.CURRENT.get().isCancelled()) {
                  protocol.add("job-5-cancelled");
                }
                verifyLatch.countDown();
              }
            }, ServerJobInput.defaults().id(5));

            if (IProgressMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled");
            }
            verifyLatch.countDown();
          }
        }, ServerJobInput.defaults().id(2).session(m_serverSession2));

        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled");
        }
        verifyLatch.countDown();
      }
    }, ServerJobInput.defaults().id(1));

    assertTrue(setupLatch.await());

    assertEquals(5, m_transactions.size());

    // test cancel of inner jobs (expected=no effect)
    assertFalse(m_jobManager.cancel(5, m_serverSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.
    assertFalse(m_jobManager.cancel(4, m_serverSession1)); // already finished
    assertFalse(m_jobManager.cancel(2, m_serverSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.

    // test cancel with wrong session (expected=no effect)
    assertFalse(m_jobManager.cancel(1, m_serverSession2));
    verify(m_transactions.get(0), never()).cancel();
    verify(m_transactions.get(1), never()).cancel();
    verify(m_transactions.get(2), never()).cancel();
    verify(m_transactions.get(3), never()).cancel();
    verify(m_transactions.get(4), never()).cancel();

    // test cancel
    assertTrue(m_jobManager.cancel(1, m_serverSession1));

    assertTrue(verifyLatch.await());

    // verify
    verify(m_transactions.get(0), times(1)).cancel(); // job-1
    verify(m_transactions.get(1), times(1)).cancel(); // job-2
    verify(m_transactions.get(2), never()).cancel(); // job-3 (scheduled job)
    verify(m_transactions.get(3), never()).cancel(); // job-4: already committed
    verify(m_transactions.get(4), times(1)).cancel(); // job-5
    assertEquals(CollectionUtility.arrayList("job-5-interrupted", "job-5-cancelled", "job-2-cancelled", "job-1-cancelled"), protocol);
  }

  /**
   * Cancel multiple jobs with the same job-id.
   */
  @Test
  public void testCancelMultipleJobsWithSameId() throws Exception {
    final Set<String> protocol = Collections.synchronizedSet(new HashSet<String>());

    final int commonJobId = 777;

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(4);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    // Job-1
    final BlockingCountDownLatch txOrderLatch1 = new BlockingCountDownLatch(1);
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        txOrderLatch1.countDown();
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-1-interrupted");
        }
        verifyLatch.countDown();
      }
    }, ServerJobInput.defaults().id(commonJobId));
    assertTrue(txOrderLatch1.await());

    // Job-2
    final BlockingCountDownLatch txOrderLatch2 = new BlockingCountDownLatch(1);
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        txOrderLatch2.countDown();
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-2-interrupted");
        }
        verifyLatch.countDown();
      }
    }, ServerJobInput.defaults().id(commonJobId));
    assertTrue(txOrderLatch2.await());

    // Job-3
    final BlockingCountDownLatch txOrderLatch3 = new BlockingCountDownLatch(1);
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        // Job-3a
        m_jobManager.runNow(new IRunnable() {

          @Override
          public void run() throws Exception {
            txOrderLatch3.countDown();
            try {
              setupLatch.countDownAndBlock();
            }
            catch (InterruptedException e) {
              protocol.add("job-3a-interrupted");
            }
            verifyLatch.countDown();
          }
        }, ServerJobInput.defaults().id(123));
      }
    }, ServerJobInput.defaults().id(commonJobId));
    assertTrue(txOrderLatch3.await());

    // Job-4 (other session)
    final BlockingCountDownLatch txOrderLatch4 = new BlockingCountDownLatch(1);
    m_jobManager.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        txOrderLatch4.countDown();
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          protocol.add("job-4-interrupted");
        }
      }
    }, ServerJobInput.defaults().id(commonJobId).session(m_serverSession2));
    assertTrue(txOrderLatch4.await());

    assertTrue(setupLatch.await());

    m_jobManager.cancel(commonJobId, m_serverSession1);

    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("job-1-interrupted", "job-2-interrupted", "job-3a-interrupted"), protocol);
    assertEquals(5, m_transactions.size());
    verify(m_transactions.get(0), times(1)).cancel(); // TX of job-1
    verify(m_transactions.get(1), times(1)).cancel(); // TX of job-2
    verify(m_transactions.get(2), times(1)).cancel(); // TX of job-3
    verify(m_transactions.get(3), times(1)).cancel(); // TX of job-3a
    verify(m_transactions.get(4), never()).cancel(); // TX of job-4 (other session)
  }

  @Test
  public void testLocale() throws ProcessingException {
    IServerSession session = mock(IServerSession.class);
    NlsLocale.CURRENT.set(Locale.CHINA); // just to test to not to be considered.

    assertNull(new _ServerJobManager().interceptLocale(null, ServerJobInput.empty().session(session)));
    assertEquals(Locale.CANADA_FRENCH, new _ServerJobManager().interceptLocale(Locale.CANADA_FRENCH, ServerJobInput.empty().session(session)));
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequiredEmtpyInput() throws ProcessingException {
    IServerSession.CURRENT.remove();
    new _ServerJobManager().validateInput(ServerJobInput.empty());
  }

  @Test(expected = AssertionException.class)
  public void testSessionRequiredDefaultInputNOK() throws ProcessingException {
    IServerSession.CURRENT.remove();
    new _ServerJobManager().validateInput(ServerJobInput.defaults());
  }

  @Test
  public void testSessionRequiredDefaultInputOK1() throws ProcessingException {
    IServerSession.CURRENT.set(mock(IServerSession.class));
    new _ServerJobManager().validateInput(ServerJobInput.defaults());
    assertTrue(true); // no exception expected
  }

  @Test
  public void testSessionRequiredDefaultInputOK2() throws ProcessingException {
    IServerSession.CURRENT.remove();
    new _ServerJobManager().validateInput(ServerJobInput.defaults().session(mock(IServerSession.class)));
    assertTrue(true); // no exception expected
  }

  @Test
  public void testSessionNotRequiredWithoutSession() throws ProcessingException {
    new _ServerJobManager().validateInput(ServerJobInput.defaults().sessionRequired(false).session(null));
    assertTrue(true); // no exception expected
  }

  @Test
  public void testSessionNotRequiredWithSession() throws ProcessingException {
    new _ServerJobManager().validateInput(ServerJobInput.defaults().sessionRequired(false).session(mock(IServerSession.class)));
    assertTrue(true); // no exception expected
  }

  private static class _ServerJobManager extends ServerJobManager {

    @Override
    public Locale interceptLocale(Locale locale, ServerJobInput input) { // public to make accessible for test.
      return super.interceptLocale(locale, input);
    }

    @Override
    public void validateInput(ServerJobInput input) { // public to make accessible for test.
      super.validateInput(input);
    }
  }
}
