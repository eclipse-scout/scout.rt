/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.shared.http.AbstractHttpTransportManager;
import org.eclipse.scout.rt.shared.http.ApacheHttpTransportFactory.ApacheHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.IHttpTransportBuilder;
import org.eclipse.scout.rt.shared.http.IHttpTransportManager;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnelConfigurationProperties.HttpServiceTunnelTransportMaxConnectionsPerRouteProperty;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnelConfigurationProperties.HttpServiceTunnelTransportMaxConnectionsTotalProperty;

/**
 * {@link IHttpTransportManager} for {@link HttpServiceTunnel}.
 */
public class HttpServiceTunnelTransportManager extends AbstractHttpTransportManager {

  @Override
  public String getName() {
    return "scout.transport.service-tunnel";
  }

  @Override
  public void interceptNewHttpTransport(IHttpTransportBuilder builder0) {
    super.interceptNewHttpTransport(builder0);

    if (builder0 instanceof ApacheHttpTransportBuilder) {
      ApacheHttpTransportBuilder builder = (ApacheHttpTransportBuilder) builder0;

      if (builder.getConnectionManager() != null && builder.getConnectionManager() instanceof PoolingHttpClientConnectionManager) {
        @SuppressWarnings("resource")
        PoolingHttpClientConnectionManager cm = (PoolingHttpClientConnectionManager) builder.getConnectionManager();

        cm.setDefaultMaxPerRoute(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsPerRouteProperty.class));
        cm.setMaxTotal(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsTotalProperty.class));
      }
      else {
        builder.getBuilder().setMaxConnPerRoute(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsPerRouteProperty.class));
        builder.getBuilder().setMaxConnTotal(CONFIG.getPropertyValue(HttpServiceTunnelTransportMaxConnectionsTotalProperty.class));
      }
    }
  }

}
