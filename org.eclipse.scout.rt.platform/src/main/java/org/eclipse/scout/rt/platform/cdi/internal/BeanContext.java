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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.InitializationException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.cdi.ApplicationScoped;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.eclipse.scout.rt.platform.cdi.CreateImmediately;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IBeanContext;
import org.eclipse.scout.rt.platform.cdi.IBeanInstanceFactory;
import org.eclipse.scout.rt.platform.cdi.IBeanRegistration;
import org.eclipse.scout.rt.platform.cdi.SimpleBeanInstanceFactory;

public class BeanContext implements IBeanContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BeanContext.class);

  private final Map<Class<?>, TreeSet<IBeanRegistration>> m_regs;
  private IBeanInstanceFactory m_beanInstanceFactory;

  public BeanContext() {
    m_regs = new HashMap<Class<?>, TreeSet<IBeanRegistration>>();
  }

  @Override
  public <T> T getInstance(Class<T> beanClazz) {
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
    T instance = m_beanInstanceFactory.select(beanClazz, regs);
    if (instance != null) {
      return instance;
    }
    throw new Assertions.AssertionException("no instance found for query: " + beanClazz);
  }

  @Override
  public <T> T getInstanceOrNull(Class<T> beanClazz) {
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
    return m_beanInstanceFactory.select(beanClazz, regs);
  }

  @Override
  public <T> List<T> getInstances(Class<T> beanClazz) {
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
    return m_beanInstanceFactory.selectAll(beanClazz, regs);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<IBean<T>> getBeans(Class<T> beanClazz) {
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
    List<IBean<T>> result = new ArrayList<IBean<T>>(regs.size());
    for (IBeanRegistration reg : regs) {
      result.add((IBean<T>) reg.getBean());
    }
    return result;
  }

  protected TreeSet<IBeanRegistration> getRegistrationsInternal(Class<?> beanClazz) {
    Assertions.assertNotNull(beanClazz);
    TreeSet<IBeanRegistration> regs = m_regs.get(beanClazz);
    if (regs == null) {
      return CollectionUtility.emptyTreeSet();
    }
    return regs;
  }

  @Override
  public Set<IBean<?>> getAllRegisteredBeans() {
    HashSet<IBean<?>> allBeans = new HashSet<IBean<?>>();
    for (Set<IBeanRegistration> regs : m_regs.values()) {
      for (IBeanRegistration reg : regs) {
        allBeans.add(reg.getBean());
      }
    }
    return allBeans;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IBean<T> registerClass(Class<T> beanClazz) {
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
    if (regs.size() == 1) {
      return (IBean<T>) regs.first().getBean();
    }
    Bean<T> bean = new Bean<T>(beanClazz);
    registerBean(bean, null);
    return bean;
  }

  @Override
  public void registerBean(IBean bean, Object instance) {
    Class[] interfacesHierarchy = BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class);
    List<Class<?>> clazzes = new ArrayList<Class<?>>(interfacesHierarchy.length + 1);
    clazzes.add(bean.getBeanClazz());
    for (Class<?> c : interfacesHierarchy) {
      clazzes.add(c);
    }

    @SuppressWarnings("unchecked")
    IBeanRegistration reg = new BeanRegistration(bean, instance);
    for (Class<?> clazz : clazzes) {
      TreeSet<IBeanRegistration> regs = m_regs.get(clazz);
      if (regs == null) {
        regs = new TreeSet<IBeanRegistration>();
        m_regs.put(clazz, regs);
      }
      regs.add(reg);
    }
  }

  @Override
  public synchronized void unregisterBean(IBean bean) {
    Assertions.assertNotNull(bean);
    for (Set<IBeanRegistration> regs : m_regs.values()) {
      Iterator<IBeanRegistration> regIt = regs.iterator();
      while (regIt.hasNext()) {
        if (regIt.next().getBean().equals(bean)) {
          regIt.remove();
        }
      }
    }
  }

  public void initBeanInstanceFactory() {
    //TODO imo shortcut code here: use config.ini or other concept to set correct m_beanInstanceFactory, also create a CompositeBeanInstanceFactory that knows client AND server
    TreeSet<IBeanRegistration> regs = getRegistrationsInternal(IBeanInstanceFactory.class);
    if (regs.size() > 0) {
      m_beanInstanceFactory = (IBeanInstanceFactory) regs.first().getInstance();
    }
    if (m_beanInstanceFactory == null) {
      m_beanInstanceFactory = new SimpleBeanInstanceFactory();
      LOG.warn("Using " + m_beanInstanceFactory.getClass().getName() + ". Please verify that this application really has no client or server side " + IBeanInstanceFactory.class.getSimpleName());
    }
  }

  public void startCreateImmediatelyBeans() {
    for (Set<IBeanRegistration> regs : m_regs.values()) {
      for (IBeanRegistration reg : regs) {
        if (BeanContext.isCreateImmediately(reg.getBean())) {
          if (BeanContext.isApplicationScoped(reg.getBean())) {
            reg.getInstance();
          }
          else {
            throw new InitializationException(String.format("Bean '%s' is marked with @CreateImmediately and is not application scoped (@ApplicationScoped) - unexpected configuration! ", reg.getBean().getBeanClazz()));
          }
        }
      }
    }
  }

  public static boolean isCreateImmediately(IBean<?> bean) {
    return bean.getBeanAnnotation(CreateImmediately.class) != null;
  }

  public static boolean isApplicationScoped(IBean<?> bean) {
    return bean.getBeanAnnotation(ApplicationScoped.class) != null;
  }
}
