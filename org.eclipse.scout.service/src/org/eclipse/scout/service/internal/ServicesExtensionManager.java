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
package org.eclipse.scout.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.runtime.ExtensionPointTracker.Listener;
import org.eclipse.scout.service.DefaultServiceFactory;
import org.eclipse.scout.service.INullService;
import org.eclipse.scout.service.IServiceFactory;
import org.eclipse.scout.service.IServiceInitializerFactory;
import org.eclipse.scout.service.ServiceConstants;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ServicesExtensionManager implements Listener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServicesExtensionManager.class);
  private static final boolean SERVICE_CACHE_ENABLED = "true".equals(Activator.getDefault().getBundle().getBundleContext().getProperty("org.eclipse.scout.service.cache.enabled"));

  public static final String PROP_DEFAULT_PROXY_SERVICE_RANKING = ServicesExtensionManager.class.getName() + ".defaultProxyServiceRanking";
  public static final String PROP_DEFAULT_SERVICE_RANKING = ServicesExtensionManager.class.getName() + ".defaultServiceRanking";

  private final HashMap<IExtension, List<ServiceRegistration>> m_serviceRegistrations = new HashMap<IExtension, List<ServiceRegistration>>();
  private final HashMap<IExtension, IServiceInitializerFactory> m_serviceInitFactories = new HashMap<IExtension, IServiceInitializerFactory>();
  private final Map<ServiceReference, DirectServiceAccessor> m_directServiceAccessorCache = Collections.synchronizedMap(new HashMap<ServiceReference, DirectServiceAccessor>());

  public Collection<IServiceInitializerFactory> getServiceIntializerFactories() {
    return m_serviceInitFactories.values();
  }

  private ServicesExtensionPointTracker m_tracker;
  private int m_defaultProxyServiceRanking;
  private int m_defaultServiceRanking;

  public ServicesExtensionManager(IExtensionRegistry registry, String extensionPointId) {
    BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
    m_defaultProxyServiceRanking = -2;
    String defaultProxyRankingString = bundleContext.getProperty(PROP_DEFAULT_PROXY_SERVICE_RANKING);
    if (!StringUtility.isNullOrEmpty(defaultProxyRankingString)) {
      try {
        m_defaultProxyServiceRanking = Integer.parseInt(defaultProxyRankingString);
      }
      catch (Exception e) {
        LOG.warn("could not parse defaultProxyServiceRanking '" + defaultProxyRankingString + "'.", e);
      }
    }
    m_defaultServiceRanking = 0;
    String defaultServiceRankingString = bundleContext.getProperty(PROP_DEFAULT_SERVICE_RANKING);
    if (!StringUtility.isNullOrEmpty(defaultServiceRankingString)) {
      try {
        m_defaultServiceRanking = Integer.parseInt(defaultServiceRankingString);
      }
      catch (Exception e) {
        LOG.warn("could not parse defaultServiceRanking '" + defaultServiceRankingString + "'.", e);
      }
    }

    m_tracker = new ServicesExtensionPointTracker(registry, extensionPointId, this);
  }

  public void start() {
    if (m_tracker != null) {
      m_tracker.open();
    }
  }

  public void stop() {
    m_tracker.close();
    m_tracker = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void added(IExtension extension) {
    Bundle contributorBundle = Platform.getBundle(extension.getContributor().getName());
    if (contributorBundle == null) {
      return;
    }

    // Ensure to have a valid bundle context to register declared services on behalf of the contributing bundle.
    if (contributorBundle.getBundleContext() == null) {
      try {
        LOG.info("Starting bundle '{}' to provide Scout services", extension.getContributor().getName());
        contributorBundle.start(); // Has no effect if the bundle is about to start or is already started in the meantime.
      }
      catch (BundleException e) {
        throw new IllegalStateException(String.format("Failed to start bundle contributing Scout services [bundle=%s]", extension.getContributor().getName()), e);
      }
    }
    ArrayList<ServiceRegistration> list = new ArrayList<ServiceRegistration>();
    for (IConfigurationElement serviceElement : extension.getConfigurationElements()) {
      try {
        final Class serviceClass = contributorBundle.loadClass(serviceElement.getAttribute("class"));
        Object factory = null;
        if (serviceElement.getAttribute("factory") != null) {
          factory = createServiceFactory(contributorBundle, serviceElement.getAttribute("factory"), serviceClass);
        }
        String sessionType = null;
        if (serviceElement.getAttribute("session") != null) {
          sessionType = serviceElement.getAttribute("session");
        }
        Integer ranking = null;
        if (!StringUtility.isNullOrEmpty(serviceElement.getAttribute("ranking"))) {
          ranking = Integer.parseInt(serviceElement.getAttribute("ranking"));
        }
        else {
          Priority prio = (Priority) serviceClass.getAnnotation(Priority.class);
          if (prio != null) {
            ranking = (int) prio.value();
          }
        }
        Hashtable<String, Object> initParams = new Hashtable<String, Object>();
        if (serviceElement.getAttribute("createImmediately") != null) {
          String createImmediately = serviceElement.getAttribute("createImmediately");
          if ("true".equals(createImmediately)) {
            initParams.put(ServiceConstants.SERVICE_CREATE_IMMEDIATELY, true);
          }
        }
        if (sessionType != null) {
          initParams.put(ServiceConstants.SERVICE_SCOPE, sessionType);
        }
        for (IConfigurationElement initParamElement : serviceElement.getChildren("init-param")) {
          initParams.put(initParamElement.getAttribute("name"), initParamElement.getAttribute("value"));
        }
        // add impl
        if ("service".equals(serviceElement.getName())) {
          if (sessionType != null && factory == null) {
            throw new IllegalArgumentException("cannot specify a session without a factory");
          }
          if (factory == null) {
            factory = new DefaultServiceFactory(serviceClass);
          }
          ArrayList<String> clazzes = new ArrayList<String>();
          clazzes.add(serviceClass.getName());
          for (Class c : ServiceUtility.getInterfacesHierarchy(serviceClass, Object.class)) {
            clazzes.add(c.getName());
          }
          // register service
          if (ranking == null) {
            initParams.put(Constants.SERVICE_RANKING, m_defaultServiceRanking);
          }
          else {
            initParams.put(Constants.SERVICE_RANKING, ranking.intValue());
          }
          final ServiceRegistration reg = contributorBundle.getBundleContext().registerService(clazzes.toArray(new String[clazzes.size()]), factory, initParams);
          list.add(reg);
          if (factory instanceof IServiceFactory) {
            addToDirectAccessorCache(extension, (IServiceFactory) factory, clazzes, reg);
            ((IServiceFactory) factory).serviceRegistered(reg);
          }
        }
        else if ("proxy".equals(serviceElement.getName())) {
          if (factory == null) {
            throw new IllegalArgumentException("a proxy requires a factory");
          }
          ArrayList<String> clazzes = new ArrayList<String>();
          clazzes.add(serviceClass.getName());
          if (!serviceClass.isInterface()) {
            for (Class c : ServiceUtility.getInterfacesHierarchy(serviceClass, Object.class)) {
              clazzes.add(c.getName());
            }
          }
          // register service
          if (ranking == null) {
            initParams.put(Constants.SERVICE_RANKING, m_defaultProxyServiceRanking);
          }
          else {
            initParams.put(Constants.SERVICE_RANKING, ranking.intValue());
          }
          ServiceRegistration reg = contributorBundle.getBundleContext().registerService(clazzes.toArray(new String[clazzes.size()]), factory, initParams);
          list.add(reg);
          if (factory instanceof IServiceFactory) {
            addToDirectAccessorCache(extension, (IServiceFactory) factory, clazzes, reg);
            ((IServiceFactory) factory).serviceRegistered(reg);
          }
        }
        else if ("serviceInitializerFactory".equals(serviceElement.getName())) {
          loadServiceInitializerFactory(extension, serviceElement, contributorBundle);
        }
        else {
          throw new IllegalArgumentException("unexpected element name: " + serviceElement.getName());
        }
      }
      catch (Throwable t) {
        LOG.error("register " + serviceElement.getName() + ": bundle=" + contributorBundle.getSymbolicName() + ", service=" + serviceElement.getAttribute("class"), t);
      }
    }
    m_serviceRegistrations.put(extension, list);
  }

  @SuppressWarnings("unchecked")
  private void loadServiceInitializerFactory(IExtension extension, IConfigurationElement serviceElement, Bundle contributorBundle) {
    Class<IServiceInitializerFactory> initializerClass;
    String className = serviceElement.getAttribute("class");
    try {
      initializerClass = (Class<IServiceInitializerFactory>) contributorBundle.loadClass(className);
      IServiceInitializerFactory intFactory = initializerClass.newInstance();
      m_serviceInitFactories.put(extension, intFactory);
    }
    catch (Throwable t) {
      LOG.error("loading service initializer factory failed: bundle=" + contributorBundle.getSymbolicName() + "class=" + className, t);
    }
  }

  @Override
  public void removed(IExtension extension) {
    m_serviceInitFactories.remove(extension);
    List<ServiceRegistration> list = m_serviceRegistrations.remove(extension);
    if (list != null) {
      for (ServiceRegistration reg : list) {
        try {
          reg.unregister();
        }
        catch (Throwable t) {
          // nop
        }
      }
    }
    removeFromDirectAccessorCache(extension);
  }

  private Object createServiceFactory(Bundle bundle, String factoryClazz, Class<?> serviceClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    Class<?> c = bundle.loadClass(factoryClazz);
    try {
      return c.getConstructor(Class.class).newInstance(serviceClass);
    }
    catch (Throwable t) {
      return c.newInstance();
    }
  }

  private void addToDirectAccessorCache(IExtension extension, IServiceFactory factory, Collection<String> clazzes, ServiceRegistration reg) {
    if (!SERVICE_CACHE_ENABLED) {
      return;
    }
    DirectServiceAccessor a = new DirectServiceAccessor(extension, factory, reg);
    m_directServiceAccessorCache.put(reg.getReference(), a);
  }

  private void removeFromDirectAccessorCache(IExtension owner) {
    if (!SERVICE_CACHE_ENABLED) {
      return;
    }
    synchronized (m_directServiceAccessorCache) {
      for (Iterator<DirectServiceAccessor> it = m_directServiceAccessorCache.values().iterator(); it.hasNext();) {
        DirectServiceAccessor a = it.next();
        if (a.getOwner() == owner) {
          it.remove();
        }
      }
    }
  }

  public void clearDirectAccessorCache() {
    if (!SERVICE_CACHE_ENABLED) {
      return;
    }
    m_directServiceAccessorCache.clear();
  }

  /**
   * safely get and immediately unget the service in an atomic section using the service reference as lock
   */
  public <S> S fastResolveService(Class<S> serviceInterfaceClass, BundleContext context, ServiceReference ref) {
    if (ref == null) {
      return null;
    }
    synchronized (ref) {
      try {
        S service = fastGetService(context, ref);
        if (service instanceof INullService) {
          return null;
        }
        return service;
      }
      finally {
        context.ungetService(ref);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <S> S fastGetService(BundleContext context, ServiceReference ref) {
    if (SERVICE_CACHE_ENABLED) {
      //use fast-access cache!
      DirectServiceAccessor a = m_directServiceAccessorCache.get(ref);
      if (a != null) {
        return (S) a.getServiceImpl(context);
      }
    }
    //slow
    return (S) context.getService(ref);
  }

}
