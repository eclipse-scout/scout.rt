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
package org.eclipse.scout.commons.job.filter;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IFuture;

/**
 * Filter which discards all Futures except the given one.
 *
 * @since 5.1
 */
public class SameFutureFilter implements IFilter<IFuture<?>> {

  private final IFuture<?> m_future;

  public SameFutureFilter(final IFuture<?> future) {
    m_future = future;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_future == future;
  }
}
