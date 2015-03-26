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
package org.eclipse.scout.rt.platform.job.internal.future;

import java.util.concurrent.RunnableFuture;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;

/**
 * FutureTask to be given to the executor for execution.
 *
 * @since 5.1
 * @see JobFutureTask
 * @see MutexAcquisitionFutureTask
 */
@Internal
public interface IFutureTask<RESULT> extends RunnableFuture<RESULT> {

  /**
   * Invoke this method if the Future was rejected by the executor.
   */
  void reject();

  /**
   * @return if being a mutex task, this method returns the mutex object, e.g. the session for model jobs.
   */
  Object getMutexObject();

  /**
   * @return <code>true</code> if this task is to be executed in sequence among tasks with the same mutex object,
   *         <code>false</code> otherwise.
   */
  boolean isMutexTask();

  /**
   * @return <code>true</code> if this task is a mutex task and currently owns the mutex.
   */
  boolean isMutexOwner();

  /**
   * Invoke this method to mark this task as blocked because waiting for a blocking condition to fall.
   *
   * @param blocked
   *          <code>true</code> if entering a blocking condition, <code>false</code> once the condition falls.
   * @see IBlockingCondition
   */
  void setBlocked(boolean blocked);
}
