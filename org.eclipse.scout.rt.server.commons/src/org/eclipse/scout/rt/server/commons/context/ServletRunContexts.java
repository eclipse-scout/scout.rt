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

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.OBJ;

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
   */
  public static final ServletRunContext empty() {
    final ServletRunContext runContext = OBJ.get(ServletRunContext.class);
    runContext.fillEmptyValues();
    return runContext;
  }

  /**
   * Creates a "snapshot" of the current calling <code>ServletRunContext</code>.
   */
  public static ServletRunContext copyCurrent() {
    final ServletRunContext runContext = OBJ.get(ServletRunContext.class);
    runContext.fillCurrentValues();
    return runContext;
  }
}
