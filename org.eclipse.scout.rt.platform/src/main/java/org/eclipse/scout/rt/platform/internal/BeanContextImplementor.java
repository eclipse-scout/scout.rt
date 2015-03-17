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
package org.eclipse.scout.rt.platform.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.InitializationException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanContext;
import org.eclipse.scout.rt.platform.IBeanInstanceFactory;

public class BeanContextImplementor implements IBeanContext {
  private final ReentrantReadWriteLock m_lock;
  private final Map<Class<?>, TreeSet<IBeanRegistration>> m_regs;
  private IBeanInstanceFactory m_beanInstanceFactory;

  public BeanContextImplementor() {
    this(null);
  }

  public BeanContextImplementor(IBeanInstanceFactory f) {
    m_lock = new ReentrantReadWriteLock(true);
    m_regs = new HashMap<Class<?>, TreeSet<IBeanRegistration>>();
    m_beanInstanceFactory = f;
  }

  @Override
  public ReentrantReadWriteLock getReadWriteLock() {
    return m_lock;
  }

  @Internal
  protected TreeSet<IBeanRegistration> getRegistrationsInternal(Class<?> beanClazz) {
    m_lock.readLock().lock();
    try {
      Assertions.assertNotNull(beanClazz);
      TreeSet<IBeanRegistration> regs = m_regs.get(beanClazz);
      if (regs == null) {
        return CollectionUtility.emptyTreeSet();
      }
      return regs;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Internal
  protected void setBeanInstanceFactory(IBeanInstanceFactory f) {
    m_beanInstanceFactory = f;
  }

  @Internal
  protected IBeanInstanceFactory getBeanInstanceFactory() {
    return m_beanInstanceFactory;
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

  @Override
  public Set<IBean<?>> getAllRegisteredBeans() {
    m_lock.readLock().lock();
    try {
      HashSet<IBean<?>> allBeans = new HashSet<IBean<?>>();
      for (Set<IBeanRegistration> regs : m_regs.values()) {
        for (IBeanRegistration reg : regs) {
          allBeans.add(reg.getBean());
        }
      }
      return allBeans;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IBean<T> registerClass(Class<T> beanClazz) {
    m_lock.writeLock().lock();
    try {
      TreeSet<IBeanRegistration> regs = getRegistrationsInternal(beanClazz);
      if (regs.size() == 1) {
        return (IBean<T>) regs.first().getBean();
      }
      BeanImplementor<T> bean = new BeanImplementor<T>(beanClazz);
      registerBean(bean, null);
      return bean;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  public void registerBean(IBean bean, Object instance) {
    m_lock.writeLock().lock();
    try {
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
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  public synchronized void unregisterBean(IBean bean) {
    m_lock.writeLock().lock();
    try {
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
    finally {
      m_lock.writeLock().unlock();
    }
  }

  public void startCreateImmediatelyBeans() {
    m_lock.readLock().lock();
    try {
      for (Set<IBeanRegistration> regs : m_regs.values()) {
        for (IBeanRegistration reg : regs) {
          if (BeanContextImplementor.isCreateImmediately(reg.getBean())) {
            if (BeanContextImplementor.isApplicationScoped(reg.getBean())) {
              reg.getInstance();
            }
            else {
              throw new InitializationException(String.format("Bean '%s' is marked with @CreateImmediately and is not application scoped (@ApplicationScoped) - unexpected configuration! ", reg.getBean().getBeanClazz()));
            }
          }
        }
      }
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  public static boolean isCreateImmediately(IBean<?> bean) {
    return bean.getBeanAnnotation(CreateImmediately.class) != null;
  }

  public static boolean isApplicationScoped(IBean<?> bean) {
    return bean.getBeanAnnotation(ApplicationScoped.class) != null;
  }
}
