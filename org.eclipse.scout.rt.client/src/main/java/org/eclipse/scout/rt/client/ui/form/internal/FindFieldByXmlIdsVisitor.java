/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

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
public class FindFieldByXmlIdsVisitor implements Consumer<IFormField> {
  private static final Logger LOG = LoggerFactory.getLogger(FindFieldByXmlIdsVisitor.class);

  private final String[] m_xmlFieldIds;

  /**
   * Tree map with candidates. The entry with the largest key is the best matching field.
   */
  private final NavigableMap<CompositeObject, IFormField> m_prioMap;

  /**
   * Set with ambiguous field keys.
   */
  private final Set<CompositeObject> m_ambiguousFieldKeys;

  public FindFieldByXmlIdsVisitor(String... xmlFieldIds) {
    m_xmlFieldIds = xmlFieldIds;
    m_prioMap = new TreeMap<>();
    m_ambiguousFieldKeys = new HashSet<>();
  }

  @Override
  public void accept(IFormField field) {
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
    if (m_ambiguousFieldKeys.contains(candidate.getKey()) && Platform.get().inDevelopmentMode()) {
      LOG.warn("ambiguous fieldId: {} returning first candidate [{}]", m_xmlFieldIds, candidate.getValue());
    }
    return candidate.getValue();
  }
}
