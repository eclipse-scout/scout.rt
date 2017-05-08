package org.eclipse.scout.rt.server.commons.healthcheck;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * The <code>IHealthChecker</code> represents a single check to determine the application health status (OK / FAILED).
 *
 * @since 6.1
 */
@ApplicationScoped
public interface IHealthChecker {

  String getName();

  /**
   * @return <code>true</code> if this <code>IHealthChecker</code> has to be checked.
   */
  boolean isActive();

  /**
   * @param context
   *          {@link RunContext} used to run the check in.
   * @return <code>true</code> if check was successful, <code>false</code> otherwise.
   */
  boolean checkHealth(RunContext context);

}
