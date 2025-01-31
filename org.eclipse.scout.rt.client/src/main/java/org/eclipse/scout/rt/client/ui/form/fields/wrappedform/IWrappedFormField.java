/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormHandler;
import org.eclipse.scout.rt.client.ui.form.NullFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Representation of a {@link IForm} inside another form as inline field (wrapped)
 */
public interface IWrappedFormField<T extends IForm> extends IFormField {

  /**
   * {@link IForm}
   */
  String PROP_INNER_FORM = "innerForm";
  String PROP_INITIAL_FOCUS_ENABLED = "initialFocusEnabled";

  /**
   * @return the current inner form.
   * <p>
   * <b>Please note:</b> This form may already be closed!
   */
  T getInnerForm();

  /**
   * Installs the given {@link IForm} as wrapped Form. Thereby, the Form must not be started yet. A previously installed
   * Form will be closed if <code>manageFormLifeCycle</code> was set to <code>true</code>.
   * <p>
   * This method is equal to {@link #setInnerForm(IForm, boolean)} with automatic form life cycle management.
   */
  void setInnerForm(T newInnerForm);

  /**
   * Installs the given {@link IForm} as wrapped Form. Thereby, the Form must not be started yet. A previously installed
   * Form will be closed if <code>manageFormLifeCycle</code> was set to <code>true</code>.
   *
   * @param form
   *     The form to wrap
   * @param manageFormLifeCycle
   *     If <code>true</code>, the {@link IForm} is started with the currently set {@link IFormHandler}, or with
   *     {@link NullFormHandler} if not set, and is closed once this {@link IFormField} is disposed, or another
   *     inner form is set. If <code>false</code>, the caller is responsible for starting and closing the Form.
   */
  void setInnerForm(T form, boolean manageFormLifeCycle);

  /**
   * @return {@code true} if the lifecycle of the {@link IForm} is managed by the {@link IWrappedFormField},
   * {@code false} otherwise.
   */
  boolean isManageInnerFormLifeCycle();

  /**
   * @return {@code true} if the inner form should request the initial focus once loaded, {@code false} otherwise.
   */
  boolean isInitialFocusEnabled();

  void setInitialFocusEnabled(boolean initialFocusEnabled);
}
