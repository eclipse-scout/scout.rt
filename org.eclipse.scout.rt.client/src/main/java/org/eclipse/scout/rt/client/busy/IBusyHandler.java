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
package org.eclipse.scout.rt.client.busy;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;

/**
 * Shows blocking progress on a long operation for a {@link ModelJobManager} with respect to
 * {@link #acceptFuture(IFuture, IJobManager)}.
 * <p>
 * The decision whether or not the progress should be visible is made in the acceptor
 * {@link IBusyHandler#acceptFuture(IFuture, IJobManager)}
 *
 * @author imo
 * @since 3.8
 */
public interface IBusyHandler {

  /**
   * Decides whether to block the ui based on that running job.
   */
  boolean acceptFuture(IFuture<?> future);

  /**
   * callback when a job accepted by {@link #acceptFuture(IFuture)} begun running
   */
  void onJobBegin(IFuture<?> future);

  /**
   * callback when a job is done
   */
  void onJobEnd(IFuture<?> future);

  /**
   * callback when a {@link IBusyHandler} starts blocking the application.
   */
  void onBlockingBegin();

  /**
   * callback when a {@link IBusyHandler} ends blocking the application.
   */
  void onBlockingEnd();

  /**
   * Blocks the caller until the application is not blocked anymore. Has no effect if the application is not blocked at
   * the time of this method call.
   */
  void waitForBlockingToEnd();

  /**
   * @return <code>true</code> if the application is in blocking mode, e.g. by a long-running operation.
   */
  boolean isBlocking();

  boolean isBusy();

  Object getStateLock();

  void cancel();

  long getShortOperationMillis();

  long getLongOperationMillis();

  void setEnabled(boolean b);

  boolean isEnabled();

}
