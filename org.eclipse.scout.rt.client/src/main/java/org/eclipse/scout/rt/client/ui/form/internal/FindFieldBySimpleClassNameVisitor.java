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
package org.eclipse.scout.rt.client.ui.form.internal;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class FindFieldBySimpleClassNameVisitor implements IFormFieldVisitor {
  private String m_simpleName;
  private IFormField m_found;

  public FindFieldBySimpleClassNameVisitor(String simpleName) {
    m_simpleName = simpleName;
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (field.getClass().getSimpleName().equalsIgnoreCase(m_simpleName)) {
      m_found = field;
    }
    return m_found == null;
  }

  public IFormField getField() {
    return m_found;
  }

}
