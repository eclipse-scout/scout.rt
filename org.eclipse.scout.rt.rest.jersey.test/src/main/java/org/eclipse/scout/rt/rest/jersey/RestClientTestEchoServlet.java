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

import static org.eclipse.scout.rt.rest.jersey.EchoServletParameters.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClientTestEchoServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(RestClientTestEchoServlet.class);

  public static final String ECHO_SERVLET_COOKIE = "echo-servlet-cookie";

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String requestId = req.getParameter(REQUEST_ID);
    IRegistrationHandle requestHandle = null;
    if (requestId != null) {
      requestHandle = BEANS.get(RequestSynchronizer.class).notifyRequestArrived(requestId);
    }
    try {
      final int statusCode = parseStatusCode(req.getParameter(STATUS));

      if (LOG.isDebugEnabled()) {
        StringBuilder sb = new StringBuilder("HTTP Headers:");
        for (Enumeration<String> headers = req.getHeaderNames(); headers.hasMoreElements();) {
          String header = headers.nextElement();
          sb.append(String.format("%n  %s: '%s'", header, req.getHeader(header)));
        }
        LOG.debug(sb.toString());
      }

      String sleep = req.getParameter(SLEEP_SEC);
      if (sleep != null) {
        int sleepSeconds = Integer.parseInt(sleep);
        SleepUtil.sleepSafe(sleepSeconds, TimeUnit.SECONDS);
        if (Thread.interrupted()) {
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
      }

      resp.setHeader(CorrelationId.HTTP_HEADER_NAME, req.getHeader(CorrelationId.HTTP_HEADER_NAME));
      resp.setHeader(HttpHeaders.ACCEPT_LANGUAGE, req.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

      if (req.getParameter(REDIRECT_URL) != null) {
        resp.sendRedirect(req.getParameter(REDIRECT_URL)); // NOSONAR findbugs:HRS_REQUEST_PARAMETER_TO_HTTP_HEADER
        return;
      }

      Status status = Status.fromStatusCode(statusCode);
      if (status != null && status.getFamily() == Status.Family.SUCCESSFUL) {
        sendEchoResponse(req, resp, statusCode, status);
      }
      else {
        sendErrorResponse(req, resp, statusCode, status);
      }
    }
    finally {
      if (requestHandle != null) {
        requestHandle.dispose();
      }
    }
  }

  public void sendEchoResponse(HttpServletRequest req, HttpServletResponse resp, int statusCode, Status status) throws IOException {
    resp.setStatus(statusCode);
    resp.setContentType(MediaType.APPLICATION_JSON);

    String cookieValue = req.getParameter(COOKIE_VALUE);
    if (cookieValue != null) {
      resp.addCookie(new Cookie(ECHO_SERVLET_COOKIE, cookieValue)); // NOSONAR findbugs:HRS_REQUEST_PARAMETER_TO_COOKIE
    }

    if (req.getParameter(EMPTY_BODY) != null) {
      return;
    }

    String body = IOUtility.readStringUTF8(req.getInputStream());

    Map<String, String> receivedHeaders = new HashMap<>();
    for (Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();) {
      String headerName = headerNames.nextElement();
      receivedHeaders.put(headerName, req.getHeader(headerName));
    }

    RestClientTestEchoResponse echoResponse = BEANS.get(RestClientTestEchoResponse.class)
        .withEcho(BEANS.get(RestClientTestEchoDo.class)
            .withHttpMethod(req.getMethod())
            .withCode(statusCode)
            .withInfo(status == null ? "unknown" : status.getReasonPhrase())
            .withBody(body))
        .withReceivedHeaders(receivedHeaders);

    if (req.getParameter(LARGE_MESSAGE) != null) {
      StringBuilder sb = new StringBuilder();
      for (char c = 'a'; c <= 'z'; c++) {
        sb.append(c);
      }
      String alphabet = sb.toString();
      sb.append(alphabet.repeat(1000));
      echoResponse.getEcho().withData(sb.toString());
    }

    BEANS.get(IDataObjectMapper.class).writeValue(resp.getOutputStream(), echoResponse);
  }

  protected void sendErrorResponse(HttpServletRequest req, HttpServletResponse resp, int statusCode, Status status) throws IOException {
    resp.setStatus(statusCode);

    if (req.getParameter(EMPTY_BODY) != null) {
      return;
    }

    if (acceptsJson(req)) {
      resp.setContentType(MediaType.APPLICATION_JSON);

      ErrorResponse errorResponse = BEANS.get(ErrorResponse.class)
          .withError(BEANS.get(ErrorDo.class)
              .withHttpStatus(statusCode)
              .withMessage(status == null ? "unknown" : status.getReasonPhrase())
              .withTitle("REST Client Test"));
      BEANS.get(IDataObjectMapper.class).writeValue(resp.getOutputStream(), errorResponse);
    }
    else {
      String content = HTML.html5(
          HTML.head(
              HTML.tag("meta").addAttribute("charset", "utf-8"),
              HTML.tag("title", "REST Client Test")),
          HTML.body(
              HTML.h1("REST Client Test"),
              HTML.div(status == null ? "unknown" : status.getReasonPhrase())))
          .toHtml();
      resp.getOutputStream().print(content);
      resp.setContentType(MediaType.TEXT_HTML);
    }
  }

  protected int parseStatusCode(String statusCode) {
    int sc = 0;
    try {
      sc = Integer.parseInt(statusCode);
    }
    catch (NumberFormatException e) {
      LOG.warn("invalid status code '{}'", statusCode, e);
    }
    return sc != 0 ? sc : Status.BAD_REQUEST.getStatusCode();
  }

  protected boolean acceptsJson(HttpServletRequest req) {
    String format = req.getParameter("format");
    if (StringUtility.isNullOrEmpty(format)) {
      format = req.getHeader("Accept");
    }
    return MediaType.APPLICATION_JSON.equals(format);
  }
}
