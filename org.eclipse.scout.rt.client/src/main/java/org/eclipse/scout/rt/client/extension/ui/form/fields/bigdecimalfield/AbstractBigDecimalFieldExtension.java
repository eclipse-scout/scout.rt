package org.eclipse.scout.rt.client.extension.ui.form.fields.bigdecimalfield;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.extension.ui.form.fields.decimalfield.AbstractDecimalFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;

public abstract class AbstractBigDecimalFieldExtension<OWNER extends AbstractBigDecimalField> extends AbstractDecimalFieldExtension<BigDecimal, OWNER> implements IBigDecimalFieldExtension<OWNER> {

  public AbstractBigDecimalFieldExtension(OWNER owner) {
    super(owner);
  }
}
