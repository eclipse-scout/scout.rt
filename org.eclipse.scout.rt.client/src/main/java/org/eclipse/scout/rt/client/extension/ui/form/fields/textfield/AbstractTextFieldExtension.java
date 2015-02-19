package org.eclipse.scout.rt.client.extension.ui.form.fields.textfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.stringfield.AbstractStringFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.textfield.AbstractTextField;

public abstract class AbstractTextFieldExtension<OWNER extends AbstractTextField> extends AbstractStringFieldExtension<OWNER> implements ITextFieldExtension<OWNER> {

  public AbstractTextFieldExtension(OWNER owner) {
    super(owner);
  }
}
