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
   *          the fields that should not be accepted by the filter.
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
