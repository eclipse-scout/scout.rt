package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;

public abstract class AbstractBooleanColumnExtension<OWNER extends AbstractBooleanColumn> extends AbstractColumnExtension<Boolean, OWNER> implements IBooleanColumnExtension<OWNER> {

  public AbstractBooleanColumnExtension(OWNER owner) {
    super(owner);
  }
}
