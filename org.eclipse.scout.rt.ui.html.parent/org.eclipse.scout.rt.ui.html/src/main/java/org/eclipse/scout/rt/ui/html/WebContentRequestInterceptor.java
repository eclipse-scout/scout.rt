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
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * This interceptor contributes to the {@link AbstractScoutAppServlet} as the default GET handler for static resources
 * contained in this bundles /WebContent directory
 * <p>
 * js and css files are automatically compiled if the name matches
 * <code>(/path/)(basename)-(version).min.(js|css)</code> and there exists a resource named
 * <code>$1$2-template.$4</code>
 * <p>
 * For example /res/scout-5.0.0.min.js is served using /src/main/js/scout-template.js
 * <p>
 * The js and css compilation can be turned on and off using the url param ?debug=true which only builds the js and css
 * but does not compile/minimize it
 */
@Priority(-10)
public class WebContentRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WebContentRequestInterceptor.class);

  //path = $1 $2-$3.min.$4 with $1=folder, $2=basename, $3=version, $4="js" or "css"
  private static final Pattern SCRIPT_FILE_PATTERN = Pattern.compile("(/(?:\\w+/)*)([-\\w]+)-([0-9.]+)\\.min\\.(js|css)");

  private static final String SESSION_ATTR_DEBUG_SCRIPT_ENABLED = WebContentRequestInterceptor.class.getSimpleName() + ".enabled";
  private static final String DEBUG_PARAM = "debug";

  @Override
  public boolean interceptGet(AbstractScoutAppServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = resolvePathInfo(req);

    //debug flag
    boolean debugScriptEnabled = checkDebugScriptEnabled(req, resp);

    //js or css
    Matcher mat = SCRIPT_FILE_PATTERN.matcher(pathInfo);
    if (mat.matches()) {
      Script templateScript = findScriptSource(servlet, mat.group(2) + "-template." + mat.group(4));
      if (templateScript != null) {
        LOG.info("building script file: " + pathInfo);
        if ("js".equals(mat.group(4))) {
          processJsScriptTemplate(req, resp, debugScriptEnabled, templateScript);
        }
        else {
          processCssScriptTemplate(req, resp, debugScriptEnabled, templateScript);
        }
        return true;
      }
    }

    //images, html, etc.
    URL url = findWebContentResource(servlet, pathInfo);
    if (url != null) {
      new StaticResourceHandler().handle(servlet, req, resp, url);
      return true;
    }

    //not found
    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    resp.getOutputStream().print("Not Found: " + req.getPathInfo());
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

  protected Script findScriptSource(AbstractScoutAppServlet servlet, String scriptPath) {
    //osgi only!
    for (OsgiWebContentService w : SERVICES.getServices(OsgiWebContentService.class)) {
      Script script = w.getLocator().getScriptSource(scriptPath);
      if (script != null) {
        return script;
      }
    }
    //jee
    return new JEEWebContentResourceLocator(servlet.getServletContext()).getScriptSource(scriptPath);
  }

  protected URL findWebContentResource(AbstractScoutAppServlet servlet, String resourcePath) {
    //osgi only!
    for (OsgiWebContentService w : SERVICES.getServices(OsgiWebContentService.class)) {
      URL url = w.getLocator().getWebContentResource(resourcePath);
      if (url != null) {
        return url;
      }
    }
    //jee
    return new JEEWebContentResourceLocator(servlet.getServletContext()).getWebContentResource(resourcePath);
  }

  protected void processJsScriptTemplate(HttpServletRequest req, HttpServletResponse resp, boolean debug, Script script) throws IOException, ServletException {
    String outputFile = new ScriptBuilder().buildJsScript(script, !debug);

    byte[] outputBytes = outputFile.getBytes("UTF-8");
    resp.setContentType("application/javascript");
    resp.getOutputStream().write(outputBytes);
  }

  protected void processCssScriptTemplate(HttpServletRequest req, HttpServletResponse resp, boolean debug, Script script) throws IOException, ServletException {
    String outputFile = new ScriptBuilder().buildCssScript(script, !debug);

    byte[] outputBytes = outputFile.getBytes("UTF-8");
    resp.setContentType("text/css");
    resp.getOutputStream().write(outputBytes);
  }

}
