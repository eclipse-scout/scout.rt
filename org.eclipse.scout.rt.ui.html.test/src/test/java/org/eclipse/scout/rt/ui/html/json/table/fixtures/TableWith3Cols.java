/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

public class TableWith3Cols extends AbstractTable {

  @Order(10)
  public class Col0Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }
  }

  @Order(20)
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }
  }

  @Order(30)
  public class Col2Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col2";
    }
  }

  public void fill(int rowCount) {
    fill(rowCount, true);
  }

  public void fill(int rowCount, boolean replace) {
    Object[][] rows = new Object[rowCount][3];
    int offset = (replace ? 0 : getRowCount());
    for (int i = 0; i < rowCount; i++) {
      rows[i][0] = "CELL{row" + (i + offset) + ",col0}";
      rows[i][1] = "CELL{row" + (i + offset) + ",col1}";
      rows[i][2] = "CELL{row" + (i + offset) + ",col2}";
    }
    if (replace) {
      replaceRowsByMatrix(rows);
    }
    else {
      addRowsByMatrix(rows);
    }
  }
}
