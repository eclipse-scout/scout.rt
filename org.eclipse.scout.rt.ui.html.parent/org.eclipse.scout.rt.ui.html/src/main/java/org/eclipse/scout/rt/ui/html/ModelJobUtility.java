package org.eclipse.scout.rt.ui.html;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters;
import org.eclipse.scout.rt.client.job.ClientJobFutureFilters.Filter;
import org.eclipse.scout.rt.client.job.ModelJobInput;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.ISession;

public final class ModelJobUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobUtility.class);

  private ModelJobUtility() {
    // static access only
  }

  /**
   * TODO [awe][dwi] Sync oder Async?
   * Wait until all sync jobs have been finished or only waitFor sync jobs are left.
   */
  public static void waitUntilAllModelJobsJobsHaveFinished(IClientSession clientSession) {
    Assertions.assertFalse(ModelJobs.isModelThread(), "Cannot wait for another model job, because the current job is a model job itself");
    try {
      Filter filter = ClientJobFutureFilters.allFilter().modelJobsOnly().session(clientSession).notBlocked().notPeriodic();
      Jobs.getJobManager().awaitDone(filter, 1, TimeUnit.HOURS);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for all jobs to complete.", e);
    }
  }

  public static void runInModelThreadAndWait(IClientSession clientSession, IExecutable<?> executable) throws ProcessingException {
    if (ModelJobs.isModelThread()) {
      ModelJobs.runNow(executable, ModelJobInput.defaults().session(clientSession));
    }
    else {
      IFuture<?> future = ModelJobs.schedule(executable, ModelJobInput.defaults().session(clientSession));
      try {
        Jobs.getJobManager().awaitDone(ClientJobFutureFilters.allFilter().futures(future).notBlocked(), 1, TimeUnit.HOURS);
      }
      catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for executable '" + executable.getClass().getName() + "'.", e);
      }
      // If the executable has finished, call awaitDone() to throw a possible exception from the job.
      // We must _not_ do that if the job is blocked ("waitFor")!
      if (future.isDone()) {
        future.awaitDoneAndGet();
      }
    }
  }

  public static void runAsSubject(final Runnable runnable) throws Exception {
    runAsSubject(runnable, null);
  }

  public static void runAsSubject(final Runnable runnable, Subject subject) throws Exception {
    if (subject == null) {
      ISession session = ISession.CURRENT.get();
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
      if (t instanceof Exception) {
        throw (Exception) t;
      }
      else {
        throw new Exception(t);
      }
    }
  }
}
