/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.session;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.service.IService;

/**
 * @since 3.8.0
 */
@Priority(-3)
@RemoteServiceAccessDenied
public interface ISessionService extends IService {

  /**
   * Returns the session which is assigned to the current client respectively server job.
   * <p>
   * It's recommended to use this service only if you don't have direct access to the session. Rather use
   * ClientJob.getCurrentSession() respectively ServerJob.getCurrentSession().
   * </p>
   */
  ISession getCurrentSession();

  /**
   * @see ISessionService#createAsyncJob(String, IJobRunnable)
   * @since 3.8.1
   */
  JobEx createAsyncJob(IJobRunnable runnable);

  /**
   * Creates a new asynchronous job that executes the given runnable. The job is attached to the current environment's
   * {@link ISession} and implementors are required to use an appropriate {@link JobEx} extension (i.e. ClientAsyncJob
   * or ServerJob).
   * 
   * @param name
   *          the job's name or <code>null</code>.
   * @param runnable
   *          the runnable to execute within the created job.
   * @return Returns a new {@link JobEx} implementation that executes the given runnable asynchronously.
   * @since 3.8.1
   */
  JobEx createAsyncJob(String name, IJobRunnable runnable);
}
