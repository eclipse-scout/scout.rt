/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.io.Serializable;
import java.util.Comparator;

public class RowIndexComparator implements Comparator<ITableRow>, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public int compare(ITableRow row1, ITableRow row2) {
    int x1 = row1.getRowIndex();
    int x2 = row2.getRowIndex();
    return Integer.compare(x1, x2);
  }
}
