/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsConnectTimeoutProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheCorePoolSizeProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheEnabledProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsPortCacheTTLProperty;
import org.eclipse.scout.rt.server.jaxws.JaxWsConfigProperties.JaxWsReadTimeoutProperty;
import org.eclipse.scout.rt.server.jaxws.consumer.PortCache.IPortProvider;
import org.eclipse.scout.rt.server.jaxws.consumer.auth.handler.BasicAuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.consumer.auth.handler.WsseUsernameTokenAuthenticationHandler;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;

/**
 * This class represents and encapsulates a webservice endpoint port to communicate with, and is based on a preemptive
 * cache to obtain new port instances. Interaction with the port is done on behalf of a {@link InvocationContext} with
 * the following characteristics:
 * <ul>
 * <li>A context provides you a transactional scope to invoke webservice operations, meaning that once the associated
 * transaction is about to complete, you are invoked to participate as a transaction member in
 * <code>2-phase-commit-protocol</code>, which allows you to finally commit or rollback your webservice interaction done
 * within a <code>InvocationContext</code>. Of course, the webservice endpoint must provide some facility for that to
 * work.</li>
 * <li>Request properties are inherited from {@link AbstractJaxWsClient}, and can be overwritten for the scope of a
 * context. That is useful if having a port with some operations require some different properties set, e.g. another
 * read-timeout to transfer big data.</li>
 * <li>Internally, port operations are invoked within a separate thread with the current {@link RunContext} set, which
 * allows cancellation of operations, once the current {@link RunMonitor} is cancelled. However, even if the request
 * returns with a {@link CancellationException}, the invocation thread is still waiting for the response to be received,
 * or the associated {@link Socket} to be closed. Closing of that socket is implementor specific, and can be implemented
 * in {@link JaxWsImplementorSpecifics#closeSocket()}.</li>
 * </ul>
 * Example usage:
 *
 * <pre>
 * <code>
 * // Obtain a context to work on a dedicated Port.
 * final InvocationContext context = BEANS.get(YourWebServiceClient.class).newInvocationContext();
 * 
 * // Optionally configure the context.
 * context.endpointUrl(&quot;http://...&quot;)
 *        .connectTimeout(1000)
 *        .readTimeout(10000)
 *        .whenRollback(new IRollbackListener() {
 * 
 *          &#064;Override
 *          public void onRollback() {
 *            invocationContext.port().webMethod_rollback();
 *          }
 *        });
 * 
 * // Invoke the port operation.
 * try {
 *   String wsResult = context.port().webMethod();
 * }
 * catch (CancellationException e) {
 *   // Webservice request was cancelled.
 * }
 * </code>
 * </pre>
 *
 * @since 5.1
 */
@ApplicationScoped
public abstract class AbstractJaxWsClient<SERVICE extends Service, PORT> {

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

  protected final PortCache<PORT> m_portCache;

  public AbstractJaxWsClient() {
    m_serviceClazz = resolveServiceClass();
    m_portTypeClazz = resolvePortClass();
    m_webServiceClientAnnotation = Assertions.assertNotNull(m_serviceClazz.getAnnotation(javax.xml.ws.WebServiceClient.class), "Missing '%s' annotation on webservice [service=%s]", AbstractJaxWsClient.class.getSimpleName(), m_serviceClazz.getName());

    final boolean cacheEnabled = CONFIG.getPropertyValue(getConfiguredPortCacheEnabledProperty()).booleanValue();
    final int corePoolSize = CONFIG.getPropertyValue(getConfiguredPortCacheCorePoolSizeProperty()).intValue();
    final long timeToLive = CONFIG.getPropertyValue(getConfiguredPortCacheTTLProperty()).longValue();
    m_portCache = new PortCache<>(cacheEnabled, corePoolSize, timeToLive, getConfiguredPortProvider(m_serviceClazz, m_portTypeClazz));

    initConfig();
  }

