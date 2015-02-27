package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerJobService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.services.common.session.ServerSessionRegistryService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

/**
 * Default implementation of {@link ITestServerSessionProvider}.
 */
public class DefaultTestServerSessionProvider extends ServerSessionRegistryService implements ITestServerSessionProvider {

  @Override
  protected void loadSessionInServerJob(final ServerJobInput input, final Bundle bundle, final IServerSession serverSession) throws ProcessingException {
    final Subject subject = input.getSubject();

    beforeStartSession(serverSession, input.getSubject());
    try {
      super.loadSessionInServerJob(input, bundle, serverSession);
    }
    finally {
      afterStartSession(serverSession, subject);
    }
  }

  @Override
  public Subject login(final String runAs) {
    return SERVICES.getService(IServerJobService.class).createSubject(runAs);
  }

  /**
   * Method invoked before the session is started.
   */
  protected void beforeStartSession(final IServerSession serverSession, final Subject subject) {
  }

  /**
   * Method invoked after the session was started; is also invoked in case the session could not be started.
   */
  protected void afterStartSession(final IServerSession serverSession, final Subject subject) {
  }
}
