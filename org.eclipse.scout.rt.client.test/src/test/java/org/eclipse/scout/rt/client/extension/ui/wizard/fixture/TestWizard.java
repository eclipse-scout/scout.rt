/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.wizard.fixture;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizard;
import org.eclipse.scout.rt.client.ui.wizard.AbstractWizardStep;
import org.eclipse.scout.rt.platform.Order;

public class TestWizard extends AbstractWizard {

  @Order(10)
  public class FirstWizardStep extends AbstractWizardStep<IForm> {
  }

  @Order(20)
  public class SecondWizardStep extends AbstractWizardStep<IForm> {
  }

  @Order(30)
  public class ThirdWizardStep extends AbstractWizardStep<IForm> {
  }
}
