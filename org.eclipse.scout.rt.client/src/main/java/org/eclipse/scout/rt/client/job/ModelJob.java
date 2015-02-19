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
package org.eclipse.scout.rt.client.job;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job to interact with the client model that operates on a {@link IClientSession} and provides your executing code with
 * the client-context.
 * Jobs of this type return no result to the caller and run on behalf of a dedicated {@link ModelJobManager}, meaning
 * that each session has its own {@link ModelJobManager}.
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in sequence among other model jobs (mutual exclusion);</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IJob#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 * <p/>
 * Within the same {@link ModelJobManager}, jobs are executed in sequence so that no more than one job will be active at
 * any given time. If a job gets blocked by entering a {@link IBlockingCondition}, the model-mutex will be released
 * which allows another model-job to run. When being unblocked, the job must compete for the model-mutex anew in order
 * to continue its execution.
 *
 * @see ModelJobWithResult
 * @see ModelJobManager
 * @since 5.1
 */
public abstract class ModelJob extends ModelJobWithResult<Void> {

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param clientSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   */
  public ModelJob(final String name, final IClientSession clientSession) {
    super(name, clientSession);
  }

  @Override
  protected final Void call() throws Exception {
    run();
    return null;
  }

  /**
   * This method is invoked by the {@link ModelJobManager} to run this job.
   *
   * @throws Exception
   *           if you encounter a problem that should be propagated to the caller; exceptions other than
   *           {@link ProcessingException} are wrapped into a {@link ProcessingException}.
   */
  protected abstract void run() throws Exception;
}
