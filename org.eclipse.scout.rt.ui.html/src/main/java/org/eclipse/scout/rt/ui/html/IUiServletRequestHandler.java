/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;

/**
 * Interface for handlers contributing to the {@link UiServlet}.
 * <p>
 * Make sure to call
 * {@link HttpCacheControl#checkAndSetCacheHeaders(HttpServletRequest, HttpServletResponse, String, org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject)}
 * on the {@link HttpCacheControl} bean in the handling of the request. Otherwise cache response headers may be missing
 * and could lead to unexpected caching of sensitive information.
 */
@ApplicationScoped
public interface IUiServletRequestHandler {

  /**
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  boolean handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

  /**
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  boolean handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
