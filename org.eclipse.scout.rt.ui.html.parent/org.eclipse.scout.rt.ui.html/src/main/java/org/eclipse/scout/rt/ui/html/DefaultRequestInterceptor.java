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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.json.JsonMessageHandler;
import org.eclipse.scout.rt.ui.html.res.HtmlFileHandler;
import org.eclipse.scout.rt.ui.html.res.ScriptFileHandler;
import org.eclipse.scout.rt.ui.html.res.StaticResourceHandler;
import org.eclipse.scout.service.AbstractService;

/**
 * This interceptor contributes to the {@link AbstractScoutAppServlet} as the default GET and POST handler
 */
@Priority(-10)
public class DefaultRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultRequestInterceptor.class);

  //TODO imo change once we switch from OSGI to JEE; move WebContent to src/main/resources/META-INF/resources/WebContent, move src/main/js to src/main/resources/META-INF/resources/js
  private IWebArchiveResourceLocator m_resourceLocator = new OsgiWebArchiveResourceLocator();

  @Override
  public boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);

    //js and css
    if ((pathInfo.endsWith(".js") || pathInfo.endsWith(".css")) && createScriptFileHandler(servlet, req, resp, pathInfo).handle()) {
      return true;
    }

    //html
    if (pathInfo.endsWith(".html") && createHtmlFileHandler(servlet, req, resp, pathInfo).handle()) {
      return true;
    }

    //static resources such as images, icons, fonts
    if (createStaticResourceHandler(servlet, req, resp, pathInfo).handle()) {
      return true;
    }

    LOG.info("404_NOT_FOUND_GET: " + pathInfo);
    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    return true;
  }

  @Override
  public boolean interceptPost(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //serve json
    String pathInfo = req.getPathInfo();
    if ("/json".equals(pathInfo) && createJsonMessageHandler(servlet, req, resp, pathInfo).handle()) {
      return true;
    }

    LOG.info("404_NOT_FOUND_POST: " + pathInfo);
    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    return true;
  }

  protected String resolvePathInfo(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    if (pathInfo == null) {
      return null;
    }
    if ("/".equals(pathInfo)) {
      pathInfo = createIndexHtmlResolver().resolve(req);
    }
    return pathInfo;
  }

  protected IndexHtmlResolver createIndexHtmlResolver() {
    return new IndexHtmlResolver();
  }

  protected AbstractRequestHandler createHtmlFileHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    return new HtmlFileHandler(servlet, req, resp, pathInfo);
  }

  protected AbstractRequestHandler createScriptFileHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    return new ScriptFileHandler(servlet, req, resp, pathInfo);
  }

  protected AbstractRequestHandler createStaticResourceHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    return new StaticResourceHandler(servlet, req, resp, pathInfo);
  }

  protected AbstractRequestHandler createJsonMessageHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    return new JsonMessageHandler(servlet, req, resp, pathInfo);
  }
}
