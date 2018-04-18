package org.eclipse.scout.rt.rest.client;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Abstract implementation of a REST client helper dealing with REST requests to a API server.
 * <p>
 * This class is stateless and may be reused for subsequent REST requests to the same API server.
 * <p>
 * Subclasses may bind this generic REST client helper to a concrete REST endpoint by implementing the
 * {@link #getBaseUri()} method.
 */
public abstract class AbstractRestClientHelper implements IRestClientHelper {

  @Override
  public Client client() {
    ClientBuilder clientBuilder = ClientBuilder.newBuilder();
    initClientBuilder(clientBuilder);

    installContextResolver(clientBuilder);
    installRequestFilters(clientBuilder);
    installFeatures(clientBuilder);

    Client client = clientBuilder.build();
    return client;
  }

  protected void initClientBuilder(ClientBuilder clientBuilder) {
    ClientConfig clientConfig = new ClientConfig();
    // TODO 8.0 pbz: Temporary workaround, see Jersey Issue 3771: https://github.com/jersey/jersey/pull/3771 (remove this code line when issue is resolved, see also TODO in pom.xml)
    clientConfig.connectorProvider(new ApacheConnectorProvider());

    initConfigurable(clientConfig);
    clientBuilder.withConfig(clientConfig);
  }

  /**
   * Override this method to setup the {@link Configurable} with custom properties. <br>
   * This default method does not setup any properties.
   */
  protected void initConfigurable(Configurable<?> configurable) {
    // NOP
  }

  @Override
  public WebTarget target(String resourcePath) {
    return client().target(buildUri(resourcePath));
  }

  @Override
  public WebTarget target(String formatString, String... args) {
    String resourcePath = String.format(formatString, (Object[]) args);
    return target(resourcePath);
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

  protected void installContextResolver(ClientBuilder clientBuilder) {
    // Context resolver, e.g. resolver for ObjectMapper
    for (IBean<ContextResolver> bean : BEANS.getBeanManager().getBeans(ContextResolver.class)) {
      clientBuilder.register(bean.getBeanClazz());
    }
  }

  protected void installRequestFilters(ClientBuilder clientBuilder) {
    for (IGlobalRestRequestFilter filter : BEANS.all(IGlobalRestRequestFilter.class)) {
      clientBuilder.register(filter);
    }
  }

  protected void installFeatures(ClientBuilder clientBuilder) {
    // NOP
  }

  @Override
  public void throwOnResponseError(WebTarget target, Response response) {
    if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
      handleForbiddenResponse(target, response);
    }
    if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
      handleErrorResponse(target, response);
    }
  }

  protected void handleForbiddenResponse(WebTarget target, Response response) {
    throw new VetoException(response.getStatusInfo().getReasonPhrase());
  }

  protected void handleErrorResponse(WebTarget target, Response response) {
    StatusType statusInfo = response.getStatusInfo();
    throw new ProcessingException("REST call to '{}' failed: {} {}", target.getUri(), statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
  }

  @Override
  public Entity<String> emptyJson() {
    return Entity.json("");
  }
}
