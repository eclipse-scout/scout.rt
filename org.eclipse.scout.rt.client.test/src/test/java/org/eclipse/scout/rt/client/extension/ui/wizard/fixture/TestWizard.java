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
