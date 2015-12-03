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
package org.eclipse.scout.rt.client.extension.ui.form.fields.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.WizardProgressFieldChains.WizardProgressFieldStepActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;

public interface IWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends IFormFieldExtension<OWNER> {

  void execStepAction(WizardProgressFieldStepActionChain wizardProgressFieldStepActionChain, int stepIndex);
}
