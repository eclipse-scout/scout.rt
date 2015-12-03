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
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
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

  /**
   * @return the current inner form.
   *         <p>
   *         <b>Please note:</b> This form may already be closed!
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
   *          The form to wrap
   * @param manageFormLifeCycle
   *          If <code>true</code>, the {@link IForm} is started with the currently set {@link IFormHandler}, or with
   *          {@link NullFormHandler} if not set, and is closed once this {@link IFormField} is disposed, or another
   *          inner form is set. If <code>false</code>, the caller is responsible for starting and closing the Form.
   */
  void setInnerForm(T form, boolean manageFormLifeCycle);

  boolean visitFields(IFormFieldVisitor visitor, int startLevel);
}
