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
package org.eclipse.scout.rt.platform.job.filter.future;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Depending on the given 'blocked' argument, this filter accepts only blocked or non-blocked Futures.
 *
 * @see IBlockingCondition
 * @since 5.1
 */
public class BlockedFutureFilter implements IFilter<IFuture<?>> {

  public static final IFilter<IFuture<?>> INSTANCE_BLOCKED = new BlockedFutureFilter(true);
  public static final IFilter<IFuture<?>> INSTANCE_NOT_BLOCKED = new BlockedFutureFilter(false);

  private final boolean m_blocked;

  private BlockedFutureFilter(final boolean blocked) {
    m_blocked = blocked;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future.isBlocked() == m_blocked;
  }
}
