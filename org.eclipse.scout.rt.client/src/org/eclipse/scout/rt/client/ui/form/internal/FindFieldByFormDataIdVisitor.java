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

import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;

public class FindFieldByFormDataIdVisitor implements IFormFieldVisitor {

  private static final CompositeObject PERFECT_VALUE_FIELD_MATCH_KEY = new CompositeObject(0, 0);

  private String[] m_fieldIdParts;
  private TreeMap<CompositeObject, IFormField> m_prioMap;

  public FindFieldByFormDataIdVisitor(String fieldId) {
    m_fieldIdParts = fieldId.split("[/]");
    m_prioMap = new TreeMap<CompositeObject, IFormField>();
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    if (matchesAllParts(field)) {
      int fieldTypeRank;
      if (field instanceof IValueField) {
        fieldTypeRank = 0;
      }
      else if (!(field instanceof ICompositeField)) {
        fieldTypeRank = 1;
      }
      else {
        fieldTypeRank = 2;
      }
      // Compute the enclosing field path rank that is used as additional hint for determining the
      // best matching form field for the requested formId. Note: for compatibility reasons, the enclosing
      // field path is not enforced.
      int enclosingFieldPathRank = getEnclosingFieldPathRank(field);
      m_prioMap.put(new CompositeObject(fieldTypeRank, enclosingFieldPathRank), field);
    }
    return !m_prioMap.containsKey(PERFECT_VALUE_FIELD_MATCH_KEY);
  }

  /**
   * field must match last part and (in correct sequence) all prior parts
   */
  private boolean matchesAllParts(IFormField f) {
    int i = m_fieldIdParts.length - 1;
    if (m_fieldIdParts[i].equals(FormDataUtility.getFieldDataId(f.getFieldId()))) {
      i--;
      f = f.getParentField();
    }
    else {
      return false;
    }
    while (i >= 0 && f != null) {
      if (m_fieldIdParts[i].equals(FormDataUtility.getFieldDataId(f.getFieldId()))) {
        i--;
      }
      f = f.getParentField();
    }
    return i < 0;
  }

  /**
   * @return Returns the rank of the given field's enclosing field path and the one the is requested by this visitor. A
   *         perfect match yields 0. Every mismatch increases the rank by 1. Hence the greatest number has the worst
   *         match.
   */
  private int getEnclosingFieldPathRank(IFormField f) {
    int rank = 0;
    int i = m_fieldIdParts.length - 2; // the last segment is the field id, i.e. not part of the enclosing field path
    Class<?> currentEnclosingContainerType = ConfigurationUtility.getEnclosingContainerType(f);
    ICompositeField p = f.getParentField();
    while (p != null) {
      Class<?> enclosingContainerType = ConfigurationUtility.getEnclosingContainerType(p);
      if (enclosingContainerType != currentEnclosingContainerType) {
        if (i >= 0 && m_fieldIdParts[i].equals(FormDataUtility.getFieldDataId(p.getFieldId()))) {
          i--;
        }
        else {
          rank++;
        }
        currentEnclosingContainerType = enclosingContainerType;
      }
      p = p.getParentField();
    }
    return rank;
  }

  public IFormField getField() {
    return m_prioMap.size() > 0 ? m_prioMap.get(m_prioMap.firstKey()) : null;
  }
}
