/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for {@link IOrdered} instances that uses the natural order of {@link IOrdered#getOrder()}.
 *
 * @since 4.1
 */
public class OrderedComparator implements Comparator<IOrdered>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(IOrdered f1, IOrdered f2) {
    if (f1 == f2) {
      return 0;
    }
    if (f1 == null) {
      return -1;
    }
    if (f2 == null) {
      return 1;
    }

    if (f1.getOrder() < f2.getOrder()) {
      return -1;
    }
    if (f1.getOrder() > f2.getOrder()) {
      return 1;
    }

    return f1.getClass().getName().compareTo(f2.getClass().getName());
  }
}
