package org.eclipse.scout.rt.server.job;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job manager to execute jobs that operate on a {@link IServerSession} and provides jobs with the server-context.
 * Typically, there is a single {@link IServerJobManager} installed per application.
 * <p/>
 * <strong>Every time that you run/schedule a job, a transaction is started and committed upon successful completion or
 * rolled back otherwise.</strong>
 * <p/>
 * While running, jobs executed on behalf of this job manager comply with the following characteristics:
 * <ul>
 * <li>run in parallel among other server jobs;</li>
 * <li>run in a new transaction;</li>
 * <li>are optionally executed on behalf of a {@link Subject};</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IFuture#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT}, {@link IHttpServletRoundtrip#CURRENT_HTTP_SERVLET_REQUEST},
 * {@link IHttpServletRoundtrip#CURRENT_HTTP_SERVLET_RESPONSE};</li>
 * </ul>
 *
 * @since 5.1
 */
public interface IServerJobManager extends IJobManager<ServerJobInput> {

  /**
   * TODO [dwi/aho]: Remove me and replace with CDI.
   */
  IServerJobManager DEFAULT = new ServerJobManager();

  /**
   * Cancels a job with its associated transaction.
   * <p/>
   * Also, any nested 'runNow'-style jobs, which where run on behalf of that job and did not complete yet, are
   * cancelled, as well as any associated transactions. In order to be cancelled, the given session must be the same as
   * the job's session.
   *
   * @param id
   *          id of the job to be cancelled.
   * @param serverSession
   *          session which the job to be cancelled must belong to; must not be <code>null</code>.
   * @return <code>true</code> if cancel was successful and transaction was in fact cancelled, <code>false</code>
   *         otherwise.
   */
  boolean cancel(long id, IServerSession serverSession);
}
