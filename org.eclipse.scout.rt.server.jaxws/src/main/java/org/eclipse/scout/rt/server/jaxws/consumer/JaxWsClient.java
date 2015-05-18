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
import java.util.concurrent.TimeUnit;

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
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.server.jaxws.consumer.PortCache.IPortProvider;
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
 * <li>Request properties are inherited from {@link JaxWsClient}, and can be overwritten for the scope of a context.
 * That is useful if having a port with some operations require some different properties set, e.g. another read-timeout
 * to transfer big data.</li>
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
public class JaxWsClient<SERVICE extends Service, PORT> {

  protected final WebServiceClient m_webServiceClientAnnotation;
  protected final Class<SERVICE> m_serviceClazz;
  protected final Class<PORT> m_portTypeClazz;

  private String m_endpointUrl;
  private URL m_wsdlLocation;
  private String m_targetNamespace;
  private String m_serviceName;
  private Integer m_readTimeout;
  private Integer m_connectTimeout;
  private List<WebServiceFeature> m_webServiceFeatures;

  private String m_username;
  private String m_password;

  protected final PortCache<PORT> m_portCache;

  public JaxWsClient() {
    m_serviceClazz = getConfiguredServiceClass();
    m_portTypeClazz = getConfiguredPortClass();
    m_webServiceClientAnnotation = Assertions.assertNotNull(m_serviceClazz.getAnnotation(javax.xml.ws.WebServiceClient.class), "Missing '%s' annotation on webservice [service=%s]", JaxWsClient.class.getSimpleName(), m_serviceClazz.getName());

    initConfig();

    m_portCache = new PortCache<>(getConfiguredPortCacheEnabled(), getConfiguredPortCacheCorePoolSize(), getConfiguredPortCacheExpirationTime(), getConfiguredPortProvider(m_serviceClazz, m_portTypeClazz));
  }

  protected void initConfig() {
    setWsdlLocation(getConfiguredWsdlLocation());
    setEndpointUrl(getConfiguredEndpointUrl());
    setTargetNamespace(getConfiguredTargetNamespace());
    setServiceName(getConfiguredServiceName());
    setUsername(getConfiguredUsername());
    setPassword(getConfiguredPassword());
    setConnectTimeout(getConfiguredConnectTimeout());
    setReadTimeout(getConfiguredReadTimeout());
    setWebServiceFeatures(getConfiguredWebServiceFeatures());
  }

  /**
   * Creates a new <code>InvocationContext</code> to interact with a webservice endpoint on behalf of a cached Port.<br/>
   * Request properties are inherited from {@link JaxWsClient}, and can be overwritten for the scope of this context.
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
   * Overwrite to configure a specific service class. By default, the super hierarchy is looked for the service type in
   * the generic type declaration.
   */
  protected Class<SERVICE> getConfiguredServiceClass() {
    return this.<SERVICE> findGenericTypeArguments(getClass())[0];
  }

  /**
   * Overwrite to configure a specific port class. By default, the super hierarchy is looked for the port type in
   * the generic type declaration.
   */
  protected Class<PORT> getConfiguredPortClass() {
    return this.<PORT> findGenericTypeArguments(getClass())[1];
  }

  /**
   * Overwrite if the WSDL file is located somewhere else than specified by {@link JaxWsClient#getWsdlLocation()}.
   * This location can be specified when generating the stub with the option <code>wsdlLocation</code>.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected URL getConfiguredWsdlLocation() {
    String wsdlLocation = Assertions.assertNotNullOrEmpty(m_webServiceClientAnnotation.wsdlLocation(), "Missing 'wsdlLocation' on %s annotation. Use argument 'wsdlLocation' when generating stub. [service=%s]", JaxWsClient.class.getSimpleName(), m_serviceClazz.getName());

    // ensure root relative path
    if (!wsdlLocation.startsWith("/")) {
      wsdlLocation = '/' + wsdlLocation;
    }

    return Assertions.assertNotNull(getClass().getResource(wsdlLocation), "Could not find WSDL file [wsdlLocation=%s, service=%s]", wsdlLocation, m_serviceClazz.getName());
  }

  /**
   * Overwrite to configure a static endpoint URL.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredEndpointUrl() {
    return null;
  }

  /**
   * Overwrite to configure a specific service name. By default, the annotation value as specified by
   * {@link WebServiceClient#name()} is used.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredServiceName() {
    return m_webServiceClientAnnotation.name();
  }

  /**
   * Overwrite to configure a specific service name. By default, the annotation value as specified by
   * {@link WebServiceClient#targetNamespace()} is used.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredTargetNamespace() {
    return m_webServiceClientAnnotation.targetNamespace();
  }

  /**
   * Overwrite to configure a static username used for authentication.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredUsername() {
    return null;
  }

  /**
   * Overwrite to configure a static password used for authentication.
   */
  @ConfigProperty(ConfigProperty.STRING)
  protected String getConfiguredPassword() {
    return null;
  }

