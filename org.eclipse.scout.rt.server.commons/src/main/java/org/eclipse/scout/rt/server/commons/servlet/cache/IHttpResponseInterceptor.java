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
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor that is called before the HTTP response is sent back to the client. This can be useful to alter the
 * answer, e.g. to add HTTP response headers.
 */
@FunctionalInterface
public interface IHttpResponseInterceptor extends Serializable {

  /**
   * Called before the HTTP response is sent back to the client.
   */
  void intercept(HttpServletRequest req, HttpServletResponse resp);
}
