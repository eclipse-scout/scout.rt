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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.FormToolButtonChains.FormToolButtonInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.IFormToolButtonExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.tool.AbstractToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * A tool button that can be used in the {@link IDesktop} to toggle a form in the tools area.
 */
public abstract class AbstractFormToolButton<FORM extends IForm> extends AbstractToolButton implements IFormToolButton<FORM> {

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public FORM getForm() {
    return (FORM) propertySupport.getProperty(PROP_FORM);
  }

  @Override
  public void setForm(FORM f) {
    propertySupport.setProperty(PROP_FORM, f);
  }

  /**
   * Configures the form to be used with this button. The form is lazily created and started when the button gets
   * selected.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a form type token
   * @see {@link #startForm(IForm)} for details how the form gets started
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(90)
  protected Class<FORM> getConfiguredForm() {
    return null;
  }

  /**
   * Initializes the {@link IForm} associated with this button, and is invoked once the {@link IForm} is showed for the
   * first time.
   *
   * @param form
   *          the {@link IForm} to be initialized.
   */
  @ConfigOperation
  @Order(120)
  protected void execInitForm(IForm form) {
  }

  @Override
  protected void execSelectionChanged(boolean selected) {
    if (!selected) {
      return;
    }
    ensureFormCreated();
    ensureFormStarted();
  }

  public void ensureFormCreated() {
    if (getForm() != null) {
      return;
    }
    FORM form = createForm();
    if (form != null) {
      decorateForm(form);
      interceptInitForm(form);
      setForm(form);
    }
  }

  public void ensureFormStarted() {
    if (getForm() == null || getForm().isFormStarted()) {
      return;
    }
    startForm();
  }

  protected FORM createForm() {
    if (getConfiguredForm() == null) {
      return null;
    }
    try {
      return getConfiguredForm().newInstance();
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + getConfiguredForm().getName() + "'.", e));
    }
    return null;
  }

  /**
   * Starts the form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method.
   */
  protected void startForm() {
    getForm().start();
  }

  protected void decorateForm(IForm form) {
    form.setShowOnStart(false);
    form.setDisplayHint(IForm.DISPLAY_HINT_VIEW); // FIXME dwi: set in UI instead
    form.setDisplayViewId(IForm.VIEW_ID_E); // FIXME dwi: set in UI instead
  }

  protected final void interceptInitForm(FORM form) {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    FormToolButtonInitFormChain<FORM> chain = new FormToolButtonInitFormChain<>(extensions);
    chain.execInitForm(form);
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    FORM form = getForm();
    if (form != null) {
      form.doClose();
    }
  }

  protected static class LocalFormToolButtonExtension<FORM extends IForm, OWNER extends AbstractFormToolButton<FORM>> extends LocalToolButtonExtension<OWNER> implements IFormToolButtonExtension<FORM, OWNER> {

    public LocalFormToolButtonExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInitForm(FormToolButtonInitFormChain<FORM> chain, FORM form) {
      getOwner().execInitForm(form);
    }
  }

  @Override
  protected IFormToolButtonExtension<FORM, ? extends AbstractFormToolButton<FORM>> createLocalExtension() {
    return new LocalFormToolButtonExtension<FORM, AbstractFormToolButton<FORM>>(this);
  }
}
