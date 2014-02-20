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

/**
 * intercept js and css files and process #include directives
 * <p>
 * The interception can be turned on and off using the url param ?debug=true
 */
public class JavascriptDebugResourceInterceptor {
  private static final long serialVersionUID = 1L;
  private static final Pattern SCRIPT_FILE_PAT = Pattern.compile("(/res/(\\w+/)*)([^/]+\\.(js|css))");

  private final List<ResourceHandler> m_resourceHandlers;

  public JavascriptDebugResourceInterceptor(List<ResourceHandler> resourceHandlers) {
    m_resourceHandlers = resourceHandlers;
  }

  public boolean handle(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException, ServletException {
    if (interceptionEnabled(req, resp)) {
      if (pathInfo != null) {
        Matcher mat = SCRIPT_FILE_PAT.matcher(pathInfo);
        if (mat.matches()) {
          if (handleInterception(req, resp, mat.group(1) + "src-" + mat.group(3))) {
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
    //process
    String content = processIncludeDirectives(handler, url);
    byte[] contentBytes = content.getBytes("UTF-8");
    int lastDot = pathInfo.lastIndexOf('.');
    String contentType = FileUtility.getContentTypeForExtension(lastDot >= 0 ? pathInfo.substring(lastDot + 1) : pathInfo);

    resp.setDateHeader("Last-Modified", System.currentTimeMillis());
    resp.setContentLength(contentBytes.length);
    if (contentType != null) {
      resp.setContentType(contentType);
    }
    resp.getOutputStream().write(contentBytes);
    return true;
  }

  protected static final Pattern INCLUDE_PAT = Pattern.compile("\\$include\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

  protected String processIncludeDirectives(ResourceHandler handler, URL url) throws IOException {
    String content = textFileContent(url);
    StringBuilder buf = new StringBuilder();
    Matcher mat = INCLUDE_PAT.matcher(content);
    int pos = 0;
    while (mat.find()) {
      buf.append(content.substring(pos, mat.start()));
      buf.append(textFileContent(handler.getBundle().getResource(mat.group(1))));
      pos = mat.end();
    }
    buf.append(content.substring(pos));
    return buf.toString();
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
