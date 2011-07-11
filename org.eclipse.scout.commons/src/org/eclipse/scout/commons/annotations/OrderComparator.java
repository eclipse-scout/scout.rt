/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.annotations;

import java.util.Comparator;

/**
 * sort objects in ascending order (lowest value first)
 */
public class OrderComparator implements Comparator<Object> {

  @Override
  public int compare(Object a, Object b) {
    if (a == b) return 0;
    if (a == null && b == null) return 0;
    if (a == null) return -1;
    if (b == null) return 1;
    double pa = getOrderOf(a);
    double pb = getOrderOf(b);
    if (pa > pb) {
      return -1;
    }
    if (pa < pb) {
      return 1;
    }
    return a.getClass().getCanonicalName().compareTo(b.getClass().getCanonicalName());
  }

  public static double getOrderOf(Object o) {
    if (o == null) return 0;
    double d = 0;
    Order order = o.getClass().getAnnotation(Order.class);
    if (order != null) {
      d = order.value();
    }
    return d;
  }

}
