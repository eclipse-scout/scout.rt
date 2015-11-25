/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.wizard;

import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardActiveStepChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAnyFieldChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCancelChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCreateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardDecorateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardFinishChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardNextStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPreviousStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardRefreshButtonPolicyChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardResetChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStepActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardSuspendChain;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardContainerForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractWizardExtension<OWNER extends AbstractWizard> extends AbstractExtension<OWNER> implements IWizardExtension<OWNER> {

  public AbstractWizardExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execActiveStepChanged(WizardActiveStepChangedChain chain) {
    chain.execActiveStepChanged();
  }

  @Override
  public void execSuspend(WizardSuspendChain chain) {
    chain.execSuspend();
  }

  @Override
  public void execRefreshButtonPolicy(WizardRefreshButtonPolicyChain chain) {
    chain.execRefreshButtonPolicy();
  }

  @Override
  public void execCancel(WizardCancelChain chain) {
    chain.execCancel();
  }

  @Override
  public void execStart(WizardStartChain chain) {
    chain.execStart();
  }

  @Override
  public IWizardContainerForm execCreateContainerForm(WizardCreateContainerFormChain chain) {
    return chain.execCreateContainerForm();
  }

  @Override
  public void execDecorateContainerForm(WizardDecorateContainerFormChain chain) {
    chain.execDecorateContainerForm();
  }

  @Override
  public void execAnyFieldChanged(WizardAnyFieldChangedChain chain, IFormField source) {
    chain.execAnyFieldChanged(source);
  }

  @Override
  public void execReset(WizardResetChain chain) {
    chain.execReset();
  }

  @Override
  public void execAppLinkAction(WizardAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }

  @Override
  public void execStepAction(WizardStepActionChain chain, IWizardStep<? extends IForm> step) {
    chain.execStepAction(step);
  }

  @Override
  public void execPreviousStep(WizardPreviousStepChain chain) {
    chain.execPreviousStep();
  }

  @Override
  public void execNextStep(WizardNextStepChain chain) {
    chain.execNextStep();
  }

  @Override
  public void execFinish(WizardFinishChain chain) {
    chain.execFinish();
  }

}
