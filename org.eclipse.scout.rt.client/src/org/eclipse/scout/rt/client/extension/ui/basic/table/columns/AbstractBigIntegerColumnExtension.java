package org.eclipse.scout.rt.client.extension.ui.basic.table.columns;

import java.math.BigInteger;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigIntegerColumn;

public abstract class AbstractBigIntegerColumnExtension<OWNER extends AbstractBigIntegerColumn> extends AbstractNumberColumnExtension<BigInteger, OWNER> implements IBigIntegerColumnExtension<OWNER> {

  public AbstractBigIntegerColumnExtension(OWNER owner) {
    super(owner);
  }
}
