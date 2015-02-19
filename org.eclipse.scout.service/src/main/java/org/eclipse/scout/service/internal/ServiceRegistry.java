///*******************************************************************************
// * Copyright (c) 2015 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.service.internal;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.TreeSet;
//
//import org.eclipse.scout.commons.CollectionUtility;
//import org.eclipse.scout.commons.CompareUtility;
//import org.eclipse.scout.commons.logger.IScoutLogger;
//import org.eclipse.scout.commons.logger.ScoutLogManager;
//import org.eclipse.scout.service.IServiceFactory;
//import org.eclipse.scout.service.IServiceReference;
//import org.eclipse.scout.service.ServiceUtility;
//
///**
// *
// */
//public final class ServiceRegistry {
//  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServiceRegistry.class);
//
//  private static final ServiceRegistry instance = new ServiceRegistry();
//
//  private Map<Class<?>, TreeSet<IServiceFactory>> m_services;
//
//  private ServiceRegistry() {
//    m_services = new HashMap<Class<?>, TreeSet<IServiceFactory>>();
//  }
//
//  public static ServiceRegistry getInstance() {
//    return instance;
//  }
//
//  public List<IServiceFactory> getServiceFactories(Class<? extends Object> service) {
//    final Set<IServiceFactory> services;
//    synchronized (m_services) {
//      services = m_services.get(service);
//    }
//    if (services == null) {
//      return CollectionUtility.emptyArrayList();
//    }
//    else {
//      return CollectionUtility.arrayList(services);
//    }
//  }
//
//  public void addService(IServiceReference serviceReference) {
//    List<Class<?>> clazzes = new ArrayList<Class<?>>();
//    clazzes.add(serviceReference.getService());
//    for (Class<?> c : ServiceUtility.getInterfacesHierarchy(serviceReference.getService(), Object.class)) {
//      clazzes.add(c);
//    }
//    addService((List<Class<?>>) clazzes, serviceReference);
//  }
//
//  /**
//   * @param clazzes
//   * @param serviceClazz
//   * @param factoryClazz
//   * @param sessionClazz
//   * @param ranking
//   * @param createImmediately
//   */
//  public void addService(List<Class<?>> clazzes, IServiceReference serviceReference) {
//    // create factory
//    IServiceFactory factory = null;
//    if (serviceReference.getFactory() != null) {
//      factory = createServiceFactory(serviceReference.getFactory(), serviceReference);
//    }
//    if (factory == null) {
//      if (serviceReference.isProxy()) {
//        throw new IllegalArgumentException("a proxy requires a factory");
//      }
//      else {
//        factory = new DefaultServiceFactory(serviceReference);
//      }
//    }
//    addService(clazzes, factory);
//  }
//
//  public void addService(Object service, IServiceReference reference) {
//    List<Class<?>> clazzes = new ArrayList<Class<?>>();
//    clazzes.add(service.getClass());
//    for (Class<?> c : ServiceUtility.getInterfacesHierarchy(service.getClass(), Object.class)) {
//      clazzes.add(c);
//    }
//    addService(clazzes, new InstanceServiceFactory(service, reference));
//  }
//
//  public void addService(List<Class<?>> clazzes, IServiceFactory factory) {
//    for (Class<?> clazz : clazzes) {
//      TreeSet<IServiceFactory> services = m_services.get(clazz);
//      if (services == null) {
//        services = new TreeSet<IServiceFactory>(new P_ServiceFacotryComparator());
//        m_services.put(clazz, services);
//      }
//      services.add(factory);
//    }
//    try {
//      factory.serviceRegistered();
//    }
//    catch (Throwable t) {
//      LOG.error(String.format("Service registration of service '%s' with factory '%s' failed. ", factory.getServiceReference().getService().getClass().getName(), factory.getClass().getName()), t);
//    }
//  }
//
//  private IServiceFactory createServiceFactory(Class<? extends IServiceFactory> serviceFactoryClazz, IServiceReference serviceReference) {
//    try {
//      return serviceFactoryClazz.getConstructor(IServiceReference.class).newInstance(serviceReference);
//    }
//    catch (Exception e) {
//      LOG.error("Could not instanciate service factory '" + serviceFactoryClazz + "'.", e);
//      return null;
//    }
//  }
//
//  /**
//   * @param ref
//   */
//  public void removeService(IServiceReference ref) {
//    for (Entry<Class<?>, TreeSet<IServiceFactory>> e : m_services.entrySet()) {
//      Iterator<IServiceFactory> it = e.getValue().iterator();
//      while (it.hasNext()) {
//        if (CompareUtility.equals(ref, it.next().getServiceReference())) {
//          it.remove();
//        }
//      }
//    }
//  }
//
//  private class P_ServiceFacotryComparator implements Comparator<IServiceFactory> {
//    @Override
//    public int compare(IServiceFactory factory1, IServiceFactory factory2) {
//      if (factory1 == factory2) {
//        return 0;
//      }
//      if (factory1 == null) {
//        return -1;
//      }
//      if (factory2 == null) {
//        return 1;
//      }
//      if (factory1.getServiceReference() == null && factory2.getServiceReference() == null) {
//        return 0;
//      }
//      if (factory1.getServiceReference() == null) {
//        return -1;
//      }
//      if (factory2.getServiceReference() == null) {
//        return 1;
//      }
//
//      int result = Float.compare(factory2.getServiceReference().getRanking(), factory1.getServiceReference().getRanking());
//      if (result == 0) {
//        return factory2.getServiceReference().getService().getName().compareTo(factory1.getServiceReference().getService().getName());
//      }
//      return result;
//
//    }
//  }
//
//}
