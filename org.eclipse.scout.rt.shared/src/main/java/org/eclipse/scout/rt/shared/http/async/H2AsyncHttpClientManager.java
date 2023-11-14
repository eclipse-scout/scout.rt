/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.async;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.H2AsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * <p>
 * Use {@link HttpAsyncClients#customHttp2()} to initialize this client.
 * </p>
 * <p>
 * <b>Use with care:</b> According to {@link H2AsyncClientBuilder} which is effectively used, all requests are
 * multiplexed over a <b>single physical connection</b>. This may result in performance loss.
 * </p>
 *
 * @see AbstractAsyncHttpClientManager
 */
@ApplicationScoped
public class H2AsyncHttpClientManager extends AbstractAsyncHttpClientManager<H2AsyncClientBuilder> {

  @Override
  protected H2AsyncClientBuilder createBuilder() {
    return HttpAsyncClients.customHttp2();
  }

  @Override
  protected void interceptCreateClient(H2AsyncClientBuilder builder) {
    builder.useSystemProperties();
  }

  @Override
  protected CloseableHttpAsyncClient createClient(H2AsyncClientBuilder builder) {
    return builder.build();
  }
}
