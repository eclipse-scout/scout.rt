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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.platform.IBean;

/**
 * This class is not thread safe
 */
public class TypeHierarchy<T> {
  private final Class<T> m_clazz;
  private final Set<IBean<T>> m_beans = new HashSet<>();
  private List<IBean<T>> m_querySingle;
  private List<IBean<T>> m_queryAll;

  public TypeHierarchy(Class<T> clazz) {
    m_clazz = clazz;
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
  public List<IBean<T>> querySingle() {
    validate();
    return m_querySingle;
  }

  /**
   * @return all beans in this hierarchy - filtered and ordered by {@link Order} and {@link Replace} - that represent
   *         the
   *         type of this hierarchy.
   *         This means when B replaces A using {@link Replace} then A is not part of the result. But if C extends A
   *         without {@link Replace} then C is part of the result.
   */
  public List<IBean<T>> queryAll() {
    validate();
    return m_queryAll;
  }

  protected void invalidate() {
    m_querySingle = null;
    m_queryAll = null;
  }

  protected void validate() {
    if (m_querySingle == null) {
      ArrayList<IBean<T>> list = new ArrayList<>(m_beans);
      for (IBean<T> bean : m_beans) {
        if (bean.getBeanAnnotation(Replace.class) != null) {
          Class<?> superClazz = bean.getBeanClazz().getSuperclass();
          if (superClazz != null && !superClazz.isInterface() && !Modifier.isAbstract(superClazz.getModifiers())) {
            list.remove(superClazz);
          }
        }
        else {
          Class<?> clazz = bean.getBeanClazz();
          if (!clazz.isInterface() && clazz != m_clazz) {
            list.remove(clazz);
          }
        }
      }
      Collections.sort(list, OBJ_GET_COMPARATOR);
      m_querySingle = Collections.unmodifiableList(list);
    }
    if (m_queryAll == null) {
      ArrayList<IBean<T>> list = new ArrayList<>(m_beans);
      for (IBean<T> bean : m_beans) {
        if (bean.getBeanAnnotation(Replace.class) != null) {
          Class<?> superClazz = bean.getBeanClazz().getSuperclass();
          if (superClazz != null && !superClazz.isInterface() && !Modifier.isAbstract(superClazz.getModifiers())) {
            list.remove(superClazz);
          }
        }
      }
      Collections.sort(list, OBJ_ALL_COMPARATOR);
      m_queryAll = Collections.unmodifiableList(list);
    }
  }

  private static final Comparator<IBean<?>> OBJ_GET_COMPARATOR = new Comparator<IBean<?>>() {
    @Override
    public int compare(IBean<?> o1, IBean<?> o2) {
      int cmp = orderOf(o1).compareTo(orderOf(o2));
      if (cmp != 0) {
        return cmp;
      }
      return o1.getBeanClazz().getName().compareTo(o2.getBeanClazz().getName());
    }
  };

  private static final Comparator<IBean> OBJ_ALL_COMPARATOR = new Comparator<IBean>() {
    @Override
    public int compare(IBean o1, IBean o2) {
      return 0;
    }
  };

  private static Double orderOf(IBean<?> b) {
    double o = 0;
    Priority priorityAnnotation = b.getBeanAnnotation(Priority.class);
    if (priorityAnnotation != null) {
      o = -priorityAnnotation.value();
    }
    Order orderAnnotation = b.getBeanAnnotation(Order.class);
    if (orderAnnotation != null) {
      o = orderAnnotation.value();
    }
    return o;
  }

}
