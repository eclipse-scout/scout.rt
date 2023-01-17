/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;

/**
 * A form field indicating the "progress" of a wizard. It is usually rendered as a step list.
 *
 * @since 5.1
 */
public interface IWizardProgressField extends IFormField {

  String PROP_STEPS = "steps";
  String PROP_ACTIVE_STEP = "activeStep";

  IWizardProgressFieldUIFacade getUIFacade();

  /**
   * @return the wizard associated with the current form (IForm{@link #getWizard()}). For a wizard container form, this
   *         is the wizard specified in the constructor. For a wizard inner form, this is the wizard specified when
   *         starting the inner form ({@link IForm#startWizardStep(IWizardStep)}). The return value may be
   *         <code>null</code>, e.g. when an inner wizard form is not started or the field is used outside the wizard
   *         context.
   */
  IWizard getWizard();

  List<IWizardStep<? extends IForm>> getSteps();

  void setSteps(List<IWizardStep<? extends IForm>> steps);

  IWizardStep<? extends IForm> getActiveStep();

  void setActiveStep(IWizardStep<? extends IForm> activeStep);
}
