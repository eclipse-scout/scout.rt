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
package org.eclipse.scout.rt.ui.html.script;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;
import org.eclipse.scout.rt.ui.html.IServletRequestInterceptor;
import org.eclipse.scout.rt.ui.html.IndexResolver;
import org.eclipse.scout.service.AbstractService;

/**
 * This interceptor contributes to the {@link AbstractScoutAppServlet} as the default GET handler for static resources
 * contained in this bundles /META-INF/resources/WebContent and /META-INF/resources/js directories
 * <p>
 * see {@link ScriptBuilder} for more details
 */
@Priority(-10)
public class WebArchiveResourceRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WebArchiveResourceRequestInterceptor.class);

  private static final String SESSION_ATTR_DEBUG_SCRIPT_ENABLED = WebArchiveResourceRequestInterceptor.class.getSimpleName() + ".enabled";
  private static final String DEBUG_PARAM = "debug";

  //TODO imo change once we switch from OSGI to JEE
  private IWebArchiveResourceLocator m_resourceLocator = new OsgiWebArchiveResourceLocator();

  @Override
  public boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);

    //debug flag
    boolean debugScriptEnabled = checkDebugScriptEnabled(req, resp);

    //js and css
    if (pathInfo.endsWith(".js") || pathInfo.endsWith(".css")) {
      LOG.info("processing script: " + pathInfo);
      ScriptBuilder builder = new ScriptBuilder(m_resourceLocator);
      builder.setDebug(debugScriptEnabled);
      try {
        byte[] outputBytes = builder.buildScript(pathInfo);
        if (pathInfo.endsWith(".js")) {
          resp.setContentType("application/javascript");
        }
        else {
          resp.setContentType("text/css");
        }
        resp.getOutputStream().write(outputBytes);
      }
      catch (Exception ex) {
        LOG.error("SCRIPT_BUILD_ERROR: " + pathInfo, ex);
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      return true;
    }

    //static resources such images, html, unprocessed js, css etc.
    URL url = m_resourceLocator.getWebContentResource(pathInfo);
    if (url != null) {
      LOG.info("processing resource: " + pathInfo);
      new StaticResourceHandler().handle(servlet, req, resp, url);
      return true;
    }

    //not found
    LOG.info("404_RESOURCE_NOT_FOUND: " + pathInfo);
    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    return true;
  }

  @Override
  public boolean interceptPost(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  protected boolean checkDebugScriptEnabled(HttpServletRequest req, HttpServletResponse resp) {
    HttpSession session = req.getSession(false);
    if (session == null) {
      return false;
    }
    String requestParam = req.getParameter(DEBUG_PARAM);
    if (requestParam != null) {
      session.setAttribute(SESSION_ATTR_DEBUG_SCRIPT_ENABLED, "true".equals(requestParam));
    }
    Boolean sessionFlag = (Boolean) session.getAttribute(SESSION_ATTR_DEBUG_SCRIPT_ENABLED);
    if (sessionFlag != null) {
      return sessionFlag.booleanValue();
    }
    if (Platform.inDevelopmentMode()) {
      return true;
    }
    return false;
  }

  protected String resolvePathInfo(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return null;
    }
    if ("/".equals(pathInfo)) {
      pathInfo = createIndexResolver().resolve(req);
    }
    return pathInfo;
  }

  protected IndexResolver createIndexResolver() {
    return new IndexResolver();
  }
}
