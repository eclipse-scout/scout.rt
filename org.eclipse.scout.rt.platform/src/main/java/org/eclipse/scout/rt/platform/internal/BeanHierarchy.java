/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * This class is not thread safe
 */
public class BeanHierarchy<T> {

  private final Class<T> m_clazz;
  private final List<IBean<T>> m_beans;

  /**
   * Redundant, derived by {@link #m_beans}
   */
  private List<IBean<T>> m_single;
  /**
   * Redundant, derived by {@link #m_beans}
   */
  private List<IBean<T>> m_all;

  public BeanHierarchy(Class<T> clazz) {
    m_clazz = clazz;
    m_beans = new ArrayList<>();
  }

  public Class<T> getClazz() {
    return m_clazz;
  }

  /**
   * @return all beans in this hierarchy regardless of {@link Order} and {@link Replace}
   */
  public List<IBean<T>> getBeans() {
    return m_beans;
  }

  /**
   * @return the exact matching {@link IBean} for specified {@code beanClazz}. The {@link IBean} is returned even if the
   *         {@code beanClazz} was replaced by another bean implementation or is not the most specific bean for the
   *         specified {@code beanClazz}. Returns {@code null} if no bean is available for the specified
   *         {@code beanClazz}.
   */
  public IBean<T> getExactBean(Class<?> beanClazz) {
    List<IBean<T>> beans = CollectionUtility.arrayList(m_beans);
    beans.sort(ORDER_COMPARATOR);
    return getExactBean(beans, beanClazz);
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
    m_single = null;
    m_all = null;
  }

  /**
   * @return A copy of m_beans with duplicate beans removed sorted first by {@link #ORDER_COMPARATOR} and second by
   *         bean-insertion-order.
   */
  protected List<IBean<T>> sortedBeanCopy() {
    ArrayList<IBean<T>> sorted = new ArrayList<>(m_beans);

    // m_beans contains the beans in insertion order.
    // In case there are duplicates (with the same order) the one last added should win.
    // Therefore the list must be reversed so that the "newest" beans come first.
    // As the following sort is stable, "newer" beans (having the same order) will stay first.
    Collections.reverse(sorted);

    // sort by Order ascending (lowest order first)
    sorted.sort(ORDER_COMPARATOR);

    // Remove duplicate registered classes, keep only bean with lowest order
    Set<Class<?>> seenBeans = new HashSet<>(sorted.size());
    sorted.removeIf(bean -> !seenBeans.add(bean.getBeanClazz()));

    return sorted;
  }

  protected List<IBean<T>> query(boolean querySingle) {
    if (m_single == null || m_all == null) {
      initialize();
    }
    if (querySingle) {
      return m_single;
    }
    return m_all;
  }

  @SuppressWarnings({"unchecked", "squid:S1244" /* Floating point numbers should not be tested for equality */})
  protected void initialize() {
    List<IBean<T>> list = sortedBeanCopy();
    //manage replaced beans
    final Map<Class<?>, IBean<?>> extendsMap = new HashMap<>();//key is replaced by value
    for (IBean<T> bean : list) {
      if (bean.hasAnnotation(Replace.class)) {
        Assertions.assertFalse(bean.getBeanClazz().isInterface(), "@{} annotation not supported on interface: {}.", Replace.class.getSimpleName(), bean);
        Class<?> superClazz = bean.getBeanClazz().getSuperclass();
        Assertions.assertNotNull(superClazz, "@{} annotation not supported for bean '{}' because it has no super class.", Replace.class.getSimpleName(), bean);
        Assertions.assertNotEquals(Object.class, superClazz, "@{} annotation not supported for bean '{}' because it has no super class.", Replace.class.getSimpleName(), bean);
        Assertions.assertFalse(Modifier.isAbstract(superClazz.getModifiers()), "Cannot replace an abstract super class: {}. Delete this @{} annotation.", bean, Replace.class.getSimpleName());

        IBean<?> existingBean = extendsMap.get(superClazz);
        if (existingBean == null) {
          //only add if first to override, respects @Order annotation
          extendsMap.put(superClazz, bean);
        }
        else {
          // there is no calculation performed on bean orders (typically these are literals).
          // therefore we accept direct equality check without epsilon.
          Assertions.assertFalse(orderOf(existingBean) == orderOf(bean),
              "Bean '{}' and '{}' replace the same super class and have identical orders. No unique result possible.",
              existingBean.getBeanClazz().getName(), bean.getBeanClazz().getName());
        }
      }
    }

    //find most specific version of @Replaced class
    Class<T> refClazz = m_clazz;
    while (extendsMap.containsKey(refClazz)) {
      refClazz = (Class<T>) extendsMap.get(refClazz).getBeanClazz();
    }

    //remove replaced beans
    list.removeIf(tiBean -> extendsMap.containsKey(tiBean.getBeanClazz()));

    if (list.isEmpty()) {
      m_all = Collections.emptyList();
      m_single = Collections.emptyList();
    }
    else {
      m_all = Collections.unmodifiableList(new ArrayList<>(list));

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
        m_single = Collections.unmodifiableList(new ArrayList<>(lowestOrderBeans));
      }
    }
  }

  /**
   * @return the exact matching {@link IBean} for the specified {@code beanClazz} according to {@code Order}. The
   *         {@link IBean} is returned even if the {@code beanClazz} was replaced by another bean implementation or is
   *         not the most specific bean for the specified {@code beanClazz}. Returns {@code null} if no bean is
   *         available for the specified {@code beanClazz}.
   *         <p>
   *         <b>The list of beans is expected to be sorted using {@link #ORDER_COMPARATOR}.</b>
   */
  protected static <T> IBean<T> getExactBean(List<IBean<T>> list, Class<?> beanClazz) {
    for (IBean<T> bean : list) {
      if (bean.getBeanClazz() == beanClazz) {
        return bean;
      }
    }
    return null; // no exact match found
  }

  private static final Comparator<IBean<?>> ORDER_COMPARATOR = Comparator
      .<IBean<?>> comparingDouble(BeanHierarchy::orderOf)
      .thenComparing(o -> o.getBeanClazz().getName());

  public static double orderOf(IBean<?> b) {
    Order orderAnnotation = b.getBeanAnnotation(Order.class);
    if (orderAnnotation != null) {
      return orderAnnotation.value();
    }
    return IBean.DEFAULT_BEAN_ORDER;
  }
}
