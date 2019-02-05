package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil.ICondition;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingDelayProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreHousekeepingMaxWaitShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitAllShutdownProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.SessionStoreMaxWaitWriteLockProperty;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestHandler;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.UnloadRequestHandler;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.eclipse.scout.rt.ui.html.json.testing.TestEnvironmentUiSession;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWithNewPlatform()
@RunWith(PlatformTestRunner.class)
public class UiSessionInitAndDisposeTest {
  private List<IBean<?>> m_beans;

  private List<String> m_protocol = Collections.synchronizedList(new ArrayList<String>());

  @Before
  public void before() {
    m_protocol.clear();
    m_beans = TestingUtility.registerBeans(
        new BeanMetaData(JobCompletionDelayOnSessionShutdown.class).withProducer(new IBeanInstanceProducer<JobCompletionDelayOnSessionShutdown>() {
          @Override
          public JobCompletionDelayOnSessionShutdown produce(IBean<JobCompletionDelayOnSessionShutdown> bean) {
            return new JobCompletionDelayOnSessionShutdown() {
              @Override
              public Long getDefaultValue() {
                return 10L;
              }
            };
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingDelayProperty.class).withInitialInstance(new SessionStoreHousekeepingDelayProperty() {
          @Override
          public Integer getDefaultValue() {
            return 2;
          }
        }),

        new BeanMetaData(SessionStoreHousekeepingMaxWaitShutdownProperty.class).withInitialInstance(new SessionStoreHousekeepingMaxWaitShutdownProperty() {
          @Override
          public Integer getDefaultValue() {
            return 10;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitWriteLockProperty.class).withInitialInstance(new SessionStoreMaxWaitWriteLockProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(SessionStoreMaxWaitAllShutdownProperty.class).withInitialInstance(new SessionStoreMaxWaitAllShutdownProperty() {
          @Override
          public Integer getDefaultValue() {
            return 1;
          }
        }),

        new BeanMetaData(HttpSessionHelper.class).withProducer(new IBeanInstanceProducer<HttpSessionHelper>() {
          @Override
          public HttpSessionHelper produce(IBean<HttpSessionHelper> bean) {
            return new HttpSessionHelper() {
              @Override
              protected ISessionStore createSessionStore(HttpSession httpSession) {
                return new FixtureSessionStore(httpSession);
              }
            };
          }
        }),

        new BeanMetaData(FixtureUiSession.class).withProducer(new IBeanInstanceProducer<FixtureUiSession>() {
          @Override
          public FixtureUiSession produce(IBean<FixtureUiSession> bean) {
            return new FixtureUiSession();
          }
        }),

        new BeanMetaData(FixtureClientSession.class).withProducer(new IBeanInstanceProducer<FixtureClientSession>() {
          @Override
          public FixtureClientSession produce(IBean<FixtureClientSession> bean) {
            return new FixtureClientSession();
          }
        }));
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_beans);
    m_protocol.clear();
  }

  private void writeToProtocol(String line) {
    System.out.println("protocol: " + line);
    m_protocol.add(line);
  }

  @Test
  public void testStartupThenModelStop() throws ServletException, IOException {
    final HttpSession httpSession = JsonTestUtility.createHttpSession(new Object());
    final JsonMessageRequestHandler messageHandler = new JsonMessageRequestHandler();

    //startup
    final String clientSessionId;
    final String uiSessionId;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", "{\"startup\":true}");
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), "UTF-8");
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
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();

    //model stop
    ModelJobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        IDesktop.CURRENT.get().getUIFacade().closeFromUI(true);
      }
    }, ModelJobs.newInput(ClientRunContexts.empty().withSession(store.getClientSessionMap().get(clientSessionId), true)))
        .awaitDone();

    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return store.isEmpty() && !store.isHttpSessionValid();
      }
    });

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
  public void testStartupThenBrowserTabClose() throws ServletException, IOException {
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
      String respContent = new String(out.getContent(), "UTF-8");
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
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();

    //brower tab closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }

    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return store.isEmpty() && !store.isHttpSessionValid();
      }
    });

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
  public void testStartupThenBrowserTabReload() throws ServletException, IOException {
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
      String respContent = new String(out.getContent(), "UTF-8");
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
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();

    //brower tab reload -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }

    //json startup with same client session
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionId);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), "UTF-8");
      uiSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionId = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "UiSession.dispose",
            "Desktop.execGuiDetached",
            "UiSession.init",
            "UiSession.getOrCreateClientSession",
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();
    assertEquals(1, store.countClientSessions());
    assertEquals(1, store.countUiSessions());

    //brower tab closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionId, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return store.isEmpty() && !store.isHttpSessionValid();
      }
    });

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
  public void testStartupThenBrowserTabDuplicate() throws ServletException, IOException {
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
      String respContent = new String(out.getContent(), "UTF-8");
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
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();

    //brower tab duplicate -> json startup with same client session
    // this results in two UiSessions attached to the same client session
    final String clientSessionIdB;
    final String uiSessionIdB;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionIdA);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), "UTF-8");
      uiSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession"
        // "Desktop.execGuiAttached" -> this is not called because there is already uiSessionA attached to the clientSession
        ),
        m_protocol);
    m_protocol.clear();
    assertEquals(clientSessionIdA, clientSessionIdB);
    assertEquals(1, store.countClientSessions());
    assertNotEquals(uiSessionIdA, uiSessionIdB);
    assertEquals(2, store.countUiSessions());

    //brower tab A closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdA, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    //brower tab B closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdB, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return store.isEmpty() && !store.isHttpSessionValid();
      }
    });

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
  public void testStartupThenBrowserTabDuplicateThenBrowserTabClose() throws ServletException, IOException {
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
      String respContent = new String(out.getContent(), "UTF-8");
      uiSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdA = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }
    final ISessionStore store = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
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
            "Desktop.execGuiAttached"),
        m_protocol);
    m_protocol.clear();

    //brower tab duplicate -> json startup with same client session
    // this results in two UiSessions attached to the same client session
    final String clientSessionIdB;
    final String uiSessionIdB;
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      String jsonData = "{\"startup\":true, \"clientSessionId\":\"$clientSessionId\"}"
          .replace("$clientSessionId", clientSessionIdA);
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/json", jsonData);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      messageHandler.handlePost(req, resp);
      String respContent = new String(out.getContent(), "UTF-8");
      uiSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("uiSessionId");
      clientSessionIdB = new JSONObject(respContent).getJSONObject("startupData").getString("clientSessionId");
    }

    assertEquals(
        Arrays.asList(
            "UiSession.init",
            "UiSession.getOrCreateClientSession"
        // "Desktop.execGuiAttached" -> this is not called because there is already uiSessionA attached to the clientSession
        ),
        m_protocol);
    m_protocol.clear();
    assertEquals(clientSessionIdA, clientSessionIdB);
    assertEquals(1, store.countClientSessions());
    assertNotEquals(uiSessionIdA, uiSessionIdB);
    assertEquals(2, store.countUiSessions());

    //brower tab A closed -> json unload
    final IClientSession clientSessionA = store.getClientSessionMap().get(clientSessionIdA);
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdA, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        int n = Jobs.getJobManager()
            .getFutures(Jobs.newFutureFilterBuilder()
                .andMatchExecutionSemaphore(clientSessionA.getModelJobSemaphore())
                .toFilter())
            .size();
        return n == 0;
      }
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

    //brower tab B closed -> json unload
    try (BufferedServletOutputStream out = new BufferedServletOutputStream()) {
      final HttpServletRequest req = JsonTestUtility.createHttpServletRequest(httpSession, "/unload/" + uiSessionIdB, null);
      final HttpServletResponse resp = JsonTestUtility.createHttpServletResponse(out);
      unloadHandler.handlePost(req, resp);
    }
    JobTestUtil.waitForCondition(new ICondition() {
      @Override
      public boolean isFulfilled() {
        return store.isEmpty() && !store.isHttpSessionValid();
      }
    });

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

  @IgnoreBean
  private class FixtureSessionStore extends SessionStore {
    public FixtureSessionStore(HttpSession httpSession) {
      super(httpSession);
    }

    //this is entry point #1 of the race condition
    @Override
    protected void doHousekeeping(IClientSession clientSession) {
      writeToProtocol("SessionStore.doHousekeeping");
      super.doHousekeeping(clientSession);
    }
  }

  @IgnoreBean
  private class FixtureUiSession extends TestEnvironmentUiSession {
    //this is entry point #2 of the race condition
    @Override
    protected IClientSession getOrCreateClientSession(HttpSession httpSession, HttpServletRequest req, JsonStartupRequest jsonStartupReq) {
      writeToProtocol("UiSession.getOrCreateClientSession");
      return super.getOrCreateClientSession(httpSession, req, jsonStartupReq);
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
  }

  @IgnoreBean
  private class FixtureClientSession extends AbstractClientSession {

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
    }
  }

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
