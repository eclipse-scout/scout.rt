/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunWithRunContext;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsConnectTimeoutProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheCorePoolSizeProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheEnabledProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheTTLProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortPoolEnabledProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsReadTimeoutProperty;
import org.eclipse.scout.rt.server.jaxws.consumer.IPortProvider.IPortInitializer;
import org.eclipse.scout.rt.server.jaxws.consumer.auth.handler.BasicAuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.consumer.auth.handler.WsseUsernameTokenAuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.consumer.pool.PooledPortProvider;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents and encapsulates a webservice endpoint port to communicate with, and is based on a preemptive
 * cache to obtain new port instances.
 * <p>
 * Interaction with the endpoint is done on behalf of a {@link InvocationContext} with the following characteristics:
 * <ul>
 * <li>Request properties are inherited from {@link AbstractWebServiceClient}, and can be overwritten for the scope of
 * an invocation context. This is useful if some operations of the port require different properties to be set, e.g.
 * another read-timeout to transfer big data.</li>
 * <li>Operations on the Port are invoked in another thread, which allows for cancellation once the current monitor is
 * cancelled. Then, the operation returns with a {@link WebServiceRequestCancelledException}. But, the thread executing
 * the operation may still be waiting for the response to be received, because closing the socket is implementor
 * specific.</li>
 * </ul>
 * <p>
 * The JAX-WS specification does not specify thread safety of a Port instance. Therefore, a Port should not be used
 * concurrently among threads. Further, JAX-WS API does not support to reset the Port's request and response context,
 * which is why a Port should only be used for a single webservice call.
 * <p>
 * Example usage:
 *
 * <pre>
 * // Obtain a context to work on a dedicated Port.
 * final InvocationContext&lt;YourWebServicePortType&gt; context = BEANS.get(YourWebServicePortType.class).newInvocationContext();
 *
 * // Optionally configure the context.
 * YouWebServicePortType port = context
 *     .withEndpointUrl("http://...")
 *     .withConnectTimeout(10, TimeUnit.SECONDS)
 *     .withReadTimeout(30, TimeUnit.SECONDS)
 *     .whenRollback(new IRollbackListener() {
 *
 *       &#64;Override
 *       public void onRollback() {
 *         context.getPort().webMethod_rollback();
 *       }
 *     })
 *     .getPort();
 *
 * // Invoke the port operation.
 * try {
 *   String wsResult = port.webMethod();
 * }
 * catch (WebServiceRequestCancelledException e) {
 *   // Webservice request was cancelled.
 * }
 * </pre>
 *
 * @since 5.1
 */
