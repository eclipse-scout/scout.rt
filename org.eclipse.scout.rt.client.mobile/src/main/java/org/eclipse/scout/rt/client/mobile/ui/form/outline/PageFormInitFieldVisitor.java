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
package org.eclipse.scout.rt.client.mobile.ui.form.outline;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.IWrappedFormField;

/**
 * Compared to the default InitFormVisitor used in
 * {@link FormUtility#initFormFields(org.eclipse.scout.rt.client.ui.form.IForm)), the inner form of a wrappedFormField
 * is never initialized. This is necessary to avoid a double initialization because the inner form always is initialized
 * already.
 */
public class PageFormInitFieldVisitor implements IFormFieldVisitor {
  private RuntimeException m_firstEx;
  private Set<IForm> m_formsToIgnore;

  public PageFormInitFieldVisitor() {
    m_formsToIgnore = new HashSet<IForm>();
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    try {
      if (allowInitField(field)) {
        field.initField();
      }
    }
    catch (RuntimeException e) {
      if (m_firstEx == null) {
        m_firstEx = e;
      }
    }
    return true;
  }

  public void handleResult() {
    m_formsToIgnore.clear();
    if (m_firstEx != null) {
      throw m_firstEx;
    }
  }

  private boolean allowInitField(IFormField field) {
    if (field instanceof IWrappedFormField<?>) {
      IForm innerForm = ((IWrappedFormField<?>) field).getInnerForm();
      if (innerForm != null) {
        m_formsToIgnore.add(innerForm);
      }
      return true;
    }

    //Don't initialize the fields of the inner form of a wrapped form field
    if (m_formsToIgnore.contains(field.getForm())) {
      return false;
    }

    return true;
  }
}
