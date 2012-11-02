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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.security.SimplePrincipal;

/**
 * a security filter allowing anonymous access to the application.
 */
public class AnonymousSecurityFilter extends AbstractChainableSecurityFilter {
  public static final String ANONYMOUS_USER_NAME = "anonymous";

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    holder.setPrincipal(new SimplePrincipal(ANONYMOUS_USER_NAME));
    return STATUS_CONTINUE_WITH_PRINCIPAL;
  }

}
