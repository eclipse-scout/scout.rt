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
package org.eclipse.scout.rt.platform.filter;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter which returns the logical 'AND' of the given Filters.
 *
 * @since 5.1
 */
public class AndFilter<ELEMENT> implements IFilter<ELEMENT> {

  private final List<IFilter<ELEMENT>> m_filters;

  @SafeVarargs
  public AndFilter(final IFilter<ELEMENT>... filters) {
    this(CollectionUtility.arrayList(filters));
  }

  public AndFilter(final Collection<IFilter<ELEMENT>> filters) {
    Assertions.assertTrue(filters.size() > 0, "Must have one filter at minimum");
    m_filters = CollectionUtility.arrayList(filters);
  }

  @Override
  public boolean accept(final ELEMENT element) {
    for (final IFilter<ELEMENT> filter : m_filters) {
      if (!filter.accept(element)) {
        return false;
      }
    }
    return true;
  }
}
