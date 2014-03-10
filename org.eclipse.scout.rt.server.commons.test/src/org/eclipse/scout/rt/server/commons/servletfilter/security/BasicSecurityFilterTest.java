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
package org.eclipse.scout.rt.server.commons.servletfilter.security;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.server.commons.cache.TestHttpSession;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link BasicSecurityFilter}
 */
public class BasicSecurityFilterTest {
  private HttpServletRequest m_requestMock;
  private HttpServletResponse m_responseMock;
  private PrincipalHolder m_testPrincipalHolder;
  private BasicSecurityFilter m_filter;

  @Before
  public void setup() {
    m_requestMock = mock(HttpServletRequest.class);
    m_responseMock = mock(HttpServletResponse.class);
    m_testPrincipalHolder = new PrincipalHolder();
    when(m_requestMock.getSession(true)).thenReturn(new TestHttpSession());
    m_filter = new BasicSecurityFilter();
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
}
