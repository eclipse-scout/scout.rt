package org.eclipse.scout.rt.client.ui.basic.table.control;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface IMapTableControl extends ITableControl {
  public static final String PROP_COLUMNS = "columns";

  List<? extends IColumn<?>> getColumns();
}
