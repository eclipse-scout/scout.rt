/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IWizardExtension<OWNER extends AbstractWizard> extends IExtension<OWNER> {

  void execActiveStepChanged(WizardActiveStepChangedChain chain);

  void execSuspend(WizardSuspendChain chain);

  void execRefreshButtonPolicy(WizardRefreshButtonPolicyChain chain);

  void execCancel(WizardCancelChain chain);

  void execStart(WizardStartChain chain);

  IWizardContainerForm execCreateContainerForm(WizardCreateContainerFormChain chain);

  void execDecorateContainerForm(WizardDecorateContainerFormChain chain);

  void execAnyFieldChanged(WizardAnyFieldChangedChain chain, IFormField source);

  void execReset(WizardResetChain chain);

  void execAppLinkAction(WizardAppLinkActionChain chain, String ref);

  void execStepAction(WizardStepActionChain chain, IWizardStep<? extends IForm> step);

  void execPreviousStep(WizardPreviousStepChain chain);

  void execNextStep(WizardNextStepChain chain);

  void execFinish(WizardFinishChain chain);
}
