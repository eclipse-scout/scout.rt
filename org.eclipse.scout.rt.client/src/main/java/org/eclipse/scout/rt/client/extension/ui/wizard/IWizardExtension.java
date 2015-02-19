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
import org.eclipse.scout.rt.shared.extension.IExtension;

/**
 *
 */
public interface IWizardExtension<OWNER extends AbstractWizard> extends IExtension<OWNER> {

  void execActiveStepChanged(WizardActiveStepChangedChain chain) throws ProcessingException;

  void execSuspend(WizardSuspendChain chain) throws ProcessingException;

  void execRefreshButtonPolicy(WizardRefreshButtonPolicyChain chain) throws ProcessingException;

  void execCancel(WizardCancelChain chain) throws ProcessingException;

  void execStart(WizardStartChain chain) throws ProcessingException;

  IWizardContainerForm execCreateContainerForm(WizardCreateContainerFormChain chain) throws ProcessingException;

  void execAnyFieldChanged(WizardAnyFieldChangedChain chain, IFormField source) throws ProcessingException;

  void execReset(WizardResetChain chain) throws ProcessingException;

  void execHyperlinkAction(WizardHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException;

  void execPreviousStep(WizardPreviousStepChain chain) throws ProcessingException;

  void execNextStep(WizardNextStepChain chain) throws ProcessingException;

  void execFinish(WizardFinishChain chain) throws ProcessingException;

}
