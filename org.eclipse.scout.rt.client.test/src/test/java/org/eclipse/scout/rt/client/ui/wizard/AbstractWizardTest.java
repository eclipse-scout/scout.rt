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

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
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

  private final AtomicInteger m_counter = new AtomicInteger();

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

  @Test
  public void testDisposeOnClose() {
    m_counter.set(0);
    AbstractWizard wizard = new P_AbstractWizardWithContainerForm(true);

    IWizardStep<IForm> step1 = new P_WizardStepWithForm();
    IWizardStep<IForm> step2 = new P_WizardStepWithForm();
    wizard.setAvailableSteps(CollectionUtility.arrayList(step1, step2));
    wizard.start();
    wizard.doNextStep();

    wizard.getContainerForm().doClose();

    assertEquals("Every form and wizard should be disposed, but ist not.", 4, m_counter.get());
    assertEquals(IWizard.CloseType.Closed, wizard.getCloseType());
  }

  @Test
  public void testDisposeOnCloseUnmanaged() {
    m_counter.set(0);
    AbstractWizard wizard = new P_AbstractWizardWithContainerForm(false);

    IWizardStep<IForm> step1 = new P_WizardStepWithForm();
    IWizardStep<IForm> step2 = new P_WizardStepWithForm();
    wizard.setAvailableSteps(CollectionUtility.arrayList(step1, step2));
    wizard.start();

    wizard.getContainerForm().doClose();

    assertEquals("Only the container form should be disposed.", 1, m_counter.get());
    assertFalse(wizard.isClosed());
  }

  @Test
  public void testListenerRemovedOnManagedClose() {
    AbstractWizard wizard = new P_AbstractWizardWithContainerForm(true);

    IWizardStep<IForm> step1 = new P_WizardStepWithForm();
    IWizardStep<IForm> step2 = new P_WizardStepWithForm();
    wizard.setAvailableSteps(CollectionUtility.arrayList(step1, step2));
    wizard.start();

    wizard.getContainerForm().doClose();

    assertTrue(wizard.isClosed());
    assertEquals("All listeners should be removed from the container form.", 0, wizard.getContainerForm().formListeners().listAll().size());
  }

  @Test
  public void testListenerRemovedOnRegularClose() {
    AbstractWizard wizard = new P_AbstractWizardWithContainerForm(true);

    IWizardStep<IForm> step1 = new P_WizardStepWithForm();
    IWizardStep<IForm> step2 = new P_WizardStepWithForm();
    wizard.setAvailableSteps(CollectionUtility.arrayList(step1, step2));
    wizard.start();

    wizard.close();

    assertTrue(wizard.isClosed());
    assertEquals("All listeners should be removed from the container form.", 0, wizard.getContainerForm().formListeners().listAll().size());
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

  @ClassId("716af451-9cde-4dda-b0e9-5ebf43da40e2")
  private class P_AbstractWizardWithContainerForm extends AbstractWizard {

    private final boolean m_managedByContainerForm;

    public P_AbstractWizardWithContainerForm(boolean managedByContainerForm) {
      super(false);
      m_managedByContainerForm = managedByContainerForm;
      callInitializer();
    }

    @Override
    protected IWizardContainerForm execCreateContainerForm() {
      return new P_ContainerForm(this);
    }

    @Override
    protected boolean getConfiguredManagedByContainerForm() {
      return m_managedByContainerForm;
    }

    @Override
    public void close() {
      m_counter.incrementAndGet();
      super.close();
    }
  }

  private class P_WizardStepWithForm extends AbstractWizardStep<IForm> {

    @Override
    protected void execActivate(int stepKind) {
      IForm f = getForm();
      if (f == null) {
        f = new P_StepForm();
        setForm(f);
        f.startWizardStep(this);
      }
      getWizard().setWizardForm(f);
    }
  }

  private class P_StepForm extends AbstractForm {

    @Override
    protected void execDisposeForm() {
      m_counter.incrementAndGet();
      super.execDisposeForm();
    }

    @ClassId("fd1d5975-fa72-43e6-ac9b-5b7747e1a036")
    public class MainBox extends AbstractGroupBox {
    }
  }

  @ClassId("772db482-f610-4cd4-b131-be19e42aedac")
  private class P_ContainerForm extends DefaultWizardContainerForm {

    public P_ContainerForm(IWizard wizard) {
      super(wizard);
    }

    @Override
    protected void execDisposeForm() {
      m_counter.incrementAndGet();
      super.execDisposeForm();
    }
  }
}
