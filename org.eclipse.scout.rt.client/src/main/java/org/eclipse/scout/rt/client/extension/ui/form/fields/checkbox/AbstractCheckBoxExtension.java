package org.eclipse.scout.rt.client.extension.ui.form.fields.checkbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.booleanfield.AbstractBooleanFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.checkbox.AbstractCheckBox;

public abstract class AbstractCheckBoxExtension<OWNER extends AbstractCheckBox> extends AbstractBooleanFieldExtension<OWNER> implements ICheckBoxExtension<OWNER> {

  public AbstractCheckBoxExtension(OWNER owner) {
    super(owner);
  }
}
