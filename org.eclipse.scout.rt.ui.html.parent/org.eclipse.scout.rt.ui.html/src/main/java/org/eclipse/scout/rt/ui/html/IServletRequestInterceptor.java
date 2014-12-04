/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import org.eclipse.scout.service.IService;

/**
 * This interceptor contributes to the {@link AbstractScoutAppServlet}
 */
public interface IServletRequestInterceptor extends IService {

  /**
   * @return true if the request was consumed by the interceptor, no further action is then necessary
   */
  boolean interceptPost(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

  /**
   * @return true if the request was consumed by the interceptor, no further action is then necessary
   */
  boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
}
