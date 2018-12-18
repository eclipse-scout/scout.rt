/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.popup;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 9.0
 */
public abstract class AbstractFormPopup extends AbstractWidgetPopup<IForm> {

  public AbstractFormPopup() {
    this(true);
  }

  public AbstractFormPopup(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected IForm createWidget() {
    IForm form = createForm();
    decorateForm(form);
    form.start();
    form.addFormListener(e -> {
      if (e.getType() == FormEvent.TYPE_CLOSED) {
        close();
      }
    });
    return form;
  }

  protected abstract IForm createForm();

  @Override
  public void open() {
    ensureFormStarted();
    super.open();
  }

  public void ensureFormStarted() {
    if (getWidget() == null || !getWidget().isFormStartable()) {
      return;
    }
    startForm();
  }

  /**
   * Starts the form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method.
   */
  protected void startForm() {
    getWidget().start();
  }

  protected void decorateForm(IForm form) {
    form.setShowOnStart(false);
    form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
  }

  @Override
  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    widgetsToDispose.remove(getWidget()); // form is closed in disposeInternal
    super.disposeChildren(widgetsToDispose);
  }

  @Override
  protected void initChildren(List<? extends IWidget> widgets) {
    widgets.remove(getWidget()); // is initialized on first use
    super.initChildren(widgets);
  }

  @Override
  protected void disposeInternal() {
    IForm form = getWidget();
    if (form != null && !form.isFormClosed()) {
      form.doClose();
    }
    super.disposeInternal();
  }
}
