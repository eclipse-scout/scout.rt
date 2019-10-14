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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("a8b33afb-5ef8-436e-991b-b47f16d6b70f")
public class TableWithLongColumn extends AbstractTable {

  public Column getColumn() {
    return getColumnSet().getColumnByClass(Column.class);
  }

  @Order(10)
  @ClassId("dcce2693-b378-4678-a67f-c0ab060db0db")
  public class Column extends AbstractLongColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }
  }
}
