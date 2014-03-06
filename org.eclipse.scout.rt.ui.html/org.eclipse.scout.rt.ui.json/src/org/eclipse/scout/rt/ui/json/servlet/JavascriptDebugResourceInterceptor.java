package org.eclipse.scout.rt.ui.json.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.ui.html.ITextFileLoader;
import org.eclipse.scout.ui.html.ScriptProcessor;
import org.eclipse.scout.ui.html.TextFileUtil;

/**
 * intercept scout-4.0.0.min.css and scout-4.0.0.min.js and replace by processing the source scout-4.0.0.css and
 * scout-4.0.0.js
 * <p>
 * The interception can be turned on and off using the url param ?debug=true
 */
public class JavascriptDebugResourceInterceptor {
  private static final long serialVersionUID = 1L;
  //path = $1 $3 $4 $5 with $1=folder, $3=basename, $4="-min", $5=".js" or ".css"
  private static final Pattern SCRIPT_FILE_PAT = Pattern.compile("(/(\\w+/)*)([^/]+)([-.]min)(\\.(js|css))");
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JavascriptDebugResourceInterceptor.class);

  private final List<ResourceHandler> m_resourceHandlers;

  public JavascriptDebugResourceInterceptor(List<ResourceHandler> resourceHandlers) {
    m_resourceHandlers = resourceHandlers;
  }

  public boolean handle(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    boolean debugEnabled = checkDebugEnabled(req, resp);
    if (debugEnabled) {
      if (pathInfo != null) {
        Matcher mat = SCRIPT_FILE_PAT.matcher(pathInfo);
        if (mat.matches()) {
          //is there a template for this script?
          String bundlePath = "src/main/js/" + mat.group(3) + "-template" + mat.group(5);
          ResourceHandler h = findHandlerFor(bundlePath);
          if (h != null) {
            LOG.info("replacing " + pathInfo + " by live processing /" + h.getBundle().getSymbolicName() + "/" + bundlePath);
            handleScriptTemplate(req, resp, h, bundlePath);
            return true;
          }
          //is there a uncompressed library version of this script?
          bundlePath = "libjs/" + mat.group(3) + mat.group(5);
          h = findHandlerFor(bundlePath);
          if (h != null) {
            LOG.info("replacing " + pathInfo + " by the uncompressed /" + h.getBundle().getSymbolicName() + "/" + bundlePath);
            handleScriptLibrary(req, resp, h, bundlePath);
            return true;
          }
        }
      }
    }
    return handleDefault(req, resp, pathInfo);
  }

  protected boolean checkDebugEnabled(HttpServletRequest req, HttpServletResponse resp) {
    HttpSession session = req.getSession(true);
    if (session == null) {
      return false;
    }
    String flag = req.getParameter("debug");
    if (flag != null) {
      session.setAttribute("JavascriptDebugResourceInterceptor.enabled", "true".equals(flag));
    }
    Boolean active = (Boolean) session.getAttribute("JavascriptDebugResourceInterceptor.enabled");
    if (active != null) {
      return active.booleanValue();
    }
    if (Platform.inDevelopmentMode()) {
      return true;
    }
    return false;
  }

  protected ResourceHandler findHandlerFor(String bundlePath) throws IOException, ServletException {
    for (ResourceHandler h : m_resourceHandlers) {
      if (h.getBundle().getEntry(bundlePath) != null) {
        return h;
      }
    }
    return null;
  }

  protected boolean handleDefault(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    for (ResourceHandler h : m_resourceHandlers) {
      if (h.handle(req, resp, pathInfo)) {
        return true;
      }
    }
    return false;
  }

  protected void handleScriptTemplate(HttpServletRequest req, HttpServletResponse resp, final ResourceHandler handler, String bundlePath) throws IOException, ServletException {
    URL url = handler.getBundle().getEntry(bundlePath);
    //process
    String input = TextFileUtil.readUTF8(url);
    ScriptProcessor processor = new ScriptProcessor();
    processor.setInput(bundlePath, input);
    processor.setIncludeFileLoader(new ITextFileLoader() {
      @Override
      public String read(String path) throws IOException {
        return TextFileUtil.readUTF8(handler.getBundle().getEntry(path));
      }
    });
    processor.setShowLineNumbers(true);
    byte[] outputBytes = processor.process().getBytes("UTF-8");
    int dot = bundlePath.lastIndexOf('.');
    String contentType = FileUtility.getContentTypeForExtension(bundlePath.substring(dot + 1));
    resp.setDateHeader("Last-Modified", System.currentTimeMillis());
    resp.setContentLength(outputBytes.length);
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    resp.getOutputStream().write(outputBytes);
  }

  protected void handleScriptLibrary(HttpServletRequest req, HttpServletResponse resp, final ResourceHandler handler, String bundlePath) throws IOException, ServletException {
    URL url = handler.getBundle().getEntry(bundlePath);
    String input = TextFileUtil.readUTF8(url);
    byte[] outputBytes = input.getBytes("UTF-8");
    int dot = bundlePath.lastIndexOf('.');
    String contentType = FileUtility.getContentTypeForExtension(bundlePath.substring(dot + 1));
    resp.setDateHeader("Last-Modified", System.currentTimeMillis());
    resp.setContentLength(outputBytes.length);
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    resp.getOutputStream().write(outputBytes);
  }

}
