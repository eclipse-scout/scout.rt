package org.eclipse.scout.rt.server;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Factory for creating {@link ServerJob}s with {@link IServerSession} and {@link Subject}
 */
public class ServerJobFactory implements IServerJobFactory {

  private final IServerSession m_serverSession;
  private final Subject m_subject;

  public ServerJobFactory(IServerSession serverSession, Subject subject) {
    m_serverSession = serverSession;
    m_subject = subject;
  }

  @Override
  public ServerJob create(String name, final ITransactionRunnable r) {
    return new ServerJob(name, m_serverSession, m_subject) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        return r.run(monitor);
      }
    };
  }

  /**
   * Creates and runs a serverjob (blocking) with no progress monitor.
   *
   * @throws ProcessingException
   */
  @Override
  public ServerJob runNow(String name, final ITransactionRunnable r) throws ProcessingException {
    final ServerJob job = create(name, r);
    job.setSystem(true);
    job.runNow(new NullProgressMonitor());
    job.throwOnError();
    return job;
  }

}
