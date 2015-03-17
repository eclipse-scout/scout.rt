package org.eclipse.scout.rt.ui.html;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.job.IExecutable;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.filter.FutureFilter;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.client.job.filter.BlockedJobFilter;
import org.eclipse.scout.rt.client.job.filter.ClientSessionFilter;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.shared.ISession;

public final class ModelJobUtility {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobUtility.class);

  private ModelJobUtility() {
    // static access only
  }

  /**
   * Wait until all sync jobs have been finished or only waitFor sync jobs are left.
   */
  public static void waitUntilJobsHaveFinished(IClientSession currentClientSession) {
    final IModelJobManager modelJobManager = OBJ.get(IModelJobManager.class);
    if (modelJobManager.isModelThread()) {
      throw new IllegalStateException("Cannot wait for another sync job, because current job is also sync!");
    }
    try {
      modelJobManager.waitUntilDone(new AndFilter<>(new ClientSessionFilter(currentClientSession), new NotFilter<>(BlockedJobFilter.INSTANCE)), 1, TimeUnit.HOURS);
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for all jobs to be finished.", e);
    }
  }

  public static void runInModelThreadAndWait(IClientSession clientSession, IExecutable<?> executable) throws ProcessingException {
    IModelJobManager modelJobManager = OBJ.get(IModelJobManager.class);
    if (modelJobManager.isModelThread()) {
      modelJobManager.runNow(executable, ClientJobInput.defaults().session(clientSession));
    }
    else {
      IFuture<?> future = modelJobManager.schedule(executable, ClientJobInput.defaults().session(clientSession));
      try {
        modelJobManager.waitUntilDone(new AndFilter<IFuture<?>>(new FutureFilter(future), new NotFilter<>(BlockedJobFilter.INSTANCE)), 1, TimeUnit.HOURS);
      }
      catch (InterruptedException e) {
        LOG.warn("Interrupted while waiting for executable '" + executable.getClass().getName() + "'.", e);
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
