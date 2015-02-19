package org.eclipse.scout.rt.client.extension.ui.form.fields.integerfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.numberfield.AbstractNumberFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;

public abstract class AbstractIntegerFieldExtension<OWNER extends AbstractIntegerField> extends AbstractNumberFieldExtension<Integer, OWNER> implements IIntegerFieldExtension<OWNER> {

  public AbstractIntegerFieldExtension(OWNER owner) {
    super(owner);
  }
}
