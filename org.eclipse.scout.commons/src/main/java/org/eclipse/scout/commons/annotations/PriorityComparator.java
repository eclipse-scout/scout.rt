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

import java.lang.reflect.Proxy;
import java.util.Comparator;

/**
 * sort objects in descending priority (highest value first)
 */
public class PriorityComparator implements Comparator<Object> {

  @Override
  public int compare(Object a, Object b) {
    if (a == b) {
      return 0;
    }
    if (a == null && b == null) {
      return 0;
    }
    if (a == null) {
      return -1;
    }
    if (b == null) {
      return 1;
    }
    double pa = getPriorityOf(a);
    double pb = getPriorityOf(b);
    if (pa > pb) {
      return -1;
    }
    if (pa < pb) {
      return 1;
    }
    return a.getClass().getCanonicalName().compareTo(b.getClass().getCanonicalName());
  }

  public static double getPriorityOf(Object o) {
    if (o == null) {
      return 0;
    }
    double d;
    Priority prio = o.getClass().getAnnotation(Priority.class);
    if (prio != null) {
      d = prio.value();
    }
    else if (Proxy.isProxyClass(o.getClass())) {
      d = -1;
    }
    else {
      d = 0;
    }
    return d;
  }

}
