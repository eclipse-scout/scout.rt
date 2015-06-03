/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.context;

import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;

/**
 * Factory methods to create new {@link ServletRunContext} objects to propagate {@link Servlet} state like
 * {@link HttpServletRequest} and {@link HttpServletResponse}.
 * <p/>
 * Usage:
 *
 * <pre>
 * ServletRunContexts.empty().locale(Locale.US).servletRequest(...).servletResponse(...).run(new IRunnable() {
 * 
 *   &#064;Override
 *   public void run() throws Exception {
 *      // run code on behalf of the new context
 *   }
 * });
 * </pre>
 *
 * @since 5.1
 * @see ServletRunContext
 */
public final class ServletRunContexts {

  private ServletRunContexts() {
  }

  /**
   * Creates an empty {@link ServletRunContext}.
   *
   * @RunMonitor a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is
   *             NOT registered as child monitor, meaning that it will not be cancelled once the current
   *             {@link RunMonitor} is cancelled.
   * @Subject <code>null</code> {@link Subject} as preferred value, meaning that it will not be set by other values like
   *          the session.
   * @Locale <code>null</code> {@link Locale} as preferred value, meaning that it will not be set by other values like
   *         the session.
   */
  public static final ServletRunContext empty() {
    final ServletRunContext runContext = BEANS.get(ServletRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling context.<br/>
   *
   * @RunMonitor a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor},
   *             it is also registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext}
   *             to be cancelled as well once the current calling {@link RunContext} is cancelled, but DOES NOT cancel
   *             the current calling {@link RunContext} if the <i>returned</i> {@link RunContext} is cancelled.
   * @Subject current {@link Subject} as non-preferred value, meaning that it will be updated by other values like the
   *          session.
   * @Locale current {@link Locale} as non-preferred value, meaning that it will be updated by other values like the
   *         session.
   */
  public static ServletRunContext copyCurrent() {
    final ServletRunContext runContext = BEANS.get(ServletRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
