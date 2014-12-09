package org.eclipse.scout.rt.client.extension.ui.form.fields.bigintegerfield;

import java.math.BigInteger;

import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.AbstractNumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.AbstractBigIntegerField;

public abstract class AbstractBigIntegerFieldExtension<OWNER extends AbstractBigIntegerField> extends AbstractNumberFieldExtension<BigInteger, OWNER> implements IBigIntegerFieldExtension<OWNER> {

  public AbstractBigIntegerFieldExtension(OWNER owner) {
    super(owner);
  }
}
