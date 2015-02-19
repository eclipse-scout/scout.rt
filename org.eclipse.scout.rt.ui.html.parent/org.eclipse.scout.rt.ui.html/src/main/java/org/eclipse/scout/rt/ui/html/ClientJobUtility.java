package org.eclipse.scout.rt.ui.html;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;

// FIXME Refactor with new client jobs (replace loop by something better)
public final class ClientJobUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientJobUtility.class);
  private static final int SLEEP_TIME = 25;

  private ClientJobUtility() {
    // static access only
  }

  /**
   * Wait until all sync jobs have been finished or only waitFor sync jobs are left.
   */
  public static void waitUntilJobsHaveFinished(IClientSession currentClientSession) {
    if (ClientJob.isSyncClientJob()) {
      throw new IllegalStateException("Cannot wait for another sync job, because current job is also sync!");
    }
    while (true) {
      int numWaitFor = 0;
      int numSync = 0;
      for (Job job : Job.getJobManager().find(ClientJob.class)) {
        ClientJob clientJob = (ClientJob) job;
        if (isSyncJobFromCurrentSession(clientJob, currentClientSession)) {
          numSync++;
          if (clientJob.isWaitFor()) {
            numWaitFor++;
          }
        }
      }
      if (numSync == numWaitFor) { // also covers numSync == 0
        LOG.trace("Job list is empty or only 'waitFor' sync jobs left in the queue - finish the request");
        break;
      }
      // TODO AWE: (jobs) prüfen, ob das mit IJobChangeListener schöner geht (sleep/notify)
      LOG.trace("There are still running sync jobs - must wait until they have finished. Jobs (sync)=" + numSync + " jobs (waitFor)=" + numWaitFor);
      LOG.trace("Going to sleep before checking the job queue again...");
      try {
        Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
  }

  protected static boolean isSyncJobFromCurrentSession(ClientJob job, IClientSession currentClientSession) {
    return (job.isSync() && job.getClientSession() == currentClientSession);
  }

  public static void waitForSyncJob(ClientJob syncJob) {
    if (Job.getJobManager().currentJob() == syncJob) {
      return;
    }
    if (ClientJob.isSyncClientJob()) {
      throw new IllegalStateException("Cannot wait for another sync job, because current job is also sync!");
    }
    while (true) {
      boolean wait = false;
      for (Job job : Job.getJobManager().find(ClientJob.class)) {
        ClientJob clientJob = (ClientJob) job;
        if (job == syncJob) {
          // still running --> wait for the job, except it is blocked by 'waitFor'
          wait = (!clientJob.isWaitFor());
          break;
        }
      }

      if (!wait) {
        break;
      }
      try {
        Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
  }

  public static void runAsSubject(final Runnable runnable) throws Throwable {
    runAsSubject(runnable, null);
  }

  public static void runAsSubject(final Runnable runnable, Subject subject) throws Throwable {
    if (subject == null) {
      IClientSession session = ClientJob.getCurrentSession();
      subject = (session == null ? null : session.getSubject());
    }
    if (subject == null) {
      throw new IllegalStateException("Subject is null");
    }
    try {
      Subject.doAs(
          subject,
          new PrivilegedExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
              runnable.run();
              return null;
            }
          });
    }
    catch (PrivilegedActionException e) {
      Throwable t = e.getCause();
      throw t;
    }
  }
}
