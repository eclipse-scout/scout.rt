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

import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractWizardStep<T extends IForm> extends AbstractPropertyObserver implements IWizardStep<T>, IPropertyObserver {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractWizardStep.class);

  private IWizard m_wizard;
  private T m_form;
  private FormListener m_formListener;
  private int m_activationCounter;
  private boolean m_initialized;

  public AbstractWizardStep() {
    this(true);
  }

  public AbstractWizardStep(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(35)
  protected String getConfiguredTitleHtml() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(38)
  protected String getConfiguredDescriptionHtml() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.DOC)
  @Order(50)
  protected String getConfiguredDoc() {
    return null;
  }

  /**
   * @param stepKind
   *          any of the STEP_* constants activate this step normally creates a
   *          form, calls {@link IForm#startWizardStep(IWizardStep, Class)} on
   *          the form and places the form inside the wizard
   *          {@link IWizard#setWizardForm(org.eclipse.scout.rt.client.ui.form.IForm)}
   */
  @Order(10)
  @ConfigOperation
  protected void execActivate(int stepKind) throws ProcessingException {
  }

  /**
   * @param stepKind
   *          any of the STEP_* constants deactivate this step
   */
  @Order(20)
  @ConfigOperation
  protected void execDeactivate(int stepKind) throws ProcessingException {
  }

  /**
   * dispose this step The default implementation closes the form at {@link #getForm()}
   */
  @Order(30)
  @ConfigOperation
  protected void execDispose() throws ProcessingException {
    T f = getForm();
    if (f != null) {
      f.doClose();
    }
  }

  /**
   * When the cached form is stored (it may still be open) this method is
   * called.
   * 
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation does
   *          nothing.
   */
  @Order(40)
  @ConfigOperation
  protected void execFormStored(boolean activation) throws ProcessingException {
  }

  /**
   * When the cached form is discarded (save was either not requested or it was
   * forcedly closed) this method is called.
   * 
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation does
   *          nothing.
   */
  @Order(50)
  @ConfigOperation
  protected void execFormDiscarded(boolean activation) throws ProcessingException {
  }

  /**
   * When the cached form is closed (after some store and/or a discard
   * operation) this method is called.
   * 
   * @param activation
   *          true if this method is called by the wizard itself by {@link IWizardStep#activate(int)},
   *          {@link IWizardStep#deactivate(int)} or {@link IWizardStep#dispose()} The default implementation calls
   *          {@link IWizard#doNextStep()} iff activation=false and form was
   *          saved (formDataChanged=true)
   */
  @Order(60)
  @ConfigOperation
  protected void execFormClosed(boolean activation) throws ProcessingException {
    if (!activation) {
      if (getForm().isFormStored()) {
        getWizard().doNextStep();
      }
    }
  }

  protected void initConfig() {
    setTitle(getConfiguredTitle());
    setTooltipText(getConfiguredTooltipText());
    setTitleHtml(getConfiguredTitleHtml());
    setDescriptionHtml(getConfiguredDescriptionHtml());
    setIconId(getConfiguredIconId());
    setEnabled(getConfiguredEnabled());
  }

  /*
   * Runtime
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propName, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(propName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propName, PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(propName, listener);
  }

  @Override
  public T getForm() {
    return m_form;
  }

  @Override
  public void setForm(T f) {
    // remove old
    if (m_form != null) {
      if (m_formListener != null) {
        m_form.removeFormListener(m_formListener);
      }
    }
    m_form = f;
    // add old
    if (m_form != null) {
      if (m_formListener == null) {
        m_formListener = new FormListener() {
          @Override
          public void formChanged(FormEvent e) throws ProcessingException {
            try {
              switch (e.getType()) {
                case FormEvent.TYPE_STORE_AFTER: {
                  execFormStored(m_activationCounter > 0);
                  break;
                }
                case FormEvent.TYPE_DISCARDED: {
                  execFormDiscarded(m_activationCounter > 0);
                  break;
                }
                case FormEvent.TYPE_CLOSED: {
                  execFormClosed(m_activationCounter > 0);
                  break;
                }
              }
            }
            catch (ProcessingException pe) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(pe);
            }
            catch (Throwable t) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
            }
            switch (e.getType()) {
              case FormEvent.TYPE_CLOSED: {
                setForm(null);
                break;
              }
            }
          }
        };
      }
      m_form.addFormListener(m_formListener);
    }
  }

  @Override
  public IWizard getWizard() {
    return m_wizard;
  }

  @Override
  public void setWizardInternal(IWizard w) {
    m_wizard = w;
  }

  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String s) {
    propertySupport.setPropertyString(PROP_ICON_ID, s);
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setTooltipText(String s) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, s);
  }

  @Override
  public String getTitleHtml() {
    return propertySupport.getPropertyString(PROP_TITLE_HTML);
  }

  @Override
  public void setTitleHtml(String s) {
    propertySupport.setPropertyString(PROP_TITLE_HTML, s);
  }

  @Override
  public String getDescriptionHtml() {
    return propertySupport.getPropertyString(PROP_DESCRIPTION_HTML);
  }

  @Override
  public void setDescriptionHtml(String s) {
    propertySupport.setPropertyString(PROP_DESCRIPTION_HTML, s);
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public void setEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_ENABLED, b);
  }

  @Override
  public void activate(int stepKind) throws ProcessingException {
    try {
      m_activationCounter++;
      execActivate(stepKind);
    }
    finally {
      m_activationCounter--;
    }
  }

  @Override
  public void deactivate(int stepKind) throws ProcessingException {
    try {
      m_activationCounter++;
      execDeactivate(stepKind);
    }
    finally {
      m_activationCounter--;
    }
  }

  @Override
  public void dispose() throws ProcessingException {
    try {
      m_activationCounter++;
      execDispose();
    }
    finally {
      m_activationCounter--;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getTitle() + "]";
  }

}
