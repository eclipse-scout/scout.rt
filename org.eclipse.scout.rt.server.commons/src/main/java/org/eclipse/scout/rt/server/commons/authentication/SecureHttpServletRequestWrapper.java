/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
