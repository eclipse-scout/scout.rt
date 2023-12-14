/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.security.auth.Subject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;
import org.eclipse.scout.rt.server.context.HttpServerRunContextProducer;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionCache;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;

/**
 * Test for {@link ServiceTunnelServlet}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ServiceTunnelServletTest {

  private List<IBean<?>> m_beans;

  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;
  private HttpSession m_testHttpSession;

  private ServerSessionProvider m_serverSessionProviderSpy;

  @Before
  public void before() {
    m_serverSessionProviderSpy = spy(BEANS.get(ServerSessionProvider.class));

    m_beans = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(ServerSessionProvider.class)
            .withInitialInstance(m_serverSessionProviderSpy)
            .withApplicationScoped(true));

    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    m_testHttpSession = mock(HttpSession.class);
    when(m_requestMock.getSession()).thenReturn(m_testHttpSession);
    when(m_requestMock.getSession(true)).thenReturn(m_testHttpSession);
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
  }

  @Test
  public void testNewSessionCreatedOnLookupHttpSession() {
    createServletRunContext(m_requestMock, m_responseMock).run(() -> {
      final ServerRunContext runContext = ServerRunContexts.copyCurrent().withClientNodeId(NodeId.of("testNodeId"));
      IServerSession session = BEANS.get(HttpServerRunContextProducer.class).getOrCreateScoutSession(m_requestMock, runContext, "testid");
      assertNotNull(session);
    });
  }

  /**
   * If a {@link IServerSession} already exists, no new session should be created.
   */
  @Test
  public void testNoNewServerSessionOnLookup() {
    RunContexts
        .empty()
        .run(() -> {
          IServerSession session1 = BEANS.get(HttpServerRunContextProducer.class).getOrCreateScoutSession(m_requestMock, ServerRunContexts.empty(), "id1");
          IServerSession session2 = BEANS.get(HttpServerRunContextProducer.class).getOrCreateScoutSession(m_requestMock, ServerRunContexts.empty(), "id1");
          assertNotNull(session1);
          assertSame(session1, session2);
        });
  }

  /**
   * Calls {@link HttpRunContextProducer#getOrCreateScoutSession(HttpServletRequest, ServerRunContext, String) in 4
   * different threads within the same HTTP session. Test ensures that the same server session is returned in all
   * threads and that {@link ServerSessionProvider#provide(ServerRunContext)}} is called only once.
   */
  @Test
  public void testLookupScoutServerSessionOnHttpSessionMultipleThreads() {
    final Map<String, IServerSession> cache = new HashMap<>();

    final TestServerSession testServerSession = new TestServerSession();
    testServerSession.start("testSessionId");
    testServerSession.setSharedContextVariable("userId", String.class, "testUser");

    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    HttpSession testHttpSession = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(testHttpSession);
    when(requestMock.getSession(true)).thenReturn(testHttpSession);

    doAnswer(putValueInCache(cache)).when(testHttpSession).setAttribute(ArgumentMatchers.eq(ServerSessionCache.SERVER_SESSION_KEY), ArgumentMatchers.any());
    when(testHttpSession.getAttribute(ServerSessionCache.SERVER_SESSION_KEY)).thenAnswer(getCachedValue(cache));

    doAnswer(slowCreateTestsession(testServerSession)).when(m_serverSessionProviderSpy).opt(ArgumentMatchers.anyString(), ArgumentMatchers.any(ServerRunContext.class));
    List<HttpSessionLookupCallable> jobs = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      jobs.add(new HttpSessionLookupCallable(requestMock, m_responseMock));
    }

    List<IFuture<?>> futures = scheduleAndJoinJobs(jobs);

    Set<IServerSession> serverSessions = new HashSet<>();
    for (IFuture<?> future : futures) {
      serverSessions.add((IServerSession) future.awaitDoneAndGet());
    }

    assertEquals(CollectionUtility.hashSet(testServerSession), serverSessions);

    verify(m_serverSessionProviderSpy, times(1)).opt(ArgumentMatchers.anyString(), ArgumentMatchers.any(ServerRunContext.class));
  }

  @Test
  public void testPostSuccessful() {
    ServiceTunnelServlet s = new ServiceTunnelServlet();
    Class[] parameterTypes = new Class[]{String.class};
    Object[] args = new Object[]{"test"};
    ServiceTunnelRequest req = new ServiceTunnelRequest(IPingService.class.getName(), "ping", parameterTypes, args);
    req.setUserAgent(UserAgents.createDefault().createIdentifier());
    ServiceTunnelResponse res = s.doPost(req);
    assertEquals("test", res.getData());
    assertNull(res.getException());
    assertEquals(0, res.getNotifications().size());
  }

  private Answer<IServerSession> slowCreateTestsession(final TestServerSession testSession) {
    return invocation -> {
      Thread.sleep(2000); // simulate long running task
      return testSession;
    };
  }

  private Answer<Object> putValueInCache(final Map<String, IServerSession> cache) {
    return invocation -> {
      Object[] args = invocation.getArguments();
      String key = (String) args[0];
      IServerSession value = (IServerSession) args[1];
      cache.put(key, value);
      return null;
    };
  }

  private Answer<Object> getCachedValue(final Map<String, IServerSession> cache) {
    return invocation -> {
      Object[] args = invocation.getArguments();
      String key = (String) args[0];
      return cache.get(key);
    };
  }

  private List<IFuture<?>> scheduleAndJoinJobs(List<? extends Callable<?>> jobs) {
    List<IFuture<?>> futures = new ArrayList<>();

    for (Callable<?> job : jobs) {
      futures.add(Jobs.schedule(job, Jobs.newInput()
          .withRunContext(RunContexts.copyCurrent())));
    }

    for (IFuture<?> future : futures) {
      future.awaitDoneAndGet();
    }

    return futures;
  }

  private static class HttpSessionLookupCallable implements Callable<IServerSession> {
    private final HttpServletRequest m_request;
    private final HttpServletResponse m_response;

    public HttpSessionLookupCallable(HttpServletRequest request, HttpServletResponse response) {
      m_request = request;
      m_response = response;
    }

    @Override
    public IServerSession call() {
      return createServletRunContext(m_request, m_response).call(() -> BEANS.get(HttpServerRunContextProducer.class)
          .getOrCreateScoutSession(m_request, ServerRunContexts.empty().withClientNodeId(NodeId.of("testNodeId")), "testSessionId"));
    }
  }

  private static RunContext createServletRunContext(final HttpServletRequest req, final HttpServletResponse resp) {
    return RunContexts.copyCurrent(true)
        .withSubject(Subject.getSubject(AccessController.getContext()))
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, req)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, resp)
        .withDiagnostics(BEANS.get(ServletDiagnosticsProviderFactory.class).getProviders(req, resp));
  }

}
