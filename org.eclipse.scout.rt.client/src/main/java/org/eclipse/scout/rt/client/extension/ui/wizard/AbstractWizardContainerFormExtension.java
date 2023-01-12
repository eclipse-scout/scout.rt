/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.wizard;

import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardContainerForm;

public abstract class AbstractWizardContainerFormExtension<OWNER extends AbstractWizardContainerForm> extends AbstractFormExtension<OWNER> implements IWizardContainerFormExtension<OWNER> {

  public AbstractWizardContainerFormExtension(OWNER owner) {
    super(owner);
  }
}
