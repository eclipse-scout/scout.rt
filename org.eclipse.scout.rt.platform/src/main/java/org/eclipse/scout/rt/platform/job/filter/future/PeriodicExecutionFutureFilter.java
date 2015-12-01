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
package org.eclipse.scout.rt.platform.job.filter.future;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Filter to accept periodic executing jobs. That are jobs, which are executed periodically, either 'at a fixed rate',
 * or 'with a fixed delay'.
 *
 * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE
 * @see JobInput#SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY
 * @since 5.1
 */
public class PeriodicExecutionFutureFilter implements IFilter<IFuture<?>> {

  public static final IFilter<IFuture<?>> INSTANCE = new PeriodicExecutionFutureFilter();

  private PeriodicExecutionFutureFilter() {
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future.getSchedulingRule() != JobInput.SCHEDULING_RULE_SINGLE_EXECUTION;
  }
}
