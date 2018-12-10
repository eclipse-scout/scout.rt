/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.rest.client.proxy.IRestClientExceptionTransformer;
import org.eclipse.scout.rt.rest.client.proxy.RestClientProxyFactory;

/**
 * Abstract implementation of a REST client helper dealing with REST requests to a API server.
 * <p>
 * {@link Client} and its derived objects are proxied to provide cancellation and exception transformation support (see
 * {@link #transformException(RuntimeException, Response)}).
 * <p>
 * This class may be reused for subsequent REST requests to the same API server.
 * <p>
 * Subclasses may bind this generic REST client helper to a concrete REST endpoint by implementing the
 * {@link #getBaseUri()} method.
 */
public abstract class AbstractRestClientHelper implements IRestClientHelper {

  private final Supplier<Client> m_clientSupplier = createClientSupplier();

  /**
   * @return a supplier of {@link Client} instances used for {@link #target(String)}. The default implementation returns
   *         a supplier that always provides the same {@link Client} instance.
   */
  protected Supplier<Client> createClientSupplier() {
    Client client = createClient();
    return () -> client;
  }

  /**
   * @return the unproxied {@link Client}.
   */
  protected Client internalClient() {
    return m_clientSupplier.get();
  }

  /**
   * Creates a new JAX-RS {@link Client} instance. Global customization should be done by an
   * {@link IRestClientConfigFactory}, system-specific settings should be applied in
   * {@link #configureClientBuilder(ClientBuilder)} and REST service-specific ones by configuring one of the REST client
   * objects, i.e. {@link WebTarget} and {@link Invocation.Builder}.
   */
  protected Client createClient() {
    Configuration clientConfig = createClientConfig();
    ClientBuilder clientBuilder = ClientBuilder.newBuilder()
        .withConfig(clientConfig);

    // IMPORTANT: initClientBuilder must happen _after_ calling clientBuilder.withConfig() because "withConfig()" replaces the entire configuration!
    initClientBuilder(clientBuilder);
    return clientBuilder.build();
  }

  /**
   * Creates and sets-up a JAX-RS client configuration with any custom properties.
   * <p>
   * This default implementation delegates to {@link IRestClientConfigFactory#createClientConfig()}
   */
  protected Configuration createClientConfig() {
    return BEANS.get(IRestClientConfigFactory.class).createClientConfig();
  }

  protected void initClientBuilder(ClientBuilder clientBuilder) {
    registerContextResolvers(clientBuilder);
    registerRequestFilters(clientBuilder);

    configureClientBuilder(clientBuilder);
  }

  protected void registerContextResolvers(ClientBuilder clientBuilder) {
    // Context resolver, e.g. resolver for ObjectMapper
    for (IBean<ContextResolver> bean : BEANS.getBeanManager().getBeans(ContextResolver.class)) {
      clientBuilder.register(bean.getBeanClazz());
    }
  }

  protected void registerRequestFilters(ClientBuilder clientBuilder) {
    for (IGlobalRestRequestFilter filter : BEANS.all(IGlobalRestRequestFilter.class)) {
      clientBuilder.register(filter);
    }
  }

  protected void configureClientBuilder(ClientBuilder clientBuilder) {
    for (IGlobalRestClientConfigurator configurator : BEANS.all(IGlobalRestClientConfigurator.class)) {
      configurator.configure(clientBuilder);
    }
  }

  /**
   * @return proxied {@link Client} instance that delegates {@link WebApplicationException}s and
   *         {@link javax.ws.rs.ProcessingException}s to {@link #transformException(RuntimeException, Response)}.
   */
  public Client client() {
    return client(this::transformException);
  }

  /**
   * @return proxied {@link Client} instance that delegates {@link WebApplicationException}s and
   *         {@link javax.ws.rs.ProcessingException}s to the given {@link IRestClientExceptionTransformer}. The
   *         {@code null}-transformer returns the passed exception unchanged.
   */
  public Client client(IRestClientExceptionTransformer exceptionTransformer) {
    return getProxyFactory().createClientProxy(m_clientSupplier.get(), exceptionTransformer);
  }

  @Override
  public WebTarget target(String resourcePath) {
    return target(resourcePath, this::transformException);
  }

  @Override
  public WebTarget target(String resourcePath, IRestClientExceptionTransformer exceptionTransformer) {
    WebTarget target = internalClient().target(buildUri(resourcePath));
    return getProxyFactory().createWebTargetProxy(target, exceptionTransformer);
  }

  protected RestClientProxyFactory getProxyFactory() {
    return BEANS.get(RestClientProxyFactory.class);
  }

  @Override
  public WebTarget applyQueryParams(WebTarget target, Map<String, Object> queryParams) {
    for (Entry<String, Object> param : queryParams.entrySet()) {
      target = target.queryParam(param.getKey(), param.getValue());
    }
    return target;
  }

  protected URI buildUri(String resourcePath) {
    return new UriBuilder(getBaseUri())
        .addPath(resourcePath)
        .createURI();
  }

  /**
   * @return base URI to use for REST requests using this helper
   */
  protected abstract String getBaseUri();

  /**
   * Call-back method for transforming {@link WebApplicationException} and {@link javax.ws.rs.ProcessingException}
   * thrown during a REST service invocation. Subclasses may extract service-specific error objects.
   * <p>
   * This default implementation just returns the passed exception.
   */
  protected RuntimeException transformException(RuntimeException e, Response response) {
    return e;
  }

  @Override
  public Entity<String> emptyJson() {
    return Entity.json("");
  }
}
