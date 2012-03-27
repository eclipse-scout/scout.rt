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
package org.eclipse.scout.rt.client.ui.form.internal;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Visitor to find a field with the same XMLFieldId.
 */
public class FindFieldByXMLFieldIdVisitor implements IFormFieldVisitor {
  private String m_xmlFieldId;
  private IFormField m_found;

  public FindFieldByXMLFieldIdVisitor(String xmlId) {
    m_xmlFieldId = xmlId;
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (m_xmlFieldId.equals(field.getXMLFieldId())) {
      m_found = field;
    }
    return m_found == null;
  }

  public IFormField getField() {
    return m_found;
  }

}
