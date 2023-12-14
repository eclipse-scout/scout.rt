/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspEnabledProperty;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

/**
 * Test for {@link HttpServletControl}
 */
public class HttpServletControlTest {

  private static final String TEST_CSP_TOKEN = "mocked-csp-token";

  @Test
  public void testSetResponseHeaders() {
    // GET request with CSP enabled  -> expect CSP headers, expect other headers
    runTestSetResponseHeader(true, true, "GET", true, true, true, true);
    runTestSetResponseHeader(false, true, "GET", true, true, true, true);

    // GET request with CSP disabled -> do not expect CSP headers, expect other headers
    runTestSetResponseHeader(true, false, "GET", false, true, true, true);
    runTestSetResponseHeader(false, false, "GET", false, true, true, true);

    // POST request with CSP enabled -> do not expect CSP headers, do not expect other headers
    runTestSetResponseHeader(true, true, "POST", false, false, false, true);
    runTestSetResponseHeader(false, true, "POST", false, false, false, true);

    // POST request with CSP disabled -> do not expect CSP headers, do not expect other headers
    runTestSetResponseHeader(true, false, "POST", false, false, false, true);
    runTestSetResponseHeader(false, false, "POST", false, false, false, true);
  }

  protected void runTestSetResponseHeader(boolean mshtml, boolean cspEnabled, String method, boolean expectCspHeader, boolean expectFrameOptions, boolean expectXssProtection, boolean expectNoSniff) {
    CspEnabledProperty cspProperty = Mockito.mock(CspEnabledProperty.class);
    Mockito.when(cspProperty.getValue(ArgumentMatchers.any())).thenReturn(cspEnabled);
    IBean<?> bean = BeanTestingHelper.get().registerBean(new BeanMetaData(CspEnabledProperty.class, cspProperty));

    try {
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

      Mockito.verifyNoInteractions(servlet);
      VerificationMode calledOnce = Mockito.times(1);
      VerificationMode calledNever = Mockito.never();

      Mockito.verify(resp, expectFrameOptions ? calledOnce : calledNever)
          .setHeader(HttpServletControl.HTTP_HEADER_X_FRAME_OPTIONS, HttpServletControl.SAMEORIGIN);
      Mockito.verify(resp, expectXssProtection ? calledOnce : calledNever)
          .setHeader(HttpServletControl.HTTP_HEADER_X_XSS_PROTECTION, HttpServletControl.XSS_MODE_BLOCK);
      Mockito.verify(resp, expectNoSniff ? calledOnce : calledNever)
          .setHeader(HttpServletControl.HTTP_HEADER_X_CONTENT_TYPE_OPTIONS, HttpServletControl.CONTENT_TYPE_OPTION_NO_SNIFF);

      if (expectCspHeader) {
        if (mshtml) {
          Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_CSP_LEGACY, TEST_CSP_TOKEN);
        }
        else {
          Mockito.verify(resp).setHeader(HttpServletControl.HTTP_HEADER_CSP, TEST_CSP_TOKEN);
        }
      }
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }
}
