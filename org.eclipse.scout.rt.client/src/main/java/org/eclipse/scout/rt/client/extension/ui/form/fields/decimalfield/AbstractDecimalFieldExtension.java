package org.eclipse.scout.rt.client.extension.ui.form.fields.decimalfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.AbstractNumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.decimalfield.AbstractDecimalField;

public abstract class AbstractDecimalFieldExtension<T extends Number, OWNER extends AbstractDecimalField<T>> extends AbstractNumberFieldExtension<T, OWNER> implements IDecimalFieldExtension<T, OWNER> {

  public AbstractDecimalFieldExtension(OWNER owner) {
    super(owner);
  }
}
