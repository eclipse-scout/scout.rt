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

import java.net.URL;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardActiveStepChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardAnyFieldChangedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCancelChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardCreateContainerFormChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardFinishChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardNextStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardPreviousStepChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardRefreshButtonPolicyChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardResetChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardStartChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardChains.WizardSuspendChain;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardContainerForm;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

/**
 *
 */
public abstract class AbstractWizardExtension<OWNER extends AbstractWizard> extends AbstractExtension<OWNER> implements IWizardExtension<OWNER> {

  public AbstractWizardExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execActiveStepChanged(WizardActiveStepChangedChain chain) throws ProcessingException {
    chain.execActiveStepChanged();
  }

  @Override
  public void execSuspend(WizardSuspendChain chain) throws ProcessingException {
    chain.execSuspend();
  }

  @Override
  public void execRefreshButtonPolicy(WizardRefreshButtonPolicyChain chain) throws ProcessingException {
    chain.execRefreshButtonPolicy();
  }

  @Override
  public void execCancel(WizardCancelChain chain) throws ProcessingException {
    chain.execCancel();
  }

  @Override
  public void execStart(WizardStartChain chain) throws ProcessingException {
    chain.execStart();
  }

  @Override
  public IWizardContainerForm execCreateContainerForm(WizardCreateContainerFormChain chain) throws ProcessingException {
    return chain.execCreateContainerForm();
  }

  @Override
  public void execAnyFieldChanged(WizardAnyFieldChangedChain chain, IFormField source) throws ProcessingException {
    chain.execAnyFieldChanged(source);
  }

  @Override
  public void execReset(WizardResetChain chain) throws ProcessingException {
    chain.execReset();
  }

  @Override
  public void execHyperlinkAction(WizardHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
    chain.execHyperlinkAction(url, path, local);
  }

  @Override
  public void execPreviousStep(WizardPreviousStepChain chain) throws ProcessingException {
    chain.execPreviousStep();
  }

  @Override
  public void execNextStep(WizardNextStepChain chain) throws ProcessingException {
    chain.execNextStep();
  }

  @Override
  public void execFinish(WizardFinishChain chain) throws ProcessingException {
    chain.execFinish();
  }

}
