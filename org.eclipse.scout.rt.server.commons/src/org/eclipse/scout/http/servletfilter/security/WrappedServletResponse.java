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
package org.eclipse.scout.http.servletfilter.security;

import javax.servlet.http.HttpServletResponse;

/**
 * @deprecated use {@link org.eclipse.scout.rt.server.commons.servletfilter.security.WrappedServletResponse} instead.
 *             Will be removed in the M-Release.
 */
@Deprecated
public class WrappedServletResponse extends org.eclipse.scout.rt.server.commons.servletfilter.security.WrappedServletResponse {

  public WrappedServletResponse(HttpServletResponse response) {
    super(response);
  }

}
