package org.eclipse.scout.rt.server.admin.healthcheck;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthCheckServlet;

/**
 * Default health check servlet for backend applications.
 *
 * @since 6.1
 * @see AbstractHealthCheckServlet
 */
public class ServerHealthCheckServlet extends AbstractHealthCheckServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected RunContext execCreateRunContext() {
    return RunContexts.empty();
  }

}
