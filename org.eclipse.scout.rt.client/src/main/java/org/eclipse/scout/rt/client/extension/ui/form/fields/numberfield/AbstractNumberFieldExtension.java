package org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractBasicFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.numberfield.AbstractNumberField;

public abstract class AbstractNumberFieldExtension<NUMBER extends Number, OWNER extends AbstractNumberField<NUMBER>> extends AbstractBasicFieldExtension<NUMBER, OWNER> implements INumberFieldExtension<NUMBER, OWNER> {

  public AbstractNumberFieldExtension(OWNER owner) {
    super(owner);
  }
}
