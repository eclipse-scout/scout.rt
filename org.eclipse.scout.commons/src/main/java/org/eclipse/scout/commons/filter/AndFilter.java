/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.filter;

import org.eclipse.scout.commons.Assertions;

/**
 * Filter which returns the logical 'AND' of two other filters.
 *
 * @since 5.1
 */
public class AndFilter<ELEMENT> implements IFilter<ELEMENT> {

  private final IFilter<ELEMENT>[] m_filters;

  @SafeVarargs
  public AndFilter(final IFilter<ELEMENT>... filters) {
    Assertions.assertTrue(filters.length > 0, "Must have one filter at minimum");
    m_filters = filters;
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
