/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
