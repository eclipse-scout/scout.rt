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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.commons.BlockingCountDownLatch;
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
public class ClientJobManagerCancelTest {

  private static ScheduledExecutorService s_executor;

  @Mock
  private IClientSession m_clientSession1;
  @Mock
  private IClientSession m_clientSession2;

  private ClientJobManager m_jobManager;

  @BeforeClass
  public static void beforeClass() {
    s_executor = Executors.newScheduledThreadPool(5);
  }

  @AfterClass
  public static void afterClass() {
    s_executor.shutdown();
  }

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    ISession.CURRENT.set(m_clientSession1);

    m_jobManager = new ClientJobManager();
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
  public void testCancel1() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);

    s_executor.submit(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
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
        }, ClientJobInput.empty().id(1).session(m_clientSession1));
        return null;
      }
    });

    assertTrue(setupLatch.await());

    // run the test
    assertTrue(m_jobManager.cancel(1, m_clientSession1));

    assertTrue(verifyLatch.await());

    // verify
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
    }, ClientJobInput.defaults().id(1));

    assertTrue(setupLatch.await());

    // run the test
    assertTrue(m_jobManager.cancel(1, m_clientSession1));

    assertTrue(verifyLatch.await());

    // verify
    assertEquals(CollectionUtility.arrayList("interrupted-job-1"), protocol);
  }

  /**
   * Cancel a 'runNow-job' that has nested jobs.
   */
  @Test
  public void testCancelCascade1() throws Exception {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>());

    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(3);

    s_executor.submit(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
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
                }, ClientJobInput.empty().id(3).session(m_clientSession1));
                latch3.await();

                m_jobManager.runNow(new IRunnable() {

                  @Override
                  public void run() throws Exception {
                    // NOOP
                  }
                }, ClientJobInput.empty().id(4).session(m_clientSession1));

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
                }, ClientJobInput.empty().id(5).session(m_clientSession1));

                if (IProgressMonitor.CURRENT.get().isCancelled()) {
                  protocol.add("job-2-cancelled");
                }
                verifyLatch.countDown();
              }
            }, ClientJobInput.empty().id(2).session(m_clientSession2));

            if (IProgressMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-1-cancelled");
            }
            verifyLatch.countDown();
          }
        }, ClientJobInput.empty().id(1).session(m_clientSession1));
        return null;
      }
    });

    assertTrue(setupLatch.await());

    // test cancel of inner jobs (expected=no effect)
    assertFalse(m_jobManager.cancel(5, m_clientSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.
    assertFalse(m_jobManager.cancel(4, m_clientSession1)); // already finished
    assertFalse(m_jobManager.cancel(2, m_clientSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.

    // test cancel with wrong session (expected=no effect)
    assertFalse(m_jobManager.cancel(1, m_clientSession2));

    // test cancel
    assertTrue(m_jobManager.cancel(1, m_clientSession1));

    assertTrue(verifyLatch.await());

    // verify
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
            }, ClientJobInput.defaults().id(3).session(m_clientSession1));
            latch3.await();

            m_jobManager.runNow(new IRunnable() {

              @Override
              public void run() throws Exception {
                // NOOP
              }
            }, ClientJobInput.defaults().id(4).session(m_clientSession1));

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
            }, ClientJobInput.defaults().id(5).session(m_clientSession1));

            if (IProgressMonitor.CURRENT.get().isCancelled()) {
              protocol.add("job-2-cancelled");
            }
            verifyLatch.countDown();
          }
        }, ClientJobInput.defaults().id(2).session(m_clientSession1));

        if (IProgressMonitor.CURRENT.get().isCancelled()) {
          protocol.add("job-1-cancelled");
        }
        verifyLatch.countDown();
      }
    }, ClientJobInput.defaults().id(1).session(m_clientSession1));

    assertTrue(setupLatch.await());

    // test cancel of inner jobs (expected=no effect)
    assertFalse(m_jobManager.cancel(5, m_clientSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.
    assertFalse(m_jobManager.cancel(4, m_clientSession1)); // already finished
    assertFalse(m_jobManager.cancel(2, m_clientSession1)); // only outermost 'runNow'-job can be cancelled directly or not at all if nested in a scheduled job.

    // test cancel with wrong session (expected=no effect)
    assertFalse(m_jobManager.cancel(1, m_clientSession2));

    // test cancel
    assertTrue(m_jobManager.cancel(1, m_clientSession1));

    assertTrue(verifyLatch.await());

    // verify
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
    }, ClientJobInput.defaults().id(commonJobId));
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
    }, ClientJobInput.defaults().id(commonJobId));
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
        }, ClientJobInput.defaults().id(123));
      }
    }, ClientJobInput.defaults().id(commonJobId));
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
    }, ClientJobInput.defaults().id(commonJobId).session(m_clientSession2));
    assertTrue(txOrderLatch4.await());

    assertTrue(setupLatch.await());

    m_jobManager.cancel(commonJobId, m_clientSession1);

    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.hashSet("job-1-interrupted", "job-2-interrupted", "job-3a-interrupted"), protocol);
  }
}
