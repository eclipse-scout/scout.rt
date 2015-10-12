package org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;

public abstract class AbstractLabelFieldExtension<OWNER extends AbstractLabelField> extends AbstractValueFieldExtension<String, OWNER> implements ILabelFieldExtension<OWNER> {

  public AbstractLabelFieldExtension(OWNER owner) {
    super(owner);
  }
}
