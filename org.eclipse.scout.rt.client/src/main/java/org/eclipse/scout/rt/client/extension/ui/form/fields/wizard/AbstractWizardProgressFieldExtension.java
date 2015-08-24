package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;

public abstract class AbstractWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends AbstractFormFieldExtension<OWNER>implements IWizardProgressFieldExtension<OWNER> {

  public AbstractWizardProgressFieldExtension(OWNER owner) {
    super(owner);
  }
}
