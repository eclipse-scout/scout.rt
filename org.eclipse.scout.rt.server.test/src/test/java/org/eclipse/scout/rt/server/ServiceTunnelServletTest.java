/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.job.IExecutable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.JobInput;
import org.eclipse.scout.commons.job.internal.JobManager;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.server.commons.cache.ICacheEntry;
import org.eclipse.scout.rt.server.commons.cache.StickySessionCacheService;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test for {@link ServiceTunnelServlet}
 */
@RunWith(PlatformTestRunner.class)
public class ServiceTunnelServletTest {

  private static final int TEST_SERVICE_RANKING = 1000;
  private List<IBean<?>> m_serviceReg;
  private ServiceTunnelServlet m_testServiceTunnelServlet;
  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;
  private HttpSession m_testHttpSession;

  private IJobManager<JobInput> m_jobManager;

  @Before
  public void before() throws ServletException {
    m_serviceReg = TestingUtility.registerServices(TEST_SERVICE_RANKING, new StickySessionCacheService(), new AbstractAccessControlService() {
    });
    m_testServiceTunnelServlet = getServiceTunnelServletWithTestSession();
    m_testServiceTunnelServlet.lazyInit(null, null);
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    m_testHttpSession = mock(HttpSession.class);
    when(m_requestMock.getSession()).thenReturn(m_testHttpSession);
    when(m_requestMock.getSession(true)).thenReturn(m_testHttpSession);

    m_jobManager = new JobManager<>("test-manager");
  }

  @After
  public void after() {
    TestingUtility.unregisterServices(m_serviceReg);

    m_jobManager.shutdown();
  }

  @Test
  public void testNewSessionCreatedOnLookupHttpSession() throws ProcessingException, ServletException {
    IServerSession session = m_testServiceTunnelServlet.lookupScoutServerSessionOnHttpSession(ServerJobInput.empty().servletRequest(m_requestMock).servletResponse(m_responseMock));
    assertNotNull(session);
  }

  @Test
  public void testNoNewServerSessionOnLookup() throws ProcessingException, ServletException {
    TestServerSession testSession = new TestServerSession();
    ICacheEntry cacheMock = mock(ICacheEntry.class);
    when(cacheMock.getValue()).thenReturn(testSession);
    when(cacheMock.isActive()).thenReturn(true);

    when(m_testHttpSession.getAttribute(IServerSession.class.getName())).thenReturn(cacheMock);
    IServerSession session = m_testServiceTunnelServlet.lookupScoutServerSessionOnHttpSession(ServerJobInput.empty().servletRequest(m_requestMock).servletResponse(m_responseMock));
    assertEquals(testSession, session);
  }

