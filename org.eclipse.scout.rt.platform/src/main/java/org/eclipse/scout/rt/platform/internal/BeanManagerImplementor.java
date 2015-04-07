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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.InitializationException;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;

public class BeanManagerImplementor implements IBeanManager {
  private final ReentrantReadWriteLock m_lock;
  private final Map<Class<?>, TypeHierarchy> m_typeHierarchies;
  private IBeanDecorationFactory m_beanDecorationFactory;

  public BeanManagerImplementor() {
    this(null);
  }

  public BeanManagerImplementor(IBeanDecorationFactory f) {
    m_lock = new ReentrantReadWriteLock(true);
    m_typeHierarchies = new HashMap<>();
    m_beanDecorationFactory = f;
  }

  @Override
  public ReentrantReadWriteLock getReadWriteLock() {
    return m_lock;
  }

  @Internal
  protected <T> List<IBean<T>> querySingle(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      @SuppressWarnings("unchecked")
      TypeHierarchy<T> h = m_typeHierarchies.get(beanClazz);
      return (h == null) ? Collections.<IBean<T>> emptyList() : h.querySingle();
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Internal
  protected <T> List<IBean<T>> queryAll(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      @SuppressWarnings("unchecked")
      TypeHierarchy<T> h = m_typeHierarchies.get(beanClazz);
      if (h == null) {
        return Collections.emptyList();
      }
      return h.queryAll();
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected List<Class<?>> listImplementedTypes(IBean<?> bean) {
    //interfaces
    List<Class<?>> classes = BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class);
    //super types
    Class c = bean.getBeanClazz();
    while (c != null && c != Object.class) {
      classes.add(c);
      c = c.getSuperclass();
    }
    classes.add(Object.class);
    return classes;
  }

  @Override
  public <T> IBean<T> registerClass(Class<T> beanClazz) {
    return registerBean(new BeanMetaData(beanClazz));
  }

  @Override
  public <T> void unregisterClass(Class<T> beanClazz) {
    for (IBean<T> bean : getRegisteredBeans(beanClazz)) {
      if (bean.getBeanClazz() == beanClazz) {
        unregisterBean(bean);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IBean<T> registerBean(BeanMetaData beanData) {
    m_lock.writeLock().lock();
    try {
      IBean<T> bean = new BeanImplementor<T>(beanData, this);
      for (Class<?> type : listImplementedTypes(bean)) {
        TypeHierarchy h = m_typeHierarchies.get(type);
        if (h == null) {
          h = new TypeHierarchy(type);
          m_typeHierarchies.put(type, h);
        }
        h.addBean(bean);
      }
      return bean;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void unregisterBean(IBean bean) {
    m_lock.writeLock().lock();
    try {
      Assertions.assertNotNull(bean);
      for (Class<?> type : listImplementedTypes(bean)) {
        TypeHierarchy h = m_typeHierarchies.get(type);
        if (h != null) {
          h.removeBean(bean);
        }
      }
      if (bean instanceof BeanImplementor) {
        ((BeanImplementor) bean).dispose();
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<IBean<T>> getRegisteredBeans(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      TypeHierarchy<T> h = m_typeHierarchies.get(beanClazz);
      if (h == null) {
        return CollectionUtility.emptyArrayList();
      }
      return new ArrayList<IBean<T>>(h.getBeans());
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public <T> IBean<T> getBean(Class<T> beanClazz) {
    List<IBean<T>> list = querySingle(beanClazz);
    if (list.size() == 1) {
      return list.get(0);
    }
    if (list.size() == 0) {
      throw new Assertions.AssertionException("no instances found for query: " + beanClazz + " " + list);
    }
    throw new Assertions.AssertionException("multiple instances found for query: " + beanClazz + " " + list);
  }

  @Override
  public <T> IBean<T> optBean(Class<T> beanClazz) {
    List<IBean<T>> list = querySingle(beanClazz);
    if (list.size() == 1) {
      return list.get(0);
    }
    if (list.size() == 0) {
      return null;
    }
    throw new Assertions.AssertionException("multiple instances found for query: " + beanClazz + " " + list);
  }

  @Override
  public <T> List<IBean<T>> getBeans(Class<T> beanClazz) {
    return queryAll(beanClazz);
  }

  @Internal
  protected void setBeanDecorationFactory(IBeanDecorationFactory f) {
    m_beanDecorationFactory = f;
  }

  @Internal
  protected IBeanDecorationFactory getBeanDecorationFactory() {
    return m_beanDecorationFactory;
  }

  @SuppressWarnings("unchecked")
  public void startCreateImmediatelyBeans() {
    m_lock.readLock().lock();
    try {
      for (IBean bean : getBeans(Object.class)) {
        if (BeanManagerImplementor.isCreateImmediately(bean)) {
          if (BeanManagerImplementor.isApplicationScoped(bean)) {
            bean.getInstance(Object.class);
          }
          else {
            throw new InitializationException(String.format(
                "Bean '%s' is marked with @%s and is not application scoped (@%s) - unexpected configuration! ",
                bean.getBeanClazz(),
                CreateImmediately.class.getSimpleName(),
                ApplicationScoped.class.getSimpleName()
                ));
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
