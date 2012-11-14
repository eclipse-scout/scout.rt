/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.jaxws.annotation.ScoutWebServiceClient;
import org.eclipse.scout.jaxws.security.consumer.IAuthenticationHandler;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;
import org.osgi.framework.ServiceRegistration;

import com.sun.xml.internal.ws.client.BindingProviderProperties;

/**
 * <p>
 * To proxy a webservice stub to an {@link IService}.
 * </p>
 * <ul>
 * <li>An authentication strategy can be specified by setting the annotation {@link ScoutWebServiceClient}<br/>
 * A user's credential can be configured in config.ini by setting the properties domain, username and password or
 * directly in respective <code>getConfigured</code> methods.</li>
 * <li>Custom handlers can be installed by overriding {@link AbstractWebServiceClient#execInstallHandlers(List)}.</li>
 * <li>The endpoint URL can be configured by specifying the property url in config.ini or directly in
 * {@link AbstractWebServiceClient#getConfiguredUrl()}. If it is about a dynamic URL, it also can be set at runtime when
 * obtaining the port type.</li>
 * </ul>
 * 
 * @param <S>
 *          The service to be proxied. The service is unique among all services defined within in the enclosing WSDL
 *          document and groups a set of related ports together.
 * @param <P>
 *          The port type to communicate with. The port type is unique among all port types defined within in the
 *          enclosing WSDL document. A port type is a named set of abstract operations and the abstract messages
 *          involved. The port type is unique among all port types defined within in the enclosing WSDL document.
 */
