/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link IFormFieldFilter} implementation that allows to provide a set of excluded {@link IFormField}s.<br>
 * The filter will accept all non-null fields that are not in the excluded list.<br>
 * The fields are compared using the {@link IFormField#getFieldId()}. So a field is discarded if the fieldId matches one
 * of the fieldIds of the excluded list.
 */
public class ExcludeFormFieldFilter implements IFormFieldFilter {

  private Set<String /* field ID */> m_excludedFields;

  /**
   * @param fields
   *     the fields that should not be accepted by the filter.
   */
  public ExcludeFormFieldFilter(IFormField... fields) {
    if (fields != null && fields.length > 0) {
      m_excludedFields = new HashSet<>(fields.length);
      for (IFormField f : fields) {
        if (f != null) {
          m_excludedFields.add(f.getFieldId());
        }
      }
    }
  }

  @Override
  public boolean accept(IFormField field) {
    if (m_excludedFields == null) {
      return true;
    }
    if (field == null) {
      return false;
    }
    return !m_excludedFields.contains(field.getFieldId());
  }
}
