/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;

public interface IWizardContainerForm extends IForm {
  /**
   * Normally starts the {@link IFormHandler} with the name WizardHandler.
   * 
   * @throws ProcessingException
   */
  void startWizard() throws ProcessingException;

  IButton getWizardCancelButton();

  IButton getWizardSuspendButton();

  IButton getWizardPreviousStepButton();

  IButton getWizardNextStepButton();

  IButton getWizardFinishButton();

  IButton getWizardResetButton();
}
