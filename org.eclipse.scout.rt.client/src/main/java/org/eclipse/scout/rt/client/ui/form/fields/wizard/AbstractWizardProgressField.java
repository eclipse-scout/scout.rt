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
package org.eclipse.scout.rt.client.ui.form.fields.wizard;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.IWizardProgressFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.wizard.WizardProgressFieldChains.WizardProgressFieldStepActionChain;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.wizard.IWizard;
import org.eclipse.scout.rt.client.ui.wizard.IWizardStep;
import org.eclipse.scout.rt.client.ui.wizard.WizardEvent;
import org.eclipse.scout.rt.client.ui.wizard.WizardListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.WeakEventListener;

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
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
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
  public List<IWizardStep<? extends IForm>> getSteps() {
    return (List<IWizardStep<? extends IForm>>) propertySupport.getProperty(PROP_STEPS);
  }

  @Override
  public void setSteps(List<IWizardStep<? extends IForm>> steps) {
    // always fire event, even when only the _content_ of steps changes, not only the step list itself
    propertySupport.setPropertyAlwaysFire(PROP_STEPS, steps);
  }

  @Override
  @SuppressWarnings("unchecked")
  public IWizardStep<? extends IForm> getActiveStep() {
    return (IWizardStep<? extends IForm>) propertySupport.getProperty(PROP_ACTIVE_STEP);
  }

  @Override
  public void setActiveStep(IWizardStep<? extends IForm> activeStep) {
    propertySupport.setProperty(PROP_ACTIVE_STEP, activeStep);
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
  protected void execInitField() {
    super.execInitField();
    installWizard(getWizard());
  }

  @Override
  protected void execDisposeField() {
    super.execDisposeField();
    uninstallWizard();
  }

  /**
   * Executes the "wizard step action" for the given step index. May throw an exception if the step index is invalid.
   * <p>
   * The default implementation resolves the step and then calls {@link IWizardStep#doAction()} on it.
   */
  @Order(10)
  @ConfigOperation
  protected void execStepAction(int stepIndex) {
    IWizardStep<? extends IForm> step = CollectionUtility.getElement(getSteps(), stepIndex);
    if (step == null) {
      throw new IllegalStateException("Invalid stepIndex: " + stepIndex);
    }
    step.doAction();
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
      setSteps(wizard.getSteps());
      setActiveStep(wizard.getActiveStep());
    }
  }

  protected void handleWizardClosed() {
    // Automatic uninstallation
    uninstallWizard();
  }

  protected class P_UIFacade implements IWizardProgressFieldUIFacade {

    @Override
    public void stepActionFromUI(int stepIndex) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      interceptStepAction(stepIndex);
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

  protected void interceptStepAction(int stepIndex) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    WizardProgressFieldStepActionChain chain = new WizardProgressFieldStepActionChain(extensions);
    chain.execStepIndex(stepIndex);
  }

  protected static class LocalWizardProgressFieldExtension<OWNER extends AbstractWizardProgressField> extends LocalFormFieldExtension<OWNER> implements IWizardProgressFieldExtension<OWNER> {

    public LocalWizardProgressFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execStepAction(WizardProgressFieldStepActionChain wizardProgressFieldStepActionChain, int stepIndex) {
      getOwner().execStepAction(stepIndex);
    }
  }

  @Override
  protected IWizardProgressFieldExtension<? extends AbstractWizardProgressField> createLocalExtension() {
    return new LocalWizardProgressFieldExtension<AbstractWizardProgressField>(this);
  }
}
