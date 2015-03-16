/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.IWizardProgressFieldExtension;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.client.ui.wizard.WizardEvent;
import org.eclipse.scout.rt.client.ui.wizard.WizardListener;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class AbstractWizardProgressField extends AbstractFormField implements IWizardProgressField {

  private IWizardProgressFieldUIFacade m_uiFacade;
  private final WizardListener m_wizardListener = new P_WizardListener();

  public AbstractWizardProgressField() {
    this(true);
  }

  public AbstractWizardProgressField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
  }

  @Override
  public IWizardProgressFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public IWizard getWizard() {
    if (getForm() != null) {
      return getForm().getWizard();
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IWizardStep<? extends IForm>> getWizardSteps() {
    return (List<IWizardStep<? extends IForm>>) propertySupport.getProperty(PROP_WIZARD_STEPS);
  }

  @Override
  public void setWizardSteps(List<IWizardStep<? extends IForm>> wizardSteps) {
    propertySupport.setProperty(PROP_WIZARD_STEPS, wizardSteps);
  }

  @Override
  @SuppressWarnings("unchecked")
  public IWizardStep<? extends IForm> getActiveWizardStep() {
    return (IWizardStep<? extends IForm>) propertySupport.getProperty(PROP_ACTIVE_WIZARD_STEP);
  }

  @Override
  public void setActiveWizardStep(IWizardStep<? extends IForm> activeWizardStep) {
    propertySupport.setProperty(PROP_ACTIVE_WIZARD_STEP, activeWizardStep);
  }

  @Override
  protected int getConfiguredGridW() {
    return 2;
  }

  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 0;
  }

  @Override
  protected void execInitField() throws ProcessingException {
    super.execInitField();
    installWizard(getWizard());
  }

  @Override
  protected void execDisposeField() throws ProcessingException {
    super.execDisposeField();
    uninstallWizard();
  }

  protected void installWizard(IWizard wizard) {
    uninstallWizard();
    if (wizard != null) {
      wizard.addWizardListener(m_wizardListener);
      // Sync state initially
      handleWizardStateChanged(wizard);
    }
  }

  protected void uninstallWizard() {
    IWizard wizard = getWizard();
    if (wizard != null) {
      wizard.removeWizardListener(m_wizardListener);
    }
  }

  protected void handleWizardStateChanged(IWizard wizard) {
    if (wizard != null) {
      setWizardSteps(wizard.getSteps());
      setActiveWizardStep(wizard.getActiveStep());
    }
  }

  protected void handleWizardClosed() {
    // Automatic uninstallation
    uninstallWizard();
  }

  private class P_UIFacade implements IWizardProgressFieldUIFacade {

    @Override
    public void activateStepFromUI(int stepIndex) {
      try {
        IWizard wizard = getWizard();
        if (wizard != null && stepIndex >= 0) {
          IWizardStep<? extends IForm> step = wizard.getStep(stepIndex);
          if (step == null) {
            throw new IllegalStateException("Invalid stepIndex: " + stepIndex);
          }
          wizard.activateStep(step);
        }
      }
      catch (Exception e) {
        ProcessingException pe = (e instanceof ProcessingException ? (ProcessingException) e : new ProcessingException("Unexpected error", e));
        SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
      }
    }
  }

  private class P_WizardListener implements WizardListener, WeakEventListener {

    @Override
    public void wizardChanged(WizardEvent e) {
      if (e.getType() == WizardEvent.TYPE_STATE_CHANGED) {
        handleWizardStateChanged(e.getWizard());
      }
      else if (e.getType() == WizardEvent.TYPE_CLOSED) {
        handleWizardClosed();
      }
    }
  }

  protected static class LocalWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends LocalFormFieldExtension<OWNER> implements IWizardProgressFieldExtension<OWNER> {

    public LocalWizardProgressFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IWizardProgressFieldExtension<? extends AbstractWizardProgressField> createLocalExtension() {
    return new LocalWizardProgressFieldExtension<AbstractWizardProgressField>(this);
  }
}
