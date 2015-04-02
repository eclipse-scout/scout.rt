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
package org.eclipse.scout.rt.platform.job;

/**
 * Monitor to track the progress of an activity.
 *
 * @since 5.1
 */
public interface IProgressMonitor {

  /**
   * The {@link IProgressMonitor} which is currently associated with the current thread.
   */
  ThreadLocal<IProgressMonitor> CURRENT = new ThreadLocal<IProgressMonitor>() {

    @Override
    protected IProgressMonitor initialValue() {
      // TODO [dwi][imo]: Most likely, IProgressMonitor can be removed without substitution.
      return new IProgressMonitor() {

        @Override
        public boolean isCancelled() {
          return false;
        }

        @Override
        public boolean cancel(final boolean interruptIfRunning) {
          return true;
        }
      };
    }
  };

  /**
   * @return <code>true</code> if this job was cancelled and the job should terminate its work.
   */
  boolean isCancelled();

  /**
   * Attempts to cancel the execution of the associated job. The <code>interruptIfRunning</code> parameter determines
   * whether the thread executing the job should be interrupted in an attempt to stop the job.
   *
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing the job should be interrupted; otherwise, in-progress jobs are
   *          allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally.
   */
  boolean cancel(boolean interruptIfRunning);
}
