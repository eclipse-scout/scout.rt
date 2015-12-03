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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Comparator;

public class RowIndexComparator implements Comparator<ITableRow> {

  @Override
  public int compare(ITableRow row1, ITableRow row2) {
    int x1 = row1.getRowIndex();
    int x2 = row2.getRowIndex();
    if (x1 < x2) {
      return -1;
    }
    else if (x1 > x2) {
      return 1;
    }
    else {
      return 0;
    }
  }

}
