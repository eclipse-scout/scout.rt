/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.client.AbstractRestClientHelper;
import org.eclipse.scout.rt.rest.client.proxy.ErrorDoRestClientExceptionTransformer;

@ApplicationScoped
public class JerseyTestRestClientHelper extends AbstractRestClientHelper {

  @Override
  public String getBaseUri() {
    return "http://localhost:" + BEANS.get(JerseyTestApplication.class).getPort();
  }

  /**
   * Returns the raw, un-proxied {@link Client}.
   */
  public Client rawClient() {
    return internalClient();
  }

  @Override
  protected RuntimeException transformException(RuntimeException e, Response response) {
    return BEANS.get(ErrorDoRestClientExceptionTransformer.class).transform(e, response);
  }

  @Override
  protected List<ClientRequestFilter> getRequestFiltersToRegister() {
    List<ClientRequestFilter> filters = super.getRequestFiltersToRegister();
    filters.add(new LanguageAndCorrelationIdRestRequestFilter());
    return filters;
  }
}
