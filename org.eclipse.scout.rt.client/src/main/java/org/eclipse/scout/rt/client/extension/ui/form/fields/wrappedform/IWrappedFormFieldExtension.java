package org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wrappedform.WrappedFormFieldChains.WrappedFormFieldInnerFormChangedChain;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;

public interface IWrappedFormFieldExtension<FORM extends IForm, OWNER extends AbstractWrappedFormField<FORM>> extends IFormFieldExtension<OWNER> {

  void execInnerFormChanged(WrappedFormFieldInnerFormChangedChain<FORM> chain, FORM oldInnerForm, FORM newInnerForm);
}
