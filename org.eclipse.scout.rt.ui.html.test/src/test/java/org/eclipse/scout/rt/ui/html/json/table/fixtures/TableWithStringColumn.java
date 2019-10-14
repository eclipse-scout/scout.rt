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

@ClassId("f09e963b-228c-45df-b950-081efe877d48")
public class TableWithStringColumn extends AbstractTable {

  public Column getColumn() {
    return getColumnSet().getColumnByClass(Column.class);
  }

  @Order(10)
  @ClassId("7fca3b1b-ce76-49c0-b45f-e185b312e3cc")
  public class Column extends AbstractStringColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }
  }
}
