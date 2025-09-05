/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.rest;

import java.util.List;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;

import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.BackendUrlProperty;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.rest.ServletConstants;
import org.eclipse.scout.rt.rest.cancellation.RestRequestCancellationClientRequestFilter;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;

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
    filters.add(new RestRequestCancellationClientRequestFilter(BEANS.get(CancellationResourceClient.class)::cancel));
    // no classic IdSignatureClientRequestFilter; ProcessResourceClient will determine on its own if header should be added
    return filters;
  }

  @Override
  protected void configureClientBuilder(ClientBuilder clientBuilder) {
    super.configureClientBuilder(clientBuilder);

    BEANS.all(IScoutBackendRestClientBuilderContributor.class).forEach(contributor -> contributor.contribute(clientBuilder));
  }

  @Bean
  public interface IScoutBackendRestClientBuilderContributor {
    void contribute(ClientBuilder clientBuilder);
  }
}
