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

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Filter which accepts all Futures that are assigned to the given {@link IExecutionSemaphore}.
 *
 * @since 5.1
 */
public class ExecutionSemaphoreFutureFilter implements Predicate<IFuture<?>> {

  private final IExecutionSemaphore m_semaphore;

  public ExecutionSemaphoreFutureFilter(final IExecutionSemaphore semaphore) {
    m_semaphore = semaphore;
  }

  @Override
  public boolean test(final IFuture<?> future) {
    return ObjectUtility.equals(m_semaphore, future.getJobInput().getExecutionSemaphore());
  }
}
