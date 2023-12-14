/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;

/**
 * Interface for handlers contributing to the {@link UiServlet}.
 * <p>
 * Make sure to call
 * {@link HttpCacheControl#checkAndSetCacheHeaders(HttpServletRequest, HttpServletResponse, HttpCacheObject)} on the
 * {@link HttpCacheControl} bean in the handling of the request. Otherwise cache response headers may be missing and
 * could lead to unexpected caching of sensitive information.
 */
@ApplicationScoped
public interface IUiServletRequestHandler {

  /**
   * @return <code>true</code> if the request was consumed by the handler, no further action is then necessary. If
   *         <code>false</code> is returned, other handlers may handle the request afterwards.
   */
  boolean handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
