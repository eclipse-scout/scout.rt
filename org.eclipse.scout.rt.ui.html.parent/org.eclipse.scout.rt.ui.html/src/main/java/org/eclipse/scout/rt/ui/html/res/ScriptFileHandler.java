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
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.AbstractRequestHandler;
import org.eclipse.scout.rt.ui.html.AbstractScoutAppServlet;

public class ScriptFileHandler extends AbstractRequestHandler {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScriptFileHandler.class);

  public ScriptFileHandler(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp, String pathInfo) {
    super(servlet, req, resp, pathInfo);
  }

  @Override
  public boolean handle() throws ServletException, IOException {
    String pathInfo = getPathInfo();
    LOG.info("processing script: " + pathInfo);
    ScriptBuilder builder = new ScriptBuilder(getServlet().getResourceLocator());
    builder.setDebug(isDebug());
    try {
      byte[] outputBytes = builder.buildScript(pathInfo);
      if (pathInfo.endsWith(".js")) {
        getHttpServletResponse().setContentType("application/javascript");
      }
      else {
        getHttpServletResponse().setContentType("text/css");
      }
      getHttpServletResponse().getOutputStream().write(outputBytes);
    }
    catch (Exception ex) {
      LOG.error("SCRIPT_BUILD_ERROR: " + pathInfo, ex);
      getHttpServletResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    return true;
  }

}
