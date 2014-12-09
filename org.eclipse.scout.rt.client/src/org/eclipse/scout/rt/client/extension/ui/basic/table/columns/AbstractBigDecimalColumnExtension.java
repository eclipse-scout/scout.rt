package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;

public abstract class AbstractBigDecimalColumnExtension<OWNER extends AbstractBigDecimalColumn> extends AbstractDecimalColumnExtension<BigDecimal, OWNER> implements IBigDecimalColumnExtension<OWNER> {

  public AbstractBigDecimalColumnExtension(OWNER owner) {
    super(owner);
  }
}
