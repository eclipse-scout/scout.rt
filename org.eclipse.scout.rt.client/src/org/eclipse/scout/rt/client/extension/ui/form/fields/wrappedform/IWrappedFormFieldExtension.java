package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public interface IWrappedFormFieldExtension<T extends IForm, OWNER extends AbstractWrappedFormField<T>> extends IFormFieldExtension<OWNER> {
}
