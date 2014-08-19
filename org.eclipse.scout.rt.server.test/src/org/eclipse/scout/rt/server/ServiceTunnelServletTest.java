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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.server.commons.cache.ICacheEntry;
import org.eclipse.scout.rt.server.commons.cache.StickySessionCacheService;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.ServiceRegistration;

/**
 * Test for {@link ServiceTunnelServlet}
 */
public class ServiceTunnelServletTest {

  private static final int TEST_SERVICE_RANKING = 1000;
  private List<ServiceRegistration> m_serviceReg;
  private ServiceTunnelServlet m_testServiceTunnelServlet;
  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;
  private HttpSession m_testHttpSession;

  @Before
  public void setup() throws ServletException {
    m_serviceReg = TestingUtility.registerServices(Activator.getDefault().getBundle(), TEST_SERVICE_RANKING, new StickySessionCacheService(), new AbstractAccessControlService());
    m_testServiceTunnelServlet = getServiceTunnelServletWithTestSession();
    m_testServiceTunnelServlet.lazyInit(null, null);
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    m_testHttpSession = mock(HttpSession.class);
    when(m_requestMock.getSession()).thenReturn(m_testHttpSession);
    when(m_requestMock.getSession(true)).thenReturn(m_testHttpSession);
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterServices(m_serviceReg);
  }

  @Test
  public void testNewSessionCreatedOnLookupHttpSession() throws ProcessingException, ServletException {
    IServerSession session = m_testServiceTunnelServlet.lookupScoutServerSessionOnHttpSession(m_requestMock, m_responseMock, null, null);
    assertNotNull(session);
  }

  @Test
  public void testNoNewServerSessionOnLookup() throws ProcessingException, ServletException {
    TestServerSession testSession = new TestServerSession();
    ICacheEntry cacheMock = mock(ICacheEntry.class);
    when(cacheMock.getValue()).thenReturn(testSession);
    when(cacheMock.isActive()).thenReturn(true);

    when(m_testHttpSession.getAttribute(IServerSession.class.getName())).thenReturn(cacheMock);
    IServerSession session = m_testServiceTunnelServlet.lookupScoutServerSessionOnHttpSession(m_requestMock, m_responseMock, null, null);
    assertEquals(testSession, session);
  }

