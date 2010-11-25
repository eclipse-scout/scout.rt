/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.util.HashSet;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * <h3>AbstractWizardContainerForm</h3> The form to extends to provide a
 * customized wizard container form.
 * 
 * @since 24.11.2009
 */
public abstract class AbstractWizardContainerForm extends AbstractForm implements IWizardContainerForm {
  private IWizard m_wizard;
  private P_WizardPropertyListener m_propertyChangeListener;

  public AbstractWizardContainerForm(IWizard w) throws ProcessingException {
    super(false);
    m_wizard = w;
    callInitializer();
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
  protected void execInitForm() throws ProcessingException {
    // attach to wizard
    if (m_wizard != null) {
      if (m_propertyChangeListener == null) {
        m_propertyChangeListener = new P_WizardPropertyListener();
      }
      m_wizard.addPropertyChangeListener(m_propertyChangeListener);
    }
    updateTitleFromWizard();
  }

  @Override
  protected void execDisposeForm() throws ProcessingException {
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
  protected void execOnCloseRequest(boolean kill, HashSet<Integer> enabledButtonSystemTypes) throws ProcessingException {
    handleEscapeKey(kill);
  }

  protected void handleEscapeKey(boolean kill) throws ProcessingException {
    if (kill) {
      if (getWizardSuspendButton() != null && getWizardSuspendButton().isVisible() && getWizardSuspendButton().isEnabled()) {
        getWizard().doSuspend();
      }
      else if (getWizardCancelButton() != null && getWizardCancelButton().isVisible() && getWizardCancelButton().isEnabled()) {
        getWizard().doCancel();
      }
      else {
        getWizard().doCancel();
      }
    }
    else {
      if (getWizardCancelButton() != null && getWizardCancelButton().isVisible() && getWizardCancelButton().isEnabled()) {
        getWizard().doCancel();
      }
      else if (getWizardSuspendButton() != null && getWizardSuspendButton().isVisible() && getWizardSuspendButton().isEnabled()) {
        getWizard().doSuspend();
      }
      else {
        getWizard().doCancel();
      }
    }
  }

  protected void handleEnterKey() throws ProcessingException {
    if (getWizardNextStepButton() != null && getWizardNextStepButton().isVisible() && getWizardNextStepButton().isEnabled()) {
      getWizard().doNextStep();
    }
    else if (getWizardFinishButton() != null && getWizardFinishButton().isVisible() && getWizardFinishButton().isEnabled()) {
      getWizard().doFinish();
    }
  }

  protected void updateTitleFromWizard() {
    if (getWizard() != null) {
      String title = getWizard().getTitle();
      String subTitle = getWizard().getSubTitle();
      String s = "";
      if (title != null) {
        s += title;
      }
      if (subTitle != null) {
        s += " - " + subTitle;
      }
      setTitle(s);
    }
  }

  protected abstract IForm getInnerWizardForm();

  protected abstract void setInnerWizardForm(IForm form);

  /**
   * may be overridden to handle property changes.
   */
  protected void handleWizardPropertyChanged(String propertyName, Object oldValue, Object newValue) {
    if (IWizard.PROP_WIZARD_FORM.equals(propertyName)) {
      setInnerWizardForm(getWizard().getWizardForm());
    }
    else if (IWizard.PROP_TITLE.equals(propertyName)) {
      updateTitleFromWizard();
    }
    else if (IWizard.PROP_SUB_TITLE.equals(propertyName)) {
      updateTitleFromWizard();
    }
  }

  private class P_WizardPropertyListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      handleWizardPropertyChanged(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }

}
