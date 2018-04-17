/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.platform.Order;

public class HierarchicalTable extends AbstractTable {

  public ParentKeyColumn getParentKeyColumn() {
    return getColumnSet().getColumnByClass(ParentKeyColumn.class);
  }

  public PrimaryKeyColumn getPrimaryKeyColumn() {
    return getColumnSet().getColumnByClass(PrimaryKeyColumn.class);
  }

  @Order(100)
  public class PrimaryKeyColumn extends AbstractIntegerColumn {
    @Override
    protected boolean getConfiguredPrimaryKey() {
      return true;
    }
  }

  @Order(200)
  public class ParentKeyColumn extends AbstractIntegerColumn {
    @Override
    protected boolean getConfiguredParentKey() {
      return true;
    }
  }
}
