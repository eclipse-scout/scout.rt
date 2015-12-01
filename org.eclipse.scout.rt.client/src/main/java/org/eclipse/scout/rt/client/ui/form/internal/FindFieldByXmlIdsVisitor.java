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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.8.0
 */
public class FindFieldByXmlIdsVisitor implements IFormFieldVisitor {
  private static final Logger LOG = LoggerFactory.getLogger(FindFieldByXmlIdsVisitor.class);

  private final String[] m_xmlFieldIds;

  /**
   * Tree map with candidates. The entry with the largest key is the best matching field.
   */
  private final TreeMap<CompositeObject, IFormField> m_prioMap;

  /**
   * Set with ambiguous field keys.
   */
  private final Set<CompositeObject> m_ambiguousFieldKeys;

  public FindFieldByXmlIdsVisitor(String... xmlFieldIds) {
    m_xmlFieldIds = xmlFieldIds;
    m_prioMap = new TreeMap<CompositeObject, IFormField>();
    m_ambiguousFieldKeys = new HashSet<CompositeObject>();
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    int fieldIdRank = getFieldIdRank(field);
    if (fieldIdRank > 0) {
      int enclosingFieldPathRank = getEnclosingFieldPathRank(field);
      CompositeObject key;
      if (field instanceof IValueField) {
        key = new CompositeObject(fieldIdRank, enclosingFieldPathRank, 2);
      }
      else if (!(field instanceof ICompositeField)) {
        key = new CompositeObject(fieldIdRank, enclosingFieldPathRank, 1);
      }
      else {
        key = new CompositeObject(fieldIdRank, enclosingFieldPathRank, 0);
      }
      if (m_prioMap.containsKey(key)) {
        m_ambiguousFieldKeys.add(key);
      }
      else {
        m_prioMap.put(key, field);
      }
    }
    return true;
  }

  /**
   * @return Returns the rank of the given field's fieldId path and the one requested by this visitor. A perfect match
   *         yields the largest number. 0 is returned if there is no match at all.
   */
  private int getFieldIdRank(IFormField f) {
    // Class primary type
    int i = m_xmlFieldIds.length - 1;
    if (m_xmlFieldIds[i].equals(f.getFieldId())) {
      i--;
      f = f.getParentField();
    }
    else {
      // field does not match
      return 0;
    }
    while (i >= 0 && f != null) {
      if (m_xmlFieldIds[i].equals(f.getFieldId())) {
        i--;
      }
      f = f.getParentField();
    }
    return m_xmlFieldIds.length - 1 - i;
  }

  /**
   * @return Returns the rank of the given field's enclosing field path and the one the is requested by this visitor. A
   *         perfect match yields 0. Every mismatch reduces the match by 1. Hence the smallest (negative) number has the
   *         worst match.
   */
  private int getEnclosingFieldPathRank(IFormField f) {
    int rank = 0;
    int i = m_xmlFieldIds.length - 2; // the last segment is the field id, i.e. not part of the enclosing field path
    List<ICompositeField> enclosingFieldList = f.getEnclosingFieldList();
    Collections.reverse(enclosingFieldList);
    for (ICompositeField p : enclosingFieldList) {
      if (i >= 0 && p.getFieldId().equals(m_xmlFieldIds[i])) {
        i--;
      }
      else {
        rank--;
      }
    }
    return rank;
  }

  /**
   * @return Returns the best matching form field or <code>null</code>.
   */
  public IFormField getField() {
    if (m_prioMap.isEmpty()) {
      return null;
    }
    Entry<CompositeObject, IFormField> candidate = m_prioMap.lastEntry();
    if (m_ambiguousFieldKeys.contains(candidate.getKey())) {
      if (Platform.get().inDevelopmentMode() && LOG.isWarnEnabled()) {
        LOG.warn("ambiguous fieldId: " + Arrays.toString(m_xmlFieldIds) + " returning first candidate [" + candidate.getValue() + "]");
      }
    }
    return candidate.getValue();
  }
}
