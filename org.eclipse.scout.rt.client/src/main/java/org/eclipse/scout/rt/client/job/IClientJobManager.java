package org.eclipse.scout.rt.client.job;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobManager;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.internal.ClientJobManager;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job manager to execute jobs that operate on a {@link IClientSession} and provides jobs with the client-context.
 * Typically, there is a single {@link IClientJobManager} installed per application.
 * <p/>
 * <strong>If interacting with the client-model, use {@link IModelJobManager}.</strong>
 * <p/>
 * While running, jobs executed on behalf of this job manager comply with the following characteristics:
 * <ul>
 * <li>run in parallel among other client jobs;</li>
 * <li>are optionally executed on behalf of a {@link Subject};</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IFuture#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 *
 * @since 5.1
 */
public interface IClientJobManager extends IJobManager<ClientJobInput> {

  /**
   * TODO [dwi/aho]: Remove me and replace with CDI.
   */
  IClientJobManager DEFAULT = new ClientJobManager();

  /**
   * Cancels all jobs that belong to the given <code>id</code> and {@link IClientSession}. The jobs to be cancelled are
   * cancelled 'hard', meaning that their executing threads are <code>interrupted</code>. If canceling a job that
   * executed nested jobs in 'runNow'-style, those are cancelled as well - however, only the very outermost job can be
   * cancelled.
   *
   * @param id
   *          the <code>id</code> of the job to be cancelled; the same <code>id</code> may be shared by multiple jobs.
   * @param clientSession
   *          the {@link IClientSession} which jobs to be cancelled must belong to; must not be <code>null</code>.
   * @return <code>true</code> if cancelled successfully, <code>false</code> if no job was found or could not be
   *         cancelled.
   */
  boolean cancel(long id, IClientSession clientSession);
}
