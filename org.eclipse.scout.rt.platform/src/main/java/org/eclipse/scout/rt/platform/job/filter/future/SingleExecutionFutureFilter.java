/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.filter.future;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept only single executing jobs. That are jobs which are 'one-shot' executions, meaning executed just at
 * a single point in time.
 *
 * @see IFuture#isSingleExecution()
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