  protected void initConfig() {
    m_endpointUrl = CONFIG.getPropertyValue(getConfiguredEndpointUrlProperty());
    m_username = AbstractJaxWsClient.getOptionalConfigPropertyValue(getConfiguredUsernameProperty());
    m_password = AbstractJaxWsClient.getOptionalConfigPropertyValue(getConfiguredPasswordProperty());
    m_connectTimeout = CONFIG.getPropertyValue(getConfiguredConnectTimeoutProperty());
    m_readTimeout = CONFIG.getPropertyValue(getConfiguredReadTimeoutProperty());

    m_wsdlLocation = resolveWsdlUrl(Assertions.assertNotNullOrEmpty(m_webServiceClientAnnotation.wsdlLocation(), "Missing 'wsdlLocation' on %s annotation. Use argument 'wsdlLocation' when generating webservice stub. [service=%s]", WebServiceClient.class.getSimpleName(), m_serviceClazz.getName()));
    m_targetNamespace = m_webServiceClientAnnotation.targetNamespace();
    m_serviceName = m_webServiceClientAnnotation.name();
  }

  /**
   * Creates a new <code>InvocationContext</code> to interact with a webservice endpoint on behalf of a cached Port.<br/>
   * Request properties are inherited from {@link AbstractJaxWsClient}, and can be overwritten for the scope of this
   * context.
   * That is useful if having a port with some operations require some different properties set, e.g. another
   * read-timeout to transfer big data. Also, if associated with a transaction, respective commit or rollback listeners
   * are called upon leaving the transaction boundary, e.g. to implement a 2-phase-commit-protocol (2PC) for the
   * webservice operations invoked.
   */
  public InvocationContext<PORT> newInvocationContext() throws ProcessingException {
    final PORT port = m_portCache.get();

    final InvocationContext<PORT> portHandle = new InvocationContext<>(port, getClass().getSimpleName());
    portHandle.endpointUrl(m_endpointUrl);
    portHandle.connectTimeout(m_connectTimeout);
    portHandle.readTimeout(m_readTimeout);
    portHandle.username(m_username);
    portHandle.password(m_password);

    return portHandle;
  }

  /**
   * Overwrite to install JAX-WS handlers by adding them to the given {@link List}. The handlers are invoked in the
   * order as placed in the handler-chain list. By default, no handlers are installed.
   * <p>
   * This method is invoked the time the service and port is created, and by any thread, meaning that it is not
   * guaranteed to be the calling thread, when invoking a webservice endpoint.
   * <p>
   * If the endpoint requires to authenticate requests, typically an authentication handler is added to the list, e.g.
   * {@link BasicAuthenticationHandler} for 'Basic authentication', or {@link WsseUsernameTokenAuthenticationHandler}
   * for 'Message Level WS-Security authentication', or some other handler to provide credentials.
   */
  @ConfigOperation
  protected void execInstallHandlers(final List<Handler> handlerChain) {
  }

  /**
   * Overwrite to install JAX-WS webservice features to enable implementor specific functionality. Features are
   * installed by adding them to the given {@link List}. By default, no features are installed.
   * <p>
   * This method is invoked the time the service and port is created, and by any thread, meaning that it is not
   * guaranteed to be the calling thread, when invoking a webservice endpoint.
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
    return this.<SERVICE> resolveGenericTypeArguments(getClass())[0];
  }

  /**
   * Overwrite to resolve to a specific port class. By default, the super hierarchy is looked for the port type in
   * the generic type declaration.
   */
  protected Class<PORT> resolvePortClass() {
    return this.<PORT> resolveGenericTypeArguments(getClass())[1];
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
    return Assertions.assertNotNull(getClass().getResource(wsdlLocation), "Failed to locate WSDL file [wsdlLocation=%s, service=%s]", wsdlLocation, m_serviceClazz.getName());
  }

