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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.wizard.IWizardContainerFormExtension;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;

/**
 * <h3>AbstractWizardContainerForm</h3> The form to extends to provide a customized wizard container form.
 *
 * @since 24.11.2009
 */
@ClassId("c2e405b4-be26-4d27-b379-06ec62793d84")
public abstract class AbstractWizardContainerForm extends AbstractForm implements IWizardContainerForm {

  private IWizard m_wizard;
  private P_WizardPropertyListener m_propertyChangeListener;

  public AbstractWizardContainerForm(IWizard wizard) {
    this(wizard, true);
  }

  public AbstractWizardContainerForm(IWizard wizard, boolean callInitializer) {
    super(false);
    m_wizard = wizard;
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public IWizard getWizard() {
    return m_wizard;
  }

  @Override
  protected boolean getConfiguredMaximizeEnabled() {
    return true;
  }

  @Override
  protected void execInitForm() {
    // attach to wizard
    if (m_wizard != null) {
      if (m_propertyChangeListener == null) {
        m_propertyChangeListener = new P_WizardPropertyListener();
      }
      m_wizard.addPropertyChangeListener(m_propertyChangeListener);
    }
    updateTitleFromWizard(false);
  }

  @Override
  protected void execDisposeForm() {
    // detach from wizard
    if (m_wizard != null) {
      if (m_propertyChangeListener != null) {
        m_wizard.removePropertyChangeListener(m_propertyChangeListener);
        m_propertyChangeListener = null;
      }
    }
    super.doFinally();
  }

  @Override
  protected void execOnCloseRequest(boolean kill, Set<Integer> enabledButtonSystemTypes) {
    handleEscapeKey(kill);
  }

  protected void handleEscapeKey(boolean kill) {
    IWizardAction action = getEscapeAction(kill);
    if (action instanceof IAction) {
      ((IAction) action).doAction();
    }
    else if (action instanceof IButton) {
      ((IButton) action).doClick();
    }
    else {
      getWizard().doCancel();
    }
  }

  protected void handleEnterKey() {
    IWizardAction action = getEnterAction();
    if (action instanceof IAction) {
      ((IAction) action).doAction();
    }
    else if (action instanceof IButton) {
      ((IButton) action).doClick();
    }
    else {
      //skip
    }
  }

  protected IWizardAction getEscapeAction(boolean kill) {
    if (kill) {
      if (getWizardSuspendButton() != null && getWizardSuspendButton().isVisible() && getWizardSuspendButton().isEnabled()) {
        return getWizardSuspendButton();
      }
      else if (getWizardCancelButton() != null && getWizardCancelButton().isVisible() && getWizardCancelButton().isEnabled()) {
        return getWizardCancelButton();
      }
      else {
        return null;
      }
    }
    else {
      if (getWizardCancelButton() != null && getWizardCancelButton().isVisible() && getWizardCancelButton().isEnabled()) {
        return getWizardCancelButton();
      }
      else if (getWizardSuspendButton() != null && getWizardSuspendButton().isVisible() && getWizardSuspendButton().isEnabled()) {
        return getWizardSuspendButton();
      }
      else {
        return null;
      }
    }
  }

  protected IWizardAction getEnterAction() {
    if (getWizardNextStepButton() != null && getWizardNextStepButton().isVisible() && getWizardNextStepButton().isEnabled()) {
      return getWizardNextStepButton();
    }
    else if (getWizardFinishButton() != null && getWizardFinishButton().isVisible() && getWizardFinishButton().isEnabled()) {
      return getWizardFinishButton();
    }
    else {
      return null;
    }
  }

  /**
   * @param force
   *          If <code>true</code>, title properties are always copied from the wizard. Otherwise, they are only copied
   *          if their value is not <code>null</code>.
   */
  protected void updateTitleFromWizard(boolean force) {
    if (getWizard() != null) {
      String title = getWizard().getTitle();
      String subTitle = getWizard().getSubTitle();
      if (title != null || force) {
        setTitle(title);
      }
      if (subTitle != null || force) {
        setSubTitle(subTitle);
      }
    }
  }

  protected abstract IForm getInnerWizardForm();

  protected abstract void setInnerWizardForm(IForm form);

  /**
   * may be overridden to handle property changes.
   */
  protected void handleWizardPropertyChanged(String propertyName, Object oldValue, Object newValue) {
    try {
      if (IWizard.PROP_WIZARD_FORM.equals(propertyName)) {
        setInnerWizardForm(getWizard().getWizardForm());
      }
      else if (IWizard.PROP_TITLE.equals(propertyName)) {
        updateTitleFromWizard(true);
      }
      else if (IWizard.PROP_SUB_TITLE.equals(propertyName)) {
        updateTitleFromWizard(true);
      }
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  private class P_WizardPropertyListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handleWizardPropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

  protected static class LocalWizardContainerFormExtension<OWNER extends AbstractWizardContainerForm> extends LocalFormExtension<OWNER> implements IWizardContainerFormExtension<OWNER> {

    public LocalWizardContainerFormExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IWizardContainerFormExtension<? extends AbstractWizardContainerForm> createLocalExtension() {
    return new LocalWizardContainerFormExtension<AbstractWizardContainerForm>(this);
  }
}