  /**
   * Overwrite to configure the connect timeout in milliseconds. If the timeout expires before the connection can be
   * established, the request is aborted. Use <code>null</code> to specify an infinite timeout.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected Integer getConfiguredConnectTimeout() {
    return null;
  }

  /**
   * Overwrite to configure the read timeout in milliseconds. If the timeout expires before there is data available for
   * read, the request is aborted. Use <code>null</code> to specify an infinite timeout.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected Integer getConfiguredReadTimeout() {
    return null;
  }

  /**
   * Overwrite to enable proprietary webservice features.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected List<WebServiceFeature> getConfiguredWebServiceFeatures() {
    return null;
  }

  /**
   * Overwrite to provide another {@link IPortProvider}.
   */
  protected IPortProvider<PORT> getConfiguredPortProvider(final Class<SERVICE> serviceClazz, final Class<PORT> portClazz) {
    return new IPortProvider<PORT>() {

      @Override
      public PORT provide() {
        Assertions.assertNotNull(m_wsdlLocation, "No 'wsdlLocation' configured on webservice stub '%s'.", getClass().getSimpleName());

        try {
          // Create the service.
          final Constructor<? extends Service> constructor = serviceClazz.getConstructor(URL.class, QName.class);
          @SuppressWarnings("unchecked")
          final SERVICE service = (SERVICE) constructor.newInstance(m_wsdlLocation, new QName(m_targetNamespace, m_serviceName));

          // Install the handler chain.
          service.setHandlerResolver(new HandlerResolver() {

            @Override
            public List<Handler> getHandlerChain(final PortInfo portInfo) {
              final List<Handler> handlers = new ArrayList<>();
              execInstallHandlers(handlers);
              return handlers;
            }
          });

          // Create the port.
          return service.getPort(m_portTypeClazz, CollectionUtility.toArray(m_webServiceFeatures, WebServiceFeature.class));
        }
        catch (final ReflectiveOperationException e) {
          throw new WebServiceException("Failed to instantiate webservice stub.", e);
        }
      }
    };
  }

  /**
   * Overwrite to disable port caching.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredPortCacheEnabled() {
    return true;
  }

  /**
   * Overwrite to specify 'time-to-live' in milliseconds for ports in the cache if the 'corePoolSize' is exceeded. That
   * typically happens at high load or if 'corePortCacheSize' is undersized.
   */
  @ConfigProperty(ConfigProperty.LONG)
  protected long getConfiguredPortCacheExpirationTime() {
    return TimeUnit.MINUTES.toMillis(15);
  }

  /**
   * Overwrite to configure the number of ports to be created in advance to speed up webservice calls. Cached ports may
   * increase performance because Port creation is an expensive operation due to WSDL/schema validation.<br/>
   * The cache is based on a 'corePoolSize', meaning that that number of Ports is created on a preemptively basis. If
   * more Ports than that number is required, they are are created on demand and additionally added to the cache until
   * expired, which is useful at a high load.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  protected int getConfiguredPortCacheCorePoolSize() {
    return 50;
  }

  /**
   * Overwrite to install JAX-WS handlers. The handlers are invoked in the order as placed in the handler-chain list.
   */
  @ConfigOperation
  protected void execInstallHandlers(final List<Handler> handlers) {
  }

  public String getEndpointUrl() {
    return m_endpointUrl;
  }

  public void setEndpointUrl(final String endpointUrl) {
    m_endpointUrl = endpointUrl;
  }

  public URL getWsdlLocation() {
    return m_wsdlLocation;
  }

  public void setWsdlLocation(final URL wsdlLocation) {
    m_wsdlLocation = wsdlLocation;
  }

  public String getTargetNamespace() {
    return m_targetNamespace;
  }

  public void setTargetNamespace(final String targetNamespace) {
    m_targetNamespace = targetNamespace;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void setServiceName(final String serviceName) {
    m_serviceName = serviceName;
  }

  public Integer getReadTimeout() {
    return m_readTimeout;
  }

  public void setReadTimeout(final Integer readTimeout) {
    m_readTimeout = readTimeout;
  }

  public Integer getConnectTimeout() {
    return m_connectTimeout;
  }

  public void setConnectTimeout(final Integer connectTimeout) {
    m_connectTimeout = connectTimeout;
  }

  public String getUsername() {
    return m_username;
  }

  public void setUsername(final String username) {
    m_username = username;
  }

  public String getPassword() {
    return m_password;
  }

  public void setPassword(final String password) {
    m_password = password;
  }

  public List<WebServiceFeature> getWebServiceFeatures() {
    return m_webServiceFeatures;
  }

  public void setWebServiceFeatures(final List<WebServiceFeature> webServiceFeatures) {
    m_webServiceFeatures = webServiceFeatures;
  }

  /**
   * Finds the generic type arguments in the super hierarchy of the actual class.
   */
  protected <T> Class<T>[] findGenericTypeArguments(final Type type) {
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
}
