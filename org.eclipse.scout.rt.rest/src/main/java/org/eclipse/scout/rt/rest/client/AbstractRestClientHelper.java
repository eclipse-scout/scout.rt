package org.eclipse.scout.rt.rest.client;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.ContextResolver;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.rest.error.ErrorDo;
import org.eclipse.scout.rt.rest.error.ErrorResponse;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a REST client helper dealing with REST requests to a API server.
 * <p>
 * This class may be reused for subsequent REST requests to the same API server.
 * <p>
 * Subclasses may bind this generic REST client helper to a concrete REST endpoint by implementing the
 * {@link #getBaseUri()} method.
 */
public abstract class AbstractRestClientHelper implements IRestClientHelper {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRestClientHelper.class);

  private final Supplier<Client> m_clientSupplier = createClientSupplier();

  /**
   * @return a supplier of {@link Client} instances used for {@link #target(String)}. The default implementation returns
   *         a supplier that always provides the same {@link Client} instance.
   */
  protected Supplier<Client> createClientSupplier() {
    Client client = createClient();
    return () -> client;
  }

  protected Client createClient() {
    // Prepare client config
    // IMPORTANT: This must happen _before_ calling initClientBuilder() because "withConfig()" replaces the entire configuration!
    ClientConfig clientConfig = new ClientConfig();
    // TODO 8.0 pbz: Temporary workaround, this code line and the direct dependency to the Apache connector will be removed as soon as the jersey issue is resolved.
    // See Jersey Issue 3771: https://github.com/jersey/jersey/pull/3771 (see also TO DO in pom.xml)
    clientConfig.connectorProvider(new ApacheConnectorProvider());
    initClientConfig(clientConfig);

    ClientBuilder clientBuilder = ClientBuilder.newBuilder()
        .withConfig(clientConfig);
    initClientBuilder(clientBuilder);
    return clientBuilder.build();
  }

  /**
   * @return the {@link Client} used by {@link #target(String)}
   */
  protected Client client() {
    return m_clientSupplier.get();
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
   * Override this method to setup the JAX-RS client configuration with any custom properties. <br>
   * The default implementation doesn't setup any properties.
   */
  protected void initClientConfig(Configurable<?> clientConfig) {
    // NOP
  }

  @Override
  public WebTarget target(String resourcePath) {
    return client().target(buildUri(resourcePath));
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

  @Override
  public void throwOnResponseError(WebTarget target, Response response) {
    // TODO [8.0] pbz,abr: Remove special handling and exception wrapping, throw WebApplicationException with nested response, leave reading ErrorDo from response to caller
    if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
      handleForbiddenResponse(target, response);
    }
    if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
      handleErrorResponse(target, response);
    }
  }

  protected void handleForbiddenResponse(WebTarget target, Response response) {
    try {
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      throw new VetoException(error.getMessage()).withTitle(error.getTitle()); // add other errordo attributes
    }
    catch (@SuppressWarnings("squid:S1166") javax.ws.rs.ProcessingException | IllegalStateException e) {
      StatusType statusInfo = response.getStatusInfo();
      LOG.debug("REST call to '{}' returned forbidden {} {} without error response object.", target.getUri(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());

      VetoException vetoException = new VetoException(response.getStatusInfo().getReasonPhrase());
      vetoException.addSuppressed(e);
      throw vetoException;
    }
  }

  protected void handleErrorResponse(WebTarget target, Response response) {
    try {
      ErrorDo error = response.readEntity(ErrorResponse.class).getError();
      throw new PlatformException(error.getMessage());
    }
    catch (@SuppressWarnings("squid:S1166") javax.ws.rs.ProcessingException | IllegalStateException e) {
      StatusType statusInfo = response.getStatusInfo();
      LOG.debug("REST call to '{}' returned error {} {} without error response object.", target.getUri(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());

      ProcessingException processingException = new ProcessingException("REST call to '{}' failed: {} {}", target.getUri(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
      processingException.addSuppressed(e);
      throw processingException;
    }
  }

  @Override
  public Entity<String> emptyJson() {
    return Entity.json("");
  }
}
