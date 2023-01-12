/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform;

import java.io.Serializable;
import java.util.Comparator;

public final class OrderAnnotationComparator implements Comparator<Object>, Serializable {
  private static final long serialVersionUID = 1L;

  public static final OrderAnnotationComparator INSTANCE = new OrderAnnotationComparator();

  private OrderAnnotationComparator() {
  }

  @Override
  public int compare(Object a, Object b) {
    if (a == b) {
      return 0;
    }
    else if (a == null) {
      return 1;
    }
    else if (b == null) {
      return -1;
    }
    int cmp = Double.compare(getOrderOf(a.getClass()), getOrderOf(b.getClass()));
    if (cmp != 0) {
      return cmp;
    }
    return a.getClass().getName().compareTo(b.getClass().getName());
  }

  public static double getOrderOf(Class<?> c) {
    double d = IBean.DEFAULT_BEAN_ORDER;
    Order orderAnnotation;
    while ((orderAnnotation = c.getAnnotation(Order.class)) == null && c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    if (orderAnnotation != null) {
      d = orderAnnotation.value();
    }
    return d;
  }
}
