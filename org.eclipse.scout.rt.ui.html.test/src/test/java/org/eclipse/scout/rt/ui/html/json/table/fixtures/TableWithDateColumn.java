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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("ea8ed53d-ebd4-4163-9882-a64dd0188fca")
public class TableWithDateColumn extends AbstractTable {

  public Column getColumn() {
    return getColumnSet().getColumnByClass(Column.class);
  }

  @Order(10)
  @ClassId("8bcae097-ea0b-426d-9e7a-b634cea0719f")
  public class Column extends AbstractDateColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }
  }
}
