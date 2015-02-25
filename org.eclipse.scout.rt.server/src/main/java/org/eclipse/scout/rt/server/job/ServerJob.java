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
package org.eclipse.scout.rt.server.job;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletRoundtrip;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IServerSession} and provides your executing code with the server-context.
 * Jobs of this type return no result to the caller and run on behalf of the the JVM-wide {@link JobManager}.
 * <p/>
 * <strong>Every time that you run a {@link ServerJob}, a transaction is started and committed upon successful
 * completion or rolled back otherwise.</strong>
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in parallel among other server jobs;</li>
 * <li>run in a new transaction;</li>
 * <li>are executed on behalf of a {@link Subject};</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IJob#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT}, {@link HttpServletRoundtrip#CURRENT_HTTP_SERVLET_REQUEST},
 * {@link HttpServletRoundtrip#CURRENT_HTTP_SERVLET_RESPONSE};</li>
 * </ul>
 *
 * @see ServerJobWithResult
 * @see JobManager
 * @since 5.1
 */
public abstract class ServerJob extends ServerJobWithResult<Void> {

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   */
  public ServerJob(final String name, final IServerSession serverSession) {
    super(name, serverSession);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed.
   */
  public ServerJob(final String name, final IServerSession serverSession, final Subject subject) {
    super(name, serverSession, subject);
  }

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param serverSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   * @param subject
   *          {@link Subject} of behalf of which this job is to be executed.
   * @param transactionId
   *          unique transaction <code>id</code> among the {@link IServerSession} or {@link ITransaction#TX_ZERO_ID} if
   *          not to be registered within the {@link IServerSession}; is primarily used to identify the transaction if
   *          the user likes to cancel a transaction.
   */
  public ServerJob(final String name, final IServerSession serverSession, final Subject subject, final long transactionId) {
    super(name, serverSession, subject, transactionId);
  }

  @Override
  protected final Void call() throws Exception {
    run();
    return null;
  }

  /**
   * This method is invoked by the {@link JobManager} to run this job.
   *
   * @throws Exception
   *           if you encounter a problem that should be propagated to the caller; exceptions other than
   *           {@link ProcessingException} are wrapped into a {@link ProcessingException}.
   */
  protected abstract void run() throws Exception;
}
