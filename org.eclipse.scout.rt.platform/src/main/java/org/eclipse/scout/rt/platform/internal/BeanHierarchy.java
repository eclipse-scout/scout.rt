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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is not thread safe
 */
public class BeanHierarchy<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BeanHierarchy.class);

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
              throw new IllegalArgumentException('@' + Replace.class.getSimpleName() + " annotation not supported on interface: " + bean + '.');
            }
            else {
              //class replaces class
              Class<?> s = bean.getBeanClazz().getSuperclass();
              if (s != null) {
                if (Modifier.isAbstract(s.getModifiers())) {
                  LOG.warn("Cannot replace an abstract super class: {}. Delete this @{} annotation.", bean, Replace.class.getSimpleName());
                }
                else {
                  superClazz = s;
                }
              }
            }
            if (superClazz != null && !extendsMap.containsKey(superClazz)) {
              //only add if first to override, respects @Order annotation
              extendsMap.put(superClazz, bean.getBeanClazz());
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

        if (list.isEmpty()) {
          m_all = Collections.emptyList();
          m_single = Collections.emptyList();
        }
        else {
          m_all = Collections.unmodifiableList(new ArrayList<IBean<T>>(list));

          IBean<T> exactBean = getExactBean(list, refClazz);
          if (exactBean != null) {
            // we have an exact match: use it
            m_single = Collections.singletonList(exactBean);
          }
          else if (!refClazz.isInterface() && !Modifier.isAbstract(refClazz.getModifiers())) {
            // we queried an specific class (no interface, no abstract class): only exact beans are allowed but we don't have one.
            m_single = Collections.emptyList();
          }
          else if (list.size() == 1) {
            m_single = Collections.singletonList(list.get(0));
          }
          else {
            //only retain lowest order and if lowest order is same for multiple beans, keep them all, provocating a multiple instance exception on querySingle
            List<IBean<T>> lowestOrderBeans = new ArrayList<>(list.size());
            Iterator<IBean<T>> iterator = list.iterator();

            // first bean
            IBean<T> curBean = iterator.next();
            double lowestOrder = orderOf(curBean);
            lowestOrderBeans.add(curBean);

            // all others having the same order
            while (iterator.hasNext() && orderOf(curBean = iterator.next()) == lowestOrder) {
              lowestOrderBeans.add(curBean);
            }
            m_single = Collections.unmodifiableList(new ArrayList<IBean<T>>(lowestOrderBeans));
          }
        }
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

  protected static <T> IBean<T> getExactBean(List<IBean<T>> list, Class<?> c) {
    for (IBean<T> bean : list) {
      if (bean.getBeanClazz() == c) {
        return bean;
      }
    }
    return null; // no exact match found
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
