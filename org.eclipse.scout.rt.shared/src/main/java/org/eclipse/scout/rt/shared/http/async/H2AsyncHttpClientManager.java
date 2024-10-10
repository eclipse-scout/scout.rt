/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.async;

import java.util.function.BiConsumer;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.H2AsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.eclipse.scout.rt.platform.ApplicationScoped;

import io.opentelemetry.api.OpenTelemetry;

/**
 * <p>
 * Use {@link HttpAsyncClients#customHttp2()} to initialize this client.
 * </p>
 * <p>
 * Note: This {@link AbstractAsyncHttpClientManager} implementation does not provide any {@link OpenTelemetry} metrics
 * since is uses only one HTTP connection per route.
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

    builder.addExecInterceptorFirst(AsyncHttpInvocationHandler.class.getSimpleName(), (request, entityProducer, scope, chain, asyncExecCallback) -> chain.proceed(request,
        createAsyncInvocationHandler(AsyncEntityProducer.class, entityProducer),
        scope,
        createAsyncInvocationHandler(AsyncExecCallback.class, asyncExecCallback)));
  }

  @Override
  protected BiConsumer<H2AsyncClientBuilder, CookieStore> getInstallCookieStoreBiConsumer() {
    return (builder, cookieStore) -> builder.setDefaultCookieStore(cookieStore);
  }

  @Override
  protected CloseableHttpAsyncClient createClient(H2AsyncClientBuilder builder) {
    return builder.build();
  }
}
