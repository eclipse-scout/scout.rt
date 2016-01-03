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
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.util.CompareUtility;

/**
 * Filter which accepts all Futures that are assigned to the given {@link IExecutionSemaphore}.
 *
 * @since 5.1
 */
public class ExecutionSemaphoreFutureFilter implements IFilter<IFuture<?>> {

  private final IExecutionSemaphore m_semaphore;

  public ExecutionSemaphoreFutureFilter(final IExecutionSemaphore semaphore) {
    m_semaphore = semaphore;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return CompareUtility.equals(m_semaphore, future.getJobInput().getExecutionSemaphore());
  }
}
