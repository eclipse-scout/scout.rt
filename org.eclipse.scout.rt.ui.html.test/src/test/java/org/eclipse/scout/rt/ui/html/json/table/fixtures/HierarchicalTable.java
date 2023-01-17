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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("62b6c2d0-0048-43a9-8937-6bed3cae609c")
public class HierarchicalTable extends AbstractTable {

  public ParentKeyColumn getParentKeyColumn() {
    return getColumnSet().getColumnByClass(ParentKeyColumn.class);
  }

  public PrimaryKeyColumn getPrimaryKeyColumn() {
    return getColumnSet().getColumnByClass(PrimaryKeyColumn.class);
  }

  @Order(100)
  @ClassId("298acab6-28b8-4e9a-99cd-ec256f924913")
  public class PrimaryKeyColumn extends AbstractIntegerColumn {
    @Override
    protected boolean getConfiguredPrimaryKey() {
      return true;
    }
  }

  @Order(200)
  @ClassId("82bb67f1-58fc-45ff-823f-201956143017")
  public class ParentKeyColumn extends AbstractIntegerColumn {
    @Override
    protected boolean getConfiguredParentKey() {
      return true;
    }
  }
}
