/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingListener;

import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.fixtures.SessionStoreTestForm;
import org.eclipse.scout.rt.ui.html.fixtures.SessionStoreTestForm.CloseAction;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.hamcrest.MatcherAssert;
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
  private RunContext m_oldRunContext;

  @Before
  public void before() {
    m_oldRunContext = RunContext.CURRENT.get();
    RunContext.CURRENT.set(RunContexts.empty()); // Because this test must be executed by a bare JUnit runner (see JavaDoc of test class).

    m_beans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withInitialInstance(new JobCompletionDelayOnSessionShutdown() {
          @Override
          public Long getDefaultValue() {
            return 1L;
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingDelayProperty.class).withInitialInstance(new SessionStoreHousekeepingDelayProperty() {
          @Override
          public Integer getDefaultValue() {
            return 0;
          }
        }),

        new BeanMetaData(TestEnvironmentClientSession.class));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    RunContext.CURRENT.set(m_oldRunContext);
  }

  @Test
  public void testDispose() {
    UiSession session = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    WeakReference<IUiSession> ref = new WeakReference<>(session);

    JsonTestUtility.endRequest(session);
    session.dispose();
    assertTrue(session.isDisposed());
    session = null;
    TestingUtility.assertGC(ref);
  }

  @Test
  public void testLogout() {
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();

    uiSession.getClientSession().stop();

    assertTrue(uiSession.isDisposed());
    assertNull(uiSession.currentJsonResponse());
    JsonTestUtility.endRequest(uiSession);
  }

  @Test
  public void testSessionInvalidation() {
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // Don't waste time waiting for client jobs to finish. Test job itself runs inside a client job so we always have to wait until max time
    WeakReference<IUiSession> ref = new WeakReference<>(uiSession);
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
    doTestLogoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_LOOP, true, false);
  }

  /**
   * Tests that session invalidation still works even if the model is blocking during its disposal. Especially the
   * {@link HttpSessionBindingListener#valueUnbound(jakarta.servlet.http.HttpSessionBindingEvent)} method is expected not
   * to be blocked by the {@link SessionStore} (the method is invoked by the servlet container and blocking its thread
   * could interfere with the application server itself, for example when a background thread is cleaning up timed out
   * sessions).
   */
  protected void doTestLogoutWithBlockingModelDisposal(final CloseAction openFormCloseAction, boolean expectFormFinallyCompleted, boolean expectSessionActiveAfterwards) throws InterruptedException {
    // create new UI session along with client client and HTTP sessions
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    final HttpSession httpSession = UiSessionTestUtility.getHttpSession(uiSession);
    final IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // register ui session in session store
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);
    MatcherAssert.assertThat(sessionStore, is(instanceOf(HttpSessionBindingListener.class)));

    // create and start test form in a model job
    final SessionStoreTestForm form = ModelJobs.schedule(() -> {
      SessionStoreTestForm f = new SessionStoreTestForm(openFormCloseAction);
      f.start();
      return f;
    }, ModelJobs
        .newInput(ClientRunContexts
            .empty()
            .withSession(clientSession, true)))
        .awaitDoneAndGet(5, TimeUnit.SECONDS);

    // schedule a job that emulates servlet container that performs session invalidation on session timeout
    IFuture<Void> appServerSessionTimeoutFuture = Jobs.schedule(httpSession::invalidate, Jobs.newInput()
        .withName("simulate session timeout")
        .withExecutionTrigger(Jobs
            .newExecutionTrigger()
            .withStartIn(200, TimeUnit.MILLISECONDS)));

    // perform logout on UI session that stops the client session and disposes all model objects.
    uiSession.logout();
    assertTrue(uiSession.isDisposed());

    form.awaitDoFinallyEntered(5, TimeUnit.SECONDS);

    if (!expectSessionActiveAfterwards) {
      JobTestUtil.waitForCondition(() -> !clientSession.isActive());
    }

    if (expectFormFinallyCompleted) {
      form.awaitDoFinallyCompleted(5, TimeUnit.SECONDS);
    }

    appServerSessionTimeoutFuture.awaitDone(3, TimeUnit.SECONDS);
    assertTrue(appServerSessionTimeoutFuture.isDone());

    assertEquals(expectFormFinallyCompleted, form.isFinallyCompleted());
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
    doTestSessionTimeoutWithBlockingModelDisposal(CloseAction.WAIT_FOR_LOOP, false, false);
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
    final IClientSession clientSession = uiSession.getClientSession();
    assertFalse(uiSession.isDisposed());

    // register ui session in session store
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);
    MatcherAssert.assertThat(sessionStore, is(instanceOf(HttpSessionBindingListener.class)));

    // create and start test form in a model job
    SessionStoreTestForm form = ModelJobs.schedule(() -> {
      SessionStoreTestForm f = new SessionStoreTestForm(openFormCloseAction);
      f.start();
      return f;
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
      JobTestUtil.waitForCondition(() -> !clientSession.isActive());
    }

    assertTrue(uiSession.isDisposed());
    assertEquals(expectSessionActiveAfterwards, clientSession.isActive());
  }
}
