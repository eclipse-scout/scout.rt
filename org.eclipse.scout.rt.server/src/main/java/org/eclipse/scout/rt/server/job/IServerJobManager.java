package org.eclipse.scout.rt.server.job;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.IScheduler;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
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
 * <li>exceptions are translated into {@link ProcessingException}s; see {@link ExceptionTranslator} for more
 * information;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IFuture#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT}, {@link IHttpServletRoundtrip#CURRENT_HTTP_SERVLET_REQUEST},
 * {@link IHttpServletRoundtrip#CURRENT_HTTP_SERVLET_RESPONSE};</li>
 * </ul>
 *
 * @since 5.1
 */
public interface IServerJobManager extends IJobManager<ServerJobInput>, IScheduler<ServerJobInput> {

  /**
   * Cancels all Futures and associated transactions which are accepted by the given Filter. Also, any nested
   * 'runNow'-style jobs, which where run on behalf of accepted jobs and did not complete yet, are cancelled as well.
   *
   * @param filter
   *          Filter to control the Futures to be cancelled.
   * @return <code>true</code> if cancel was successful, <code>false</code> otherwise.
   */
  @Override
  public boolean cancel(IFilter<IFuture<?>> filter);
}
