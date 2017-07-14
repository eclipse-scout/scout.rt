package org.eclipse.scout.rt.shared.http;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportConnectionTimeToLiveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportKeepAliveProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportMaxConnectionsTotalProperty;
import org.eclipse.scout.rt.shared.http.HttpConfigurationProperties.ApacheHttpTransportRetryPostProperty;
import org.eclipse.scout.rt.shared.http.proxy.ConfigurableProxySelector;
import org.eclipse.scout.rt.shared.http.transport.ApacheHttpTransport;
import org.eclipse.scout.rt.shared.servicetunnel.http.MultiSessionCookieStore;

import com.google.api.client.http.HttpTransport;

/**
 * Factory to create the {@link ApacheHttpTransport} instances.
 */
public class ApacheHttpTransportFactory implements IHttpTransportFactory {

  @Override
  public HttpTransport newHttpTransport(IHttpTransportManager manager) {
    HttpClientBuilder builder = HttpClients.custom();

    installConfigurableProxySelector(builder);
    installMultiSessionCookieStore(builder);

    setConnectionKeepAliveAndRetrySettings(builder);

    HttpClientConnectionManager cm = getConfiguredConnectionManager();
    if (cm != null) {
      builder.setConnectionManager(cm);
    }

    interceptNewHttpTransport(builder, manager);
    manager.interceptNewHttpTransport(new ApacheHttpTransportBuilder(builder, cm));

    return new ApacheHttpTransport(builder.build());
  }

  /**
   * @param builder
   */
  protected void setConnectionKeepAliveAndRetrySettings(HttpClientBuilder builder) {
    final boolean keepAliveProp = CONFIG.getPropertyValue(ApacheHttpTransportKeepAliveProperty.class);
    builder.setConnectionReuseStrategy(new ConnectionReuseStrategy() {

      @Override
      public boolean keepAlive(HttpResponse response, HttpContext context) {
        return keepAliveProp;
      }
    });

    // Connections should not be kept open forever, there are numerous reasons a connection could get invalid. Also it does not make much sense to keep a connection open forever
    builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {

      @Override
      public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        long headerKeepAliveDuration = super.getKeepAliveDuration(response, context);
        return headerKeepAliveDuration < 0 ? 5000 : headerKeepAliveDuration;
      }

    });

    final boolean retryPostProp = CONFIG.getPropertyValue(ApacheHttpTransportRetryPostProperty.class);
    builder.setRetryHandler(new DefaultHttpRequestRetryHandler(1, true) {

      @Override
      protected boolean handleAsIdempotent(HttpRequest request) {
        return retryPostProp || super.handleAsIdempotent(request);
      }

    });
  }

  /**
   * Return the {@link HttpClientConnectionManager}. Return <code>null</code> to create it using the
   * {@link HttpClientBuilder}. Caution: Returning a custom connection manager overrides several properties of the
   * {@link HttpClientBuilder}.
   */
  protected HttpClientConnectionManager getConfiguredConnectionManager() {
    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
        (SSLSocketFactory) SSLSocketFactory.getDefault(),
        StringUtility.split(System.getProperty("https.protocols"), "\\s*,\\s*"),
        StringUtility.split(System.getProperty("https.cipherSuites"), "\\s*,\\s*"),
        new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault()));
    final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", sslConnectionSocketFactory)
            .build(),
        null, null, null, CONFIG.getPropertyValue(ApacheHttpTransportConnectionTimeToLiveProperty.class), TimeUnit.MILLISECONDS);
    connectionManager.setValidateAfterInactivity(1);

    Integer maxTotal = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsTotalProperty.class);
    if (maxTotal != null && maxTotal > 0) {
      connectionManager.setMaxTotal(maxTotal);
    }
    Integer defaultMaxPerRoute = CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsPerRouteProperty.class);
    if (defaultMaxPerRoute > 0) {
      connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    }
    return connectionManager;
  }

  /**
   * Install an instance of the {@link ConfigurableProxySelector} to select proxies.
   */
  protected void installConfigurableProxySelector(HttpClientBuilder builder) {
    builder.setRoutePlanner(new SystemDefaultRoutePlanner(BEANS.get(ConfigurableProxySelector.class)));
  }

  /**
   * Install a {@link MultiSessionCookieStore} to store cookies by session.
   */
  protected void installMultiSessionCookieStore(HttpClientBuilder builder) {
    builder.setDefaultCookieStore(BEANS.get(ApacheMultiSessionCookieStore.class));
  }

  /**
   * Intercept the building of the new {@link HttpTransport}.
   */
  protected void interceptNewHttpTransport(HttpClientBuilder builder, IHttpTransportManager manager) {
    // nop
  }

  public static class ApacheHttpTransportBuilder implements IHttpTransportBuilder {
    private final HttpClientBuilder m_builder;
    private final HttpClientConnectionManager m_connectionManager;

    public ApacheHttpTransportBuilder(HttpClientBuilder builder, HttpClientConnectionManager connectionManager) {
      m_builder = builder;
      m_connectionManager = connectionManager;
    }

    public HttpClientBuilder getBuilder() {
      return m_builder;
    }

    public HttpClientConnectionManager getConnectionManager() {
      return m_connectionManager;
    }
  }
}
