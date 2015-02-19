package org.eclipse.scout.rt.client.extension.ui.basic.table;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractSortOrderColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface ISortOrderColumnExtension<OWNER extends AbstractSortOrderColumn> extends IColumnExtension<IColumn, OWNER> {
}
