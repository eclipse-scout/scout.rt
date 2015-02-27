package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;

/**
 * Provides {@link IServerSession} for Scout tests and allows to create more than one instance of the same
 * session type (e.g. for different users).
 */
public interface ITestServerSessionProvider extends IServerSessionRegistryService {

  /**
   * Method invoked to created the {@link Subject} on behalf of which a server session is started.
   *
   * @param principal
   *          principal
   * @return {@link Subject} or <code>null</code> to not start the session within a
   *         {@link Subject#doAs(Subject, java.security.PrivilegedAction)} call.
   */
  Subject login(String principal);
}
