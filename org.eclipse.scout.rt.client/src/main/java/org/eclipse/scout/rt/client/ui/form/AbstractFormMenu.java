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
package org.eclipse.scout.rt.client.ui.form;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.form.FormMenuChains.FormMenuInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.form.IFormMenuExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * A tool button that can be used in the {@link IDesktop} to toggle a form in the tools area.
 */
@ClassId("c40132fa-5aac-4d25-8330-76b9210c07ca")
public abstract class AbstractFormMenu<FORM extends IForm> extends AbstractMenu implements IFormMenu<FORM> {

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
      form.addFormListener(e -> {
        if (e.getType() == FormEvent.TYPE_CLOSED) {
          setSelected(false);
          setForm(null);
        }
      });
      decorateForm(form);
      interceptInitForm(form);
      setForm(form);
    }
  }

  public void ensureFormStarted() {
    if (getForm() == null || !getForm().isFormStartable()) {
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

  protected void decorateForm(FORM form) {
    form.setShowOnStart(false);
    form.setDisplayHint(IForm.DISPLAY_HINT_VIEW); // TODO [7.0] bsh: set in UI instead
    form.setDisplayViewId(IForm.VIEW_ID_E); // TODO [7.0] bsh: set in UI instead
  }

  protected final void interceptInitForm(FORM form) {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    FormMenuInitFormChain<FORM> chain = new FormMenuInitFormChain<>(extensions);
    chain.execInitForm(form);
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    FORM form = getForm();
    if (form != null && !form.isFormClosed()) {
      form.doClose();
    }
  }

  protected static class LocalFormMenuExtension<FORM extends IForm, OWNER extends AbstractFormMenu<FORM>> extends LocalMenuExtension<OWNER> implements IFormMenuExtension<FORM, OWNER> {

    public LocalFormMenuExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInitForm(FormMenuInitFormChain<FORM> chain, FORM form) {
      getOwner().execInitForm(form);
    }
  }

  @Override
  protected IFormMenuExtension<FORM, ? extends AbstractFormMenu<FORM>> createLocalExtension() {
    return new LocalFormMenuExtension<>(this);
  }
}
