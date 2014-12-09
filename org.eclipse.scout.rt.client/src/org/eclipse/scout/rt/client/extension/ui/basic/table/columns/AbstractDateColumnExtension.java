package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;

public abstract class AbstractDateColumnExtension<OWNER extends AbstractDateColumn> extends AbstractColumnExtension<Date, OWNER> implements IDateColumnExtension<OWNER> {

  public AbstractDateColumnExtension(OWNER owner) {
    super(owner);
  }
}
