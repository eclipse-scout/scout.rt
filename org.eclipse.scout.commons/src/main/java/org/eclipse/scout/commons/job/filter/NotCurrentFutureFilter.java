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
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.job.IFuture;

/**
 * Filter which accepts all Futures except the current one.
 *
 * @see IFuture#CURRENT
 * @since 5.1
 */
public class NotCurrentFutureFilter implements IFilter<IFuture<?>> {

  private final IFilter<IFuture<?>> m_filter = new NotFilter<>(new CurrentFutureFilter());

  @Override
  public boolean accept(final IFuture<?> future) {
    return m_filter.accept(future);
  }
}
