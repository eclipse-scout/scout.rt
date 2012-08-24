/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.internal;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.TypeCastUtility;

/**
 * @since 3.9.0
 */
public class AbstractCompositeExtensionFilter<T> {

  private final List<T> m_filters;

  public AbstractCompositeExtensionFilter(T... filters) {
    m_filters = new LinkedList<T>();
    if (filters != null) {
      for (T f : filters) {
        addFilter(f);
      }
    }
  }

  public boolean addFilter(T filter) {
    if (filter == null) {
      return false;
    }
    return m_filters.add(filter);
  }

  public boolean addFilterAtBegin(T filter) {
    if (filter == null) {
      return false;
    }
    m_filters.add(0, filter);
    return true;
  }

  public boolean removeFilter(T filter) {
    return m_filters.remove(filter);
  }

  public boolean isEmpty() {
    return m_filters.isEmpty();
  }

  public int size() {
    return m_filters.size();
  }

  public T[] getFilters() {
    Class typeParameter = TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractCompositeExtensionFilter.class);
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(typeParameter, m_filters.size());
    if (result.length == 0) {
      return result;
    }
    return m_filters.toArray(result);
  }
}
