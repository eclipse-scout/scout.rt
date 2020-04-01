/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey;

import static org.eclipse.scout.rt.rest.jersey.EchoServletParameters.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClientTestEchoServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(RestClientTestEchoServlet.class);

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String requestId = req.getParameter(REQUEST_ID);
    if (requestId != null) {
      BEANS.get(RequestSynchronizer.class).notifyRequestArrived(requestId);
    }

    final int statusCode = parseStatusCode(req.getParameter(STATUS));

    if (LOG.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder("HTTP Headers:");
      for (Enumeration<String> headers = req.getHeaderNames(); headers.hasMoreElements();) {
        String header = headers.nextElement();
        sb.append(String.format("%n  %s: '%s'", header, req.getHeader(header)));
      }
      LOG.info(sb.toString());
    }

    String sleep = req.getParameter(SLEEP_SEC);
    if (sleep != null) {
      int sleepSeconds = Integer.parseInt(sleep);
      SleepUtil.sleepSafe(sleepSeconds, TimeUnit.SECONDS);
    }

    resp.setHeader(CorrelationId.HTTP_HEADER_NAME, req.getHeader(CorrelationId.HTTP_HEADER_NAME));
    resp.setHeader(HttpHeaders.ACCEPT_LANGUAGE, req.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

    Status status = Status.fromStatusCode(statusCode);
    if (status != null && status.getFamily() == Status.Family.SUCCESSFUL) {
      sendEchoResponse(req, resp, statusCode, status);
    }
    else {
      sendErrorResponse(req, resp, statusCode, status);
    }
  }

  public void sendEchoResponse(HttpServletRequest req, HttpServletResponse resp, int statusCode, Status status) throws IOException {
    resp.setStatus(statusCode);
    resp.setContentType(MediaType.APPLICATION_JSON);

    if (req.getParameter(EMPTY_BODY) != null) {
      return;
    }

    Map<String, String> receivedHeaders = new HashMap<>();
    for (Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();) {
      String headerName = headerNames.nextElement();
      receivedHeaders.put(headerName, req.getHeader(headerName));
    }

    RestClientTestEchoResponse echoResponse = BEANS.get(RestClientTestEchoResponse.class)
        .withEcho(BEANS.get(RestClientTestEchoDo.class)
            .withHttpMethod(req.getMethod())
            .withCode(statusCode)
            .withInfo(status == null ? "unknown" : status.getReasonPhrase()))
        .withReceivedHeaders(receivedHeaders);

    if (req.getParameter(LARGE_MESSAGE) != null) {
      StringBuilder sb = new StringBuilder();
      for (char c = 'a'; c <= 'z'; c++) {
        sb.append(c);
      }
      String alphabet = sb.toString();
      for (int i = 0; i < 1000; i++) {
        sb.append(alphabet);
      }
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
