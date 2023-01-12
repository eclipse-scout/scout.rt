/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
