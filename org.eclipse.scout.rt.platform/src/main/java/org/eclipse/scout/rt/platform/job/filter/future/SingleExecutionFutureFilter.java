/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.filter.future;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept only single executing jobs. That are jobs which are 'one-shot' executions, meaning executed just at
 * a single point in time.
 *
 * @see IFuture#isSingleExecuting(boolean)
 * @since 5.1
 */
public final class SingleExecutionFutureFilter implements Predicate<IFuture<?>> {

  public static final Predicate<IFuture<?>> INSTANCE = new SingleExecutionFutureFilter();

  private SingleExecutionFutureFilter() {
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return future.isSingleExecution();
  }
}
