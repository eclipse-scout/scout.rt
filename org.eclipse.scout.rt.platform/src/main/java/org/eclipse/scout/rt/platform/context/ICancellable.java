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
package org.eclipse.scout.rt.platform.context;

import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * This is any kind of object interested in active cancellation of a {@link RunMonitor#cancel(boolean)}.
 * <p>
 * Note that a {@link IFuture} represents itself a {@link RunMonitor}
 *
 * @since 5.1
 */
public interface ICancellable {

  /**
   * Attempts to cancel the execution of the associated run phase (maybe inside a job).
   *
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this run phase (maybe inside a job) should be interrupted;
   *          otherwise, in-progress jobs are allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally.
   */
  boolean cancel(boolean interruptIfRunning);

  /**
   * @return <code>true</code> if the associated run phase (maybe inside a job) was cancelled before it completed
   *         normally.
   */
  boolean isCancelled();
}
