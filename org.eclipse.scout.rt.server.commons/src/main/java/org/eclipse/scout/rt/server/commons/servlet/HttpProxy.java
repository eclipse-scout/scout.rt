/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;
import org.apache.hc.core5.http.nio.support.classic.AbstractClassicEntityConsumer;
import org.apache.hc.core5.http.nio.support.classic.AbstractClassicEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.support.AbstractRequestBuilder;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ConnectionErrorDetector;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyConfigProperties.HttpProxyAsyncHttpClientManagerConfigProperty;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyConfigProperties.HttpProxyAsyncTimeoutConfigProperty;
import org.eclipse.scout.rt.shared.http.async.AbstractAsyncHttpClientManager;
import org.eclipse.scout.rt.shared.http.async.DefaultAsyncHttpClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards HTTP requests to the given remote URL.
 */
@Bean
public class HttpProxy {
  private static final Logger LOG = LoggerFactory.getLogger(HttpProxy.class);

  private AbstractAsyncHttpClientManager m_httpClientManager;
  private int m_initialBufferSize = 4096;
  private String m_remoteBaseUrl;
  private final List<IHttpHeaderFilter> m_requestHeaderFilters;
  private final List<IHttpHeaderFilter> m_responseHeaderFilters;
  private Executor m_blockingOperationExecutor;
  private Supplier<HttpClientContext> m_httpClientContextSupplier;

  public HttpProxy() {
    m_httpClientManager = BEANS.get(CONFIG.getPropertyValue(HttpProxyAsyncHttpClientManagerConfigProperty.class));
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

    m_blockingOperationExecutor = createBlockingOperationExecutor();
  }

  protected ExecutorService createBlockingOperationExecutor() {
    return BEANS.get(JobManager.class).getExecutor();
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
   * @see #writeRequestParameters(HttpServletRequest, AsyncRequestBuilder)
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
    // response processing is async, start async context as processing must still be possible after exiting this method
    AsyncContext asyncContext = req.startAsync(req, resp);
    asyncContext.setTimeout(CONFIG.getPropertyValue(HttpProxyAsyncTimeoutConfigProperty.class));

    if (options == null) {
      options = new HttpProxyRequestOptions();
    }

    String url = rewriteUrl(req, options);
    LOG.debug("Forwarding {} request to {}", req.getMethod(), url);

    AsyncRequestBuilder asyncRequestBuilder = prepareRequest(AsyncRequestBuilder
        .create(req.getMethod())
        .setUri(url));

    writeRequestHeaders(req, asyncRequestBuilder);
    writeCustomRequestHeaders(asyncRequestBuilder, options.getCustomRequestHeaders());

    if (shouldIncludeRequestPayload(req)) {
      // Payload is empty if parameters are used (usually with content type = application/x-www-form-urlencoded)
      // -> write parameters if there are any, otherwise write the raw payload
      if (shouldWriteParametersAsPayload(req)) {
        writeRequestParameters(req, asyncRequestBuilder);
      }
      else {
        asyncRequestBuilder.setEntity(createEntityProducer(req));
      }
    }

    LOG.trace("Executing request for {}", url);
    // Execute request and add a listener potentially cancelling the proxy request if async context times out/fails (if incoming request is not available anymore)
    Future<Boolean> future = getHttpClientManager().getClient().execute(asyncRequestBuilder.build(), createAsyncResponseConsumer(resp), createHttpContext(), createExecuteCallback(resp, asyncContext));
    addCancelListener(asyncContext, future);
  }

  protected HttpContext createHttpContext() {
    Supplier<HttpClientContext> httpClientContextSupplier = getHttpClientContextSupplier();
    return httpClientContextSupplier != null ? httpClientContextSupplier.get() : HttpClientContext.create();
  }

  /**
   * Rewrites the <code>pathInfo</code> part of the current request if the rewriteRule and rewriteReplacement is set on
   * the options object. This allows to redirect the request to a different URL than the URL that has been requested.
   */
  protected String rewriteUrl(HttpServletRequest req, HttpProxyRequestOptions options) {
    String pathInfo = ObjectUtility.nvl(req.getPathInfo(), "");
    IRewriteRule rewriteRule = options.getRewriteRule();
    if (rewriteRule != null) {
      pathInfo = rewriteRule.rewrite(pathInfo);
    }
    // pathInfo must be url-encoded; except for forward-slashes they should be kept
    pathInfo = IOUtility.urlEncode(pathInfo).replaceAll("%2F", "/");
    return StringUtility.join("?", StringUtility.join("", getRemoteBaseUrl(), pathInfo), req.getQueryString());
  }

