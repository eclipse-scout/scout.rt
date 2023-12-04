/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Contributor for servlets called when Scout application ({@link org.eclipse.scout.rt.app.Application}) is initialized.
 * <p>
 * A servlet is responsible to process requests, i.e. provide a response.
 */
@ApplicationScoped
public interface IServletContributor {

  /**
   * Contribute servlets by using {@link ServletContextHandler#addServlet(Class, String)} or one of the overloads.
   */
  void contribute(final ServletContextHandler handler);
}
