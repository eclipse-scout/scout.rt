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
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;

public class TableWith3Cols extends AbstractTable {

  @Order(10.0)
  public class Col0Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }

  }

  @Order(20.0)
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }

  }

  @Order(30.0)
  public class Col2Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col2";
    }

  }

  public void fill(int rowCount) throws ProcessingException {
    Object[][] rows = new Object[rowCount][3];
    for (int i = 0; i < rowCount; i++) {
      rows[i][0] = new Object[]{"cell" + i + "_0"};
      rows[i][1] = new Object[]{"cell" + i + "_1"};
      rows[i][2] = new Object[]{"cell" + i + "_2"};
    }
    replaceRowsByArray(rows);
  }
}