  /**
   * Calls
   * {@link ServiceTunnelServlet#lookupScoutServerSessionOnHttpSession(HttpServletRequest, HttpServletResponse, Subject, UserAgent)}
   * in 4
   * different threads within the same HTTP session.
   * Test ensures that the same server session is returned in all threads and that
   * {@link IServerSessionRegistryService#newServerSession(Class, ServerJobInput)} is called only once.
   */
  @Test
  public void testLookupScoutServerSessionOnHttpSessionMultipleThreads() throws ProcessingException, ServletException, InterruptedException {
    final Map<String, ICacheEntry<?>> cache = new HashMap<String, ICacheEntry<?>>();
    final TestServerSession testSession = new TestServerSession();
    HttpServletRequest requestMock = mock(HttpServletRequest.class);
    HttpSession testHttpSession = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(testHttpSession);
    when(requestMock.getSession(true)).thenReturn(testHttpSession);

    ICacheEntry cacheEntryMock = mock(ICacheEntry.class);
    when(cacheEntryMock.getValue()).thenReturn(testSession);
    when(cacheEntryMock.isActive()).thenReturn(true);

    doAnswer(putValueInCache(cache)).when(testHttpSession).setAttribute(eq(IServerSession.class.getName()), anyObject());
    when(testHttpSession.getAttribute(IServerSession.class.getName())).thenAnswer(getCachedValue(cache));

    IServerSessionRegistryService serverSessionRegistryServiceMock = mock(IServerSessionRegistryService.class);
    when(serverSessionRegistryServiceMock.newServerSession(eq(TestServerSession.class), any(ServerJobInput.class))).thenAnswer(slowCreateTestsession(testSession));

    List<IBean<?>> registerServices = TestingUtility.registerServices(TEST_SERVICE_RANKING, serverSessionRegistryServiceMock);
    try {
      List<HttpSessionLookupRunnable> jobs = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        jobs.add(new HttpSessionLookupRunnable(m_testServiceTunnelServlet, requestMock, m_responseMock));
      }

      List<IFuture<?>> futures = scheduleAndJoinJobs(jobs);

      Set<IServerSession> serverSessions = new HashSet<IServerSession>();
      for (IFuture<?> future : futures) {
        serverSessions.add((IServerSession) future.get());
      }

      assertEquals(1, serverSessions.size());
      assertTrue(serverSessions.contains(testSession));

      verify(serverSessionRegistryServiceMock, times(1)).newServerSession(eq(TestServerSession.class), any(ServerJobInput.class));
    }
    finally {
      TestingUtility.unregisterServices(registerServices);
    }
  }

  private Answer<IServerSession> slowCreateTestsession(final TestServerSession testSession) {
    return new Answer<IServerSession>() {
      @Override
      public IServerSession answer(InvocationOnMock invocation) throws Throwable {
        Thread.sleep(2000); // simulate long running task
        return testSession;
      }
    };
  }

  private Answer<Object> putValueInCache(final Map<String, ICacheEntry<?>> cache) {
    return new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        String key = (String) args[0];
        ICacheEntry<?> value = (ICacheEntry<?>) args[1];
        cache.put(key, value);
        return null;
      }
    };
  }

  private Answer<Object> getCachedValue(final Map<String, ICacheEntry<?>> cache) {
    return new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        String key = (String) args[0];
        return cache.get(key);
      }
    };
  }

  private ServiceTunnelServlet getServiceTunnelServletWithTestSession() {
    return new TestServiceTunnelServlet(false);
  }

  private List<IFuture<?>> scheduleAndJoinJobs(List<? extends IExecutable<?>> jobs) throws ProcessingException {
    List<IFuture<?>> futures = new ArrayList<>();

    for (IExecutable<?> job : jobs) {
      futures.add(m_jobManager.schedule(job));
    }

    for (IFuture<?> future : futures) {
      future.get();
    }

    return futures;
  }

  private static class HttpSessionLookupRunnable implements ICallable<IServerSession> {

    private final ServiceTunnelServlet m_serviceTunnelServlet;
    private final HttpServletRequest m_request;
    private final HttpServletResponse m_response;

    public HttpSessionLookupRunnable(ServiceTunnelServlet serviceTunnelServlet, HttpServletRequest request, HttpServletResponse response) {
      m_serviceTunnelServlet = serviceTunnelServlet;
      m_request = request;
      m_response = response;
    }

    @Override
    public IServerSession call() throws Exception {
      return m_serviceTunnelServlet.lookupScoutServerSessionOnHttpSession(ServerJobInput.empty().servletRequest(m_request).servletResponse(m_response));
    }
  }

  private static class TestServiceTunnelServlet extends ServiceTunnelServlet {
    private static final long serialVersionUID = 1L;

    public TestServiceTunnelServlet(boolean debug) {
      super(debug);
    }

    @Override
    public ServletConfig getServletConfig() {
      // TODO [dwi]: remove me once there is only one session class.
      ServletConfig config = mock(ServletConfig.class);
      when(config.getInitParameter(eq("session"))).thenReturn(TestServerSession.class.getName());
      return config;
    }
  }
}
