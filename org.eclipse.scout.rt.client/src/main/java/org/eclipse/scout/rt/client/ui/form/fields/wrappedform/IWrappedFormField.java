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
package org.eclipse.scout.rt.client.ui.form.fields.wrappedform;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Representation of a {@link IForm} inside another form as inline field
 * (wrapped)
 */
public interface IWrappedFormField<T extends IForm> extends IFormField {

  /**
   * {@link IForm}
   */
  String PROP_INNER_FORM = "innerForm";

  T getInnerForm();

  /**
   * Install a (new) inner form into the wrapped form field.
   * <p>
   * Equal to {@link #setInnerForm(IForm, boolean)} without automatic form life cycle management.
   */
  void setInnerForm(T newInnerForm);

  /**
   * Installs a form into the wrapped form field. Any previously wrapped form will be uninstalled.
   *
   * @param form
   *          The form to wrap
   * @param manageFormLifeCycle
   *          If <code>true</code>, the wrapped form field automatically starts the inner form if is not yet open. It
   *          also closes the form automatically when the wrapped form field is disposed, or the inner form is replaced
   *          by another form. If <code>false</code> is passed, the caller is responsible for starting and disposing
   *          the inner form.
   * @throws ProcessingException
   *           May be thrown by the form handler when starting it
   */
  void setInnerForm(T form, boolean manageFormLifeCycle) throws ProcessingException;

  boolean visitFields(IFormFieldVisitor visitor, int startLevel);
}
