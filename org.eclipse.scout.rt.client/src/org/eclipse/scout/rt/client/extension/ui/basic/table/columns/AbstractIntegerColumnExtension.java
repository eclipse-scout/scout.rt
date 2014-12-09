package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;

public abstract class AbstractIntegerColumnExtension<OWNER extends AbstractIntegerColumn> extends AbstractNumberColumnExtension<Integer, OWNER> implements IIntegerColumnExtension<OWNER> {

  public AbstractIntegerColumnExtension(OWNER owner) {
    super(owner);
  }
}
