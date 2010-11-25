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
package org.eclipse.scout.rt.server.servlet.filter;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class SecureHttpServletRequestWrapper extends HttpServletRequestWrapper {

  private Principal m_principal;
  private String m_authType;

  public SecureHttpServletRequestWrapper(HttpServletRequest req, Principal principal, String authType) {
    super(req);
    m_principal = principal;
    m_authType = authType;
  }

  @Override
  public String getRemoteUser() {
    return m_principal.getName();
  }

  @Override
  public Principal getUserPrincipal() {
    return m_principal;
  }

  @Override
  public String getAuthType() {
    return m_authType;
  }
}
