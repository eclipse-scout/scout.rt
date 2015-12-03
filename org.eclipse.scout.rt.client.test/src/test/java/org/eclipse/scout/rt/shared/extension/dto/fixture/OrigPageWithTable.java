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
package org.eclipse.scout.rt.shared.extension.dto.fixture;

import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTable.Table;

@PageData(OrigPageWithTableData.class)
public class OrigPageWithTable extends AbstractPageWithTable<Table> {
  public class Table extends AbstractTable {

    public SecondSmartColumn getSecondSmartColumn() {
      return getColumnSet().getColumnByClass(SecondSmartColumn.class);
    }

    public FirstBigDecimalColumn getFirstBigDecimalColumn() {
      return getColumnSet().getColumnByClass(FirstBigDecimalColumn.class);
    }

    @Order(1000)
    public class FirstBigDecimalColumn extends AbstractBigDecimalColumn {
    }

    @Order(2000)
    public class SecondSmartColumn extends AbstractSmartColumn<Long> {
    }
  }
}
