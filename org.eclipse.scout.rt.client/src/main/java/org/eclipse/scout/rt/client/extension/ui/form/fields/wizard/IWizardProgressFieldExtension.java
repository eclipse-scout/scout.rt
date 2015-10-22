package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.WizardProgressFieldChains.WizardProgressFieldWizardStepActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;

public interface IWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends IFormFieldExtension<OWNER> {

  void execWizardStepAction(WizardProgressFieldWizardStepActionChain wizardProgressFieldWizardStepActionChain, int stepIndex);
}
