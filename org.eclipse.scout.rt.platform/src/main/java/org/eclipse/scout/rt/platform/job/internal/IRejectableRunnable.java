/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;

/**
 * Runnable to be given to the executor, and which is notified if being rejected by the executor. This may occur when
 * being scheduled and no more threads or queue slots are available, or upon shutdown of the executor.
 *
 * @since 5.1
 */
public interface IRejectableRunnable extends Runnable {

  /**
   * Rejects this runnable from being executed. If this task is assigned to an {@link IExecutionSemaphore}, this task
   * might still be a permit owner.
   */
  void reject();
}
