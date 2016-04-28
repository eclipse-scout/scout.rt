package org.eclipse.scout.rt.client.mobile.transformation;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;

public class FormFieldExtension extends AbstractFormFieldExtension<AbstractFormField> {

  public FormFieldExtension(AbstractFormField ownerField) {
    super(ownerField);
  }

  @Override
  public void execInitField(FormFieldInitFieldChain chain) {
    super.execInitField(chain);
    BEANS.get(IDeviceTransformationService.class).getDeviceTransformer().transformFormField(getOwner());
  }

}
