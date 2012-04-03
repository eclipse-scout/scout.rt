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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

/**
 * Visitor to determine the shortest unique id based on the class name and its ancestors.
 * Assumes that all fields have a single root group box. <br/>
 */
public class FindShortestUniqueIdVisitor implements IFormFieldVisitor {
  private final IFormField m_field;
  private final String[] m_ancestors;

  private IFormField m_found;
  private int m_maxMatchCount = 0;

  public FindShortestUniqueIdVisitor(IFormField field) {
    m_field = field;
    m_ancestors = getAncestorNames(field);
  }

  @Override
  public boolean visitField(IFormField field, int level, int fieldIndex) {
    int matchCount = getMatchCount(field);
    if (!m_field.equals(field) && matchCount > m_maxMatchCount) {
      m_found = field;
      m_maxMatchCount = matchCount;
    }
    return true;
  }

  public IFormField getField() {
    return m_found;
  }

  public int getMaxMatchCount() {
    return m_maxMatchCount;
  }

  public String getShortestUniqueId() {
    String id = m_ancestors[m_maxMatchCount];
    for (int i = m_maxMatchCount - 1; i >= 0; i--) {
      id = id + "$" + m_ancestors[i];
    }
    return id;
  }

  /**
   * @return number of matching parent classes including the class itself
   */
  public int getMatchCount(IFormField field) {
    if (field == null) {
      return 0;
    }
    String[] names1 = getAncestorNames(m_field);
    String[] names2 = getAncestorNames(field);

    int count = 0;
    for (int i = 0; i < names1.length; i++) {
      if (names1[i].equals(names2[i])) {
        count++;
      }
      else {
        break;
      }
    }
    return count;
  }

  /**
   * @return the class name of the field and the names of all its ancestors
   */
  public String[] getAncestorNames(IFormField f) {
    List<String> path = new ArrayList<String>();
    path.add(f.getClass().getSimpleName());
    while (f.getParentField() != null) {
      f = f.getParentField();
      path.add(f.getClass().getSimpleName());
    }
    path.add(f.getForm().getClass().getSimpleName());
    return path.toArray(new String[path.size()]);
  }
}
