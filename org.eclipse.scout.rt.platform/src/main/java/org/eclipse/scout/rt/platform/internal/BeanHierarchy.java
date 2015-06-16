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

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanScopeEvaluator;

/**
 * This class is not thread safe
 */
public class BeanHierarchy<T> {
  private final Class<T> m_clazz;
  private final Set<IBean<T>> m_beans;
  private final Map<Object, List<IBean<T>>> m_singleQueryByScope;
  private final Map<Object, List<IBean<T>>> m_allQueryByScope;
  private final ReadWriteLock m_queryCacheLock;

  public BeanHierarchy(Class<T> clazz) {
    m_clazz = clazz;
    m_beans = new HashSet<>();
    m_singleQueryByScope = new HashMap<>();
    m_allQueryByScope = new HashMap<>();
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
   *         that represent the type of this hierarchy.
   *         This means when B replaces A using {@link Replace} then A is not part of the result. Also if C extends A
   *         without {@link Replace} then C is not part of the result.
   */
  public List<IBean<T>> querySingle(IBeanScopeEvaluator evaluator) {
    return query(evaluator, true);
  }

  /**
   * @return all beans in this hierarchy - filtered and ordered by {@link Order} and {@link Replace} - that represent
   *         the
   *         type of this hierarchy.
   *         This means when B replaces A using {@link Replace} then A is not part of the result. But if C extends A
   *         without {@link Replace} then C is part of the result.
   */
  public List<IBean<T>> queryAll(IBeanScopeEvaluator evaluator) {
    return query(evaluator, false);
  }

  protected Object getCurrentScope(IBeanScopeEvaluator evaluator) {
    if (evaluator == null) {
      return null;
    }
    return evaluator.getCurrentScope();
  }

  protected void invalidate() {
    m_queryCacheLock.writeLock().lock();
    try {
      m_singleQueryByScope.clear();
      m_allQueryByScope.clear();
    }
    finally {
      m_queryCacheLock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  protected List<IBean<T>> query(IBeanScopeEvaluator evaluator, boolean querySingle) {
    Object scope = getCurrentScope(evaluator);

    List<IBean<T>> singleList;
    List<IBean<T>> allList;

    m_queryCacheLock.readLock().lock();
    try {
      singleList = m_singleQueryByScope.get(scope);
      allList = m_allQueryByScope.get(scope);
    }
    finally {
      m_queryCacheLock.readLock().unlock();
    }

    if (singleList == null || allList == null) {
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
        // TODO[aho] remove filtering
        //filter beans not matching scope
//        if (evaluator != null) {
//          list = evaluator.filter(list, scope);
//        }

        //manage replaced beans
        HashMap<Class<?>, Class<?>> extendsMap = new HashMap<>();//key is replaced by value
        for (IBean<T> bean : list) {
          if (bean.getBeanAnnotation(Replace.class) != null) {
            Class<?> superClazz = bean.getBeanClazz().getSuperclass();
            if (superClazz != null && !superClazz.isInterface() && !Modifier.isAbstract(superClazz.getModifiers())) {
              //only add if first to override, respects @Order annotation
              if (!extendsMap.containsKey(superClazz)) {
                extendsMap.put(superClazz, bean.getBeanClazz());
              }
            }
          }
        }
        //find most specific version of @Replaced class if this hierarchy is not based on an interface
        Class<T> refClazz = m_clazz;
        if (!refClazz.isInterface()) {
          while (extendsMap.containsKey(refClazz)) {
            refClazz = (Class<T>) extendsMap.get(refClazz);
          }
        }
        //remove replaced beans
        for (Iterator<IBean<T>> it = list.iterator(); it.hasNext();) {
          if (extendsMap.containsKey(it.next().getBeanClazz())) {
            it.remove();
          }
        }

        allList = Collections.unmodifiableList(new ArrayList<IBean<T>>(list));
        m_allQueryByScope.put(scope, allList);

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
        //only retain lowest order and if lowest order is same for multiple beans, keep them all, provocating a mutliple instance exception on getBean()
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
        singleList = Collections.unmodifiableList(new ArrayList<IBean<T>>(list));
        m_singleQueryByScope.put(scope, singleList);
      }
      finally {
        m_queryCacheLock.writeLock().unlock();
      }
    }

    if (querySingle) {
      return singleList;
    }
    return allList;
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
    return 0;
  }

}
