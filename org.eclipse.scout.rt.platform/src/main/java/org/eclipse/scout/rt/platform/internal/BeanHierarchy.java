/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

/**
 * This class is not thread safe
 */
public class BeanHierarchy<T> {

  private final Class<T> m_clazz;
  private final Set<IBean<T>> m_beans;
  private final ReadWriteLock m_queryCacheLock;

  private List<IBean<T>> m_single;
  private List<IBean<T>> m_all;

  public BeanHierarchy(Class<T> clazz) {
    m_clazz = clazz;
    m_beans = new HashSet<>();
    m_queryCacheLock = new ReentrantReadWriteLock();
  }

  public Class<T> getClazz() {
    return m_clazz;
  }

  /**
   * @return all beans in this hierarchy regardless of {@link Order} and {@link Replace}
   */
  public Set<IBean<T>> getBeans() {
    return m_beans;
  }

  public void addBean(IBean<T> bean) {
    invalidate();
    m_beans.add(bean);
  }

  public void removeBean(IBean<T> bean) {
    invalidate();
    m_beans.remove(bean);
  }

  /**
   * @return the most significant bean in this hierarchy - filtered and ordered by {@link Order} and {@link Replace} -
   *         that represent the type of this hierarchy. This means when B replaces A using {@link Replace} then A is not
   *         part of the result. Also if C extends A without {@link Replace} then C is not part of the result.
   */
  public List<IBean<T>> querySingle() {
    return query(true);
  }

  /**
   * @return all beans in this hierarchy - filtered and ordered by {@link Order} and {@link Replace} - that represent
   *         the type of this hierarchy. This means when B replaces A using {@link Replace} then A is not part of the
   *         result. But if C extends A without {@link Replace} then C is part of the result.
   */
  public List<IBean<T>> queryAll() {
    return query(false);
  }

  protected void invalidate() {
    m_queryCacheLock.writeLock().lock();
    try {
      m_single = null;
      m_all = null;
    }
    finally {
      m_queryCacheLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  protected List<IBean<T>> query(boolean querySingle) {

    boolean isInitialized = false;
    m_queryCacheLock.readLock().lock();
    try {
      isInitialized = m_single != null && m_all != null;
    }
    finally {
      m_queryCacheLock.readLock().unlock();
    }

    if (!isInitialized) {
      m_queryCacheLock.writeLock().lock();
      try {
        List<IBean<T>> list = new ArrayList<>(m_beans);
        //sort by Order ascending
        Collections.sort(list, ORDER_COMPARATOR);
        //remove duplicate classes
        Class<?> lastSeen = null;
        for (Iterator<IBean<T>> it = list.iterator(); it.hasNext();) {
          IBean<T> bean = it.next();
          if (bean.getBeanClazz() == lastSeen) {
            it.remove();
          }
          lastSeen = bean.getBeanClazz();
        }

        //manage replaced beans
        Map<Class<?>, Class<?>> extendsMap = new HashMap<>();//key is replaced by value
        for (IBean<T> bean : list) {
          if (bean.getBeanAnnotation(Replace.class) != null) {
            Class<?> superClazz = null;
            if (bean.getBeanClazz().isInterface()) {
              //interface replaces interfaces, only replace FIRST declared interface
              Class[] ifs = bean.getBeanClazz().getInterfaces();
              if (ifs != null && ifs.length > 0) {
                superClazz = ifs[0];
              }
            }
            else {
              //class replaces class
              Class<?> s = bean.getBeanClazz().getSuperclass();
              if (s != null && !s.isInterface() && !Modifier.isAbstract(s.getModifiers())) {
                superClazz = s;
              }
            }
            if (superClazz != null) {
              //only add if first to override, respects @Order annotation
              if (!extendsMap.containsKey(superClazz)) {
                extendsMap.put(superClazz, bean.getBeanClazz());
              }
            }
          }
        }
        //find most specific version of @Replaced class
        Class<T> refClazz = m_clazz;
        while (extendsMap.containsKey(refClazz)) {
          refClazz = (Class<T>) extendsMap.get(refClazz);
        }
        //remove replaced beans
        for (Iterator<IBean<T>> it = list.iterator(); it.hasNext();) {
          if (extendsMap.containsKey(it.next().getBeanClazz())) {
            it.remove();
          }
        }

        m_all = Collections.unmodifiableList(new ArrayList<IBean<T>>(list));

        //now retain only beans that are exactly of type refClazz, but only if refClass is not an interface
        if (!refClazz.isInterface()) {
          for (Iterator<IBean<T>> it = list.iterator(); it.hasNext();) {
            IBean<T> bean = it.next();
            if (bean.getBeanClazz().isInterface()) {
              continue;
            }
            if (bean.getBeanClazz() == refClazz) {
              continue;
            }
            it.remove();
          }
        }
        //only retain lowest order and if lowest order is same for multiple beans, keep them all, provocating a multiple instance exception on getBean()
        if (list.size() > 1) {
          Double lowestOrder = null;
          for (Iterator<IBean<T>> it = list.iterator(); it.hasNext();) {
            IBean<T> bean = it.next();
            if (lowestOrder == null || orderOf(bean) == lowestOrder.doubleValue()) {
              //keep it
              if (lowestOrder == null) {
                lowestOrder = orderOf(bean);
              }
              continue;
            }
            it.remove();
          }
        }
        m_single = Collections.unmodifiableList(new ArrayList<IBean<T>>(list));
      }
      finally {
        m_queryCacheLock.writeLock().unlock();
      }
    }

    if (querySingle) {
      return m_single;
    }
    return m_all;
  }

  private static final Comparator<IBean<?>> ORDER_COMPARATOR = new Comparator<IBean<?>>() {
    @Override
    public int compare(IBean<?> o1, IBean<?> o2) {
      int cmp = Double.compare(orderOf(o1), orderOf(o2));
      if (cmp != 0) {
        return cmp;
      }
      return o1.getBeanClazz().getName().compareTo(o2.getBeanClazz().getName());
    }
  };

  public static double orderOf(IBean<?> b) {
    Order orderAnnotation = b.getBeanAnnotation(Order.class);
    if (orderAnnotation != null) {
      return orderAnnotation.value();
    }
    return IBean.DEFAULT_BEAN_ORDER;
  }
}
