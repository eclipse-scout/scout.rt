/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("0287e077-2b8a-426e-a753-bf86a7e08733")
public class TableWith3Cols extends AbstractTable {

  @Order(10)
  @ClassId("e429eed5-0b8b-4152-8801-c4dc50d77631")
  public class Col0Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }
  }

  @Order(20)
  @ClassId("6cef0c34-8aff-4f44-9d90-ec6a04b239ce")
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }
  }

  @Order(30)
  @ClassId("0519af62-a019-45c4-ac19-e85f6378b005")
  public class Col2Column extends AbstractStringColumn {

    @Override
    public void initColumn() {
      // this column is first configured visible and set visible during init on purpose
      // careful: calling resetColumns on table would set it invisible again
      setVisible(true);
    }

    @Override
    protected boolean getConfiguredVisible() {
      return false;
    }

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
