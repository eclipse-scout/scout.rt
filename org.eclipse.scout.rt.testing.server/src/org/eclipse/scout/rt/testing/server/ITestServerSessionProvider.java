package org.eclipse.scout.rt.testing.server;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.IServerSession;

/**
 * Provides {@link IServerSession} for Scout tests and allows to create more than one instance of the same
 * session type (e.g. for different users).
 */
public interface ITestServerSessionProvider {

  /**
   * Performs a login for the given user.
   * 
   * @param runAs
   * @return
   */
  Subject login(String runAs);

  /**
   * Creates a new server session.
   * 
   * @param <T>
   *          type of the resulting server session.
   * @param clazz
   *          requested server session.
   * @param user
   *          name of the user the session belongs to or is created for.
   * @return
   * @throws ProcessingException
   */
  <T extends IServerSession> T createServerSession(Class<T> clazz, Subject subject) throws ProcessingException;
}
