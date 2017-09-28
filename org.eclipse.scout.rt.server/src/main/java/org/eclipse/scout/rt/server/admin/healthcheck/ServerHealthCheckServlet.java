/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
