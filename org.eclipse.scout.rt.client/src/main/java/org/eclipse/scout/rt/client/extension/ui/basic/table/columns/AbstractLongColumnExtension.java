package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;

public abstract class AbstractLongColumnExtension<OWNER extends AbstractLongColumn> extends AbstractNumberColumnExtension<Long, OWNER> implements ILongColumnExtension<OWNER> {

  public AbstractLongColumnExtension(OWNER owner) {
    super(owner);
  }
}
