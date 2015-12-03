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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.wizard.fixture.TestWizard;
import org.eclipse.scout.rt.client.extension.ui.wizard.fixture.TestWizard.FirstWizardStep;
import org.eclipse.scout.rt.client.extension.ui.wizard.fixture.TestWizard.SecondWizardStep;
import org.eclipse.scout.rt.client.extension.ui.wizard.fixture.TestWizard.ThirdWizardStep;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class MoveWizardStepTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() {
    TestWizard wizard = new TestWizard();
    assertWizardSteps(wizard, FirstWizardStep.class, SecondWizardStep.class, ThirdWizardStep.class);
  }

  @Test
  public void testMoveWizardStep() {
    BEANS.get(IExtensionRegistry.class).registerMove(FirstWizardStep.class, 40);

    TestWizard wizard = new TestWizard();
    assertWizardSteps(wizard, SecondWizardStep.class, ThirdWizardStep.class, FirstWizardStep.class);
  }

  @Test
  public void testMoveWizardSteps() {
    BEANS.get(IExtensionRegistry.class).registerMove(FirstWizardStep.class, 40);
    BEANS.get(IExtensionRegistry.class).registerMove(ThirdWizardStep.class, 5);

    TestWizard wizard = new TestWizard();
    assertWizardSteps(wizard, ThirdWizardStep.class, SecondWizardStep.class, FirstWizardStep.class);
  }

  @Test
  public void testMoveWizardStepMultipleTimes() {
    BEANS.get(IExtensionRegistry.class).registerMove(FirstWizardStep.class, 15);
    BEANS.get(IExtensionRegistry.class).registerMove(FirstWizardStep.class, 25);

    TestWizard wizard = new TestWizard();
    assertWizardSteps(wizard, SecondWizardStep.class, FirstWizardStep.class, ThirdWizardStep.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testMoveWizardStepToRoot() {
    BEANS.get(IExtensionRegistry.class).registerMoveToRoot(FirstWizardStep.class, 15d);
  }

  protected static void assertWizardSteps(TestWizard wizard, Class<?>... expectedWizardStepClasses) {
    List<IWizardStep<? extends IForm>> steps = wizard.getAvailableSteps();
    assertEquals(expectedWizardStepClasses.length, steps.size());
    for (int i = 0; i < expectedWizardStepClasses.length; i++) {
      assertSame(expectedWizardStepClasses[i], steps.get(i).getClass());
    }
  }
}
