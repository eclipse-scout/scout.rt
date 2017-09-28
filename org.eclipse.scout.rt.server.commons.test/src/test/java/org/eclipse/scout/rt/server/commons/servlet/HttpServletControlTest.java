/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspEnabledProperty;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test for {@link HttpServletControl}
 */
public class HttpServletControlTest {

  private static final List<IBean<?>> s_beans = new ArrayList<>();

  private static final String TEST_CSP_TOKEN = "mocked-csp-token";

  @BeforeClass
  public static void beforeClass() {

  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(s_beans);
  }

  @Test
  public void testSetResponseHeaders() {
    // GET request with CSP enabled  -> expect CSP headers
    runTestSetResponseHeader(true, true, "GET", true);
    runTestSetResponseHeader(false, true, "GET", true);

    // GET request with CSP disabled -> do not expect CSP headers
    runTestSetResponseHeader(true, false, "GET", false);
    runTestSetResponseHeader(false, false, "GET", false);

    // POST request with CSP enabled -> do not expect CSP headers
    runTestSetResponseHeader(true, true, "POST", false);
    runTestSetResponseHeader(false, true, "POST", false);

    // POST request with CSP disabled -> do not expect CSP headers
    runTestSetResponseHeader(true, false, "POST", false);
    runTestSetResponseHeader(false, false, "POST", false);
  }

  protected void runTestSetResponseHeader(boolean mshtml, boolean cspEnabled, String method, boolean expectCspHeader) {
    CspEnabledProperty cspProperty = Mockito.mock(CspEnabledProperty.class);
    Mockito.when(cspProperty.getValue(ArgumentMatchers.<String> any())).thenReturn(cspEnabled);
    s_beans.add(TestingUtility.registerBean(new BeanMetaData(CspEnabledProperty.class, cspProperty)));

    HttpClientInfo httpClientInfo = Mockito.mock(HttpClientInfo.class);
    Mockito.when(httpClientInfo.isMshtml()).thenReturn(mshtml);

    HttpServletControl httpServletControl = new HttpServletControl();
    httpServletControl.setCspToken(TEST_CSP_TOKEN);
    HttpServlet servlet = Mockito.mock(HttpServlet.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);

    Mockito.when(req.getMethod()).thenReturn(method);
    Mockito.when(req.getSession(false)).thenReturn(session);
    Mockito.when(session.getAttribute(HttpClientInfo.HTTP_CLIENT_INFO_ATTRIBUTE_NAME)).thenReturn(httpClientInfo);

    httpServletControl.setResponseHeaders(servlet, req, resp);

    Mockito.verifyZeroInteractions(servlet);
    if (expectCspHeader) {
      Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_X_FRAME_OPTIONS, HttpServletControl.SAMEORIGIN);
      Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_X_XSS_PROTECTION, HttpServletControl.XSS_MODE_BLOCK);
      if (mshtml) {
        Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_CSP_LEGACY, TEST_CSP_TOKEN);
      }
      else {
        Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_CSP, TEST_CSP_TOKEN);
      }
    }
  }
}
