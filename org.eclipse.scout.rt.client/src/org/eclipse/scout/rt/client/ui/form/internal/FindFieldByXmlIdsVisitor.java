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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * @since 3.8.0
 */
public class FindFieldByXmlIdsVisitor implements IFormFieldVisitor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FindFieldByXmlIdsVisitor.class);

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
      IFormField existingMatch = m_prioMap.put(key, field);
      if (existingMatch != null && !m_ambiguousFieldKeys.contains(key)) {
        if (Platform.inDevelopmentMode() && LOG.isWarnEnabled()) {
          LOG.warn("ignoring ambiguous fieldId: " + Arrays.toString(m_xmlFieldIds) + " matching fields [" + existingMatch + "] and [" + field + "]");
        }
        m_ambiguousFieldKeys.add(key);
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
    Class<?> currentEnclosingContainerType = ConfigurationUtility.getEnclosingContainerType(f);
    ICompositeField p = f.getParentField();
    while (p != null) {
      Class<?> enclosingContainerType = ConfigurationUtility.getEnclosingContainerType(p);
      if (enclosingContainerType != currentEnclosingContainerType) {
        if (i >= 0 && p.getFieldId().equals(m_xmlFieldIds[i])) {
          i--;
        }
        else {
          rank--;
        }
        currentEnclosingContainerType = enclosingContainerType;
      }
      p = p.getParentField();
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
      return null;
    }
    return candidate.getValue();
  }
}
