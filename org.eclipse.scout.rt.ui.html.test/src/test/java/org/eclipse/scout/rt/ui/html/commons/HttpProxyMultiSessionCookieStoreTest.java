/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.commons;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.IntStream;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.core5.http.Method;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.commons.BufferedServletOutputStream;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxy;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.eclipse.scout.rt.shared.http.ApacheMultiSessionCookieStore;
import org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.mock.RegisterBeanTestRule;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

/**
 * Additional test cases for {@link HttpProxy} in combination with {@link ApacheMultiSessionCookieStore}.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class HttpProxyMultiSessionCookieStoreTest {

  private HttpProxy m_proxy;
  private Server m_server;
  private Handler.Collection m_handlerCollection;

  private static final TestApacheMultiSessionCookieStore COOKIE_STORE = spy(new TestApacheMultiSessionCookieStore());

  @ClassRule // use a fixed cookie store for all tests (with accessible default cookie store)
  public static final RegisterBeanTestRule<ApacheMultiSessionCookieStore> COOKIE_STORE_REGISTER_BEAN_TEST_RULE = new RegisterBeanTestRule<>(ApacheMultiSessionCookieStore.class, COOKIE_STORE);

  @Before
  public void before() {
    m_proxy = spy(BEANS.get(HttpProxy.class));
    m_proxy.withHttpClientManager(BEANS.get(DefaultAsyncHttpClientManager.class));

    // we use a static spy mocked bean, therefore reset it for each test
    reset(COOKIE_STORE);
    COOKIE_STORE.m_defaultCookieStore.clear();
  }

  @Before
  public void createHttpServer() throws Exception {
    m_server = new Server();
    m_handlerCollection = new Handler.Sequence();
    m_server.setHandler(m_handlerCollection);
    @SuppressWarnings("resource")
    ServerConnector connector = new ServerConnector(m_server, new HttpConnectionFactory());
    m_server.setConnectors(new Connector[]{connector});
    m_server.start();
  }

  @After
  public void uninstallHttpServer() throws Exception {
    if (m_server == null) {
      return;
    }

    m_server.stop();
    m_server = null;
  }

  @Test
  public void testProxyRequest_cookiesSessionBasedCookieStore() {
    IClientSession session = mock(IClientSession.class);
    RunContext ctx = ClientRunContexts.copyCurrent(true)
        .withSession(session, false);
    ctx.run(() -> testProxyRequest_cookiesInternal(false));
  }

  @Test
  public void testProxyRequest_cookiesDefaultCookieStore() {
    RunContext ctx = ClientRunContexts.copyCurrent(true)
        .withSession(null, false);
    ctx.run(() -> testProxyRequest_cookiesInternal(true));
  }

  protected void testProxyRequest_cookiesInternal(boolean expectDefaultCookieStore) {
    CookieStore defaultCookieStore = COOKIE_STORE.m_defaultCookieStore;
    int previousDefaultCount = defaultCookieStore.getCookies().size();

    Handler.Abstract handler = new Handler.Abstract() {
      @Override
      public boolean handle(Request request, Response response, Callback callback) {
        Response.addCookie(response, HttpCookie.from("snickers", "bar"));
        response.write(true, null, callback); // flush buffer
        return true;
      }
    };

    // call the request
    testProxyRequestInternal(handler, 1, "/");

    // one cookie should have been added, check expectations
    ArgumentCaptor<org.apache.hc.client5.http.cookie.Cookie> cookieCaptor = ArgumentCaptor.forClass(org.apache.hc.client5.http.cookie.Cookie.class);
    verify(COOKIE_STORE, times(1)).addCookie(cookieCaptor.capture());
    assertEquals("snickers", cookieCaptor.getValue().getName());
    assertEquals("bar", cookieCaptor.getValue().getValue());

    // never-ever add a cookie to the default store (session specific should have been used)
    assertEquals(previousDefaultCount + (expectDefaultCookieStore ? 1 : 0), defaultCookieStore.getCookies().size());
  }

  protected HttpServletResponse testProxyRequestInternal(Handler handler, int numberOfRequests, String pathInfo) {
    return testProxyRequestInternal(handler, 30 * 1000L, numberOfRequests, pathInfo);
  }

  protected HttpServletResponse testProxyRequestInternal(Handler handler, long timeoutUntilCompletion, int numberOfRequests, String pathInfo) {
    return testProxyRequestInternal(handler, timeoutUntilCompletion, numberOfRequests, prepareHttpRequest(pathInfo != null ? pathInfo : "/"));
  }

  protected HttpServletResponse testProxyRequestInternal(Handler handler, long timeoutUntilCompletion, int numberOfRequests, HttpServletRequest httpReq) {
    try {
      m_handlerCollection.addHandler(handler);
      m_proxy.withRemoteBaseUrl(m_server.getURI().toString());

      HttpServletResponse httpResp = mock(HttpServletResponse.class);
      BufferedServletOutputStream outputStream = new BufferedServletOutputStream();
      when(httpResp.getOutputStream()).thenReturn(outputStream);

      IntStream.range(0, numberOfRequests).parallel().forEach(i -> {
        try {
          m_proxy.proxy(httpReq, httpResp, mock(HttpProxyRequestOptions.class));
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      verify(httpReq.getAsyncContext(), timeout(timeoutUntilCompletion).times(numberOfRequests)).complete();
      return httpResp;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      m_handlerCollection.removeHandler(handler);
    }
  }

  protected HttpServletRequest prepareHttpRequest(String pathInfo) {
    AsyncContext asyncContext = mock(AsyncContext.class);
    HttpServletRequest httpReq = mock(HttpServletRequest.class);

    when(httpReq.getMethod()).thenReturn(Method.GET.toString());
    when(httpReq.getPathInfo()).thenReturn(pathInfo);
    when(httpReq.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(httpReq.startAsync(any(), any())).thenReturn(asyncContext);
    when(httpReq.getAsyncContext()).thenReturn(asyncContext);

    return httpReq;
  }

  @IgnoreBean
  public static class TestApacheMultiSessionCookieStore extends ApacheMultiSessionCookieStore {

    private CookieStore m_defaultCookieStore;

    @Override
    protected CookieStore createDefaultCookieStore() {
      m_defaultCookieStore = super.createDefaultCookieStore();
      return m_defaultCookieStore;
    }

    public CookieStore getDefaultCookieStore() {
      return m_defaultCookieStore;
    }
  }
}
