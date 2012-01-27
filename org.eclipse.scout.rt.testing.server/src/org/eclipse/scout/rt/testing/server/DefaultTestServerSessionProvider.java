package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ThreadContext;
import org.osgi.framework.Bundle;

/**
 * Default implementation of {@link ITestServerSessionProvider}.
 */
public class DefaultTestServerSessionProvider implements ITestServerSessionProvider {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultTestServerSessionProvider.class);

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IServerSession> T createServerSession(Class<T> clazz, final Subject subject) throws ProcessingException {
    IServerSession serverSession;
    try {
      serverSession = clazz.newInstance();
    }
    catch (Throwable t) {
      throw new ProcessingException("create instance of " + clazz, t);
    }
    ServerJob initJob = new ServerJob("new " + clazz.getSimpleName(), serverSession, subject) {
      @Override
      protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
        // load session
        IServerSession serverSessionInside = ThreadContext.getServerSession();
        String symbolicName = serverSessionInside.getClass().getPackage().getName();
        Bundle bundle = Platform.getBundle(symbolicName);
        beforeStartSession(serverSessionInside, subject);
        serverSessionInside.loadSession(bundle);
        afterStartSession(serverSessionInside, subject);
        return Status.OK_STATUS;
      }
    };
    initJob.runNow(new NullProgressMonitor());
    initJob.throwOnError();
    return (T) serverSession;
  }

  @Override
  public Subject login(String runAs) {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(runAs));
    return subject;
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
