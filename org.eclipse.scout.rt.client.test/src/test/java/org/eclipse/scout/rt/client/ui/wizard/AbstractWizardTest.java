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
package org.eclipse.scout.rt.client.ui.wizard;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractWizard}
 *
 * @since 4.1.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractWizardTest {

  /**
   * Before bug 440621 {@link AbstractWizard#getStepIndex(IWizardStep)} compared the steps by comparing their identity
   * <code>==</code>. The new logic should compare two {@link IWizardStep}s by using the <code>equals()</code> method.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testGetStepIndex() {
    AbstractWizard wizard = new P_AbstractWizard();
    IWizardStep step1 = new P_WizardStepWithEqualsImplemented(1);
    IWizardStep step2 = new P_WizardStepWithEqualsImplemented(2);
    IWizardStep step3 = new P_WizardStepWithEqualsImplemented(1); //this one is equal to step1
    IWizardStep stepNotContained = new P_WizardStepWithEqualsImplemented(3);
    wizard.setSteps(step1, step2);

    int index = wizard.getStepIndex(null);
    assertEquals(-1, index);

    index = wizard.getStepIndex(step1);
    assertEquals(0, index);

    index = wizard.getStepIndex(step2);
    assertEquals(1, index);

    index = wizard.getStepIndex(step3);
    assertEquals("Step3 is equals to Step1, so index 0 (Step1) should be returned!", 0, index);

    index = wizard.getStepIndex(stepNotContained);
    assertEquals(-1, index);
  }

  private class P_AbstractWizard extends AbstractWizard {

  }

  private class P_WizardStepWithEqualsImplemented extends AbstractWizardStep<IForm> {
    private int m_uid;

    public P_WizardStepWithEqualsImplemented(int uniqueId) {
      m_uid = uniqueId;
    }

    public int getUid() {
      return m_uid;
    }

    @Override
    public int hashCode() {
      return m_uid;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof P_WizardStepWithEqualsImplemented) {
        return m_uid == ((P_WizardStepWithEqualsImplemented) obj).getUid();
      }
      return false;
    }
  }
}
