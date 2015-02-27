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
package org.eclipse.scout.rt.platform.cdi.internal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.interceptor.InterceptorBinding;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.CreateImmediately;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;

public class BeanContext implements IBeanContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanContext.class);
  private final Map<Class<?>, TreeSet<IBean<?>>> m_beans;
  private final Map<Class<? extends Annotation>, ?> m_interceptors;

  public BeanContext() {
    m_beans = new HashMap<Class<?>, TreeSet<IBean<?>>>();
    m_interceptors = new BeansXmlParser().getInterceptors();
  }

  @Override
  public <T> T getInstance(Class<T> beanClazz) {
    return getBean(beanClazz).get();
  }

  @Override
  public <T> T getInstanceOrNull(Class<T> beanClazz) {
    IBean<T> bean = getBeanInternal(beanClazz);
    if (bean != null) {
      return bean.get();
    }
    return null;
  }

  @Override
  public <T> List<T> getInstances(Class<T> beanClazz) {
    List<IBean<T>> beans = getBeans(beanClazz);
    List<T> instances = new ArrayList<T>(beans.size());
    for (IBean<T> bean : beans) {
      instances.add(bean.get());
    }
    return instances;
  }

  @Override
  public <T> IBean<T> getBean(Class<T> beanClazz) {
    return Assertions.assertNotNull(getBeanInternal(beanClazz), "No beans bound to '%s'", beanClazz);
  }

  @Override
  public <T> IBean<T> getBeanOrNull(Class<T> beanClazz) {
    return getBeanInternal(beanClazz);
  }

  @SuppressWarnings("unchecked")
  private <T> IBean<T> getBeanInternal(Class<T> beanClazz) {
    TreeSet<IBean<?>> beans = getBeansInternal(beanClazz);
    if (beans.size() > 0) {
      IBean<T> bean = (IBean<T>) beans.first();
      if (!beanClazz.isInterface() && bean.isIntercepted()) {
        throw new IllegalArgumentException(String.format("Intercepted beans can only be accessed with an interface. '%s' is not an interface.", beanClazz.getName()));
      }
      return bean;
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<IBean<T>> getBeans(Class<T> beanClazz) {
    TreeSet<IBean<?>> beans = getBeansInternal(beanClazz);
    List<IBean<T>> result = new ArrayList<IBean<T>>(beans.size());
    final boolean isInterface = beanClazz.isInterface();
    for (IBean<?> bean : beans) {
      result.add((IBean<T>) bean);
      if (!isInterface && bean.isIntercepted()) {
        throw new IllegalArgumentException(String.format("Intercepted beans can only be accessed with an interface. '%s' is not an interface.", beanClazz.getName()));
      }

    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public <T> List<IBean<T>> getBeansWithoutInterceptionCheck(Class<T> beanClazz) {
    TreeSet<IBean<?>> beans = getBeansInternal(beanClazz);
    List<IBean<T>> result = new ArrayList<IBean<T>>(beans.size());
    for (IBean<?> bean : beans) {
      result.add((IBean<T>) bean);
    }
    return result;
  }

  private synchronized TreeSet<IBean<?>> getBeansInternal(Class<?> beanClazz) {
    Assertions.assertNotNull(beanClazz);
    TreeSet<IBean<?>> beans = m_beans.get(beanClazz);
    if (beans == null) {
      return CollectionUtility.emptyTreeSet();
    }
    return beans;
  }

  @Override
  public List<IBean<?>> getAllRegisteredBeans() {
    List<IBean<?>> allBeans = new LinkedList<IBean<?>>();
    for (Set<IBean<?>> beans : m_beans.values()) {
      allBeans.addAll(beans);
    }
    return allBeans;
  }

  @Override
  public <T> IBean<T> registerClass(Class<T> beanClazz) {
    TreeSet<IBean<?>> beans = getBeansInternal(beanClazz);
    if (beans.size() == 1) {
      return (IBean<T>) beans.first();
    }
    Bean<T> bean = new Bean<T>(beanClazz);
    registerBean(bean);
    return bean;
  }

  @Override
  public void registerBean(IBean<?> bean) {
    Class[] interfacesHierarchy = BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class);
    List<Class<?>> clazzes = new ArrayList<Class<?>>(interfacesHierarchy.length + 1);
    clazzes.add(bean.getBeanClazz());
    for (Class<?> c : interfacesHierarchy) {
      clazzes.add(c);
    }
    registerBean(clazzes, bean);
  }

  public synchronized void registerBean(List<Class<?>> clazzes, IBean<?> bean) {
    IBean<?> interceptedBean = createInterceptedBean(bean);
    for (Class<?> clazz : clazzes) {
      TreeSet<IBean<?>> beans = m_beans.get(clazz);
      if (beans == null) {
        beans = new TreeSet<IBean<?>>(new P_BeanComparator());
        m_beans.put(clazz, beans);
      }
      beans.add(interceptedBean);
    }
  }

  /**
   * @param bean
   * @return
   */
  private <T> IBean<T> createInterceptedBean(IBean<T> bean) {
    for (Annotation a : bean.getBeanAnnotations().values()) {
      if (a.annotationType().getAnnotation(InterceptorBinding.class) != null) {
        Object interceptor = m_interceptors.get(a.annotationType());
        if (interceptor != null) {
          return new InterceptedBean<T>(bean, interceptor);
        }
      }
    }
    return bean;
  }

  @Override
  public synchronized void unregisterBean(IBean<?> bean) {
    Assertions.assertNotNull(bean);
    for (Set<IBean<?>> beans : m_beans.values()) {
      Iterator<IBean<?>> beanIt = beans.iterator();
      while (beanIt.hasNext()) {
        if (beanIt.next().equals(bean)) {
          beanIt.remove();
        }
      }
    }
  }

  private class P_BeanComparator implements Comparator<IBean<?>> {
    @Override
    public int compare(IBean<?> bean1, IBean<?> bean2) {
      if (bean1 == bean2) {
        return 0;
      }
      if (bean1 == null) {
        return -1;
      }
      if (bean2 == null) {
        return 1;
      }

      int result = Float.compare(getPriority(bean2), getPriority(bean1));
      if (result != 0) {
        return result;
      }

      return bean1.getBeanClazz().getName().compareTo(bean2.getBeanClazz().getName());
    }

    public float getPriority(IBean<?> bean) {
      float prio = -1;
      Priority priorityAnnotation = bean.getBeanAnnotation(Priority.class);
      if (priorityAnnotation != null) {
        prio = priorityAnnotation.value();
      }
      return prio;
    }
  }

  public static boolean isCreateImmediately(IBean<?> bean) {
    return bean.getBeanAnnotation(CreateImmediately.class) != null;
  }

  public static boolean isApplicationScoped(IBean<?> bean) {
    return bean.getBeanAnnotation(ApplicationScoped.class) != null;
  }
}
