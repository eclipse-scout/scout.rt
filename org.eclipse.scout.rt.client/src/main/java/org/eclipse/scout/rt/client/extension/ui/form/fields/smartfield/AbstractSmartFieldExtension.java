package org.eclipse.scout.rt.client.extension.ui.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;

public abstract class AbstractSmartFieldExtension<VALUE, OWNER extends AbstractSmartField<VALUE>> extends AbstractMixedSmartFieldExtension<VALUE, VALUE, OWNER> implements ISmartFieldExtension<VALUE, OWNER> {

  public AbstractSmartFieldExtension(OWNER owner) {
    super(owner);
  }
}
