package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public abstract class AbstractWrappedFormFieldExtension<T extends IForm, OWNER extends AbstractWrappedFormField<T>> extends AbstractFormFieldExtension<OWNER> implements IWrappedFormFieldExtension<T, OWNER> {

  public AbstractWrappedFormFieldExtension(OWNER owner) {
    super(owner);
  }
}
