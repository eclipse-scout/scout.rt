package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDoubleColumn;

public abstract class AbstractDoubleColumnExtension<OWNER extends AbstractDoubleColumn> extends AbstractDecimalColumnExtension<Double, OWNER> implements IDoubleColumnExtension<OWNER> {

  public AbstractDoubleColumnExtension(OWNER owner) {
    super(owner);
  }
}
