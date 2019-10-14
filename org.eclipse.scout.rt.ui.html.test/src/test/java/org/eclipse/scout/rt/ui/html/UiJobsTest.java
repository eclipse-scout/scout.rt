package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class UiJobsTest {

  private List<IBean<?>> m_beans;
  private UiSession m_session;

  @Before
  public void before() {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(TestEnvironmentClientSession.class));
    m_session = (UiSession) JsonTestUtility.createAndInitializeUiSession();
  }

  @After
  public void after() {
    try {
      JsonTestUtility.endRequest(m_session);
    }
    finally {
      m_session = null;
      TestingUtility.unregisterBeans(m_beans);
    }
  }

  protected IClientSession clientSession() {
    return m_session.getClientSession();
  }

  @Test
  public void testCancelOrdinaryModelJob() throws InterruptedException {
    final CountDownLatch jobStarted = new CountDownLatch(1);
    final CountDownLatch jobsCancelled = new CountDownLatch(1);
    IFuture<String> future = ModelJobs.schedule(() -> {
      jobStarted.countDown();
      try {
        jobsCancelled.await();
      }
      catch (InterruptedException expected) {
        // expected
      }
      return "completed";
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession(), true)));

    jobStarted.await();
    BEANS.get(UiJobs.class).cancelModelJobs(clientSession());
    assertTrue(future.isCancelled());

    jobsCancelled.countDown();
    try {
      future.awaitDoneAndGet();
      fail("Expecting a " + FutureCancelledError.class);
    }
    catch (FutureCancelledError expected) {
      // expected
    }
  }

  @Test
  public void testCancelNotCancellableByUserModelJob() throws InterruptedException {
    final CountDownLatch jobStarted = new CountDownLatch(1);
    final CountDownLatch jobsCancelled = new CountDownLatch(1);
    IFuture<String> future = ModelJobs.schedule(() -> {
      jobStarted.countDown();
      jobsCancelled.await();
      SleepUtil.sleepSafe(1, TimeUnit.SECONDS);
      return "completed";
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession(), true))
        .withExecutionHint(ModelJobs.EXECUTION_HINT_NOT_CANCELLABLE_BY_USER));

    jobStarted.await();
    BEANS.get(UiJobs.class).cancelModelJobs(clientSession());
    assertFalse(future.isCancelled());

    jobsCancelled.countDown();
    assertEquals("completed", future.awaitDoneAndGet());
  }
}
