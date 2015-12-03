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
package org.eclipse.scout.rt.server.scheduler;

public interface ISchedulerJob {

  /**
   * jobs can be grouped using this groupId
   */
  String getGroupId();

  /**
   * every job (should) have a unique id which is composed of the jobId and the groupId
   */
  String getJobId();

  /**
   * This call is synchronous from the job queue and therefore must not block for long times during execution
   */
  boolean acceptTick(TickSignal signal);

  void run(IScheduler scheduler, TickSignal signal);

  /**
   * the interrupted property is set to false by the scheduler every time just before the job is run the interrupted
   * property is set to true by the scheduler whenever the scheduler or one of its job should interrupt
   */
  boolean isInterrupted();

  void setInterrupted(boolean b);

  /**
   * the dispose property when set to true (by the scheduler or by the job itself) causes the scheduler to remove the
   * job from the queue after its pending execution when a job is (re-) added to the scheduler this property is set to
   * false by the scheduler
   */
  boolean isDisposed();

  void setDisposed(boolean b);
}