  /**
   * Calls
   * {@link ServiceTunnelServlet#lookupScoutServerSessionOnHttpSession(HttpServletRequest, HttpServletResponse, Subject, UserAgent)}
   * in 4
   * different threads within the same HTTP session.
   * Test ensures that the same server session is returned in all threads and that
   * {@link IServerSessionRegistryService#newServerSession(Class, Subject, UserAgent)} is called only once.
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
    when(serverSessionRegistryServiceMock.newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class))).thenAnswer(slowCreateTestsession(testSession));

    List<ServiceRegistration> registerServices = TestingUtility.registerServices(Activator.getDefault().getBundle(), TEST_SERVICE_RANKING, serverSessionRegistryServiceMock);
    try {
      List<HttpSessionJob> jobs = new ArrayList<HttpSessionJob>();
      for (int i = 0; i < 4; i++) {
        jobs.add(new HttpSessionJob("HttpSessionJobId " + i, m_testServiceTunnelServlet, requestMock, m_responseMock));
      }

      scheduleAndJoinJobs(jobs);

      Set<IServerSession> serverSessions = new HashSet<IServerSession>();
      for (int i = 0; i < jobs.size(); i++) {
        serverSessions.add(jobs.get(i).getServerSession());
      }

      assertEquals(1, serverSessions.size());
      assertTrue(serverSessions.contains(testSession));

      verify(serverSessionRegistryServiceMock, times(1)).newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class));
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

  @Test
  public void testVirtualSessionLookupSingleThread() throws ProcessingException, ServletException {
    final TestServerSession testSession = new TestServerSession();
    final TestServerSession anotherTestSession = new TestServerSession();

    ServiceTunnelRequest serviceTunnelRequestMock = mock(ServiceTunnelRequest.class);
    when(serviceTunnelRequestMock.getVirtualSessionId()).thenReturn("VirtualSession1", "VirtualSession2", "VirtualSession1", "VirtualSession2");
    when(serviceTunnelRequestMock.getUserAgent()).thenReturn("SWING|DESKTOP|Windows7");

    IServerSessionRegistryService serverSessionRegistryServiceMock = mock(IServerSessionRegistryService.class);
    when(serverSessionRegistryServiceMock.newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class))).thenReturn(testSession, anotherTestSession, testSession, anotherTestSession);

    List<ServiceRegistration> registerServices = TestingUtility.registerServices(Activator.getDefault().getBundle(), TEST_SERVICE_RANKING, serverSessionRegistryServiceMock);
    try {
      IServerSession session = m_testServiceTunnelServlet.lookupServerSession(m_requestMock, m_responseMock, null, serviceTunnelRequestMock);
      assertEquals(testSession, session);

      session = m_testServiceTunnelServlet.lookupServerSession(m_requestMock, m_responseMock, null, serviceTunnelRequestMock);
      assertEquals(anotherTestSession, session);

      session = m_testServiceTunnelServlet.lookupServerSession(m_requestMock, m_responseMock, null, serviceTunnelRequestMock);
      assertEquals(testSession, session);

      session = m_testServiceTunnelServlet.lookupServerSession(m_requestMock, m_responseMock, null, serviceTunnelRequestMock);
      assertEquals(anotherTestSession, session);

      verify(serverSessionRegistryServiceMock, times(2)).newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class));
    }
    finally {
      TestingUtility.unregisterServices(registerServices);
    }
  }

  /**
   * Calls
   * {@link ServiceTunnelServlet#lookupServerSession(HttpServletRequest, HttpServletResponse, Subject, ServiceTunnelRequest)}
   * in 4 different threads accessing from 2 different server sessions.
   * Test ensures that 2 server sessions are returned in all threads and that
   * {@link IServerSessionRegistryService#newServerSession(Class, Subject, UserAgent)} is called only twice.
   */
  @Test
  public void testVirtualSessionLookupMultipleThreads() throws ProcessingException, ServletException, InterruptedException {
    final TestServerSession testSession = new TestServerSession();
    final TestServerSession anotherTestSession = new TestServerSession();

    final ServiceTunnelRequest serviceTunnelRequestMock = mock(ServiceTunnelRequest.class);
    when(serviceTunnelRequestMock.getVirtualSessionId()).thenReturn("VirtualSession1", "VirtualSession2", "VirtualSession1", "VirtualSession2");
    when(serviceTunnelRequestMock.getUserAgent()).thenReturn("SWING|DESKTOP|Windows7");
    when(m_requestMock.getSession().getMaxInactiveInterval()).thenReturn(6000); // set large timeout

    IServerSessionRegistryService serverSessionRegistryServiceMock = mock(IServerSessionRegistryService.class);
    when(serverSessionRegistryServiceMock.newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class))).thenAnswer(testOrOtherSession(testSession, anotherTestSession));

    List<ServiceRegistration> registerServices = TestingUtility.registerServices(Activator.getDefault().getBundle(), TEST_SERVICE_RANKING, serverSessionRegistryServiceMock);
    try {
      List<VirtualSessionJob> jobs = new ArrayList<VirtualSessionJob>();
      for (int i = 0; i < 4; i++) {
        jobs.add(new VirtualSessionJob("VirtualSessionJobId " + i, m_testServiceTunnelServlet, m_requestMock, m_responseMock, serviceTunnelRequestMock));
      }

      scheduleAndJoinJobs(jobs);

      Set<IServerSession> serverSessions = new HashSet<IServerSession>();
      for (int i = 0; i < jobs.size(); i++) {
        serverSessions.add(jobs.get(i).getServerSession());
      }

      assertEquals(2, serverSessions.size());
      assertTrue(serverSessions.contains(testSession));
      assertTrue(serverSessions.contains(anotherTestSession));

      verify(serverSessionRegistryServiceMock, times(2)).newServerSession(eq(TestServerSession.class), any(Subject.class), any(UserAgent.class));
    }
    finally {
      TestingUtility.unregisterServices(registerServices);
    }
  }

  private Answer<IServerSession> testOrOtherSession(final TestServerSession testSession, final TestServerSession anotherTestSession) {
    return new Answer<IServerSession>() {
      private int m_count = 0;

      @Override
      public IServerSession answer(InvocationOnMock invocation) throws Throwable {
        Thread.sleep(2000); // simulate long running task
        IServerSession result = null;
        if (m_count % 2 == 0) {
          result = testSession;
        }
        else {
          result = anotherTestSession;
        }
        m_count++;
        return result;
      }
    };
  }

  private ServiceTunnelServlet getServiceTunnelServletWithTestSession() {
    return new TestServiceTunnelServlet();
  }

  private void scheduleAndJoinJobs(List<? extends JobEx> jobs) throws InterruptedException {
    for (int i = 0; i < jobs.size(); i++) {
      jobs.get(i).schedule();
    }

    for (int i = 0; i < jobs.size(); i++) {
      jobs.get(i).join();
    }
  }

  private static class VirtualSessionJob extends JobEx {

    private final ServiceTunnelServlet m_serviceTunnelServlet;
    private final HttpServletRequest m_request;
    private final HttpServletResponse m_response;
    private final ServiceTunnelRequest m_serviceTunnelRequest;

    private IServerSession m_serverSession;

    public VirtualSessionJob(String name, ServiceTunnelServlet serviceTunnelServlet, HttpServletRequest request, HttpServletResponse response, ServiceTunnelRequest serviceTunnelRequest) {
      super(name);
      m_serviceTunnelServlet = serviceTunnelServlet;
      m_request = request;
      m_response = response;
      m_serviceTunnelRequest = serviceTunnelRequest;
    }

    public IServerSession getServerSession() {
      return m_serverSession;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        m_serverSession = m_serviceTunnelServlet.lookupServerSession(m_request, m_response, null, m_serviceTunnelRequest);
        return Status.OK_STATUS;
      }
      catch (ProcessingException e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error executing lookup virtual session", e);
      }
      catch (ServletException e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error executing lookup virtual session", e);
      }
    }
  }

  private static class HttpSessionJob extends JobEx {

    private final ServiceTunnelServlet m_serviceTunnelServlet;
    private final HttpServletRequest m_request;
    private final HttpServletResponse m_response;

    private IServerSession m_serverSession;

    public HttpSessionJob(String name, ServiceTunnelServlet serviceTunnelServlet, HttpServletRequest request, HttpServletResponse response) {
      super(name);
      m_serviceTunnelServlet = serviceTunnelServlet;
      m_request = request;
      m_response = response;
    }

    public IServerSession getServerSession() {
      return m_serverSession;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        m_serverSession = m_serviceTunnelServlet.lookupScoutServerSessionOnHttpSession(m_request, m_response, null, null);
        return Status.OK_STATUS;
      }
      catch (ProcessingException e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error executing lookup http session", e);
      }
      catch (ServletException e) {
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error executing lookup http session", e);
      }
    }
  }

  private static class TestServiceTunnelServlet extends ServiceTunnelServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void initializeAjaxSessionTimeout(HttpServletRequest req) {
      super.initializeAjaxSessionTimeout(req);
    }

    @Override
    protected Class<? extends IServerSession> locateServerSessionClass(HttpServletRequest req, HttpServletResponse res) {
      return TestServerSession.class;
    }
  }

  /**
   * Tests that getSession is only called once when initializeAjaxSessionTimeout is called.
   */
  @Test
  public void testInitializeAjaxSessionTimeout() {
    final TestServiceTunnelServlet s = new TestServiceTunnelServlet();
    final HttpServletRequest mock = mock(HttpServletRequest.class);
    when(mock.getSession()).thenReturn(mock(HttpSession.class));
    s.initializeAjaxSessionTimeout(mock);
    s.initializeAjaxSessionTimeout(mock);
    verify(mock, times(1)).getSession();
  }
}
