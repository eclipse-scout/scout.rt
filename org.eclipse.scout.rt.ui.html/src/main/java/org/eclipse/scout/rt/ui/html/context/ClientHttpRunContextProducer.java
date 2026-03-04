/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.context;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;
import org.eclipse.scout.rt.ui.html.filter.UiServletUserBootstrapFilter;

/**
 * Producer for {@link ClientRunContext} instances handling HTTP requests in Scout client.
 */
public class ClientHttpRunContextProducer extends HttpRunContextProducer {

  protected ServletFilterHelper m_filterHelper = BEANS.get(ServletFilterHelper.class);

  /**
   * Get {@link User} from HTTP session
   *
   * @see UiServletUserBootstrapFilter
   */
  @Override
  protected User currentUser(HttpServletRequest req) {
    return m_filterHelper.getUserOnSession(req);
  }
}
