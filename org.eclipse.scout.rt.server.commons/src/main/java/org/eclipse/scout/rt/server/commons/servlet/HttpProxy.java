/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;

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
    // field is set by http client itself
    m_requestHeaderFilters.add(new HttpHeaderNameFilter("Content-Length"));

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
    HttpRequest httpReq = BEANS.get(DefaultHttpTransportManager.class).getHttpRequestFactory().buildGetRequest(new GenericUrl(url));
    httpReq = prepareRequest(httpReq);

    writeRequestHeaders(req, httpReq);
    writeCustomRequestHeaders(httpReq, options.getCustomRequestHeaders());
    HttpResponse httpResp = httpReq.execute();

    writeResponseHeaders(resp, httpResp);
    writeResponseStatus(resp, httpResp);
    writeResponsePayload(resp, httpResp);
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
    HttpRequest httpReq = BEANS.get(DefaultHttpTransportManager.class).getHttpRequestFactory().buildPostRequest(new GenericUrl(url), null);
    httpReq.getHeaders().setCacheControl("no-cache");
    httpReq = prepareRequest(httpReq);

    writeRequestHeaders(req, httpReq);
    writeCustomRequestHeaders(httpReq, options.getCustomRequestHeaders());

    // Payload is empty if parameters are used (usually with content type = application/x-www-form-urlencoded)
    // -> write parameters if there are any, otherwise write the raw payload
    if (req.getParameterMap().size() > 0) {
      writeRequestParameters(req, httpReq);
    }
    else {
      writeRequestPayload(req, httpReq);
    }
    HttpResponse httpResp = httpReq.execute();

    writeResponseHeaders(resp, httpResp);
    writeResponseStatus(resp, httpResp);
    writeResponsePayload(resp, httpResp);
    LOG.debug("Forwarded post request to " + url);
  }

  protected HttpRequest prepareRequest(HttpRequest httpReq) {
    return httpReq;
  }

  protected void writeRequestHeaders(HttpServletRequest req, HttpRequest httpReq) {
    Enumeration<String> headerNames = req.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      String value = req.getHeader(name);
      for (IHttpHeaderFilter filter : getRequestHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        HttpHeaders headers = httpReq.getHeaders();
        headers.set(name, Collections.singletonList(value));
        LOG.trace("Wrote request header: {}: {}", name, value);
      }
      else {
        LOG.trace("Removed request header: {} (original value: {})", name, req.getHeader(name));
      }
    }
  }

  protected void writeCustomRequestHeaders(HttpRequest httpReq, Map<String, String> customHeaders) {
    if (customHeaders == null) {
      return;
    }
    for (Entry<String, String> header : customHeaders.entrySet()) {
      httpReq.getHeaders().set(header.getKey(), header.getValue());
      LOG.trace("Wrote custom request header: {}: {}", header.getValue(), header.getValue());
    }
  }

  protected void writeRequestPayload(HttpServletRequest req, HttpRequest httpReq) throws IOException {
    httpReq.setContent(new InputStreamContent(null, req.getInputStream()));
  }

  protected void writeRequestParameters(HttpServletRequest req, HttpRequest httpReq) throws IOException {
    String parameters = formatFormParameters(req.getParameterMap());
    httpReq.setContent(new InputStreamContent(null, new ByteArrayInputStream(parameters.getBytes(StandardCharsets.UTF_8))));
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
  protected void writeResponsePayload(HttpServletResponse resp, HttpResponse httpResp) throws IOException {
    try (InputStream in = httpResp.getContent()) {
      writeResponsePayload(resp, in);
    }
  }

  protected void writeResponseStatus(HttpServletResponse resp, HttpResponse httpResp) throws IOException {
    int responseCode = httpResp.getStatusCode();
    resp.setStatus(responseCode);
  }

  protected void writeResponsePayload(HttpServletResponse resp, InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return;
    }
    IOUtility.writeFromToStream(resp.getOutputStream(), inputStream);
  }

  protected void writeResponseHeaders(HttpServletResponse resp, HttpResponse httpResp) {
    for (Entry<String, Object> entry : httpResp.getHeaders().entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue() instanceof Collection<?> ? CollectionUtility.firstElement(entry.getValue()).toString() : entry.getValue().toString();
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
