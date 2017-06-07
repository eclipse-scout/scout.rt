package org.eclipse.scout.rt.shared.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
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

    builder.setConnectionTimeToLive(CONFIG.getPropertyValue(ApacheHttpTransportConnectionTimeToLiveProperty.class), TimeUnit.MILLISECONDS);
    builder.setMaxConnPerRoute(CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsPerRouteProperty.class));
    builder.setMaxConnTotal(CONFIG.getPropertyValue(ApacheHttpTransportMaxConnectionsTotalProperty.class));

    interceptNewHttpTransport(builder, manager);
    manager.interceptNewHttpTransport(builder);

    return new ApacheHttpTransport(builder.build());
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

}
