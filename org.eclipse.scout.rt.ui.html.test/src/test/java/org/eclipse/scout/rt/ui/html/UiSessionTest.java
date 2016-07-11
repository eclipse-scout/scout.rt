/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.SessionStore.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.SessionStore.SessionStoreHousekeepingMaxWaitShutdownProperty;
import org.eclipse.scout.rt.ui.html.SessionStore.SessionStoreMaxWaitAllShutdownProperty;
import org.eclipse.scout.rt.ui.html.SessionStore.SessionStoreMaxWaitWriteLockProperty;
import org.eclipse.scout.rt.ui.html.fixtures.SessionStoreTestForm;
import org.eclipse.scout.rt.ui.html.fixtures.SessionStoreTestForm.CloseAction;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * This test must be executed by a bare JUnit runner.
 * Reason: The PlatformTestRunner and its sub classes keep track of every job scheduled during test execution and verify that they are completed. The list of scheduled jobs
 *         are referencing a JobInput which in turn references a RunContext and a session. The tests in this class will fail because they assert that the sessions are
 *         not referenced by any other object and therefore garbage collected.
 */
public class UiSessionTest {

  private List<IBean<?>> m_beans;

  @Before
  public void before() {
    m_beans = TestingUtility.registerBeans(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withProducer(new IBeanInstanceProducer<JobCompletionDelayOnSessionShutdown>() {
          @Override
          public JobCompletionDelayOnSessionShutdown produce(IBean<JobCompletionDelayOnSessionShutdown> bean) {
            return new JobCompletionDelayOnSessionShutdown() {
              @Override
              protected Long getDefaultValue() {
                return 0L;
              }
            };
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingDelayProperty.class).withInitialInstance(new SessionStoreHousekeepingDelayProperty() {
          @Override
          protected Integer getDefaultValue() {
            return 0;
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingMaxWaitShutdownProperty.class).withInitialInstance(new SessionStoreHousekeepingMaxWaitShutdownProperty() {
          @Override
          protected Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitWriteLockProperty.class).withInitialInstance(new SessionStoreMaxWaitWriteLockProperty() {
          @Override
          protected Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitAllShutdownProperty.class).withInitialInstance(new SessionStoreMaxWaitAllShutdownProperty() {
          @Override
          protected Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(TestEnvironmentClientSession.class));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
  }

  @Test
  public void testDispose() throws Exception {
    UiSession session = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    WeakReference<IUiSession> ref = new WeakReference<IUiSession>(session);

    JsonTestUtility.endRequest(session);
    session.dispose();
    assertTrue(session.isDisposed());
    session = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testLogout() throws Exception {
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();

    uiSession.getClientSession().stop();

    assertTrue(uiSession.isDisposed());
    assertNull(uiSession.currentJsonResponse());
    JsonTestUtility.endRequest(uiSession);

    // TODO CGU This does not work, because somehow, the housekeeping job is not executed?
    //HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    //Mockito.verify(httpSession).invalidate();
  }

  @Test
  public void testSessionInvalidation() throws Exception {
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // Don't waste time waiting for client jobs to finish. Test job itself runs inside a client job so we always have to wait until max time
    WeakReference<IUiSession> ref = new WeakReference<IUiSession>(uiSession);
    ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);

    JsonTestUtility.endRequest(uiSession);
    httpSession.invalidate();
    BEANS.get(UiJobs.class).awaitModelJobs(clientSession, JUnitExceptionHandler.class);
    assertFalse(clientSession.isActive());
    assertTrue(uiSession.isDisposed());

    uiSession = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testLogoutWithOpenForm() throws Exception {
    doTestLogoutWithBlockingModelDisposal(CloseAction.DO_NOTHING, true, false);
  }

  @Test
  public void testLogoutWithOpenFormThatWaitsOnMessageBox() throws Exception {
    doTestLogoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_MESSAGE_BOX, true, false);
  }

  @Test
  public void testLogoutWithOpenFormThatWaitsOnAnotherForm() throws Exception {
    doTestLogoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_ANOTHER_FORM, false, false);
  }

  @Test
  public void testLogoutWithOpenFormThatWaitsOnJob() throws Exception {
    doTestLogoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_JOB, false, false);
  }

  @Test
  public void testLogoutWithOpenFormThatWaitsOnLoop() throws Exception {
    doTestLogoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_LOOP, false, true);
  }

  /**
   * Tests that session invalidation still works even if the model is blocking during its disposal. Especially the
   * {@link HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)} method is expected not
   * to be blocked by the {@link SessionStore} (the method is invoked by the servlet container and blocking its thread
   * could interfere with the application server itself, for example when a background thread is cleaning up timed out
   * sessions).
   */
  protected void doTestLogoutWithBlockingModelDisposal(final CloseAction openFormCloseAction, boolean expectFormFinallyCompleted, boolean expectSessionActiveAfterwards) throws InterruptedException {
    // create new UI session along with client client and HTTP sessions
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    final HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // register ui session in session store
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);
    assertThat(sessionStore, is(instanceOf(HttpSessionBindingListener.class)));

    // create and start test form in a model job
    SessionStoreTestForm form = ModelJobs.schedule(new Callable<SessionStoreTestForm>() {
      @Override
      public SessionStoreTestForm call() throws Exception {
        SessionStoreTestForm f = new SessionStoreTestForm(openFormCloseAction);
        f.start();
        return f;
      }
    }, ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(clientSession, true)))
        .awaitDoneAndGet(1, TimeUnit.SECONDS);

    // schedule a job that emulates servlet container that performs session invalidation on session timeout
    IFuture<Void> appServerSessionTimeoutFuture = Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        httpSession.invalidate();
      }
    }, Jobs.newInput()
        .withName("simulate session timeout")
        .withExecutionTrigger(Jobs
            .newExecutionTrigger()
            .withStartIn(200, TimeUnit.MILLISECONDS)));

    // perform logout on UI session that stops the client session and disposes all model objects.
    uiSession.logout();
    assertTrue(uiSession.isDisposed());

    if (expectFormFinallyCompleted) {
      form.awaitDoFinallyCompleted(1, TimeUnit.SECONDS);
    }
    else {
      BEANS.get(UiJobs.class).awaitModelJobs(clientSession, JUnitExceptionHandler.class);
    }

    appServerSessionTimeoutFuture.awaitDone(3, TimeUnit.SECONDS);
    assertTrue(appServerSessionTimeoutFuture.isDone());

    if (expectFormFinallyCompleted) {
      assertTrue(form.isFinallyCompleted());
      BEANS.get(UiJobs.class).awaitModelJobs(clientSession, JUnitExceptionHandler.class);
    }
    else {
      assertFalse(form.isFinallyCompleted());
      // The model job was canceled by the SessionStore. Hence we cannot use UiJobs.awaitModelJobs().
      // We still sleep some time
      SleepUtil.sleepSafe(200, TimeUnit.MILLISECONDS);
    }

    assertEquals(expectSessionActiveAfterwards, clientSession.isActive());
  }

