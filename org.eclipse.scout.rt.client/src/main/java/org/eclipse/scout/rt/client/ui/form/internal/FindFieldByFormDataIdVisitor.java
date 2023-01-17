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
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.FormDataUtility;

/**
 * Visitor for finding a form field by its form data field Id.
 *
 * @see IFormField#getFieldId()
 * @see FormDataUtility#getFieldDataId(String)
 */
public class FindFieldByFormDataIdVisitor implements Function<IFormField, TreeVisitResult> {

  private static final CompositeObject PERFECT_VALUE_FIELD_MATCH_KEY = new CompositeObject(0, 0);
  private static final Pattern FIELD_PATH_SPLIT_PATTERN = Pattern.compile("[" + AbstractFormData.FIELD_PATH_DELIM + "]");

  private final String[] m_fieldIdParts;
  private final SortedMap<CompositeObject, IFormField> m_prioMap;
  /**
   * {@link IForm} this visitor starts on. The closer a field is embedded into this form, the more likely it will be the
   * result of this visitor. may be <code>null</code>
   */
  private IForm m_searchContextRootForm;

  public FindFieldByFormDataIdVisitor(String fieldId) {
    this(fieldId, null);
  }

  /**
   * @param fieldId
   *          The field Id of the {@link IFormField} to find
   * @param searchContextRootForm
   *          Optional form the visitor starts on. If <code>null</code>, the form is derived from the first field
   *          visited.
   * @since 3.8.2
   */
  public FindFieldByFormDataIdVisitor(String fieldId, IForm searchContextRootForm) {
    m_searchContextRootForm = searchContextRootForm;
    m_fieldIdParts = FIELD_PATH_SPLIT_PATTERN.split(fieldId);
    m_prioMap = new TreeMap<>();
  }

  @Override
  public TreeVisitResult apply(IFormField field) {
    if (m_searchContextRootForm == null) {
      // auto-initialize search context
      // we assume the form field tree is traversed pre-order (i.e. first node itself, then its children)
      m_searchContextRootForm = field.getForm();
    }
    if (matchesAllParts(field)) {
      int fieldTypeRank = 0;
      if (m_searchContextRootForm != null) {
        IForm form = field.getForm();
        while (form != null && form != m_searchContextRootForm) {
          fieldTypeRank += 10;
          form = form.getOuterForm();
        }
      }
      if (field instanceof IValueField) {
        // fieldTypeRank is fine
      }
      else if (field instanceof ITableField) {
        fieldTypeRank += 1;
      }
      else if (!(field instanceof ICompositeField)) {
        fieldTypeRank += 2;
      }
      else {
        fieldTypeRank += 3;
      }
      // Compute the enclosing field path rank that is used as additional hint for determining the
      // best matching form field for the requested formId. Note: for compatibility reasons, the enclosing
      // field path is not enforced.
      int enclosingFieldPathRank = getEnclosingFieldPathRank(field);
      m_prioMap.put(new CompositeObject(fieldTypeRank, enclosingFieldPathRank), field);
    }
    boolean found = m_prioMap.containsKey(PERFECT_VALUE_FIELD_MATCH_KEY);

    if (found) {
      return TreeVisitResult.TERMINATE;
    }
    return TreeVisitResult.CONTINUE;
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
   * @return Returns the rank of the given field's enclosing field path and the one that is requested by this visitor. A
   *         perfect match yields 0. Every mismatch increases the rank by 1. Hence the greatest number has the worst
   *         match.
   */
  private int getEnclosingFieldPathRank(IFormField f) {
    int rank = 0;
    int i = m_fieldIdParts.length - 2; // the last segment is the field id, i.e. not part of the enclosing field path
    List<ICompositeField> enclosingFieldList = f.getEnclosingFieldList();
    Collections.reverse(enclosingFieldList);
    for (ICompositeField p : enclosingFieldList) {
      if (i >= 0 && m_fieldIdParts[i].equals(FormDataUtility.getFieldDataId(p.getFieldId()))) {
        i--;
      }
      else {
        rank++;
      }
    }
    return rank;
  }

  public IFormField getField() {
    return !m_prioMap.isEmpty() ? m_prioMap.get(m_prioMap.firstKey()) : null;
  }
}
