/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.controls;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.action.IActionExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.controls.FormTableControlChains.TableControlInitFormChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.controls.IFormTableControlExtension;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * @since 6.0
 */
@ClassId("ee0673b2-f19b-4fa6-be96-a1f373edfc52")
public abstract class AbstractFormTableControl extends AbstractTableControl implements IFormTableControl {

  public AbstractFormTableControl() {
    this(true);
  }

  public AbstractFormTableControl(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  /**
   * Configures the form to be used with this control. The form is lazily created and started when the control gets
   * selected.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a form type token
   * @see {@link #startForm(IForm)} for details how the form gets started
   */
  @ConfigProperty(ConfigProperty.FORM)
  @Order(90)
  protected Class<? extends IForm> getConfiguredForm() {
    return null;
  }

  /**
   * Initializes the form associated with this button. This method is called before the form is used for the first time.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(120)
  protected void execInitForm() {
  }

  protected IForm createForm() {
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
   * a custom start method or implement a {@link IForm#start()} on the detail form.
   */
  protected void startForm() {
    getForm().start();
  }

  @Override
  public void setForm(IForm form) {
    propertySupport.setProperty(PROP_FORM, form);
  }

  @Override
  public final IForm getForm() {
    return (IForm) propertySupport.getProperty(PROP_FORM);
  }

  @Override
  protected void execSelectionChanged(boolean selected) {
    super.execSelectionChanged(selected);
    ensureFormCreated();
    ensureFormStarted();
  }

  public void ensureFormCreated() {
    if (getForm() != null) {
      return;
    }
    IForm form = createForm();
    if (form != null) {
      form.addFormListener(e -> {
        if (e.getType() == FormEvent.TYPE_CLOSED) {
          setSelected(false);
          setForm(null);
        }
      });
      setForm(form);
      decorateForm();
      interceptInitForm();
    }
  }

  public void ensureFormStarted() {
    if (getForm() == null || !getForm().isFormStartable()) {
      return;
    }
    startForm();
  }

  public void decorateForm() {
    getForm().setShowOnStart(false);
  }

  @Override
  protected IActionExtension<? extends AbstractAction> createLocalExtension() {
    return new LocalTableControlExtension<>(this);
  }

  protected final void interceptInitForm() {
    List<? extends IActionExtension<? extends AbstractAction>> extensions = getAllExtensions();
    TableControlInitFormChain chain = new TableControlInitFormChain(extensions);
    chain.execInitForm();
  }

  @Override
  public void disposeInternal() {
    super.disposeInternal();
    IForm form = getForm();
    if (form != null && !form.isFormClosed()) {
      form.doClose();
    }
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalTableControlExtension<OWNER extends AbstractFormTableControl> extends AbstractAction.LocalActionExtension<OWNER> implements IFormTableControlExtension<OWNER> {

    public LocalTableControlExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execInitForm(TableControlInitFormChain chain) {
      getOwner().execInitForm();
    }

  }
}