  @Test
  public void testSessionTimeoutWithOpenForm() throws Exception {
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.DO_NOTHING, true, false);
  }

  @Test
  public void testSessionTimeoutOpenFormThatWaitsOnMessageBox() throws Exception {
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_MESSAGE_BOX, true, false);
  }

  @Test
  public void testSessionTimeoutOpenFormThatWaitsOnAnotherForm() throws Exception {
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_ANOTHER_FORM, false, false);
  }

  @Test
  public void testSessionTimeoutOpenFormThatWaitsOnJob() throws Exception {
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_JOB, false, false);
  }

  @Test
  public void testSessionTimeoutOpenFormThatWaitsOnLoop() throws Exception {
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_LOOP, false, true);
  }

  /**
   * Checks the same problematic as {@link #doTestLogoutWithBlockingModelDisposal(CloseAction, boolean, boolean)} except
   * that there is no {@link UiSession#logout()}, but only a {@link HttpSession#invalidate()}, invoked by the servlet
   * container due to session timeout.
   */
  protected void doTestSessionTimeoutWithBlockingModelDisposal(final CloseAction openFormCloseAction, boolean expectFormFinallyCompleted, boolean expectSessionActiveAfterwards) throws InterruptedException {
    // create new UI session along with client client and HTTP sessions
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    final HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // register ui session in session store
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);
    assertThat(sessionStore, is(instanceOf(HttpSessionBindingListener.class)));

    // create and start test form in a model job
    SessionStoreTestForm form = ModelJobs.schedule(new Callable<SessionStoreTestForm>() {
      @Override
      public SessionStoreTestForm call() throws Exception {
        SessionStoreTestForm f = new SessionStoreTestForm(openFormCloseAction);
        f.start();
        return f;
      }
    }, ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(clientSession, true)))
        .awaitDoneAndGet(1, TimeUnit.SECONDS);

    // invalidate HTTP session
    httpSession.invalidate();

    if (expectFormFinallyCompleted) {
      form.awaitDoFinallyCompleted(1, TimeUnit.SECONDS);
      assertTrue(form.isFinallyCompleted());
      BEANS.get(UiJobs.class).awaitModelJobs(clientSession, JUnitExceptionHandler.class);
    }
    else {
      assertFalse(form.isFinallyCompleted());
      // The model job was canceled by the SessionStore. Hence we cannot use UiJobs.awaitModelJobs().
      // We still sleep some time
      for (int i = 0; i < 10; i++) {
        SleepUtil.sleepSafe(200, TimeUnit.MILLISECONDS);
        if (expectSessionActiveAfterwards == clientSession.isActive()) {
          break;
        }
      }
    }

    assertTrue(uiSession.isDisposed());
    assertEquals(expectSessionActiveAfterwards, clientSession.isActive());
  }
}