@ApplicationScoped
@CreateImmediately
public abstract class AbstractWebServiceClient<SERVICE extends Service, PORT> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractWebServiceClient.class);

  protected final WebServiceClient m_webServiceClientAnnotation;
  protected final Class<SERVICE> m_serviceClazz;
  protected final Class<PORT> m_portTypeClazz;

  protected URL m_wsdlLocation;
  protected String m_targetNamespace;
  protected String m_serviceName;

  protected String m_endpointUrl;
  protected Integer m_readTimeout;
  protected Integer m_connectTimeout;
  protected String m_username;
  protected String m_password;

  protected IPortProvider<PORT> m_portProvider;

  public AbstractWebServiceClient() {
    m_serviceClazz = resolveServiceClass();
    m_portTypeClazz = resolvePortClass();
    m_webServiceClientAnnotation =
        Assertions.assertNotNull(m_serviceClazz.getAnnotation(WebServiceClient.class), "Missing '{}' annotation on webservice [service={}]", AbstractWebServiceClient.class.getSimpleName(), m_serviceClazz.getName());
  }

  @PostConstruct
  protected void initConfig() {
    m_endpointUrl = optConfigValue(getConfiguredEndpointUrlProperty());
    m_username = optConfigValue(getConfiguredUsernameProperty());
    m_password = optConfigValue(getConfiguredPasswordProperty());
    m_connectTimeout = CONFIG.getPropertyValue(getConfiguredConnectTimeoutProperty());
    m_readTimeout = CONFIG.getPropertyValue(getConfiguredReadTimeoutProperty());

    m_wsdlLocation = resolveWsdlUrl(Assertions.assertNotNullOrEmpty(m_webServiceClientAnnotation.wsdlLocation(), "Missing 'wsdlLocation' on {} annotation. Use argument 'wsdlLocation' when generating webservice stub. [service={}]",
        WebServiceClient.class.getSimpleName(), m_serviceClazz.getName()));
    m_targetNamespace = m_webServiceClientAnnotation.targetNamespace();
    m_serviceName = m_webServiceClientAnnotation.name();

    m_portProvider = getConfiguredPortProvider(m_serviceClazz, m_portTypeClazz, m_wsdlLocation, m_targetNamespace, m_serviceName, new IPortInitializer() {

      @Override
      public void initWebServiceFeatures(final List<WebServiceFeature> webServiceFeatures) {
        execInstallWebServiceFeatures(webServiceFeatures);
      }

      @Override
      public void initHandlers(final List<Handler<? extends MessageContext>> handlerChain) {
        execInstallHandlers(handlerChain);
      }
    });
  }

  /**
   * Creates a new <code>InvocationContext</code> to interact with a webservice endpoint.
   * <p>
   * Request properties are inherited from {@link AbstractWebServiceClient}, and can be overwritten for the scope of
   * this context.
   */
  public InvocationContext<PORT> newInvocationContext() {
    final PORT port = m_portProvider.provide();
    final InvocationContext<PORT> invocationContext = new InvocationContext<>(port, getClass().getSimpleName());

    if (m_endpointUrl != null) {
      invocationContext.withEndpointUrl(m_endpointUrl);
    }
    if (m_username != null) {
      invocationContext.withUsername(m_username);
    }
    if (m_password != null) {
      invocationContext.withPassword(m_password);
    }
    if (m_connectTimeout != null) {
      invocationContext.withConnectTimeout(m_connectTimeout, TimeUnit.MILLISECONDS);
    }
    if (m_readTimeout != null) {
      invocationContext.withReadTimeout(m_readTimeout, TimeUnit.MILLISECONDS);
    }

    return invocationContext;
  }

  /**
   * Overwrite to install JAX-WS handlers by adding them to the given {@link List}. The handlers are invoked in the
   * order as added to the handler-chain. By default, there is no handler installed.
   * <p>
   * This method is invoked upon preemptive creation of the port. Consequently, you cannot do any assumption about the
   * calling thread.
   * <p>
   * If a handler requires to run in another {@link RunContext} than the calling context, annotate it with
   * {@link RunWithRunContext} annotation, e.g. to start a new transaction to log into database.
   * <p>
   * If the endpoint requires to authenticate requests, an authentication handler is typically added to the list, e.g.
   * {@link BasicAuthenticationHandler} for 'Basic authentication', or {@link WsseUsernameTokenAuthenticationHandler}
   * for 'Message Level WS-Security authentication', or some other handler to provide credentials.
   */
  @ConfigOperation
  protected void execInstallHandlers(final List<Handler<? extends MessageContext>> handlerChain) {
  }

  /**
   * Overwrite to install JAX-WS webservice features to enable implementor specific functionality. Features are
   * installed by adding them to the given {@link List}. By default, no features are installed.
   * <p>
   * This method is invoked the time the service and port is preemptively created and put into cache. Consequently, you
   * cannot do any assumption about the calling thread.
   */
  @ConfigOperation
  protected void execInstallWebServiceFeatures(final List<WebServiceFeature> webServiceFeatures) {
  }

  public String getEndpointUrl() {
    return m_endpointUrl;
  }

  public URL getWsdlLocation() {
    return m_wsdlLocation;
  }

  public String getTargetNamespace() {
    return m_targetNamespace;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public Integer getReadTimeout() {
    return m_readTimeout;
  }

  public Integer getConnectTimeout() {
    return m_connectTimeout;
  }

  public String getUsername() {
    return m_username;
  }

  public String getPassword() {
    return m_password;
  }

  /**
   * Overwrite to resolve to a specific service class. By default, the super hierarchy is looked for the service type in
   * the generic type declaration.
   */
  protected Class<SERVICE> resolveServiceClass() {
    return this.<SERVICE> resolveGenericTypeArguments()[0];
  }

  /**
   * Overwrite to resolve to a specific port class. By default, the super hierarchy is looked for the port type in the
   * generic type declaration.
   */
  protected Class<PORT> resolvePortClass() {
    return this.<PORT> resolveGenericTypeArguments()[1];
  }

  /**
   * Overwrite to resolve the {@link URL} to the WSDL file.
   *
   * @param location
   *          location as specified by {@link WebServiceClient#wsdlLocation()}; is not <code>null</code>.
   * @return {@link URL}; must not be <code>null</code>.
   */
  protected URL resolveWsdlUrl(final String location) {
    // ensure root relative path, so that the WSDL file is not looked for in this class's location.
    final String wsdlLocation = location.startsWith("/") ? location : "/" + location;
    return Assertions.assertNotNull(getClass().getResource(wsdlLocation), "Failed to locate WSDL file [wsdlLocation={}, service={}]", wsdlLocation, m_serviceClazz.getName());
  }

  /**
   * Resolves the generic type arguments in the super hierarchy of the actual class.
   */
  protected <T> Class<T>[] resolveGenericTypeArguments() {
    Type candidate = getClass().getGenericSuperclass();

    // Find the class which declares the generic parameters.
    while (!(candidate instanceof ParameterizedType)) {
      candidate = ((Class<?>) candidate).getGenericSuperclass();
    }

    Assertions.assertTrue(candidate instanceof ParameterizedType, "Unexpected: no parameterized type found in super hierarchy of {}", getClass().getName());

    @SuppressWarnings("unchecked")
    final Class<T>[] types = TypeCastUtility.castValue(((ParameterizedType) candidate).getActualTypeArguments(), Class[].class);
    return types;
  }

  /**
   * Overwrite to enable/disable port pooling for this webservice client. By default, that mechanism is enabled/disabled
   * globally by {@link JaxWsPortPoolEnabledProperty}.
   * <p>
   * Depending on the implementor used, pooled ports may increase performance, because port creation is an expensive
   * operation due to WSDL and schema validation.
   *
   * @see JaxWsPortPoolEnabledProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Boolean>> getConfiguredPortPoolEnabledProperty() {
    return JaxWsPortPoolEnabledProperty.class;
  }

  /**
   * Overwrite to enable/disable port caching for this webservice client. By default, that mechanism is enabled/disabled
   * globally by {@link JaxWsPortCacheEnabledProperty}.
   * <p>
   * Depending on the implementor used, cached ports may increase performance, because port creation is an expensive
   * operation due to WSDL and schema validation. The cache is based on a 'corePoolSize', meaning that that number of
   * ports is created on a preemptive basis. If more ports than that number is required, they are are created on demand
   * and also added to the cache until expired, which is useful at a high load.
   *
   * @see JaxWsPortCacheEnabledProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Boolean>> getConfiguredPortCacheEnabledProperty() {
    return JaxWsPortCacheEnabledProperty.class;
  }

  /**
   * Overwrite to configure the number of ports to be preemptively cached to speed up webservice calls initiated by this
   * webservice client. By default, the same pool size applies to all webservice clients configured by
   * {@link JaxWsPortCacheCorePoolSizeProperty}.
   *
   * @see JaxWsPortCacheCorePoolSizeProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Integer>> getConfiguredPortCacheCorePoolSizeProperty() {
    return JaxWsPortCacheCorePoolSizeProperty.class;
  }

  /**
   * Overwrite to configure the maximum time in seconds to retain ports in the cache if the 'corePoolSize' is exceeded.
   * That typically occurs at high load, or if 'corePoolSize' is undersized. By default, the same 'time-to-live' is used
   * by all webservice clients configured by {@link JaxWsPortCacheTTLProperty}.
   *
   * @see JaxWsPortCacheTTLProperty
   */
  @ConfigProperty(ConfigProperty.LONG)
  protected Class<? extends IConfigProperty<Long>> getConfiguredPortCacheTTLProperty() {
    return JaxWsPortCacheTTLProperty.class;
  }

  /**
   * Overwrite to configure the connect timeout in milliseconds for requests initiated by this webservice client. If the
   * timeout expires before the connection can be established, the request is aborted. A timeout of null means an
   * infinite timeout.
   *
   * @see JaxWsConnectTimeoutProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Integer>> getConfiguredConnectTimeoutProperty() {
    return JaxWsConnectTimeoutProperty.class;
  }

  /**
   * Overwrite to configure the read timeout in milliseconds for requests initiated by this webservice client. If the
   * timeout expires before data is available for read, the request is aborted. A timeout of null means an infinite
   * timeout.
   *
   * @see JaxWsReadTimeoutProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Integer>> getConfiguredReadTimeoutProperty() {
    return JaxWsReadTimeoutProperty.class;
  }

  /**
   * Overwrite to configure the endpoint URL for requests initiated by this webservice client. If not set, the URL must
   * be set via {@link InvocationContext}.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<String>> getConfiguredEndpointUrlProperty() {
    return null;
  }

  /**
   * Overwrite to configure the username to be sent to the endpoint for authentication for requests initiated by this
   * webservice client.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<String>> getConfiguredUsernameProperty() {
    return null;
  }

  /**
   * Overwrite to configure the password to be sent to the endpoint for authentication for requests initiated by this
   * webservice client.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<String>> getConfiguredPasswordProperty() {
    return null;
  }

  /**
   * Returns the property value from the configuration unless the given property class is <code>null</code>.
   */
  private static <DATA_TYPE> DATA_TYPE optConfigValue(final Class<? extends IConfigProperty<DATA_TYPE>> propertyClazz) {
    return (propertyClazz != null ? CONFIG.getPropertyValue(propertyClazz) : null);
  }

  /**
   * Overwrite to work with another {@link IPortProvider} to create new port objects.
   */
  protected IPortProvider<PORT> getConfiguredPortProvider(final Class<SERVICE> serviceClazz, final Class<PORT> portTypeClazz, final URL wsdlLocation,
      final String targetNamespace, final String serviceName, final IPortInitializer portInitializer) {

    if (BooleanUtility.nvl(CONFIG.getPropertyValue(getConfiguredPortPoolEnabledProperty()))) {
      if (BEANS.get(JaxWsImplementorSpecifics.class).isPoolingSupported()) {
        return new PooledPortProvider<>(serviceClazz, portTypeClazz, serviceName, wsdlLocation, targetNamespace, portInitializer);
      }
      LOG.warn("The current runtime environment does not support pooling of web services. Check your configuration (i.e. either disable '{}' or use a JAX-WS implementor that supports pooling like 'JAX-WS Metro')",
          BEANS.get(getConfiguredPortPoolEnabledProperty()).getKey());
    }

    PortProducer<SERVICE, PORT> portProducer = new PortProducer<>(serviceClazz, portTypeClazz, serviceName, wsdlLocation, targetNamespace, portInitializer);
    if (CONFIG.getPropertyValue(getConfiguredPortCacheEnabledProperty())) {
      PortCache<PORT> portCache = new PortCache<>(CONFIG.getPropertyValue(getConfiguredPortCacheCorePoolSizeProperty()), TimeUnit.SECONDS.toMillis(CONFIG.getPropertyValue(getConfiguredPortCacheTTLProperty())), portProducer);
      portCache.init();
      return portCache;
    }
    return portProducer;
  }
}
