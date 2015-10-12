package org.eclipse.scout.rt.client.extension.ui.form.fields.booleanfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;

public abstract class AbstractBooleanFieldExtension<OWNER extends AbstractBooleanField> extends AbstractValueFieldExtension<Boolean, OWNER> implements IBooleanFieldExtension<OWNER> {

  public AbstractBooleanFieldExtension(OWNER owner) {
    super(owner);
  }
}
