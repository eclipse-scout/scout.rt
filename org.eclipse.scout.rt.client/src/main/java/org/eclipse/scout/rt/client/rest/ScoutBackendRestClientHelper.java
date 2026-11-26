/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.rest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.client.servicetunnel.ProcessResourceClient;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.BackendUrlProperty;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.rest.ServletConstants;
import org.eclipse.scout.rt.rest.cancellation.RestRequestCancellationClientRequestFilter;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;
import org.eclipse.scout.rt.rest.client.cancellation.CancellationResourceClient;
import org.eclipse.scout.rt.rest.client.proxy.ErrorDoRestClientExceptionTransformer;
import org.eclipse.scout.rt.rest.id.IdSignatureClientRequestFilter;
import org.eclipse.scout.rt.shared.TunnelToServer;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelOptions;

@ApplicationScoped
public class ScoutBackendRestClientHelper extends AbstractRestClientHelper {

  @Override
  public String getBaseUri() {
    return CONFIG.getPropertyValue(BackendUrlProperty.class) + ServletConstants.API_PATH;
  }

  @Override
  protected List<ClientRequestFilter> getRequestFiltersToRegister() {
    List<ClientRequestFilter> filters = super.getRequestFiltersToRegister();
    filters.add(BEANS.get(AuthenticationTokenClientRequestFilter.class));
    filters.add(new RestRequestCancellationClientRequestFilter(this::cancelRequest));
    // no classic IdSignatureClientRequestFilter; process resource will determine on its own if header should be added
    filters.add(BEANS.get(ServiceTunnelIdSignatureClientRequestFilter.class));
    filters.add(BEANS.get(SessionIdClientRequestFilter.class));
    filters.add(BEANS.get(NodeIdClientRequestFilter.class));
    return filters;
  }

  protected void cancelRequest(String requestId) {
    BEANS.get(CancellationResourceClient.class).cancel(this, requestId);
  }

  @Override
  protected void configureClientBuilder(ClientBuilder clientBuilder) {
    super.configureClientBuilder(clientBuilder);

    BEANS.all(IScoutBackendRestClientBuilderContributor.class).forEach(contributor -> contributor.contribute(clientBuilder));
  }

  @Override
  protected RuntimeException transformException(RuntimeException e, Response response) {
    return BEANS.get(ErrorDoRestClientExceptionTransformer.class).transform(e, response);
  }

  @Bean
  public interface IScoutBackendRestClientBuilderContributor {
    void contribute(ClientBuilder clientBuilder);
  }

  /**
   * Special implementation similar to {@link IdSignatureClientRequestFilter}, however for service-tunnel requests the ID signautre header is not added by default
   * but only if {@link ServiceTunnelOptions} on {@link RunContext} does request it.
   */
  @ApplicationScoped
  public static class ServiceTunnelIdSignatureClientRequestFilter extends IdSignatureClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
      if (requestContext.getProperty(ProcessResourceClient.SERVICE_TUNNEL_REQUEST_HINT) != null) {
        addServiceTunnelIdSignatureHeader(requestContext);
      }
      else {
        addIdSignatureHeader(requestContext);
      }
    }

    // FIXME PBZ deps: Pages mit DataModel Lookups  -> smartcolum via DatenModell  -> UID/Keys auflösen
    // Raw-DOs mit untypisierten Ids   -> lesen der response noch roh
    // Kacheln & Prozesse @TunnelToServer(idSignature = true)

    protected void addServiceTunnelIdSignatureHeader(ClientRequestContext requestContext) {
      if (Optional.ofNullable(RunContext.CURRENT.get())
          .map(rc -> rc.getPropertyOrDefault(ServiceTunnelOptions.ID_SIGNATURE_PROP, false))
          .orElse(false)) {
        requestContext.getHeaders().putSingle(IdSignatureClientRequestFilter.ID_SIGNATURE_HTTP_HEADER, Boolean.TRUE.toString());
      }
    }
  }
}
