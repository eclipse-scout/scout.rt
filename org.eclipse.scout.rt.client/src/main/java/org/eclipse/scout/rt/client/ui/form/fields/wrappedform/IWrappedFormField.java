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
   */
  void setInnerForm(T newInnerForm);

  boolean visitFields(IFormFieldVisitor visitor, int startLevel);
}
