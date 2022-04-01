/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.jersey;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;

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
