/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;

public interface IWizardContainerForm extends IForm {

  /**
   * Normally starts the {@link IFormHandler} with the name WizardHandler.
   */
  void startWizard();

  IWizardAction getWizardCancelButton();

  IWizardAction getWizardSuspendButton();

  IWizardAction getWizardPreviousStepButton();

  IWizardAction getWizardNextStepButton();

  IWizardAction getWizardFinishButton();

  IWizardAction getWizardResetButton();
}
