/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.protocol.HTTP;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.AbstractLongConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.rest.IRestHttpRequestUriEncoder;
import org.eclipse.scout.rt.rest.client.RestClientProperties;
import org.eclipse.scout.rt.shared.http.HttpClientMetricsHelper;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.message.internal.Statuses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;

/**
 * A {@link Connector} that utilizes the Apache HTTP Client to send and receive HTTP request and responses.
 * <p/>
 * See {@link RestClientProperties} for a list of supported properties.
 */
public class ScoutApacheConnector implements Connector {

  private static final Logger LOG = LoggerFactory.getLogger(ScoutApacheConnector.class);

  protected final CloseableHttpClient m_client;
  protected final RequestConfig m_requestConfig;
  protected final CookieStore m_cookieStore;

  /**
   * Create Scout Apache HTTP Client connector.
   *
   * @param client
   *          JAX-RS client instance for which the connector is being created.
   * @param config
   *          client configuration.
   */
  public ScoutApacheConnector(Client client, Configuration config) {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();

    // (1) setup HTTP connection manager
    SSLContext sslContext = client.getSslContext();
    clientBuilder.setConnectionManager(createConnectionManager(client, config, sslContext));
    clientBuilder.setConnectionManagerShared(isConnectionManagerShared());
    clientBuilder.setSSLContext(sslContext);

    // (2) setup proxy configuration
    initProxyConfig(config, clientBuilder);

    // (3) setup default request and cookie handling
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    boolean enableCookies = initCookieConfig(config, clientBuilder, requestConfigBuilder);
    m_cookieStore = createCookieStore(clientBuilder, enableCookies);

    // (4) setup and build HTTP client
    m_requestConfig = buildRequestConfig(requestConfigBuilder);
    clientBuilder.setDefaultRequestConfig(m_requestConfig);
    clientBuilder.disableDefaultUserAgent(); // disable sending user agent header like "Apache-HttpClient/4.5.13 (Java/1.8.0_191)"
    m_client = buildHttpClient(clientBuilder);
  }

