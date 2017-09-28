/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.authentication;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class SecureHttpServletRequestWrapper extends HttpServletRequestWrapper {

  private static final List<String> AUTH_TYPES = CollectionUtility.arrayList(BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH, DIGEST_AUTH, "NTLM");

  private final Principal m_principal;
  private final String m_authType;

  public SecureHttpServletRequestWrapper(HttpServletRequest req, Principal principal) {
    this(req, principal, null);
  }

  public SecureHttpServletRequestWrapper(HttpServletRequest req, Principal principal, String authType) {
    super(req);
    m_principal = principal;
    if (authType != null && !AUTH_TYPES.contains(authType)) {
      authType = null;
    }
    if (authType == null) {
      authType = req.getAuthType();
    }
    if (authType == null) {
      authType = AUTH_TYPES.get(0);
    }
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
