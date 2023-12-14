/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.apache.hc.core5.http.HttpHeaders;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClientHttpProxyServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(RestClientHttpProxyServlet.class);

  public static final String PROXY_OK_RESPONSE = "proxy-ok";

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder("HTTP Headers:");
      for (Enumeration<String> headers = req.getHeaderNames(); headers.hasMoreElements();) {
        String header = headers.nextElement();
        sb.append(String.format("%n  %s: '%s'", header, req.getHeader(header)));
      }
      LOG.info(sb.toString());
    }

    // set common response headers
    resp.setHeader(CorrelationId.HTTP_HEADER_NAME, req.getHeader(CorrelationId.HTTP_HEADER_NAME)); // NOSONAR findbugs:HRS_REQUEST_PARAMETER_TO_HTTP_HEADER

    if (req.getHeader(HttpHeaders.PROXY_AUTHORIZATION) != null) {
      // Case 1) Request contains proxy authentication credentials -> validate
      String proxyAuth = StringUtility.substring(req.getHeader(HttpHeaders.PROXY_AUTHORIZATION), 6);
      String decoded = new String(Base64Utility.decode(proxyAuth), StandardCharsets.ISO_8859_1);
      String expectedCreds = req.getParameter(ProxyServletParameters.PROXY_USER) + ":" + req.getParameter(ProxyServletParameters.PROXY_PASSWORD);
      assertEquals(expectedCreds, decoded);
      sendProxyOk(resp);
    }
    else if (req.getParameter(ProxyServletParameters.REQUIRE_AUTH) != null) {
      // Case 2) Request requires proxy auth, but did not provide credentials
      resp.setStatus(Status.PROXY_AUTHENTICATION_REQUIRED.getStatusCode());
      resp.setHeader(HttpHeaders.PROXY_AUTHENTICATE, "Basic realm=\"MockProxy\"");
    }
    else {
      // Case 3) Proxy request without authentication
      sendProxyOk(resp);
    }
  }

  protected void sendProxyOk(HttpServletResponse resp) throws IOException {
    resp.setStatus(Status.OK.getStatusCode());
    resp.setContentType(MediaType.APPLICATION_JSON);
    RestClientTestEchoResponse echoResponse = BEANS.get(RestClientTestEchoResponse.class);
    echoResponse.withEcho(BEANS.get(RestClientTestEchoDo.class).withData(PROXY_OK_RESPONSE));
    BEANS.get(IDataObjectMapper.class).writeValue(resp.getOutputStream(), echoResponse);
  }
}
