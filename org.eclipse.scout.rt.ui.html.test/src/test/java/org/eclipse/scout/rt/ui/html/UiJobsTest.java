package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.Callable;
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
  public void after() throws Exception {
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
  public void testCancelOrdinaryModelJob() {
    final CountDownLatch jobStarted = new CountDownLatch(1);
    final CountDownLatch jobsCancelled = new CountDownLatch(1);
    IFuture<String> future = ModelJobs.schedule(new Callable<String>() {
      @Override
      public String call() throws Exception {
        jobStarted.countDown();
        try {
          jobsCancelled.await();
        }
        catch (InterruptedException expected) {
          // expected
        }
        return "completed";
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession(), true)));

    BEANS.get(UiJobs.class).cancelModelJobs(clientSession());
    assertTrue(future.isCancelled());
    jobsCancelled.countDown();
    try {
      future.awaitDoneAndGet(200, TimeUnit.MILLISECONDS);
      fail("Expecting a " + FutureCancelledError.class);
    }
    catch (FutureCancelledError expected) {
      // expected
    }
  }

  @Test
  public void testCancelNotCancellableByUserModelJob() {
    final CountDownLatch jobStarted = new CountDownLatch(1);
    final CountDownLatch jobsCancelled = new CountDownLatch(1);
    IFuture<String> future = ModelJobs.schedule(new Callable<String>() {
      @Override
      public String call() throws Exception {
        jobStarted.countDown();
        jobsCancelled.await();
        return "completed";
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(clientSession(), true))
        .withExecutionHint(ModelJobs.EXECUTION_HINT_NOT_CANCELLABLE_BY_USER));

    BEANS.get(UiJobs.class).cancelModelJobs(clientSession());
    assertFalse(future.isCancelled());
    jobsCancelled.countDown();
    assertEquals("completed", future.awaitDoneAndGet(200, TimeUnit.MILLISECONDS));
  }
}
