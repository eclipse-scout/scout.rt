package org.eclipse.scout.rt.client.extension.ui.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardContainerForm;

public abstract class AbstractWizardContainerFormExtension<OWNER extends AbstractWizardContainerForm> extends AbstractFormExtension<OWNER> implements IWizardContainerFormExtension<OWNER> {

  public AbstractWizardContainerFormExtension(OWNER owner) {
    super(owner);
  }
}