  protected AsyncRequestBuilder prepareRequest(AsyncRequestBuilder asyncRequestBuilder) {
    return asyncRequestBuilder;
  }

  protected void writeRequestHeaders(HttpServletRequest req, AbstractRequestBuilder asyncRequestBuilder) {
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
        asyncRequestBuilder.addHeader(name, value);
        LOG.trace("Added request header: {}: {}", name, value);
      }
      else {
        LOG.trace("Removed request header: {} (original value: {})", name, req.getHeader(name));
      }
    }
  }

  protected void writeCustomRequestHeaders(AsyncRequestBuilder asyncRequestBuilder, Map<String, String> customHeaders) {
    if (customHeaders == null) {
      return;
    }
    for (Entry<String, String> header : customHeaders.entrySet()) {
      asyncRequestBuilder.addHeader(header.getKey(), header.getValue());
      LOG.trace("Added custom request header: {}: {}", header.getKey(), header.getValue());
    }
  }

  protected void writeRequestPayload(HttpServletRequest req, OutputStream outputStream) throws IOException {
    ServletInputStream inputStream = req.getInputStream();
    if (inputStream == null) {
      return;
    }
    IOUtility.writeFromToStream(outputStream, inputStream);
  }

  protected void writeRequestParameters(HttpServletRequest req, AsyncRequestBuilder requestBuilder) {
    String parameters = formatFormParameters(req.getParameterMap());
    requestBuilder.setEntity(parameters);
  }

  protected String formatFormParameters(Map<String, String[]> parameterMap) {
    StringBuilder parameters = new StringBuilder();
    for (Entry<String, String[]> entry : parameterMap.entrySet()) {
      for (String value : entry.getValue()) {
        if (parameters.length() > 0) {
          parameters.append("&");
        }
        parameters
            .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
            .append("=")
            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
      }
    }
    return parameters.toString();
  }

  protected void writeResponseStatus(HttpServletResponse resp, HttpResponse httpResp) {
    int responseCode = httpResp.getCode();
    resp.setStatus(responseCode);
  }

  /**
   * Writes the response payload of forwarded request to the servlet response.
   */
  protected void writeResponsePayload(HttpServletResponse resp, InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return;
    }
    IOUtility.writeFromToStream(resp.getOutputStream(), inputStream);
  }

  protected void writeResponseHeaders(HttpServletResponse resp, HttpResponse httpResp) {
    final Set<String> hopByHopHeaderNames = getConnectionHeaderValues(httpResp);
    for (Header header : httpResp.getHeaders()) {
      String name = header.getName();
      String value = header.getValue();
      if (name != null && hopByHopHeaderNames.contains(name.toLowerCase(Locale.US))) {
        LOG.trace("Removed hop-by-hop response header: {} (original value: {})", name, value);
        continue;
      }
      String originalValue = value;
      for (IHttpHeaderFilter filter : getResponseHeaderFilters()) {
        value = filter.filter(name, value);
      }
      if (value != null) {
        resp.setHeader(name, value);
        LOG.trace("Added response header: {}: {}", name, value);
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
    return Arrays.stream(httpResp.getHeaders())
        .filter(h -> "Connection".equals(h.getName()))
        .map(Header::getValue)
        .flatMap(v -> Stream.of(StringUtility.split(v, ",")))
        .filter(StringUtility::hasText)
        .map(StringUtility::trim)
        .map(s -> s.toLowerCase(Locale.US))
        .collect(Collectors.toSet());
  }

  protected void addCancelListener(AsyncContext asyncContext, Future<Boolean> future) {
    try {
      asyncContext.addListener(createCancelListener(future));
    }
    catch (IllegalStateException e) {
      LOG.info("Unable to add timeout/error listener for proxy request, maybe request was already completed", e);
    }
  }

  protected AsyncListener createCancelListener(Future<Boolean> future) {
    return new AsyncListener() {
      @Override
      public void onComplete(AsyncEvent event) {
        // nop
      }

      @Override
      public void onTimeout(AsyncEvent event) {
        LOG.info("Servlet request timed-out, cancelling proxy request", event.getThrowable());
        future.cancel(true);
      }

      @Override
      public void onError(AsyncEvent event) {
        LOG.info("Error while forwarding servlet request to proxy, cancelling proxy request", event.getThrowable());
        future.cancel(true);
      }

      @Override
      public void onStartAsync(AsyncEvent event) {
        // nop
      }
    };
  }

  public AbstractAsyncHttpClientManager getHttpClientManager() {
    return m_httpClientManager;
  }

  /**
   * @param manager
   *          used to initialize {@link CloseableHttpAsyncClient}, by default {@link DefaultAsyncHttpClientManager} is
   *          used
   */
  public HttpProxy withHttpClientManager(AbstractAsyncHttpClientManager manager) {
    m_httpClientManager = manager;
    return this;
  }

  public int getInitialBufferSize(HttpServletRequest request) {
    return m_initialBufferSize;
  }

  public int getInitialBufferSize(HttpServletResponse response) {
    return m_initialBufferSize;
  }

  /**
   * @param initialBufferSize
   *          specify the initialBufferSize used for {@link AbstractClassicEntityConsumer} and
   *          {@link AbstractClassicEntityProducer}
   */
  public HttpProxy withInitialBufferSize(int initialBufferSize) {
    m_initialBufferSize = initialBufferSize;
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
   *          by concatenating this URL and the requests "path info". If URL contains a trailing slash this method
   *          removes it.
   * @see #rewriteUrl(HttpServletRequest, HttpProxyRequestOptions)
   */
  public HttpProxy withRemoteBaseUrl(String remoteBaseUrl) {
    if (remoteBaseUrl != null && remoteBaseUrl.endsWith("/")) {
      // remove trailing slash (if set), rewriteUrl takes care of slash between remoteBaseUrl and pathInfo
      remoteBaseUrl = remoteBaseUrl.substring(0, remoteBaseUrl.length() - 1);
    }
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

  public Executor getBlockingOperationExecutor() {
    return m_blockingOperationExecutor;
  }

  public Supplier<HttpClientContext> getHttpClientContextSupplier() {
    return m_httpClientContextSupplier;
  }

  /**
   * Create a supplier for {@link HttpClientContext} which is called upon each request.
   */
  public HttpProxy withHttpClientContextSupplier(Supplier<HttpClientContext> httpClientContextSupplier) {
    m_httpClientContextSupplier = httpClientContextSupplier;
    return this;
  }

  /**
   * <p>
   * Create an {@link AsyncEntityProducer} which will read data supplied by {@link HttpServletRequest} amd write it to
   * the proxy request (as soon as data is requested).
   * </p>
   */
  protected AsyncEntityProducer createEntityProducer(HttpServletRequest req) {
    return new AbstractClassicEntityProducer(getInitialBufferSize(req), null, getBlockingOperationExecutor()) {
      @Override
      protected void produceData(ContentType contentType, OutputStream outputStream) throws IOException {
        LOG.trace("Producing data for forwarded request (original uri: {})", req.getRequestURI());
        writeRequestPayload(req, outputStream);
      }
    };
  }

  /**
   * <p>
   * Create an {@link AsyncEntityConsumer} which will write incoming data to the {@link HttpServletResponse} payload.
   * </p>
   * <p>
   * This consumer writes the entity data immediately to the output consumer to avoid caching the data; therefore the
   * return type is just boolean to mark data has been written successfully (no exception occurred).
   * </p>
   */
  protected AsyncEntityConsumer<Boolean> createEntityConsumer(HttpServletResponse resp) {
    return new AbstractClassicEntityConsumer<>(getInitialBufferSize(resp), getBlockingOperationExecutor()) {
      @Override
      protected Boolean consumeData(ContentType contentType, InputStream inputStream) throws IOException {
        LOG.trace("Consuming data with contentType {}", contentType);
        writeResponsePayload(resp, inputStream);
        return true;
      }
    };
  }

  /**
   * <p>
   * Create the {@link AsyncResponseConsumer} which will internally call
   * {@link #createEntityConsumer(HttpServletResponse)} to consume the response.
   * </p>
   * <p>
   * Before the actual response payload is consumed the methods
   * {@link #writeResponseHeaders(HttpServletResponse, HttpResponse)} and
   * {@link #writeResponseStatus(HttpServletResponse, HttpResponse)} are called (in this order) to forward header and
   * status.
   * </p>
   */
  protected AsyncResponseConsumer<Boolean> createAsyncResponseConsumer(HttpServletResponse resp) {
    return new AsyncResponseConsumer<>() {

      private volatile AsyncEntityConsumer<Boolean> m_dataConsumer = createEntityConsumer(resp);

      @Override
      public void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context, FutureCallback<Boolean> resultCallback) throws HttpException, IOException {
        LOG.trace("Consuming response (protocol version: {})", context.getProtocolVersion());
        writeResponseHeaders(resp, response);
        writeResponseStatus(resp, response);

        if (entityDetails != null) {
          LOG.trace("Starting stream for entity (content-type: {})", entityDetails.getContentType());
          m_dataConsumer.streamStart(entityDetails, resultCallback);
        }
        else {
          LOG.trace("No entity data for response");
          resultCallback.completed(true);
        }
      }

      @Override
      public void informationResponse(HttpResponse response, HttpContext context) {
        // just informal
      }

      @Override
      public void failed(Exception cause) {
        LOG.trace("Response consumer failed: ", cause);
        try {
          BEANS.get(ExceptionHandler.class).handle(cause);
        }
        finally {
          releaseResources();
        }
      }

      @Override
      public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
        m_dataConsumer.updateCapacity(capacityChannel);
      }

      @Override
      public void consume(ByteBuffer src) throws IOException {
        m_dataConsumer.consume(src);
      }

      @Override
      public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
        m_dataConsumer.streamEnd(trailers);
      }

      @Override
      public void releaseResources() {
        if (m_dataConsumer != null) {
          m_dataConsumer.releaseResources();
        }
        m_dataConsumer = null;
      }
    };
  }

  /**
   * Provide the {@link FutureCallback} for requests which will call {@link AsyncContext#complete()} (to also complete
   * the outer proxied request) after request has either completed or failed.
   */
  protected FutureCallback<Boolean> createExecuteCallback(HttpServletResponse resp, AsyncContext asyncContext) {
    return new FutureCallback<>() {
      @Override
      public void completed(Boolean result) {
        LOG.trace("Request execution completed with result: {}", result);
        assertTrue(result);
        asyncContext.complete();
      }

      @Override
      public void failed(Exception ex) {
        LOG.trace("Request execution failed", ex);
        BEANS.get(ExceptionHandler.class).handle(ex);
        try {
          boolean alreadyCommitted = resp.isCommitted();
          if (!alreadyCommitted) {
            resp.setStatus(computeStatusCodeForFailure(ex));
          }
        }
        catch (AlreadyInvalidatedException e) {
          LOG.trace("Response is invalidated", e);
        }
        asyncContext.complete();
      }

      @Override
      public void cancelled() {
        LOG.trace("Request execution cancelled");
        asyncContext.complete();
      }
    };
  }

  protected int computeStatusCodeForFailure(Exception e) {
    // see org.apache.hc.client5.http.ConnectExceptionSupport for details which exceptions might throw
    if (e instanceof ConnectException) {
      // is thrown if the target system is unavailable (cannot be connected)
      // includes e.g. org.apache.hc.client5.http.HttpHostConnectException
      return HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
    if (BEANS.get(ConnectionErrorDetector.class).isConnectionError(e)) {
      // on connection reset (e.g. if connection was successful but has been aborted).
      return HttpStatus.SC_SERVICE_UNAVAILABLE;
    }
    if (e instanceof SocketTimeoutException) {
      // includes e.g. org.apache.hc.client5.http.ConnectTimeoutException
      return HttpStatus.SC_GATEWAY_TIMEOUT;
    }
    return HttpStatus.SC_INTERNAL_SERVER_ERROR;
  }
}
