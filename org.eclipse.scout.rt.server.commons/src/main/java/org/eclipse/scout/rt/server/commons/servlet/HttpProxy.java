/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.http.DefaultHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;

/**
 * Forwards HTTP requests to the given remote URL.
 */
@Bean
public class HttpProxy {
  private static final Logger LOG = LoggerFactory.getLogger(HttpProxy.class);

  private IHttpTransportManager m_httpTransportManager;
  private String m_remoteBaseUrl;
  private final List<IHttpHeaderFilter> m_requestHeaderFilters;
  private final List<IHttpHeaderFilter> m_responseHeaderFilters;

  public HttpProxy() {
    m_httpTransportManager = BEANS.get(DefaultHttpTransportManager.class);
    m_requestHeaderFilters = new ArrayList<>();
    m_responseHeaderFilters = new ArrayList<>();
  }

  @PostConstruct
  protected void initialize() {
    // -------------------------------------------------------------------------
    // Remove hop-by-hop headers which are valid for a single transport-level
    // connection only and must not be forwarded by a proxy.
    //
    // See also:
    //  https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers#hbh
    //  https://tools.ietf.org/html/rfc7230#section-6.1
    //  https://www.mnot.net/blog/2011/07/11/what_proxies_must_do
    Set<String> hopByHopRequestHeaders = CollectionUtility.hashSet(
        "Connection",
        "Upgrade",
        "Keep-Alive",
        "Transfer-Encoding",
        "Proxy-Authorization",
        "TE");
    for (String header : hopByHopRequestHeaders) {
      m_requestHeaderFilters.add(new HttpHeaderNameFilter(header));
    }

    Set<String> hopByHopResponseHeaders = CollectionUtility.hashSet(
        "Connection",
        "Keep-Alive",
        "Transfer-Encoding",
        "Proxy-Authenticate",
        "Trailer");
    for (String header : hopByHopResponseHeaders) {
      m_responseHeaderFilters.add(new HttpHeaderNameFilter(header));
    }

    // -------------------------------------------------------------------------
    // remove headers computed by the HTTP client itself
    m_requestHeaderFilters.add(new HttpHeaderNameFilter("Content-Length"));
    m_requestHeaderFilters.add(new HttpHeaderNameFilter("Host"));

    // remove null header from response headers
    m_responseHeaderFilters.add(new HttpHeaderNameFilter(null));
  }

  /**
   * @return <code>true</code> if the request payload should be included in the proxy call, <code>false</code>
   *         otherwise. The default implementation returns <code>true</code> for POST, PUT and PATCH requests.
   */
  protected boolean shouldIncludeRequestPayload(HttpServletRequest req) {
    return ObjectUtility.isOneOf(req.getMethod(), "POST", "PUT", "PATCH");
  }

  /**
   * @return Whether the {@linkplain HttpServletRequest#getParameterMap() request parameters} should be written as
   *         payload instead of the {@linkplain HttpServletRequest#getInputStream() original payload}.
   *         <p>
   *         This is mostly relevant for form submissions (content type <code>application/x-www-form-urlencoded</code>).
   *         Because the servlet container parses the parameters from the payload, they cannot be read again from the
   *         request body. Instead, they have to be read from the parameter map and be converted back to a valid body.
   * @see #writeRequestParameters(HttpServletRequest, HttpRequest)
   */
  protected boolean shouldWriteParametersAsPayload(HttpServletRequest req) {
    if (req.getParameterMap().isEmpty()) {
      return false;
    }
    // https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
    // https://tools.ietf.org/html/rfc2045#section-5.1
    String contentType = req.getContentType();
    if (contentType == null) {
      return false;
    }
    int i = contentType.indexOf(";");
    if (i != -1) {
      contentType = contentType.substring(0, i); // ignore parameters
    }
    return "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
  }

  /**
   * Forwards the given request to the remote URL using the given method.
   * <ul>
   * <li>Adds every request header beside the blacklisted to the forwarded request.<br>
   * <li>If and only if {@link #shouldIncludeRequestPayload(HttpServletRequest)} returns <code>true</code>, writes the
   * request payload to the forwarded request or adds every query parameter to the forwarded request if parameters are
   * used.
   * <li>Writes the returned response body, headers and status to the response.
   * </ul>
   *
   * @param req
   *          original request
   * @param resp
   *          response where the response from the remote server is written to
   * @param options
   *          optional options for this request
   */
  public void proxy(HttpServletRequest req, HttpServletResponse resp, HttpProxyRequestOptions options) throws IOException {
    if (options == null) {
      options = new HttpProxyRequestOptions();
    }

    String url = rewriteUrl(req, options);
    HttpRequest httpReq = getHttpTransportManager().getHttpRequestFactory().buildRequest(req.getMethod(), new GenericUrl(url), null);
    httpReq = prepareRequest(httpReq);

    writeRequestHeaders(req, httpReq);
    writeCustomRequestHeaders(httpReq, options.getCustomRequestHeaders());

    if (shouldIncludeRequestPayload(req)) {
      // Payload is empty if parameters are used (usually with content type = application/x-www-form-urlencoded)
      // -> write parameters if there are any, otherwise write the raw payload
      if (shouldWriteParametersAsPayload(req)) {
        writeRequestParameters(req, httpReq);
      }
      else {
        writeRequestPayload(req, httpReq);
      }
    }
    HttpResponse httpResp = httpReq.execute();

    writeResponseHeaders(resp, httpResp);
    writeResponseStatus(resp, httpResp);
    writeResponsePayload(resp, httpResp);

    LOG.debug("Forwarded {} request to {}", req.getMethod(), url);
  }

