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

/**
 * Filter which returns the logical 'AND' of two other filters.
 *
 * @since 5.1
 */
public class AndFilter<ELEMENT> implements IFilter<ELEMENT> {

  private final IFilter<ELEMENT> m_filter1;
  private final IFilter<ELEMENT> m_filter2;

  public AndFilter(final IFilter<ELEMENT> filter1, final IFilter<ELEMENT> filter2) {
    m_filter1 = filter1;
    m_filter2 = filter2;
  }

  @Override
  public boolean accept(final ELEMENT element) {
    return m_filter1.accept(element) && m_filter2.accept(element);
  }
}
