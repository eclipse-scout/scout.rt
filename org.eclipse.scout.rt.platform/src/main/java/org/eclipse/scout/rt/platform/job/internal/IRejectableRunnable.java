/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
