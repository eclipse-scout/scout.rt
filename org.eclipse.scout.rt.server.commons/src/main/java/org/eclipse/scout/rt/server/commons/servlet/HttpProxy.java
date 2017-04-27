/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards get and post requests to the given remote URL.
 */
@Bean
public class HttpProxy {
  private static final Logger LOG = LoggerFactory.getLogger(HttpProxy.class);

  private String m_remoteUrl;
  private List<IHttpHeaderFilter> m_requestHeaderFilters;
  private List<IHttpHeaderFilter> m_responseHeaderFilters;

  public HttpProxy() {
    m_requestHeaderFilters = new ArrayList<>();
    m_responseHeaderFilters = new ArrayList<>();
  }

  @PostConstruct
  protected void initialize() {
    // remove null header from response headers
    m_responseHeaderFilters.add(new HttpHeaderNameFilter(null));

    // remove "Transfer-Encoding: chunked" header, server should decide about response on its own
    m_responseHeaderFilters.add(new HttpHeaderNameValueFilter("Transfer-Encoding", "chunked"));
  }

  public void proxyGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    proxyGet(req, resp, new HttpProxyOptions());
  }

  /**
   * Adds every request header beside the blacklisted to the forwarded request.<br>
   * Writes the request payload to the forwarded request or adds every query parameter to the forwarded request if
   * parameters are used.<br>
   * Writes the returned response body of the forwarded request, the headers and the status to the response.<b>
   */
  public void proxyGet(HttpServletRequest req, HttpServletResponse resp, HttpProxyOptions options) throws ServletException, IOException {
    String url = StringUtility.join("", getRemoteUrl(), req.getPathInfo(), StringUtility.box("?", req.getQueryString(), ""));
    URLConnection connection = openConnection(url);

    writeRequestHeaders(req, connection);
    writeCustomRequestHeaders(connection, options.getCustomRequestHeaders());

    writeResponseHeaders(resp, connection);
    writeResponseStatus(resp, connection);
    writeResponsePayload(resp, connection);
    LOG.debug("Forwarded get request to " + url);
  }

  public void proxyPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    proxyPost(req, resp, new HttpProxyOptions());
  }

  /**
   * Adds every request header beside the blacklisted to the forwarded request.<br>
   * Adds every form parameter to the forwarded request.<br>
   * Writes the returned response body of the forwarded request, the headers and the status to the response.<b>
   */
  public void proxyPost(HttpServletRequest req, HttpServletResponse resp, HttpProxyOptions options) throws ServletException, IOException {
    String url = StringUtility.join("", getRemoteUrl(), req.getPathInfo());
    URLConnection connection = openConnection(url);
    connection.setDoOutput(true);
    connection.setDefaultUseCaches(false);
    connection.setUseCaches(false);

    writeRequestHeaders(req, connection);
    writeCustomRequestHeaders(connection, options.getCustomRequestHeaders());

    // Payload is empty if parameters are used (usually with content type = application/x-www-form-urlencoded)
    // -> write parameters if there are any, otherwise write the raw payload
    if (req.getParameterMap().size() > 0) {
      writeRequestParameters(req, connection);
    }
    else {
      writeRequestPayload(req, connection);
    }
    writeResponseHeaders(resp, connection);
    writeResponseStatus(resp, connection);
    writeResponsePayload(resp, connection);
    LOG.debug("Forwarded post request to " + url);
  }

  protected URLConnection openConnection(String url) throws IOException {
    return new URL(url).openConnection();
  }

  protected void writeRequestHeaders(HttpServletRequest req, URLConnection connection) {
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      String value = req.getHeader(name);
      for (IHttpHeaderFilter filter : getRequestHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        connection.setRequestProperty(name, value);
        LOG.trace("Wrote request header: {}: {}", name, value);
      }
      else {
        LOG.trace("Removed request header: {} (original value: {})", name, req.getHeader(name));
      }
    }
  }

  protected void writeCustomRequestHeaders(URLConnection connection, Map<String, String> customHeaders) {
    if (customHeaders == null) {
      return;
    }
    for (Entry<String, String> header : customHeaders.entrySet()) {
      connection.setRequestProperty(header.getKey(), header.getValue());
      LOG.trace("Wrote custom request header: {}: {}", header.getValue(), header.getValue());
    }
  }

  protected void writeRequestPayload(HttpServletRequest req, URLConnection connection) throws IOException {
    try (OutputStream out = connection.getOutputStream()) {
      IOUtility.writeFromToStream(out, req.getInputStream());
    }
  }

  protected void writeRequestParameters(HttpServletRequest req, URLConnection connection) throws IOException {
    String parameters = formatFormParameters(req.getParameterMap());

    try (OutputStream out = connection.getOutputStream()) {
      out.write(parameters.getBytes(StandardCharsets.UTF_8));
    }
  }

  protected String formatFormParameters(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {
    StringBuilder parameters = new StringBuilder();
    for (Entry<String, String[]> entry : parameterMap.entrySet()) {
      for (String value : entry.getValue()) {
        if (parameters.length() > 0) {
          parameters.append("&");
        }
        parameters
            .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
            .append("=")
            .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
      }
    }
    return parameters.toString();
  }

  /**
   * Writes the response payload of forwarded request to the servlet response.
   */
  protected void writeResponsePayload(HttpServletResponse resp, URLConnection connection) throws IOException {
    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    int responseCode = httpConnection.getResponseCode();
    if (responseCode == 200) {
      try (InputStream in = httpConnection.getInputStream()) {
        writeResponsePayload(resp, in);
      }
    }
    else {
      try (InputStream in = httpConnection.getErrorStream()) {
        writeResponsePayload(resp, in);
      }
    }
  }

  protected void writeResponseStatus(HttpServletResponse resp, URLConnection connection) throws IOException {
    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    int responseCode = httpConnection.getResponseCode();
    resp.setStatus(responseCode);
  }

  protected void writeResponsePayload(HttpServletResponse resp, InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return;
    }
    IOUtility.writeFromToStream(resp.getOutputStream(), inputStream);
  }

  protected void writeResponseHeaders(HttpServletResponse resp, URLConnection connection) {
    for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
      String name = entry.getKey();
      String value = CollectionUtility.format(entry.getValue(), ",");
      String originalValue = value;
      for (IHttpHeaderFilter filter : getResponseHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        resp.setHeader(entry.getKey(), value);
        LOG.trace("Wrote response header: {}: {}", entry.getKey(), value);
      }
      else {
        LOG.trace("Removed response header: {} (original value: {})", name, originalValue);
      }
    }
  }

  public String getRemoteUrl() {
    return m_remoteUrl;
  }

  public void setRemoteUrl(String remoteUrl) {
    m_remoteUrl = remoteUrl;
  }

  public List<IHttpHeaderFilter> getRequestHeaderFilters() {
    return m_requestHeaderFilters;
  }

  public void addRequestHeaderFilter(IHttpHeaderFilter filter) {
    m_requestHeaderFilters.add(filter);
  }

  public List<IHttpHeaderFilter> getResponseHeaderFilters() {
    return m_responseHeaderFilters;
  }

  public void addResponseHeaderFilter(IHttpHeaderFilter filter) {
    m_responseHeaderFilters.add(filter);
  }
}
