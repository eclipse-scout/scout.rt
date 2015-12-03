/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.cache.TestHttpSession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;

/**
 * Test for {@link BasicSecurityFilter}
 */
@RunWith(PlatformTestRunner.class)
@SuppressWarnings("deprecation")
public class BasicSecurityFilterTest {
  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;
  private PrincipalHolder m_testPrincipalHolder;
  private BasicSecurityFilter m_filter;
  private List<IBean<?>> m_registeredServices = new ArrayList<IBean<?>>();
  private FilterConfig m_testFilterConfig;

  @Before
  public void setup() {
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    m_testPrincipalHolder = new PrincipalHolder();
    when(m_requestMock.getSession(true)).thenReturn(new TestHttpSession());
    when(m_requestMock.getSession()).thenReturn(new TestHttpSession());
    m_filter = new BasicSecurityFilter();
    m_testFilterConfig = mock(FilterConfig.class);
    when(m_testFilterConfig.getInitParameter("users")).thenReturn("admin=secret");
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterBeans(m_registeredServices);
  }

  @Test
  public void testAuthenticateHeaderOnFirstAttempt() throws IOException, ServletException {
    negotiateNTimes(1);
    verify(m_responseMock, times(1)).setHeader(anyString(), anyString());
  }

  @Test
  public void testNoAuthenticateHeaderOnThirdAttempt() throws IOException, ServletException {
    negotiateNTimes(10);
    verify(m_responseMock, times(3)).setHeader(anyString(), anyString());
  }

  private void negotiateNTimes(int n) throws IOException, ServletException {
    for (int i = 0; i < n; i++) {
      m_filter.negotiate(m_requestMock, m_responseMock, m_testPrincipalHolder);
    }
  }

  @Test
  public void testFilterNotAuthenticated() throws IOException, ServletException {
    IHttpSessionCacheService cacheService = mock(IHttpSessionCacheService.class);
    registerTestService(cacheService, IHttpSessionCacheService.class);

    BasicSecurityFilter f = new BasicSecurityFilter();
    f.init(m_testFilterConfig);
    f.doFilter(m_requestMock, m_responseMock, null);

    verify(cacheService, times(0)).put(subjectProperty(), anyObject(), any(HttpServletRequest.class), any(HttpServletResponse.class));
    verify(cacheService, times(0)).put(subjectProperty(), anyObject(), any(HttpServletRequest.class), any(HttpServletResponse.class), anyLong());
  }

  @Test
  public void testFilterAuthenticated() throws IOException, ServletException {
    IHttpSessionCacheService cacheService = mock(IHttpSessionCacheService.class);
    registerTestService(cacheService, IHttpSessionCacheService.class);

    BasicSecurityFilter f = new BasicSecurityFilter();
    f.init(m_testFilterConfig);
    final String validAutHeader = "Basic YWRtaW46c2VjcmV0";
    when(m_requestMock.getHeader(anyString())).thenReturn(validAutHeader);
    f.doFilter(m_requestMock, m_responseMock, mock(FilterChain.class));
    verify(cacheService).put(subjectProperty(), anyObject(), any(HttpServletRequest.class), any(HttpServletResponse.class));
  }

  private <SERVICE> void registerTestService(SERVICE service, Class<? extends SERVICE> clazz) {
    m_registeredServices.add(
        TestingUtility.registerBean(
            new BeanMetaData(clazz)
                .withInitialInstance(service)
                .withApplicationScoped(true)));
  }

  private String subjectProperty() {
    return argThat(new ArgumentMatcher<String>() {

      @Override
      public boolean matches(Object item) {
        return BasicSecurityFilter.PROP_SUBJECT.equals(item);
      }
    });
  }
}
