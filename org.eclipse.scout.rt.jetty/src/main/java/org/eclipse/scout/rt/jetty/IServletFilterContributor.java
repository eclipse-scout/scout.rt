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

import java.util.EnumSet;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Contributor for servlet filters called when Scout application ({@link org.eclipse.scout.rt.app.Application}) is
 * initialized.
 * <p>
 * A servlet filter is responsible to filter and/or modify requests based on specific conditions, e.g. for
 * authentication or logging.
 */
@ApplicationScoped
public interface IServletFilterContributor {

  /**
   * Contribute servlet filters by using {@link ServletContextHandler#addFilter(Class, String, EnumSet)} or one of the
   * overloads.
   */
  void contribute(final ServletContextHandler handler);
}
