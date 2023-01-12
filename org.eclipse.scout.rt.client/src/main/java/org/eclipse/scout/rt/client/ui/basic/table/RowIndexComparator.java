/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
