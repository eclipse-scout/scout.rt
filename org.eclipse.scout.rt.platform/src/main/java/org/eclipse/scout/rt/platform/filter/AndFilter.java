/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.filter;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

/**
 * Filter which returns the logical 'AND' of the given Filters.
 *
 * @since 5.1
 */
public class AndFilter<ELEMENT> implements Predicate<ELEMENT> {

  private final List<Predicate<ELEMENT>> m_filters;

  @SafeVarargs
  public AndFilter(final Predicate<ELEMENT>... filters) {
    this(CollectionUtility.arrayList(filters));
  }

  public AndFilter(final Collection<Predicate<ELEMENT>> filters) {
    Assertions.assertTrue(!filters.isEmpty(), "Must have one filter at minimum");
    m_filters = CollectionUtility.arrayList(filters);
  }

  @Override
  public boolean test(final ELEMENT element) {
    for (final Predicate<ELEMENT> filter : m_filters) {
      if (!filter.test(element)) {
        return false;
      }
    }
    return true;
  }
}