  /**
   * Creates a preconfigured Apache HTTP {@link HttpClientConnectionManager}
   */
  protected HttpClientConnectionManager createConnectionManager(Client client, Configuration config, SSLContext sslContext) {
    String[] sslProtocols = split(System.getProperty("https.protocols"));
    String[] sslCipherSuites = split(System.getProperty("https.cipherSuites"));

    HostnameVerifier hostnameVerifier = client.getHostnameVerifier();
    LayeredConnectionSocketFactory sslConnectionSocketFactory;
    if (sslContext != null) {
      sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, sslProtocols, sslCipherSuites, hostnameVerifier);
    }
    else {
      sslConnectionSocketFactory = new SSLConnectionSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault(), sslProtocols, sslCipherSuites, hostnameVerifier);
    }

    HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = null;
    IRestHttpRequestUriEncoder uriEncoder = (IRestHttpRequestUriEncoder) config.getProperty(RestClientProperties.REQUEST_URI_ENCODER);
    if (uriEncoder != null) {
      // explicitly create connection factory to replace default LineFormatter
      connFactory = new ManagedHttpClientConnectionFactory(
          new DefaultHttpRequestWriterFactory(new LineFormatterWithUriEncoder(uriEncoder)),
          DefaultHttpResponseParserFactory.INSTANCE);
    }

    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslConnectionSocketFactory)
            .build(),
        connFactory, null, null, getKeepAliveTimeoutMillis(config), TimeUnit.MILLISECONDS);

    final int validateAfterInactivityMillis = getValidateAfterInactivityMillis(config);
    if (validateAfterInactivityMillis > 0) {
      connectionManager.setValidateAfterInactivity(validateAfterInactivityMillis);
    }

    final int maxTotal = getMaxConnectionsTotal(config);
    if (maxTotal > 0) {
      connectionManager.setMaxTotal(maxTotal);
    }

    final int defaultMaxPerRoute = getMaxConnectionsPerRoute(config);
    if (defaultMaxPerRoute > 0) {
      connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }

    initMetrics(config, connectionManager);
    return connectionManager;
  }

  /**
   * Initializes metrics for this HTTP connection manager (if configured by setting a value for
   * {@link RestClientProperties#OTEL_HTTP_CLIENT_NAME}).
   */
  protected void initMetrics(Configuration config, PoolingHttpClientConnectionManager connectionManager) {
    Object httpClientName = config.getProperty(RestClientProperties.OTEL_HTTP_CLIENT_NAME);
    if (httpClientName instanceof String) {
      Meter meter = GlobalOpenTelemetry.get().getMeter(getClass().getName());
      BEANS.get(HttpClientMetricsHelper.class).initMetrics(meter, (String) httpClientName,
          () -> connectionManager.getTotalStats().getAvailable(),
          () -> connectionManager.getTotalStats().getLeased(),
          () -> connectionManager.getTotalStats().getMax());
    }
  }

  /**
   * @see HttpClientBuilder#setConnectionManagerShared(boolean)
   */
  protected boolean isConnectionManagerShared() {
    return false;
  }

  /**
   * Splits comma-separated input string into its parts
   */
  protected String[] split(String input) {
    String[] values = StringUtility.split(input, "\\s*,\\s*");
    return values != null && values.length > 0 ? values : null;
  }

  /**
   * @deprecated This method will be removed with Scout release 25.1. Use the configuration property to change this
   * configuration or override method {@link #getKeepAliveTimeoutMillis(Configuration)}.
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  protected long getKeepAliveTimeoutMillis() {
    return CONFIG.getPropertyValue(RestHttpTransportConnectionKeepAliveProperty.class);
  }

  /**
   * Max timeout in ms connections are kept open when idle (requires keep-alive support).
   */
  protected long getKeepAliveTimeoutMillis(Configuration config) {
    return ObjectUtility.nvl(
        TypeCastUtility.castValue(config.getProperty(RestClientProperties.CONNECTION_KEEP_ALIVE), Long.class),
        getKeepAliveTimeoutMillis());
  }

  /**
   * @deprecated This method will be removed with Scout release 25.1. Use the configuration property to change this
   *             configuration or override method {@link #getMaxConnectionsTotal(Configuration)}.
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  protected int getMaxConnectionsTotal() {
    return CONFIG.getPropertyValue(RestHttpTransportMaxConnectionsTotalProperty.class);
  }

  /**
   * Max number of total concurrent connections managed by the {@link HttpClientConnectionManager} returned by
   * {@link #createConnectionManager(Client, Configuration, SSLContext)}.
   */
  protected int getMaxConnectionsTotal(Configuration config) {
    return ObjectUtility.nvl(
        TypeCastUtility.castValue(config.getProperty(RestClientProperties.MAX_CONNECTIONS_TOTAL), Integer.class),
        getMaxConnectionsTotal());
  }

  /**
   * @deprecated This method will be removed with Scout release 25.1. Use the configuration property to change this
   *             configuration or override method {@link #getMaxConnectionsPerRoute(Configuration)}.
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  protected int getMaxConnectionsPerRoute() {
    return CONFIG.getPropertyValue(RestHttpTransportMaxConnectionsPerRouteProperty.class);
  }

  /**
   * Max number of concurrent connections per route managed by the {@link HttpClientConnectionManager} returned by
   * {@link #createConnectionManager(Client, Configuration, SSLContext)}.
   */
  protected int getMaxConnectionsPerRoute(Configuration config) {
    return ObjectUtility.nvl(
        TypeCastUtility.castValue(config.getProperty(RestClientProperties.MAX_CONNECTIONS_PER_ROUTE), Integer.class),
        getMaxConnectionsPerRoute());
  }

  /**
   * @deprecated This method will be removed with Scout release 25.1. Use the configuration property to change this
   *             configuration or override method {@link #getValidateAfterInactivityMillis(Configuration)}.
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  protected int getValidateAfterInactivityMillis() {
    return CONFIG.getPropertyValue(RestHttpTransportValidateAfterInactivityProperty.class);
  }

  /**
   * Defines period of inactivity in milliseconds after which persistent connections must be re-validated prior to being
   * leased to the consumer. Non-positive value passed to this method disables connection validation. This check helps
   * detect connections that have become stale (half-closed) while kept inactive in the pool.
   */
  protected int getValidateAfterInactivityMillis(Configuration config) {
    return ObjectUtility.nvl(
        TypeCastUtility.castValue(config.getProperty(RestClientProperties.VALIDATE_CONNECTION_AFTER_INACTIVITY), Integer.class),
        getValidateAfterInactivityMillis());
  }

  /**
   * Setup proxy configuration for {@link HttpClientConnectionManager}. A proxy may be specified using the
   * {@link RestClientProperties#PROXY_URI} property or by using the Scout {@link ConfigurableProxySelector}
   * configuration.
   */
  protected void initProxyConfig(Configuration config, HttpClientBuilder clientBuilder) {
    Object proxyUri = config.getProperty(RestClientProperties.PROXY_URI);
    if (proxyUri != null) {
      URI uri = parseProxyUri(proxyUri);
      HttpHost proxy = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
      String userName = ClientProperties.getValue(config.getProperties(), RestClientProperties.PROXY_USERNAME, String.class);
      if (userName != null) {
        String password = ClientProperties.getValue(config.getProperties(), RestClientProperties.PROXY_PASSWORD, String.class);
        if (password != null) {
          CredentialsProvider credsProvider = new BasicCredentialsProvider();
          credsProvider.setCredentials(
              new AuthScope(uri.getHost(), uri.getPort()),
              new UsernamePasswordCredentials(userName, password));
          clientBuilder.setDefaultCredentialsProvider(credsProvider);
          clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        }
      }
      clientBuilder.setProxy(proxy);
    }
    else {
      // if no specific proxy configuration available, use default scout ConfigurableProxySelector
      clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
    }
  }

  protected URI parseProxyUri(Object proxy) {
    if (proxy instanceof URI) {
      return (URI) proxy;
    }
    else if (proxy instanceof String) {
      return URI.create((String) proxy);
    }
    else {
      throw new AssertionException("The proxy URI ('{0}') property MUST be an instance of String or URI", proxy);
    }
  }

  protected boolean initCookieConfig(Configuration config, HttpClientBuilder clientBuilder, RequestConfig.Builder requestConfigBuilder) {
    boolean enableCookies = PropertiesHelper.isProperty(config.getProperties(), RestClientProperties.ENABLE_COOKIES);
    if (enableCookies) {
      String cookieSpec = TypeCastUtility.castValue(config.getProperty(RestClientProperties.COOKIE_SPEC), String.class);
      requestConfigBuilder.setCookieSpec(cookieSpec);
    }
    else {
      requestConfigBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
    }
    return enableCookies;
  }

  protected CookieStore createCookieStore(HttpClientBuilder clientBuilder, boolean enableCookies) {
    if (enableCookies) {
      CookieStore cookieStore = new BasicCookieStore();
      clientBuilder.setDefaultCookieStore(cookieStore);
      return cookieStore;
    }
    return null;
  }

  protected RequestConfig buildRequestConfig(Builder requestConfigBuilder) {
    return requestConfigBuilder.build();
  }

  protected CloseableHttpClient buildHttpClient(HttpClientBuilder httpClientBuilder) {
    return httpClientBuilder.build();
  }

  @Override
  public ClientResponse apply(final ClientRequest clientRequest) throws ProcessingException {
    HttpUriRequest request = getUriHttpRequest(clientRequest);

    // Work around for rare abnormal connection terminations (258238)
    ensureHttpHeaderCloseConnection(clientRequest, request);

    // setup default user agent
    ensureDefaultUserAgent(clientRequest);

    Map<String, String> clientHeadersSnapshot = writeOutBoundHeaders(clientRequest, request);
    IRegistrationHandle cancellableHandle = registerCancellable(clientRequest, request);

    try {
      CloseableHttpResponse response;
      HttpClientContext context = HttpClientContext.create();

      HttpHost target = new HttpHost(request.getURI().getHost(), request.getURI().getPort(), request.getURI().getScheme());
      //noinspection resource (will be closed by ClosingInputStream in ClientResponse)
      response = m_client.execute(target, request, context);
      HeaderUtils.checkHeaderChanges(clientHeadersSnapshot, clientRequest.getHeaders(), this.getClass().getName(), clientRequest.getConfiguration());

      Response.StatusType status = response.getStatusLine().getReasonPhrase() == null
          ? Statuses.from(response.getStatusLine().getStatusCode())
          : Statuses.from(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());

      ClientResponse responseContext = new ClientResponse(status, clientRequest);
      List<URI> redirectLocations = context.getRedirectLocations();
      if (redirectLocations != null && !redirectLocations.isEmpty()) {
        responseContext.setResolvedRequestUri(redirectLocations.get(redirectLocations.size() - 1));
      }

      Header[] respHeaders = response.getAllHeaders();
      MultivaluedMap<String, String> headers = responseContext.getHeaders();
      for (Header header : respHeaders) {
        String headerName = header.getName();
        List<String> list = headers.get(headerName);
        if (list == null) {
          list = new ArrayList<>();
        }
        list.add(header.getValue());
        headers.put(headerName, list);
      }

      HttpEntity entity = response.getEntity();
      if (entity != null) {
        if (headers.get(HttpHeaders.CONTENT_LENGTH) == null && entity.getContentLength() >= 0) {
          headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
        }

        Header contentEncoding = entity.getContentEncoding();
        if (headers.get(HttpHeaders.CONTENT_ENCODING) == null && contentEncoding != null) {
          headers.add(HttpHeaders.CONTENT_ENCODING, contentEncoding.getValue());
        }
      }
      responseContext.setEntityStream(getResponseInputStream(request, response));
      return responseContext;
    }
    catch (Exception e) {
      throw new ProcessingException("Failed to execute request, message=" + e.getMessage(), e);
    }
    finally {
      cancellableHandle.dispose();
    }
  }

  /**
   * Setup {@link HttpUriRequest} based on given {@link ClientRequest}.
   */
  protected HttpUriRequest getUriHttpRequest(final ClientRequest clientRequest) {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.copy(m_requestConfig);

    // jersey property set by helper method clientBuilder.connectTimeout()
    int connectTimeout = clientRequest.resolveProperty(ClientProperties.CONNECT_TIMEOUT, -1);
    if (connectTimeout >= 0) {
      requestConfigBuilder.setConnectTimeout(connectTimeout);
    }

    // jersey property set by helper method clientBuilder.readTimeout()
    int socketTimeout = clientRequest.resolveProperty(ClientProperties.READ_TIMEOUT, -1);
    if (socketTimeout >= 0) {
      requestConfigBuilder.setSocketTimeout(socketTimeout);
    }

    boolean redirectsEnabled = clientRequest.resolveProperty(RestClientProperties.FOLLOW_REDIRECTS, m_requestConfig.isRedirectsEnabled());
    requestConfigBuilder.setRedirectsEnabled(redirectsEnabled);

    boolean bufferingEnabled = BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.DISABLE_CHUNKED_TRANSFER_ENCODING, Boolean.class));
    HttpEntity entity = getHttpEntity(clientRequest, bufferingEnabled);

    return RequestBuilder
        .create(clientRequest.getMethod())
        .setUri(clientRequest.getUri())
        .setConfig(requestConfigBuilder.build())
        .setEntity(entity)
        .build();
  }

  /**
   * Creates {@link HttpEntity} wrapping payload out of given {@code clientRequest}.
   */
  protected HttpEntity getHttpEntity(ClientRequest clientRequest, boolean bufferingEnabled) {
    if (clientRequest.getEntity() == null) {
      return null; // no http payload
    }

    HttpEntity httpEntity = new NonStreamingHttpEntity(clientRequest, bufferingEnabled);
    if (bufferingEnabled) {
      return bufferEntity(httpEntity);
    }
    return httpEntity;
  }

  protected HttpEntity bufferEntity(HttpEntity httpEntity) {
    try {
      return new BufferedHttpEntity(httpEntity);
    }
    catch (IOException e) {
      throw new ProcessingException("Error buffering entity", e);
    }
  }

  /**
   * Adds the HTTP header {@code Connection: close} if {@code RestClientProperties.CONNECTION_CLOSE} is {@code true} or
   * {@link RestEnsureHttpHeaderConnectionCloseProperty} is {@code true} and the given {@code headers} do not contain
   * the key {@code Connection}.
   */
  protected void ensureHttpHeaderCloseConnection(ClientRequest clientRequest, HttpUriRequest httpRequest) {
    boolean closeConnection = BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.CONNECTION_CLOSE, CONFIG.getPropertyValue(RestEnsureHttpHeaderConnectionCloseProperty.class)), true);
    MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
    if (closeConnection && !headers.containsKey(HTTP.CONN_DIRECTIVE)) {
      LOG.trace("Adding HTTP header '" + HTTP.CONN_DIRECTIVE + ": " + HTTP.CONN_CLOSE + "'");
      httpRequest.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
    }
  }

  /**
   * Adds a default user agent header if {@code RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT} is {@code false} and
   * no user agent header is present.
   */
  protected void ensureDefaultUserAgent(ClientRequest clientRequest) {
    boolean suppressDefaultUserAgent = BooleanUtility.nvl(clientRequest.resolveProperty(RestClientProperties.SUPPRESS_DEFAULT_USER_AGENT, false));
    if (!suppressDefaultUserAgent && !clientRequest.getHeaders().containsKey(HttpHeaders.USER_AGENT)) {
      clientRequest.getHeaders().add(HttpHeaders.USER_AGENT, "Generic");
    }
  }

  protected Map<String, String> writeOutBoundHeaders(final ClientRequest clientRequest, HttpUriRequest request) {
    Map<String, String> stringHeaders = HeaderUtils.asStringHeadersSingleValue(clientRequest.getHeaders(), clientRequest.getConfiguration());
    for (Map.Entry<String, String> e : stringHeaders.entrySet()) {
      request.addHeader(e.getKey(), e.getValue());
    }
    return stringHeaders;
  }

  /**
   * Registers an {@link ICancellable} if this method is invoked in the context of a {@link RunMonitor} (i.e.
   * {@link RunMonitor#CURRENT} is not {@code null}).
   */
  protected IRegistrationHandle registerCancellable(ClientRequest clientRequest, final HttpUriRequest request) {
    final RunMonitor runMonitor = RunMonitor.CURRENT.get();
    if (runMonitor == null) {
      return IRegistrationHandle.NULL_HANDLE;
    }
    ICancellable cancellable;
    Object c = clientRequest.getProperty(RestClientProperties.CANCELLABLE);
    if (c instanceof ICancellable) {
      // use cancellable provided by the client request and ignore the default HTTP connection-aborting strategy
      cancellable = (ICancellable) c;
    }
    else {
      if (c != null) {
        LOG.debug("non-null cancellable has unexpected type: " + c.getClass());
      }
      cancellable = new ICancellable() {
        @Override
        public boolean isCancelled() {
          return request.isAborted();
        }

        @Override
        public boolean cancel(boolean interruptIfRunning) {
          LOG.debug("Aborting HTTP REST request");
          request.abort();
          return true;
        }
      };
    }
    runMonitor.registerCancellable(cancellable);
    return () -> runMonitor.unregisterCancellable(cancellable);
  }

  @SuppressWarnings("resource")
  protected InputStream getResponseInputStream(HttpUriRequest httpUriRequest, CloseableHttpResponse response) throws IOException {
    InputStream inputStream;

    if (response.getEntity() == null) {
      inputStream = new ByteArrayInputStream(new byte[0]);
    }
    else {
      InputStream responseInputStream = response.getEntity().getContent();
      if (responseInputStream.markSupported()) {
        inputStream = responseInputStream;
      }
      else {
        inputStream = new BufferedInputStream(responseInputStream, ReaderWriter.BUFFER_SIZE);
      }
    }
    return new ClosingInputStream(httpUriRequest, response, inputStream);
  }

  @Override
  public Future<?> apply(final ClientRequest request, final AsyncConnectorCallback callback) {
    try {
      ClientResponse response = apply(request);
      callback.response(response);
      return CompletableFuture.completedFuture(response);
    }
    catch (Throwable t) {
      callback.failure(t);
      CompletableFuture<Object> future = new CompletableFuture<>();
      future.completeExceptionally(t);
      return future;
    }
  }

  @Override
  public String getName() {
    return "Scout Apache HttpClient Connector";
  }

  @Override
  public void close() {
    try {
      m_client.close();
    }
    catch (IOException e) {
      throw new ProcessingException("Failed to stop the client, message=" + e.getMessage(), e);
    }
  }

  /**
   * Non-streaming implementation of {@link HttpEntity}.
   */
  protected static class NonStreamingHttpEntity extends AbstractHttpEntity {

    protected final ClientRequest m_clientRequest;
    protected final boolean m_bufferingEnabled;

    protected NonStreamingHttpEntity(ClientRequest clientRequest, boolean bufferingEnabled) {
      m_clientRequest = clientRequest;
      m_bufferingEnabled = bufferingEnabled;
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public long getContentLength() {
      return -1;
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
      if (m_bufferingEnabled) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
        writeTo(buffer);
        return new ByteArrayInputStream(buffer.toByteArray());
      }
      else {
        return null;
      }
    }

    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
      m_clientRequest.setStreamProvider(contentLength -> outputStream);
      m_clientRequest.writeEntity();
    }

    @Override
    public boolean isStreaming() {
      return false;
    }
  }

  /**
   * {@link FilterInputStream} implementation aborting the corresponding http request and closing the http response when
   * the stream is closed.
   */
  protected static class ClosingInputStream extends FilterInputStream {

    protected final CloseableHttpResponse m_response;
    protected final HttpUriRequest m_httpUriRequest;

    protected ClosingInputStream(HttpUriRequest httpUriRequest, CloseableHttpResponse response, InputStream in) {
      super(in);
      m_response = response;
      m_httpUriRequest = httpUriRequest;
    }

    @Override
    public void close() throws IOException {
      // (1) abort request if necessary
      if (m_response.getEntity() != null && m_response.getEntity().isChunked()) {
        m_httpUriRequest.abort();
      }
      // (2) close input stream
      try {
        super.close();
      }
      catch (IOException ex) {
        // Ignore
      }
      finally {
        // (3) close response
        m_response.close();
      }
    }
  }

  public static class RestHttpTransportConnectionKeepAliveProperty extends AbstractLongConfigProperty {

    @Override
    public Long getDefaultValue() {
      return TimeUnit.MINUTES.toMillis(30);
    }

    @Override
    public String description() {
      return "Specifies the maximum life time in milliseconds for kept alive connections of the REST HTTP client. The default value is 30 minutes.";
    }

    @Override
    public String getKey() {
      return "scout.rest.client.http.connectionKeepAlive";
    }
  }

  public static class RestHttpTransportMaxConnectionsPerRouteProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 32;
    }

    @Override
    public String description() {
      return "Configuration property to define the default maximum connections per route of the Apache HTTP client. The default value is " + getDefaultValue();
    }

    @Override
    public String getKey() {
      return "scout.rest.client.http.maxConnectionsPerRoute";
    }
  }

  public static class RestHttpTransportMaxConnectionsTotalProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 128;
    }

    @Override
    public String description() {
      return "Specifies the total maximum connections of the Apache HTTP client. The default value is " + getDefaultValue();
    }

    @Override
    public String getKey() {
      return "scout.rest.client.http.maxConnectionsTotal";
    }
  }

  public static class RestHttpTransportValidateAfterInactivityProperty extends AbstractIntegerConfigProperty {

    @Override
    public Integer getDefaultValue() {
      return 1;
    }

    @Override
    public String description() {
      return "Defines period of inactivity in milliseconds after which persistent connections must be re-validated prior to being"
          + " leased to the consumer. Non-positive value passed to this method disables connection validation. This check helps"
          + " detect connections that have become stale (half-closed) while kept inactive in the pool. The default value is " + getDefaultValue();
    }

    @Override
    public String getKey() {
      return "scout.rest.client.http.validateAfterInactivity";
    }
  }
}
