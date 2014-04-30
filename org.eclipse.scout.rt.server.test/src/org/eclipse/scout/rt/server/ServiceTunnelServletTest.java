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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.commons.cache.ICacheEntry;
import org.eclipse.scout.rt.server.commons.cache.StickySessionCacheService;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

  @Test
  public void testVirtualSessionLookup() throws ProcessingException, ServletException {
    TestServerSession testSession = new TestServerSession();
    ICacheEntry cacheMock = Mockito.mock(ICacheEntry.class);
    when(cacheMock.getValue()).thenReturn(testSession);
    when(cacheMock.isActive()).thenReturn(true);

    when(m_testHttpSession.getAttribute(IServerSession.class.getName())).thenReturn(cacheMock);
//    IServerSession session = m_testServiceTunnelServlet.lookupScoutServerSessionOnVirtualSession(m_requestMock, m_responseMock, null, null);
//    Assert.assertEquals(testSession, session);
  }

  private ServiceTunnelServlet getServiceTunnelServletWithTestSession() {
    return new ServiceTunnelServlet() {
      private static final long serialVersionUID = 1L;

      @Override
      protected Class<? extends IServerSession> locateServerSessionClass(HttpServletRequest req, HttpServletResponse res) {
        return TestServerSession.class;
      }
    };
  }

}
