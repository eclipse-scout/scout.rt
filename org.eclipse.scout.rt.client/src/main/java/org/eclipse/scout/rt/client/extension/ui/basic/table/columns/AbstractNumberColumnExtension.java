package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractNumberColumn;

public abstract class AbstractNumberColumnExtension<NUMBER extends Number, OWNER extends AbstractNumberColumn<NUMBER>> extends AbstractColumnExtension<NUMBER, OWNER> implements INumberColumnExtension<NUMBER, OWNER> {

  public AbstractNumberColumnExtension(OWNER owner) {
    super(owner);
  }
}
