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
 * Filter which negates the result of another filter.
 *
 * @since 5.1
 */
public class NotFilter<ELEMENT> implements IFilter<ELEMENT> {

  private final IFilter<ELEMENT> m_filter;

  public NotFilter(final IFilter<ELEMENT> filter) {
    Assertions.assertNotNull(filter, "Filter to negate must not be null");
    m_filter = filter;
  }

  @Override
  public boolean accept(final ELEMENT element) {
    return !m_filter.accept(element);
  }
}
