/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.popup;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;

/**
 * @since 9.0
 */
@ClassId("5930294f-05ea-4fd3-be84-963aac6ee4f8")
public abstract class AbstractFormPopup<T extends IForm> extends AbstractWidgetPopup<T> {

  public AbstractFormPopup() {
    this(true);
  }

  public AbstractFormPopup(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * Uses {@link #createForm()} to create a new form instance.
   *
   * @return the newly created form.
   */
  @Override
  protected T createContent() {
    return createForm();
  }

  @Override
  public void setContent(T content) {
    super.setContent(content);
    if (content != null) {
      decorateForm(content);
    }
  }

  /**
   * Creates a new instance of the form specified by {@link #getConfiguredContent()}. Can be overridden to create the new
   * instance manually.
   *
   * @return a new form instance.
   */
  protected T createForm() {
    Class<T> configuredContent = getConfiguredContent();
    if (configuredContent != null) {
      return ConfigurationUtility.newInnerInstance(this, configuredContent);
    }
    return null;
  }

  @Override
  public void open() {
    ensureFormStarted();
    super.open();
  }

  public void ensureFormStarted() {
    if (getContent() == null || !getContent().isFormStartable()) {
      return;
    }
    startForm();
    FormListener listener = e -> {
      if (e.getType() == FormEvent.TYPE_CLOSED) {
        close();
      }
    };
    getContent().addFormListener(listener);
  }

  /**
   * Starts the form.
   * <p>
   * The default uses {@link IForm#start()} and therefore expects a form handler to be previously set. Override to call
   * a custom start method.
   */
  protected void startForm() {
    getContent().start();
  }

  protected void decorateForm(IForm form) {
    form.setShowOnStart(false);
    form.setDisplayHint(IForm.DISPLAY_HINT_VIEW);
  }

  @Override
  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    widgetsToDispose.remove(getContent()); // form is closed in disposeInternal
    super.disposeChildren(widgetsToDispose);
  }

  @Override
  protected void initChildren(List<? extends IWidget> widgets) {
    widgets.remove(getContent()); // is initialized on first use
    super.initChildren(widgets);
  }

  @Override
  protected void disposeInternal() {
    IForm form = getContent();
    if (form != null && !form.isFormClosed()) {
      form.doClose();
    }
    super.disposeInternal();
  }
}