@SuppressWarnings("restriction")
@ScoutWebServiceClient
public class AbstractWebServiceClient<S extends Service, P> extends AbstractService implements IWebServiceClient {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractWebServiceClient.class);

  private WebServiceClient m_webServiceClientAnnotation;
  private String m_url;
  private URL m_wsdlLocation;
  private String m_targetNamespace;
  private String m_serviceName;
  private Integer m_requestTimeout;
  private Integer m_connectTimeout;

  private String m_username;
  private String m_password;

  private Class<? extends Service> m_serviceClazz;
  private Class<?> m_portTypeClazz;

  @SuppressWarnings("unchecked")
  @Override
  public void initializeService(ServiceRegistration registration) {
    m_serviceClazz = (Class<? extends Service>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    m_portTypeClazz = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    m_webServiceClientAnnotation = m_serviceClazz.getAnnotation(WebServiceClient.class);

    if (m_webServiceClientAnnotation == null) {
      throw new WebServiceException("Missing required annotation '" + WebServiceClient.class.getName() + "' on webservice client '" + getClass().getName() + "'");
    }

    initConfig();
    super.initializeService(registration);
  }

  protected void initConfig() {
    setWsdlLocation(getConfiguredWsdlLocation());
    setUrl(getConfiguredUrl());
    setTargetNamespace(getConfiguredTargetNamespace());
    setServiceName(getConfiguredServiceName());
    setUsername(getConfiguredUsername());
    setPassword(getConfiguredPassword());
    setConnectTimeout(getConfiguredConnectTimeout());
    setRequestTimeout(getConfiguredRequestTimeout());
  }

  /**
   * To get the service stub specified by generic type parameter {@link S}.<br/>
   * Please be in mind, that the endpoint URL is set on port type level. Therefore, when working directly on the
   * service, you have to set the endpoint URL manually when calling the service. <br/>
   * By using {@link AbstractWebServiceClient#getPortType()}, the URL is set accordingly.
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public S getWebService() {
    if (getWsdlLocation() == null) {
      throw new WebServiceException("No location for WSDL configured on webservice proxy '" + getClass().getName() + "'.");
    }

    try {
      Constructor<? extends Service> constructor = m_serviceClazz.getConstructor(URL.class, QName.class);
      S service = (S) constructor.newInstance(getWsdlLocation(), new QName(getTargetNamespace(), getServiceName()));

      // install handlers
      service.setHandlerResolver(new HandlerResolver() {

        @Override
        public List<Handler> getHandlerChain(PortInfo portInfo) {
          ArrayList<Handler> list = new ArrayList<Handler>();

          // authentication handler
          IAuthenticationHandler authenticationHandler = createAuthenticationHandler();
          if (authenticationHandler != null) {
            try {
              (authenticationHandler).setUsername(getUsername());
              (authenticationHandler).setPassword(getPassword());

              if (execPrepareAuthenticationHandler(authenticationHandler)) {
                list.add(authenticationHandler);
              }
            }
            catch (ProcessingException e) {
              LOG.error("Authentication handler could not be installed.", e);
            }
          }

          List<SOAPHandler<SOAPMessageContext>> handlers = new LinkedList<SOAPHandler<SOAPMessageContext>>();
          execInstallHandlers(handlers);
          if (handlers.size() > 0) {
            list.addAll(handlers);
          }

          return list;
        }
      });

      return service;
    }
    catch (Exception e) {
      throw new WebServiceException("Webservice proxy '" + getClass().getName() + "' could not be created.", e);
    }
  }

  /**
   * To get the port type specified by generic type parameter {@link P}.
   * 
   * @return
   */
  public P getPortType() {
    return getPortType(getUrl());
  }

  /**
   * To get the port type specified by generic type parameter {@link P}.
   * 
   * @param url
   *          {@link URL} to connect with endpoint. Do not use this method to distinguish between development and
   *          production {@link URL}. Use property in config.ini instead.
   * @return
   */
  @SuppressWarnings("unchecked")
  public P getPortType(String url) {
    if (StringUtility.isNullOrEmpty(url)) {
      throw new WebServiceException("No endpoint URL configured for webservice client '" + getClass().getName() + "'.");
    }

    P portType = (P) getWebService().getPort(m_portTypeClazz);

    // set endpoint URL
    ((javax.xml.ws.BindingProvider) portType).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

    // set request timeout
    if (NumberUtility.nvl(getRequestTimeout(), 0) > 0) {
      ((javax.xml.ws.BindingProvider) portType).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, getRequestTimeout());
    }

    // set connect timeout
    if (NumberUtility.nvl(getConnectTimeout(), 0) > 0) {
      ((javax.xml.ws.BindingProvider) portType).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, getConnectTimeout());
    }

    return portType;
  }

  /**
   * To be overwritten if WSDL file is located somewhere else than specified in {@link WebServiceClient#wsdlLocation()}.
   * This location can be specified when generating the stub with the option -wsdllocation.
   * 
   * @return URL to WSDL file
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(20)
  protected URL getConfiguredWsdlLocation() {
    String wsdlLocation = m_webServiceClientAnnotation.wsdlLocation();
    if (StringUtility.isNullOrEmpty(wsdlLocation)) {
      throw new WebServiceException("No WSDL file location configured on webservice proxy '" + getClass().getName() + "'");
    }
    IPath path = new Path("").addTrailingSeparator().append(new Path(wsdlLocation)); // ensure root relative path
    URL urlWsdlLocation = getClass().getResource(path.toPortableString());
    if (urlWsdlLocation == null) {
      throw new WebServiceException("Could not find WSDL file '" + StringUtility.nvl(wsdlLocation, "?") + "' of webservice client '" + getClass().getName() + "'");
    }
    return urlWsdlLocation;
  }

  /**
   * To configure a static endpoint URL
   * 
   * @return
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredUrl() {
    return null;
  }

  /**
   * To be overwritten if service name to be called is different from {@link WebServiceClient#name()} in {@link Service}
   * 
   * @return unqualified service name to be called.
   */
  @Order(40)
  protected String getConfiguredServiceName() {
    return m_webServiceClientAnnotation.name();
  }

  /**
   * To be overwritten if targetNamespace of service to be called is different from
   * {@link WebServiceClient#targetNamespace()} in {@link Service}
   * 
   * @return
   */
  @Order(50)
  protected String getConfiguredTargetNamespace() {
    return m_webServiceClientAnnotation.targetNamespace();
  }

  /**
   * To configure a static user's credential
   * 
   * @return
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  protected String getConfiguredUsername() {
    return null;
  }

  /**
   * To configure a static user's credential
   * 
   * @return
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredPassword() {
    return null;
  }

  /**
   * To configure the maximum timeout in milliseconds to wait for the connection to be established.
   * 
   * @See {@link HttpURLConnection#setConnectTimeout(int)}
   * @return the maximal timeout to wait for the connection to be established.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(80)
  protected Integer getConfiguredConnectTimeout() {
    return null;
  }

  /**
   * To configure the maximum timeout in milliseconds to wait for response data to be ready to be read.
   * 
   * @See {@link HttpURLConnection#setReadTimeout(int)}
   * @return the maximal timeout to wait for response data to be ready to be read.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(90)
  protected Integer getConfiguredRequestTimeout() {
    return null;
  }

  /**
   * Is called before the authentication handler is installed.
   * 
   * @param authenticationHandler
   *          the authentication handler
   * @return true to install the authentication handler or false to not install it
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(10)
  protected boolean execPrepareAuthenticationHandler(IAuthenticationHandler authenticationHandler) throws ProcessingException {
    return true;
  }

  /**
   * Add custom handlers to the handler chain
   * 
   * @param handlers
   *          handlers to be installed
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(20)
  protected void execInstallHandlers(List<SOAPHandler<SOAPMessageContext>> handlers) {
  }

  private IAuthenticationHandler createAuthenticationHandler() {
    ScoutWebServiceClient annotation = null;
    Class<?> declaringClass = getClass();
    while (annotation == null && declaringClass != null && declaringClass != Object.class) {
      annotation = declaringClass.getAnnotation(ScoutWebServiceClient.class);
      declaringClass = declaringClass.getSuperclass();
    }

    if (annotation == null) {
      return null;
    }

    Class<? extends IAuthenticationHandler> authenticationHandlerClazz = annotation.authenticationHandler();
    if (authenticationHandlerClazz == null || authenticationHandlerClazz == IAuthenticationHandler.NONE.class) {
      return null;
    }

    IAuthenticationHandler authenticationHandler = null;
    try {
      authenticationHandler = authenticationHandlerClazz.newInstance();
    }
    catch (Exception e) {
      LOG.error("Failed to instantiate authentication handler '" + authenticationHandlerClazz.getName() + "'.", e);
      return null;
    }

    return authenticationHandler;
  }

  @Override
  public String getPassword() {
    return m_password;
  }

  @Override
  public String getServiceName() {
    return m_serviceName;
  }

  @Override
  public String getTargetNamespace() {
    return m_targetNamespace;
  }

  @Override
  public String getUsername() {
    return m_username;
  }

  @Override
  public URL getWsdlLocation() {
    return m_wsdlLocation;
  }

  @Override
  public void setPassword(String password) {
    m_password = password;
  }

  @Override
  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  @Override
  public void setTargetNamespace(String targetNamespace) {
    m_targetNamespace = targetNamespace;
  }

  @Override
  public void setUsername(String username) {
    m_username = username;
  }

  @Override
  public Integer getRequestTimeout() {
    return m_requestTimeout;
  }

  @Override
  public void setRequestTimeout(Integer requestTimeout) {
    m_requestTimeout = requestTimeout;
  }

  @Override
  public Integer getConnectTimeout() {
    return m_connectTimeout;
  }

  @Override
  public void setConnectTimeout(Integer connectTimeout) {
    m_connectTimeout = connectTimeout;
  }

  @Override
  public void setWsdlLocation(URL wsdlLocation) {
    m_wsdlLocation = wsdlLocation;
  }

  @Override
  public String getUrl() {
    return m_url;
  }

  @Override
  public void setUrl(String url) {
    m_url = url;
  }
}
