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

import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepActionChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepActivateChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepDeactivateChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepDisposeChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormClosedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormDiscardedChain;
import org.eclipse.scout.rt.client.extension.ui.wizard.WizardStepChains.WizardStepFormStoredChain;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardStep;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IWizardStepExtension<FORM extends IForm, OWNER extends AbstractWizardStep<FORM>> extends IExtension<OWNER> {

  void execDeactivate(WizardStepDeactivateChain<? extends IForm> chain, int stepKind);

  void execDispose(WizardStepDisposeChain<? extends IForm> chain);

  void execFormClosed(WizardStepFormClosedChain<? extends IForm> chain, boolean activation);

  void execActivate(WizardStepActivateChain<? extends IForm> chain, int stepKind);

  void execFormDiscarded(WizardStepFormDiscardedChain<? extends IForm> chain, boolean activation);

  void execFormStored(WizardStepFormStoredChain<? extends IForm> chain, boolean activation);

  void execAction(WizardStepActionChain<? extends IForm> chain);

}
