/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.ui.rap.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;

/**
 * Redirects incoming requests to the corresponding branding servlet depending on the used browser.
 * <p>
 * The dispatching servlet expects the existence of three branding servlets:
 * <ul>
 * <li>web</li>
 * <li>tablet</li>
 * <li>mobile</li>
 * </ul>
 * The dispatching is based on the {@link BrowserInfo}. See {@link BrowserInfo#isMobile()} or
 * {@link BrowserInfo#isTablet()} for more details.
 * <p>
 * The registering of the dispatcher servlet is typically done with the equinox servlet extension point:
 * 
 * <pre>
 * {@code
 * <extension point="org.eclipse.equinox.http.registry.servlets">
 *   <servlet
 *     alias="/"
 *     class="org.eclipse.scout.rt.ui.rap.mobile.servlets.DeviceDispatcherServlet">
 *   </servlet>
 * </extension>
 * }
 * </pre>
 * 
 * @since 3.9.0
 */
public class DeviceDispatcherServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public static final String BRANDING_SERVLET_MOBILE = "mobile";
  public static final String BRANDING_SERVLET_TABLET = "tablet";
  public static final String BRANDING_SERVLET_WEB = "web";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    dispatch(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    dispatch(request, response);
  }

  private void dispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if ("/".equals(request.getPathInfo())) {
      String targetServlet = computeBrandingServletName(request);
      response.sendRedirect(response.encodeRedirectURL(targetServlet));
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  protected String computeBrandingServletName(HttpServletRequest request) {
    BrowserInfo browserInfo = RwtUtility.createBrowserInfo(request);

    if (browserInfo.isMobile()) {
      return BRANDING_SERVLET_MOBILE;
    }
    else if (browserInfo.isTablet()) {
      return BRANDING_SERVLET_TABLET;
    }

    return BRANDING_SERVLET_WEB;
  }

}
