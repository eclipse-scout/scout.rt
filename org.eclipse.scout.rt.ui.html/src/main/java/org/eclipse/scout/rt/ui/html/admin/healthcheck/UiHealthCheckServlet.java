package org.eclipse.scout.rt.ui.html.admin.healthcheck;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.server.commons.healthcheck.AbstractHealthCheckServlet;

/**
 * Default health check servlet for UI applications.
 *
 * @since 6.1
 * @see AbstractHealthCheckServlet
 */
public class UiHealthCheckServlet extends AbstractHealthCheckServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected RunContext execCreateRunContext() {
    return ClientRunContexts.empty();
  }

}
