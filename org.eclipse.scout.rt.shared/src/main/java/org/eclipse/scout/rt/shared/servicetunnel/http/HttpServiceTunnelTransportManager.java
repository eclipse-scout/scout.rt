package org.eclipse.scout.rt.shared.servicetunnel.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.http.AbstractHttpTransportManager;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;

/**
 * {@link IHttpTransportManager} for {@link HttpServiceTunnel}.
 */
public class HttpServiceTunnelTransportManager extends AbstractHttpTransportManager {

  @Override
  public void interceptNewHttpTransport(Object builder) {
    super.interceptNewHttpTransport(builder);

    if (builder instanceof HttpClientBuilder) {
      ((HttpClientBuilder) builder).setConnectionTimeToLive(CONFIG.getPropertyValue(HttpServiceTunnelTransportConnectionTimeToLiveProperty.class), TimeUnit.MILLISECONDS);
      ((HttpClientBuilder) builder).setMaxConnPerRoute(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsPerRouteProperty.class));
      ((HttpClientBuilder) builder).setMaxConnTotal(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsTotalProperty.class));
    }
  }

}
