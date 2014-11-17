package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerJobFactory;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ITransactionRunnable;
import org.eclipse.scout.rt.server.services.common.session.ServerSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UserAgent;

/**
 * Default implementation of {@link ITestServerSessionProvider}.
 */
public class DefaultTestServerSessionProvider extends ServerSessionRegistryService implements ITestServerSessionProvider {

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IServerSession> T createServerSession(Class<T> clazz, final Subject subject) throws ProcessingException {
    final IServerSession serverSession = createSessionInstance(clazz);
    serverSession.setUserAgent(UserAgent.createDefault());
    final IServerJobFactory jobFactory = getBackendService().createJobFactory(serverSession, subject);
    runBeforeLoadJob(subject, serverSession, jobFactory);
    runLoadSessionJob(serverSession, jobFactory);
    runAfterLoadLob(subject, serverSession, jobFactory);
    return (T) serverSession;
  }

  private void runBeforeLoadJob(final Subject subject, final IServerSession serverSession, final IServerJobFactory jobFactory) throws ProcessingException {
    jobFactory.runNow("before creating " + serverSession.getClass().getSimpleName(), new ITransactionRunnable() {
      @Override
      public IStatus run(IProgressMonitor monitor) throws ProcessingException {
        beforeStartSession(serverSession, subject);
        return Status.OK_STATUS;
      }
    });
  }

  private void runAfterLoadLob(final Subject subject, final IServerSession serverSession, final IServerJobFactory jobFactory) throws ProcessingException {
    jobFactory.runNow("after creating " + serverSession.getClass().getSimpleName(), new ITransactionRunnable() {
      @Override
      public IStatus run(IProgressMonitor monitor) throws ProcessingException {
        afterStartSession(serverSession, subject);
        return Status.OK_STATUS;
      }
    });
  }

  @Override
  public Subject login(String runAs) {
    return getBackendService().createSubject(runAs);
  }

  /**
   * Performs custom operations before the server session is started.
   *
   * @param serverSession
   * @param subject
   */
  protected void beforeStartSession(IServerSession serverSession, Subject subject) {
  }

  /**
   * Performs custom operations after the server session has been started.
   *
   * @param serverSession
   * @param subject
   */
  protected void afterStartSession(IServerSession serverSession, Subject subject) {
  }
}
