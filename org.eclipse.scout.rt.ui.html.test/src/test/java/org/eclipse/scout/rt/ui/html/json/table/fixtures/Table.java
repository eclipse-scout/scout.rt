/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("bd1f7a77-54ad-4b8f-b5c1-0f38b32229ca")
public class Table extends AbstractTable {

  @Order(10)
  @ClassId("762be9bb-cc50-4151-90c0-ed5d26c985df")
  public class Col1Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col1";
    }

  }

  public void fill(int rowCount) {
    Object[][] rows = new Object[rowCount][1];
    for (int i = 0; i < rowCount; i++) {
      rows[i][0] = new Object[]{"cell" + i};
    }
    replaceRowsByArray(rows);
  }
}
