package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDecimalColumn;

public abstract class AbstractDecimalColumnExtension<NUMBER extends Number, OWNER extends AbstractDecimalColumn<NUMBER>> extends AbstractNumberColumnExtension<NUMBER, OWNER> implements IDecimalColumnExtension<NUMBER, OWNER> {

  public AbstractDecimalColumnExtension(OWNER owner) {
    super(owner);
  }
}
