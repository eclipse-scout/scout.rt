/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestHandler;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.UnloadRequestHandler;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.testing.TestEnvironmentUiSession;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWithNewPlatform()
@RunWith(PlatformTestRunner.class)
public class UiSessionInitAndDisposeTest {

  private static final Logger LOG = LoggerFactory.getLogger(UiSessionInitAndDisposeTest.class);

  private List<IBean<?>> m_beans;
  private List<String> m_protocol = Collections.synchronizedList(new ArrayList<>());
  private Consumer<IClientSession> m_afterGetOrCreateClientSession;
  private boolean m_persistentUiSessions;
  private boolean m_synchronousHousekeeping;
  private RuntimeException m_initFailure;
  private Runnable m_beforeSessionStoreLookup;

  @Before
  public void before() {
    m_protocol.clear();
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
            return 2;
          }
        }),

        new BeanMetaData(HttpSessionHelper.class).withProducer((IBeanInstanceProducer<HttpSessionHelper>) bean -> new HttpSessionHelper() {
          @Override
          protected ISessionStore createSessionStore(HttpSession httpSession) {
            return new FixtureSessionStore(httpSession);
          }
        }),

        new BeanMetaData(FixtureUiSession.class).withProducer((IBeanInstanceProducer<FixtureUiSession>) bean -> new FixtureUiSession()),

        new BeanMetaData(FixtureClientSession.class).withProducer((IBeanInstanceProducer<FixtureClientSession>) bean -> new FixtureClientSession()));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
    m_protocol.clear();
  }

  private void writeToProtocol(String line) {
    LOG.info("New protocol entry: '{}' ", line);
    m_protocol.add(line);
  }

  @Test
  public void testStartupThenModelStop() throws IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();

    //startup
    final String clientSessionId;
    final String uiSessionId;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", "{\"startup\":true}");
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    assertEquals(1, store.getClientSessionMap().size());
    assertNotNull(store.getClientSessionMap().get(clientSessionId));
    assertEquals(1, store.getUiSessionMap().size());
    assertNotNull(store.getUiSessionMap().get(uiSessionId));

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "ClientSession.execLoadSession",
            "Desktop.execOpened",
            "Desktop.execGuiAttached",
            "UiSession.verifySubject"),
        m_protocol);
    m_protocol.clear();

    //model stop
    ModelJobs.schedule(() -> IDesktop.CURRENT.get().getUIFacade().closeFromUI(true), ModelJobs.newInput(ClientRunContexts.empty().withSession(store.getClientSessionMap().get(clientSessionId), true)))
        .awaitDone();

    JobTestUtil.waitForCondition(() -> store.isEmpty() && !store.isHttpSessionValid());

    assertEquals(
        Arrays.asList(
            "ClientSession.stopping",
            "UiSession.dispose",
            //"Desktop.execGuiDetached" -> not called from UISession because client session is stopping
            "ClientSession.execStoreSession",
            "Desktop.execGuiDetached", //called from client session
            "Desktop.execClosing",
            "ClientSession.stopped",
            "SessionStore.doHousekeeping"),
        m_protocol);
    m_protocol.clear();
  }

  @Test
  public void testStartupThenBrowserTabClose() throws IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final UnloadRequestHandler unloadHandler = new UnloadRequestHandler();

    //startup
    final String clientSessionId;
    final String uiSessionId;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true}";
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    assertEquals(1, store.getClientSessionMap().size());
    assertNotNull(store.getClientSessionMap().get(clientSessionId));
    assertEquals(1, store.getUiSessionMap().size());
    assertNotNull(store.getUiSessionMap().get(uiSessionId));

    final FixtureClientSession clientSession = (FixtureClientSession) store.getClientSessionMap().values().iterator().next();

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "ClientSession.execLoadSession",
            "Desktop.execOpened",
            "Desktop.execGuiAttached",
            "UiSession.verifySubject"),
        m_protocol);
    m_protocol.clear();

    //browser tab closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }

    JobTestUtil.waitForCondition(() -> store.isEmpty() && !store.isHttpSessionValid() && clientSession.isStopped());

    assertEquals(
        Arrays.asList(
            "UiSession.dispose",
            "Desktop.execGuiDetached", //called from UISession
            "SessionStore.doHousekeeping",
            "ClientSession.stopping",
            "ClientSession.execStoreSession",
            "Desktop.execClosing",
            "ClientSession.stopped"),
        m_protocol);
    m_protocol.clear();
  }

  @Test
  public void testStartupThenBrowserTabReload() throws IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final UnloadRequestHandler unloadHandler = new UnloadRequestHandler();

    //startup
    String clientSessionId;
    String uiSessionId;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true}";
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    assertEquals(1, store.getClientSessionMap().size());
    assertNotNull(store.getClientSessionMap().get(clientSessionId));
    assertEquals(1, store.getUiSessionMap().size());
    assertNotNull(store.getUiSessionMap().get(uiSessionId));

    final FixtureClientSession clientSession = (FixtureClientSession) store.getClientSessionMap().values().iterator().next();

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "ClientSession.execLoadSession",
            "Desktop.execOpened",
            "Desktop.execGuiAttached",
            "UiSession.verifySubject"),
        m_protocol);
    m_protocol.clear();

    //browser tab reload -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(() -> {
      int n = Jobs.getJobManager()
          .getFutures(Jobs.newFutureFilterBuilder()
              .andMatchExecutionSemaphore(clientSession.getModelJobSemaphore())
              .toFilter())
          .size();
      return n == 0;
    });

    //json startup with same client session
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionId);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "Desktop.execGuiDetached",
            "Desktop.execGuiAttached"),
        m_protocol.stream().filter(s -> s.startsWith("Desktop.")).collect(Collectors.toList()));
    assertEquals(
        Arrays.asList(
            "UiSession.dispose",
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "UiSession.verifySubject"),
        m_protocol.stream().filter(s -> s.startsWith("UiSession.")).collect(Collectors.toList()));
    m_protocol.clear();
    assertEquals(1, store.countClientSessions());
    assertEquals(1, store.countUiSessions());

    //browser tab closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(clientSession::isStopped);

    assertEquals(
        Arrays.asList(
            "UiSession.dispose",
            "Desktop.execGuiDetached", //called from UISession
            "SessionStore.doHousekeeping",
            "ClientSession.stopping",
            "ClientSession.execStoreSession",
            "Desktop.execClosing",
            "ClientSession.stopped"),
        m_protocol);
    m_protocol.clear();
  }

  @Test
  public void testStartupThenBrowserTabDuplicate() throws IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final UnloadRequestHandler unloadHandler = new UnloadRequestHandler();

    //startup
    final String clientSessionIdA;
    final String uiSessionIdA;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true}";
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final SessionStore store = (SessionStore) BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    assertEquals(1, store.getClientSessionMap().size());
    assertNotNull(store.getClientSessionMap().get(clientSessionIdA));
    assertEquals(1, store.getUiSessionMap().size());
    assertNotNull(store.getUiSessionMap().get(uiSessionIdA));

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "ClientSession.execLoadSession",
            "Desktop.execOpened",
            "Desktop.execGuiAttached",
            "UiSession.verifySubject"),
        m_protocol);
    m_protocol.clear();

    //browser tab duplicate -> json startup with same client session
    // this results in two UiSessions attached to the same client session
    final String clientSessionIdB;
    final String uiSessionIdB;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionIdA);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            // "Desktop.execGuiAttached" -> this is not called because there is already uiSessionA attached to the clientSession
            "UiSession.verifySubject"
        ),
        m_protocol);
    m_protocol.clear();
    assertEquals(clientSessionIdA, clientSessionIdB);
    assertEquals(1, store.countClientSessions());
    assertNotEquals(uiSessionIdA, uiSessionIdB);
    assertEquals(2, store.countUiSessions());

    final FixtureClientSession clientSession = (FixtureClientSession) store.getClientSessionMap().values().iterator().next();

    //browser tab A closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdA, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    //browser tab B closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdB, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(clientSession::isStopped);

    //two calls UiSession.dispose; remove them from the protocol since they can occor any time in the list
    for (int i = 1; i <= 2; i++) {
      assertTrue("" + (i == 1 ? "first" : "second") + " call to UiSession.dispose", m_protocol.remove("UiSession.dispose"));
    }
    assertEquals(
        Arrays.asList(
            "Desktop.execGuiDetached", //called from UISession, invoked only once even though there are two ui session disposes
            "SessionStore.doHousekeeping",
            "ClientSession.stopping",
            "ClientSession.execStoreSession",
            "Desktop.execClosing",
            "ClientSession.stopped"),
        m_protocol);
    m_protocol.clear();
  }

  /**
   * Start one browser tab, duplicate it, then close the first browser tab.
   * <p>
   * The clientSession must not be stopped
   * <p>
   * Desktop.execGuiAttached must not be called when the second tab opens
   * <p>
   * Desktop.execGuiDetached must not be called when the first tab closes
   */
  @Test
  public void testStartupThenBrowserTabDuplicateThenBrowserTabClose() throws IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final UnloadRequestHandler unloadHandler = new UnloadRequestHandler();

    //startup
    final String clientSessionIdA;
    final String uiSessionIdA;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true}";
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    assertEquals(1, store.getClientSessionMap().size());
    assertNotNull(store.getClientSessionMap().get(clientSessionIdA));
    assertEquals(1, store.getUiSessionMap().size());
    assertNotNull(store.getUiSessionMap().get(uiSessionIdA));

    final FixtureClientSession clientSession = (FixtureClientSession) store.getClientSessionMap().values().iterator().next();

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "ClientSession.execLoadSession",
            "Desktop.execOpened",
            "Desktop.execGuiAttached",
            "UiSession.verifySubject"),
        m_protocol);
    m_protocol.clear();

    //browser tab duplicate -> json startup with same client session
    // this results in two UiSessions attached to the same client session
    final String clientSessionIdB;
    final String uiSessionIdB;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionIdA);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), StandardCharsets.UTF_8);
      uiSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            // "Desktop.execGuiAttached" -> this is not called because there is already uiSessionA attached to the clientSession
            "UiSession.verifySubject"
        ),
        m_protocol);
    m_protocol.clear();
    assertEquals(clientSessionIdA, clientSessionIdB);
    assertEquals(1, store.countClientSessions());
    assertNotEquals(uiSessionIdA, uiSessionIdB);
    assertEquals(2, store.countUiSessions());

    //browser tab A closed -> json unload
    final IClientSession clientSessionA = store.getClientSessionMap().get(clientSessionIdA);
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdA, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(() -> {
      int n = Jobs.getJobManager()
          .getFutures(Jobs.newFutureFilterBuilder()
              .andMatchExecutionSemaphore(clientSessionA.getModelJobSemaphore())
              .toFilter())
          .size();
      return n == 0;
    });

    assertEquals(
        Arrays.asList(
            "UiSession.dispose"
            //"Desktop.execGuiDetached" // -> this is not called because there is still uiSessionB attached to the clientSession
        ),
        m_protocol);
    m_protocol.clear();
    assertEquals(1, store.countClientSessions());
    assertEquals(1, store.countUiSessions());

    //browser tab B closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdB, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(clientSession::isStopped);

    assertEquals(
        Arrays.asList(
            "UiSession.dispose",
            "Desktop.execGuiDetached", //called from last UISession that is attached to the clientSession
            "SessionStore.doHousekeeping",
            "ClientSession.stopping",
            "ClientSession.execStoreSession",
            "Desktop.execClosing",
            "ClientSession.stopped"),
        m_protocol);
    m_protocol.clear();
  }

  /**
   * A startup request arrives after the session store decided to invalidate the HTTP session (no more client sessions)
   * but before the HTTP session is actually invalidated. The UI session must not be registered, no client session must
   * be created and the UI is requested to reload the page.
   */
  @Test
  public void testStartupWhileHttpSessionInvalidationPending() {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final SessionStore store = (SessionStore) BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    final List<JSONObject> responses = new ArrayList<>();
    // getCreationTime() is called by SessionStore#checkHttpSessionOutsideWriteLock after the write lock was released and before the HTTP session is invalidated
    Mockito.when(httpSession.getCreationTime()).thenAnswer(invocation -> {
      if (responses.isEmpty()) {
        responses.add(startup(messageHandler, httpSession));
      }
      return 0L;
    });

    store.checkHttpSessionOutsideWriteLock();

    assertEquals(1, responses.size());
    JSONObject startupData = responses.get(0).getJSONObject("startupData");
    assertTrue(startupData.getBoolean("reloadPage"));
    assertFalse(startupData.has("uiSessionId"));
    assertTrue(store.isEmpty());
    assertFalse(store.isHttpSessionValid());
    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "UiSession.dispose"),
        m_protocol);
    m_protocol.clear();
  }

  /**
   * The HTTP session is invalidated after the UI session was preregistered (client session already created) but before
   * the UI session is registered. The UI session and the client session must be disposed and the UI is requested to
   * reload the page.
   */
  @Test
  public void testStartupWhileHttpSessionInvalidated() throws Exception {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    final List<FixtureClientSession> clientSessions = new ArrayList<>();
    m_afterGetOrCreateClientSession = clientSession -> {
      clientSessions.add((FixtureClientSession) clientSession);
      httpSession.invalidate();
    };

    JSONObject response = startup(messageHandler, httpSession);

    JSONObject startupData = response.getJSONObject("startupData");
    assertTrue(startupData.getBoolean("reloadPage"));
    assertFalse(startupData.has("uiSessionId"));
    assertEquals(1, clientSessions.size());
    final FixtureClientSession clientSession = clientSessions.get(0);
    JobTestUtil.waitForCondition(() -> store.isEmpty() && clientSession.isStopped());
    assertFalse(store.isHttpSessionValid());
    assertTrue(m_protocol.contains("UiSession.dispose"));
    assertFalse(m_protocol.contains("UiSession.verifySubject"));
    m_protocol.clear();
  }

  /**
   * Same as {@link #testStartupWhileHttpSessionInvalidated()} but with a persistent UI session (iOS home screen mode).
   * The client session created by the aborted startup was never registered and must be stopped even though persistent
   * UI sessions normally keep their client session.
   */
  @Test
  public void testPersistentStartupWhileHttpSessionInvalidated() throws Exception {
    m_persistentUiSessions = true;
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    final List<FixtureClientSession> clientSessions = new ArrayList<>();
    m_afterGetOrCreateClientSession = clientSession -> {
      clientSessions.add((FixtureClientSession) clientSession);
      httpSession.invalidate();
    };

    JSONObject response = startup(messageHandler, httpSession);

    assertTrue(response.getJSONObject("startupData").getBoolean("reloadPage"));
    assertEquals(1, clientSessions.size());
    final FixtureClientSession clientSession = clientSessions.get(0);
    JobTestUtil.waitForCondition(() -> store.isEmpty() && clientSession.isStopped());
    assertTrue(m_protocol.contains("UiSession.dispose"));
    assertTrue(m_protocol.contains("SessionStore.doHousekeeping"));
    m_protocol.clear();
  }

  /**
   * A registered client session of a persistent UI session (iOS home screen mode) is kept when the UI session is
   * unloaded (e.g. app switch).
   */
  @Test
  public void testPersistentStartupThenBrowserTabClose() throws Exception {
    m_persistentUiSessions = true;
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final UnloadRequestHandler unloadHandler = new UnloadRequestHandler();

    JSONObject startupData = startup(messageHandler, httpSession).getJSONObject("startupData");
    final String uiSessionId = startupData.getString("uiSessionId");
    final String clientSessionId = startupData.getString("clientSessionId");
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    final FixtureClientSession clientSession = (FixtureClientSession) store.getClientSessionMap().get(clientSessionId);
    assertNotNull(clientSession);
    m_protocol.clear();

    // app switch -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }

    Thread.sleep(3000); // longer than the session store housekeeping delay of 2 seconds
    assertTrue(store.getUiSessionMap().isEmpty());
    assertSame(clientSession, store.getClientSessionMap().get(clientSessionId));
    assertTrue(store.isHttpSessionValid());
    assertFalse(clientSession.isStopped());
    assertFalse(m_protocol.contains("SessionStore.doHousekeeping"));

    // cleanup
    httpSession.invalidate();
    JobTestUtil.waitForCondition(clientSession::isStopped);
    m_protocol.clear();
  }

  /**
   * An ordinary startup failure must be reported as such, even if the cleanup of the failed startup (session
   * housekeeping) invalidates the HTTP session. Otherwise, a recurring startup error would lead to endless page reloads.
   */
  @Test
  public void testStartupFailureWithImmediateHousekeeping() throws Exception {
    m_synchronousHousekeeping = true;
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    final List<FixtureClientSession> clientSessions = new ArrayList<>();
    m_afterGetOrCreateClientSession = clientSession -> clientSessions.add((FixtureClientSession) clientSession);
    m_initFailure = new RuntimeException("expected startup failure");

    JSONObject response = startup(messageHandler, httpSession);

    // original failure is reported, no reload
    assertFalse(response.has("startupData"));
    assertEquals(JsonResponse.ERR_STARTUP_FAILED, response.getJSONObject(JsonResponse.PROP_ERROR).getInt(JsonResponse.PROP_ERROR_CODE));
    // the cleanup invalidated the HTTP session after the failure
    assertFalse(store.isHttpSessionValid());
    assertEquals(1, clientSessions.size());
    final FixtureClientSession clientSession = clientSessions.get(0);
    JobTestUtil.waitForCondition(() -> store.isEmpty() && clientSession.isStopped());
    m_protocol.clear();
  }

  /**
   * The HTTP session is invalidated after the request handler obtained the session store but before the UI session
   * obtains it. The UI session cannot get the session store and must still be cleaned up, and the UI is requested to
   * reload the page.
   */
  @Test
  public void testStartupWhileHttpSessionInvalidatedBeforeUiSessionGetsSessionStore() throws Exception {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    m_beforeSessionStoreLookup = () -> {
      httpSession.invalidate();
      // behave like an invalidated HTTP session of a servlet container
      Mockito.doThrow(new IllegalStateException("invalidated")).when(httpSession).getAttribute(Mockito.anyString());
      Mockito.doThrow(new IllegalStateException("invalidated")).when(httpSession).getCreationTime();
    };

    JSONObject response = startup(messageHandler, httpSession);

    JSONObject startupData = response.getJSONObject("startupData");
    assertTrue(startupData.getBoolean("reloadPage"));
    assertFalse(startupData.has("uiSessionId"));
    assertFalse(store.isHttpSessionValid());
    assertTrue(store.isEmpty());
    assertTrue(m_protocol.contains("UiSession.dispose"));
    assertFalse(m_protocol.contains("UiSession.getOrCreateClientSession"));
    m_protocol.clear();
  }

  protected JSONObject startup(JsonMessageRequestHandler messageHandler, HttpSession httpSession) throws IOException {
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", "{\"startup\":true}");
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      return new JSONObject(new String(out.getContent(), StandardCharsets.UTF_8));
    }
  }

  @IgnoreBean
  private class FixtureSessionStore extends SessionStore {
    public FixtureSessionStore(HttpSession httpSession) {
      super(httpSession);
    }

    //this is entry point #1 of the race condition
    @Override
    protected void doHousekeepingOutsideWriteLock(IClientSession clientSession) {
      writeToProtocol("SessionStore.doHousekeeping");
      super.doHousekeepingOutsideWriteLock(clientSession);
    }

    @Override
    protected void startHousekeepingInsideWriteLock(IClientSession clientSession) {
      if (m_synchronousHousekeeping && clientSession != null) {
        // simulates a housekeeping delay of 0 (the write lock is reentrant)
        doHousekeepingOutsideWriteLock(clientSession);
        return;
      }
      super.startHousekeepingInsideWriteLock(clientSession);
    }
  }

  @IgnoreBean
  private class FixtureUiSession extends TestEnvironmentUiSession {
    //this is entry point #2 of the race condition
    @Override
    protected IClientSession getOrCreateClientSession(HttpSession httpSession, HttpServletRequest req, JsonStartupRequest jsonStartupReq) {
      writeToProtocol("UiSession.getOrCreateClientSession");
      IClientSession clientSession = super.getOrCreateClientSession(httpSession, req, jsonStartupReq);
      if (m_afterGetOrCreateClientSession != null) {
        m_afterGetOrCreateClientSession.accept(clientSession);
      }
      return clientSession;
    }

    @Override
    protected String createUiSessionId(JsonStartupRequest jsonStartupReq) {
      // called right before the session store is obtained in init()
      if (m_beforeSessionStoreLookup != null) {
        m_beforeSessionStoreLookup.run();
      }
      return super.createUiSessionId(jsonStartupReq);
    }

    @Override
    public void init(HttpServletRequest req, HttpServletResponse resp, JsonStartupRequest jsonStartupReq) {
      writeToProtocol("UiSession.init");
      super.init(req, resp, jsonStartupReq);
    }

    @Override
    public void dispose() {
      writeToProtocol("UiSession.dispose");
      super.dispose();
    }

    @Override
    public void verifySubject(HttpServletRequest request) {
      writeToProtocol("UiSession.verifySubject");
      super.verifySubject(request);
    }

    @Override
    public boolean isPersistent() {
      return m_persistentUiSessions || super.isPersistent();
    }

    @Override
    protected void storeHttpSessionIdInCookie(HttpServletResponse resp, HttpSession httpSession, UserAgent userAgent) {
      if (m_initFailure != null) {
        // fails after the client session was assigned to the UI session
        throw m_initFailure;
      }
      super.storeHttpSessionIdInCookie(resp, httpSession, userAgent);
    }
  }

  @IgnoreBean
  private class FixtureClientSession extends AbstractClientSession {
    private boolean m_stopped;

    public FixtureClientSession() {
      super(true);
    }

    @Override
    public FixtureDesktop getDesktop() {
      return (FixtureDesktop) super.getDesktop();
    }

    @Override
    protected void execLoadSession() {
      writeToProtocol("ClientSession.execLoadSession");
      FixtureDesktop desktop = new FixtureDesktop();
      setDesktop(desktop);
    }

    @Override
    protected void execStoreSession() {
      writeToProtocol("ClientSession.execStoreSession");
    }

    @Override
    public void stop(int exitCode) {
      writeToProtocol("ClientSession.stopping");
      super.stop(exitCode);
      writeToProtocol("ClientSession.stopped");
      m_stopped = true;
    }

    public boolean isStopped() {
      return m_stopped;
    }
  }

  @ClassId("11f173db-8c27-4836-a180-c9d2a6252bab")
  private class FixtureDesktop extends AbstractDesktop {
    public FixtureDesktop() {
      super(true);
    }

    @Override
    protected void execOpened() {
      writeToProtocol("Desktop.execOpened");
    }

    @Override
    protected void execClosing() {
      writeToProtocol("Desktop.execClosing");
    }

    @Override
    protected void execGuiAttached() {
      writeToProtocol("Desktop.execGuiAttached");
    }

    @Override
    protected void execGuiDetached() {
      writeToProtocol("Desktop.execGuiDetached");
    }
  }
}
