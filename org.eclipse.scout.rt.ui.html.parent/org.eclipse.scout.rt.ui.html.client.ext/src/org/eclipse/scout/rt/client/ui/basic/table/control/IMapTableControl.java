package org.eclipse.scout.rt.client.ui.basic.table.control;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface IMapTableControl extends ITableControl {
  String PROP_COLUMNS = "columns";

  List<? extends IColumn<?>> getColumns();
}
