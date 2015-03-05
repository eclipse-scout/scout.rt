package org.eclipse.scout.rt.server.job;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.filter.OrFilter;
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
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}.<br/>
   * e.g. <code>new AndFilter(new ServerSessionFilter(...), new NotFilter(new CurrentFuture()));</code>
   *
   * @param filter
   *          Filter to accept the Futures to be cancelled. If <code>null</code>, all Futures are accepted, which is the
   *          same as using {@link AlwaysFilter}.
   * @param interruptIfRunning
   *          <code>true</code> to interrupt in-progress jobs.
   * @return <code>true</code> if all Futures and associated transactions are cancelled successfully, or
   *         <code>false</code>, if some Futures could
   *         not be cancelled, typically because already completed normally.
   */
  @Override
  boolean cancel(IFilter<IFuture<?>> filter, boolean interruptIfRunning);
}
