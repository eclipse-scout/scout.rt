package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.WizardProgressFieldChains.WizardProgressFieldStepActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;

public interface IWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends IFormFieldExtension<OWNER> {

  void execStepAction(WizardProgressFieldStepActionChain wizardProgressFieldStepActionChain, int stepIndex);
}
