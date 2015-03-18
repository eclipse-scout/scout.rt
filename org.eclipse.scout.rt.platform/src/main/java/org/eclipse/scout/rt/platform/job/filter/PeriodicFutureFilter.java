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
package org.eclipse.scout.rt.platform.job.filter;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Depending on the given 'periodic' argument, this filter accepts only periodic or non-periodic Futures.
 *
 * @since 5.1
 */
public class PeriodicFutureFilter implements IFilter<IFuture<?>> {

  private final boolean m_periodic;

  public PeriodicFutureFilter(final boolean periodic) {
    m_periodic = periodic;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future.isPeriodic() == m_periodic;
  }
}
