/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.services.common.jdbc.AbstractSqlService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

/**
 * Base class for a JNDI configured scout service. To configure a JNDI scout service one can put in config.ini for each
 * property a value. Alternatively one could override the 'getConfigured' methods. See also {@link AbstractSqlService}.
 * <p>
 * <code>
 *   org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageService#connectionFactory=MyConnectionFactory
 *   org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageService#destination=JMSTopicName
 *   org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageService#jndiInitialContextFactory=org.apache.activemq.jndi.ActiveMQInitialContextFactory
 *   org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageService#jndiProviderUrl=tcp://localhost:61616
 *   org.eclipse.scout.rt.server.jms.clustersync.JmsPublishSubscribeMessageService#jndiProperties=C:/Temp/jndi.properties
 * </code>
 * <p>
 * For jndiProperties one can use also usual URLs. For instance:</br> <code>
 * platform:/plugin/org.eclipse.scout.rt.server.jms/jndi.properties
 * </code>
 */
public abstract class AbstractJndiService extends AbstractService {
  private String m_jndiInitialContextFactory;
  private String m_jndiProviderUrl;
  private String m_jndiUrlPkgPrefixes;
  private String m_jndiProperties;
  private String m_securityPrincipal;
  private String m_securityCredentials;

  @Override
  public void initializeService(@SuppressWarnings("rawtypes") ServiceRegistration registration) {
    initConfig();
    super.initializeService(registration);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredJndiInitialContextFactory() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredJndiProviderUrl() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(50)
  protected String getConfiguredJndiUrlPkgPrefixes() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(80)
  protected String getConfiguredJndiProperties() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  protected String getConfiguredSecurityPrincipal() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(70)
  protected String getConfiguredSecurityCredentials() {
    return null;
  }

  public String getJndiInitialContextFactory() {
    return m_jndiInitialContextFactory;
  }

  public void setJndiInitialContextFactory(String jndiInitialContextFactory) {
    m_jndiInitialContextFactory = jndiInitialContextFactory;
  }

  public String getJndiProviderUrl() {
    return m_jndiProviderUrl;
  }

  public void setJndiProviderUrl(String jndiProviderUrl) {
    m_jndiProviderUrl = jndiProviderUrl;
  }

  public String getJndiUrlPkgPrefixes() {
    return m_jndiUrlPkgPrefixes;
  }

  public void setJndiUrlPkgPrefixes(String jndiUrlPkgPrefixes) {
    m_jndiUrlPkgPrefixes = jndiUrlPkgPrefixes;
  }

  public String getJndiProperties() {
    return m_jndiProperties;
  }

  public void setJndiProperties(String jndiProperties) {
    m_jndiProperties = jndiProperties;
  }

  public String getSecurityPrincipal() {
    return m_securityPrincipal;
  }

  public void setSecurityPrincipal(String securityPrincipal) {
    m_securityPrincipal = securityPrincipal;
  }

  public String getSecurityCredentials() {
    return m_securityCredentials;
  }

  public void setSecurityCredentials(String securityCredentials) {
    m_securityCredentials = securityCredentials;
  }

  protected void initConfig() {
    setJndiInitialContextFactory(getConfiguredJndiInitialContextFactory());
    setJndiProviderUrl(getConfiguredJndiProviderUrl());
    setJndiUrlPkgPrefixes(getConfiguredJndiUrlPkgPrefixes());
    setJndiProperties(getConfiguredJndiProperties());
    setSecurityPrincipal(getConfiguredSecurityPrincipal());
    setSecurityCredentials(getConfiguredSecurityCredentials());
  }

  protected <T> T lookup(String name, Class<T> type) throws ProcessingException {
    Properties env = new Properties();
    if (StringUtility.hasText(getJndiProperties())) {
      InputStream jndiProperties = null;
      try {
        jndiProperties = getJndiPorpertiesInputStream(getJndiProperties());
        env.load(jndiProperties);
      }
      catch (Exception e) {
        throw new ProcessingException("JNDI properties could not be loaded", e);
      }
      finally {
        if (jndiProperties != null) {
          try {
            jndiProperties.close();
          }
          catch (IOException e) {
            throw new ProcessingException("Unexpected", e);
          }
        }
      }
    }
    if (StringUtility.hasText(getJndiInitialContextFactory())) {
      env.put(Context.INITIAL_CONTEXT_FACTORY, getJndiInitialContextFactory());
    }
    if (StringUtility.hasText(getJndiProviderUrl())) {
      env.put(Context.PROVIDER_URL, getJndiProviderUrl());
    }
    if (StringUtility.hasText(getJndiUrlPkgPrefixes())) {
      env.put(Context.URL_PKG_PREFIXES, getJndiUrlPkgPrefixes());
    }
    if (getSecurityPrincipal() != null) {
      env.put(Context.SECURITY_PRINCIPAL, getSecurityPrincipal());
    }
    if (getSecurityCredentials() != null) {
      env.put(Context.SECURITY_CREDENTIALS, getSecurityCredentials());
    }
    return lookupAndCast(name, type, env);
  }

  /**
   * Internal method. Should not be used directly, instead use {@link #lookup(String, Class)}
   */
  @SuppressWarnings("unchecked")
  protected <T> T lookupAndCast(String name, Class<T> type, Properties env) throws ProcessingException {
    Context ctx = null;
    try {
      if (env.size() > 0) {
        ctx = new InitialContext(env);
      }
      else {
        ctx = new InitialContext();
      }
      Object o = ctx.lookup(name);
      if (o == null) {
        return null;
      }
      if (type != null && !type.isAssignableFrom(o.getClass())) {
        throw new ProcessingException("Resolved object has unexpected type: expected '" + type + "', but has '" + o.getClass() + "'.");
      }
      return (T) o;
    }
    catch (NamingException e) {
      throw new ProcessingException("Error while looking up JNDI resource '" + name + "'", e);
    }
  }

  protected InputStream getJndiPorpertiesInputStream(String propertiesLocation) throws IOException {
    InputStream jndiProperties;
    try {
      // assume URL
      URL url = new URL(propertiesLocation);
      URLConnection connection = url.openConnection();
      connection.setUseCaches(false);
      jndiProperties = connection.getInputStream();
    }
    catch (MalformedURLException e) {
      // assume file location
      jndiProperties = new FileInputStream(propertiesLocation);
    }
    return jndiProperties;
  }
}