  /**
   * Resolves the generic type arguments in the super hierarchy of the actual class.
   */
  protected <T> Class<T>[] resolveGenericTypeArguments(final Type type) {
    Type candidate = getClass().getGenericSuperclass();

    // Find the class which declares the generic parameters.
    while (!(candidate instanceof ParameterizedType)) {
      candidate = ((Class<?>) candidate).getGenericSuperclass();
    }

    Assertions.assertTrue(candidate instanceof ParameterizedType, "Unexpected: no parameterized type found in super hierarchy of %s", getClass().getName());

    @SuppressWarnings("unchecked")
    final Class<T>[] types = TypeCastUtility.castValue(((ParameterizedType) candidate).getActualTypeArguments(), Class[].class);
    return types;
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
   * Overwrite to configure the maximum time [ms] to retain ports in the cache if the 'corePoolSize' is exceeded. That
   * typically occurs at high load, or if 'corePoolSize' is undersized. By default, the same 'time-to-live' is used by
   * all webservice clients configured by {@link JaxWsPortCacheTTLProperty}.
   *
   * @see JaxWsPortCacheTTLProperty
   */
  @ConfigProperty(ConfigProperty.LONG)
  protected Class<? extends IConfigProperty<Long>> getConfiguredPortCacheTTLProperty() {
    return JaxWsPortCacheTTLProperty.class;
  }

  /**
   * Overwrite to configure the connect timeout [ms] for requests initiated by this webservice client. If the timeout
   * expires before the connection can be established, the request is aborted. A timeout of null means an infinite
   * timeout.
   *
   * @see JaxWsConnectTimeoutProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Integer>> getConfiguredConnectTimeoutProperty() {
    return JaxWsConnectTimeoutProperty.class;
  }

  /**
   * Overwrite to configure the read timeout [ms] for requests initiated by this webservice client. If the timeout
   * expires before data is available for read, the request is aborted. A timeout of null means an infinite timeout.
   *
   * @see JaxWsReadTimeoutProperty
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected Class<? extends IConfigProperty<Integer>> getConfiguredReadTimeoutProperty() {
    return JaxWsReadTimeoutProperty.class;
  }

  /**
   * Overwrite to configure the endpoint URL for requests initiated by this webservice client.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected abstract Class<? extends IConfigProperty<String>> getConfiguredEndpointUrlProperty();

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
  private static <DATA_TYPE> DATA_TYPE getOptionalConfigPropertyValue(final Class<? extends IConfigProperty<DATA_TYPE>> propertyClazz) {
    return (propertyClazz != null ? CONFIG.getPropertyValue(propertyClazz) : null);
  }

  /**
   * Overwrite to provide another {@link IPortProvider} to create new port objects.
   */
  protected IPortProvider<PORT> getConfiguredPortProvider(final Class<SERVICE> serviceClazz, final Class<PORT> portClazz) {
    return new IPortProvider<PORT>() {

      @Override
      public PORT provide() {
        Assertions.assertNotNull(getWsdlLocation(), "No 'wsdlLocation' configured on webservice stub '%s'.", getClass().getSimpleName());

        try {
          // Create the service.
          final Constructor<? extends Service> constructor = serviceClazz.getConstructor(URL.class, QName.class);
          @SuppressWarnings("unchecked")
          final SERVICE service = (SERVICE) constructor.newInstance(getWsdlLocation(), new QName(getTargetNamespace(), getServiceName()));

          // Install the handler chain.
          service.setHandlerResolver(new HandlerResolver() {

            @Override
            public List<Handler> getHandlerChain(final PortInfo portInfo) {
              final List<Handler> handlers = new ArrayList<>();
              execInstallHandlers(handlers);
              return handlers;
            }
          });

          // Install proprietary webservice features.
          final List<WebServiceFeature> webServiceFeatures = new ArrayList<>();
          execInstallWebServiceFeatures(webServiceFeatures);

          // Create the port.
          return service.getPort(m_portTypeClazz, CollectionUtility.toArray(webServiceFeatures, WebServiceFeature.class));
        }
        catch (final ReflectiveOperationException e) {
          throw new WebServiceException("Failed to instantiate webservice stub.", e);
        }
      }
    };
  }
}
