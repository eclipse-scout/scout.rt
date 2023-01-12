/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
