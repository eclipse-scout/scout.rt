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

import java.util.TreeMap;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public class FindFieldByFormDataIdVisitor implements IFormFieldVisitor {
  private String[] m_fieldIdParts;
  private TreeMap<Integer, IFormField> m_prioMap;

  public FindFieldByFormDataIdVisitor(String fieldId) {
    m_fieldIdParts = fieldId.split("[/]");
    m_prioMap = new TreeMap<Integer, IFormField>();
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (matchesAllParts(field)) {
      if (field instanceof IValueField) {
        m_prioMap.put(0, field);
      }
      else if (!(field instanceof ICompositeField)) {
        m_prioMap.put(1, field);
      }
      else {
        m_prioMap.put(2, field);
      }
    }
    return !m_prioMap.containsKey(0);
  }

  /**
   * field must match last part and (in correct sequence) all prior parts
   */
  private boolean matchesAllParts(IFormField f) {
    int i = m_fieldIdParts.length - 1;
    if (m_fieldIdParts[i].equals(f.getFieldId())) {
      i--;
      f = f.getParentField();
    }
    else {
      return false;
    }
    while (i >= 0 && f != null) {
      if (m_fieldIdParts[i].equals(f.getFieldId())) {
        i--;
      }
      f = f.getParentField();
    }
    return i < 0;
  }

  public IFormField getField() {
    return m_prioMap.size() > 0 ? m_prioMap.get(m_prioMap.firstKey()) : null;
  }

}
