package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;

public abstract class AbstractObjectColumnExtension<OWNER extends AbstractObjectColumn> extends AbstractColumnExtension<Object, OWNER> implements IObjectColumnExtension<OWNER> {

  public AbstractObjectColumnExtension(OWNER owner) {
    super(owner);
  }
}
