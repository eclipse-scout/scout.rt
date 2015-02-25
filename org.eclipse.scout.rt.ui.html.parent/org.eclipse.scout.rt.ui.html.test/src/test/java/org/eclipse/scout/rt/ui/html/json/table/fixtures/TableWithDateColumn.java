package org.eclipse.scout.rt.ui.html.json.table.fixtures;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;

public class TableWithDateColumn extends AbstractTable {

  public Column getColumn() {
    return getColumnSet().getColumnByClass(Column.class);
  }

  @Order(10.0)
  public class Column extends AbstractDateColumn {

    @Override
    protected String getConfiguredHeaderText() {
      return "col0";
    }

  }
}
