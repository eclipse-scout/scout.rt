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
package org.eclipse.scout.rt.ui.rap.servletfilter;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.service.ContextProvider;

/**
 * @since 3.10.0-M4
 * @see {@link LogoutFilter}
 */
@SuppressWarnings("restriction")
public class LogoutHandler {
  private String m_redirectUrl;

  public LogoutHandler() {
    m_redirectUrl = computeRedirectUrl();
  }

  public void logout() {
    HttpServletResponse response = RWT.getResponse();
    String logoutUrl = response.encodeRedirectURL(getLogoutLocation());
    ContextProvider.getProtocolWriter().appendHead("redirect", logoutUrl);
  }

  protected String getLogoutLocation() {
    String path = RWT.getRequest().getServletPath();

    if (path.length() > 0 && '/' == path.charAt(0)) {
      path = path.substring(1);
    }

    path += "?" + LogoutFilter.LOGOUT_PARAM;
    String redirectUrl = getRedirectUrl();
    if (redirectUrl != null) {
      path += "&" + "redirectUrl=" + redirectUrl;
    }
    return path;
  }

  protected String computeRedirectUrl() {
    String currentServletName = RWT.getRequest().getServletPath();

    if (currentServletName.length() > 0 && '/' == currentServletName.charAt(0)) {
      currentServletName = currentServletName.substring(1);
    }

    return currentServletName;
  }

  public String getRedirectUrl() {
    return m_redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    m_redirectUrl = redirectUrl;
  }
}
