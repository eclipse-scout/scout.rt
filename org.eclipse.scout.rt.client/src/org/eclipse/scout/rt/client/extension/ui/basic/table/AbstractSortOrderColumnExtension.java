package org.eclipse.scout.rt.client.extension.ui.basic.table;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.AbstractColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractSortOrderColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public abstract class AbstractSortOrderColumnExtension<OWNER extends AbstractSortOrderColumn> extends AbstractColumnExtension<IColumn, OWNER> implements ISortOrderColumnExtension<OWNER> {

  public AbstractSortOrderColumnExtension(OWNER owner) {
    super(owner);
  }
}
