package org.eclipse.scout.rt.ui.json.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.scout.ui.html.ITextFileLoader;
import org.eclipse.scout.ui.html.ScriptProcessor;

/**
 * intercept scout-4.0.0.min.css and scout-4.0.0.min.js and replace by processing the source scout-4.0.0.css and
 * scout-4.0.0.js
 * <p>
 * The interception can be turned on and off using the url param ?debug=true
 */
public class JavascriptDebugResourceInterceptor {
  private static final long serialVersionUID = 1L;
  //path = $1 $3 $4 $5 with $1=folder, $3=basename, $4=".min", $5=".js" or ".css"
  private static final Pattern SCRIPT_FILE_PAT = Pattern.compile("(/res/(\\w+/)*)([^/]+)(\\.min)(\\.(js|css))");

  private final List<ResourceHandler> m_resourceHandlers;

  public JavascriptDebugResourceInterceptor(List<ResourceHandler> resourceHandlers) {
    m_resourceHandlers = resourceHandlers;
  }

  public boolean handle(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    if (interceptionEnabled(req, resp)) {
      if (pathInfo != null) {
        Matcher mat = SCRIPT_FILE_PAT.matcher(pathInfo);
        if (mat.matches()) {
          String pathInfo2 = mat.group(1) + mat.group(3) + mat.group(5);
          //remove ".min" token and check if such a raw file exists
          if (handleInterception(req, resp, pathInfo2)) {
            System.out.println("replacing " + pathInfo + " by live processing " + pathInfo2);
            return true;
          }
        }
      }
    }
    return handleDefault(req, resp, pathInfo);
  }

  protected boolean interceptionEnabled(HttpServletRequest req, HttpServletResponse resp) {
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

  protected boolean handleDefault(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    for (ResourceHandler h : m_resourceHandlers) {
      if (h.handle(req, resp, pathInfo)) {
        return true;
      }
    }
    return false;
  }

  protected boolean handleInterception(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    ResourceHandler handler = null;
    URL url = null;
    for (ResourceHandler h : m_resourceHandlers) {
      url = h.resolveBundleResource(pathInfo);
      if (url != null) {
        handler = h;
        break;
      }
    }
    if (handler == null || url == null) {
      return false;
    }
    final ResourceHandler fHandler = handler;
    //process
    String input = textFileContent(url);
    ScriptProcessor processor = new ScriptProcessor();
    processor.setInput(pathInfo, input);
    processor.setIncludeFileLoader(new ITextFileLoader() {
      @Override
      public String read(String path) throws IOException {
        return textFileContent(fHandler.getBundle().getEntry(path));
      }
    });
    byte[] outputBytes = processor.process().getBytes("UTF-8");
    int lastDot = pathInfo.lastIndexOf('.');
    String contentType = FileUtility.getContentTypeForExtension(lastDot >= 0 ? pathInfo.substring(lastDot + 1) : pathInfo);
    resp.setDateHeader("Last-Modified", System.currentTimeMillis());
    resp.setContentLength(outputBytes.length);
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    resp.getOutputStream().write(outputBytes);
    return true;
  }

  protected String textFileContent(URL url) throws IOException {
    if (url == null) {
      throw new IOException("Cannot find resource: " + url);
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    InputStream is = url.openStream();
    try {
      byte[] buffer = new byte[8192];
      int bytesRead = is.read(buffer);
      while (bytesRead != -1) {
        os.write(buffer, 0, bytesRead);
        bytesRead = is.read(buffer);
      }
    }
    finally {
      is.close();
    }
    return new String(os.toByteArray(), "UTF-8");
  }

}