  /**
   * Rewrites the <code>pathInfo</code> part of the current request if the rewriteRule and rewriteReplacement is set on
   * the options object. This allows to redirect the request to a different URL than the URL that has been requested.
   */
  protected String rewriteUrl(HttpServletRequest req, HttpProxyRequestOptions options) {
    String pathInfo = req.getPathInfo();
    IRewriteRule rewriteRule = options.getRewriteRule();
    if (rewriteRule != null) {
      pathInfo = rewriteRule.rewrite(pathInfo);
    }
    return StringUtility.join("", getRemoteBaseUrl(), pathInfo, StringUtility.box("?", req.getQueryString(), ""));
  }

  protected HttpRequest prepareRequest(HttpRequest httpReq) {
    return httpReq;
  }

  protected void writeRequestHeaders(HttpServletRequest req, HttpRequest httpReq) {
    Enumeration<String> headerNames = req.getHeaderNames();
    final Set<String> hopByHopHeaderNames = getConnectionHeaderValues(req);
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      String value = req.getHeader(name);
      if (name != null && hopByHopHeaderNames.contains(name.toLowerCase(Locale.US))) {
        LOG.trace("Removed hop-by-hop request header: {} (original value: {})", name, req.getHeader(name));
        continue;
      }
      for (IHttpHeaderFilter filter : getRequestHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        HttpHeaders headers = httpReq.getHeaders();
        headers.set(name, Collections.singletonList(value));
        LOG.trace("Added request header: {}: {}", name, value);
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
      LOG.trace("Added custom request header: {}: {}", header.getKey(), header.getValue());
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

  protected void writeResponseStatus(HttpServletResponse resp, HttpResponse httpResp) {
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
    final Set<String> hopByHopHeaderNames = getConnectionHeaderValues(httpResp);
    for (Entry<String, Object> entry : httpResp.getHeaders().entrySet()) {
      String name = entry.getKey();
      String value = Objects.toString(entry.getValue() instanceof Collection<?> ? CollectionUtility.firstElement((Collection<?>) entry.getValue()) : entry.getValue());
      if (name != null && hopByHopHeaderNames.contains(name.toLowerCase(Locale.US))) {
        LOG.trace("Removed hop-by-hop response header: {} (original value: {})", name, value);
        continue;
      }
      String originalValue = value;
      for (IHttpHeaderFilter filter : getResponseHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        resp.setHeader(entry.getKey(), value);
        LOG.trace("Added response header: {}: {}", entry.getKey(), value);
      }
      else {
        LOG.trace("Removed response header: {} (original value: {})", name, originalValue);
      }
    }
  }

  /**
   * Extracts the different values of the Connection HTTP request header, transformed to lower-case.
   *
   * @return set of distinct, non-null connection values in lower-case or an empty set, if the header is not set.
   */
  protected Set<String> getConnectionHeaderValues(HttpServletRequest req) {
    Enumeration<String> enumeration = req.getHeaders("Connection");
    if (enumeration == null) {
      return Collections.emptySet();
    }
    Set<String> set = new HashSet<>();
    while (enumeration.hasMoreElements()) {
      String s = enumeration.nextElement();
      if (StringUtility.hasText(s)) {
        set.add(s.toLowerCase(Locale.US));
      }
    }
    return set;
  }

  /**
   * Extracts the different values of the Connection HTTP response header, transformed to lower-case.
   *
   * @return set of distinct, non-null connection values in lower-case or an empty set, if the header is not set.
   */
  protected Set<String> getConnectionHeaderValues(HttpResponse httpResp) {
    return httpResp.getHeaders()
        .getHeaderStringValues("Connection")
        .stream()
        .flatMap(v -> Stream.of(StringUtility.split(v, ",")))
        .filter(StringUtility::hasText)
        .map(StringUtility::trim)
        .map(s -> s.toLowerCase(Locale.US))
        .collect(Collectors.toSet());
  }

  public IHttpTransportManager getHttpTransportManager() {
    return m_httpTransportManager;
  }

  /**
   * @param manager
   *          the {@link IHttpTransportManager} used to execute the http requests. By default the
   *          {@link DefaultHttpTransportManager} is used.
   */
  public HttpProxy withHttpTransportManager(IHttpTransportManager manager) {
    m_httpTransportManager = manager;
    return this;
  }

  /**
   * @return the base URL on the remote server (without trailing slash). All requests are forwarded to this destination
   *         by concatenating this URL and the requests "path info".
   * @see #rewriteUrl(HttpServletRequest, HttpProxyRequestOptions)
   */
  public String getRemoteBaseUrl() {
    return m_remoteBaseUrl;
  }

  /**
   * @param remoteBaseUrl
   *          the base URL on the remote server (without trailing slash). All requests are forwarded to this destination
   *          by concatenating this URL and the requests "path info".
   * @see #rewriteUrl(HttpServletRequest, HttpProxyRequestOptions)
   */
  public HttpProxy withRemoteBaseUrl(String remoteBaseUrl) {
    m_remoteBaseUrl = remoteBaseUrl;
    return this;
  }

  /**
   * @return live list of request header filters (use {@link #withRequestHeaderFilter(IHttpHeaderFilter)} to add
   *         filters)
   */
  public List<IHttpHeaderFilter> getRequestHeaderFilters() {
    return m_requestHeaderFilters;
  }

  public HttpProxy withRequestHeaderFilter(IHttpHeaderFilter filter) {
    m_requestHeaderFilters.add(filter);
    return this;
  }

  /**
   * @return live list of response header filters (use {@link #withResponseHeaderFilter(IHttpHeaderFilter)} to add
   *         filters)
   */
  public List<IHttpHeaderFilter> getResponseHeaderFilters() {
    return m_responseHeaderFilters;
  }

  public HttpProxy withResponseHeaderFilter(IHttpHeaderFilter filter) {
    m_responseHeaderFilters.add(filter);
    return this;
  }
}
