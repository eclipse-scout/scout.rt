package org.eclipse.scout.rt.ui.html.json.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.ui.html.ITextFileLoader;
import org.eclipse.scout.rt.ui.html.ScriptProcessor;
import org.eclipse.scout.rt.ui.html.TextFileUtil;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * intercept scout-4.0.0.min.css and scout-4.0.0.min.js and replace by processing the source scout-4.0.0.css and
 * scout-4.0.0.js
 * <p>
 * The interception can be turned on and off using the url param ?debug=true
 */
@Priority(10)
public class JavascriptDebugRequestInterceptor extends AbstractService implements IServletRequestInterceptor {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JavascriptDebugRequestInterceptor.class);
  private static final String SESSION_ATTR_ENABLED = JavascriptDebugRequestInterceptor.class.getSimpleName() + ".enabled";
  private static final String DEBUG_PARAM = "debug";
  //path = $1 $3 $4 $5 with $1=folder, $3=basename, $4="-min", $5=".js" or ".css"
  private static final Pattern SCRIPT_FILE_PATTERN = Pattern.compile("(/(\\w+/)*)([^/]+)([-.]min)(\\.(js|css))");
  private static final Pattern VERSION_PATTERN = Pattern.compile("([0-9]*\\.[0-9]*\\.[0-9]*)");

  @Override
  public boolean interceptPost(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    return false;
  }

  @Override
  public boolean interceptGet(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    boolean debugEnabled = checkDebugEnabled(req, resp);
    if (!debugEnabled || pathInfo == null) {
      return false;
    }

    Matcher mat = SCRIPT_FILE_PATTERN.matcher(pathInfo);
    if (mat.matches()) {
      //is there a template for this script?
      String name = extractName(mat);
      String bundlePath = "src/main/js/" + name + "-template" + mat.group(5); // path in development mode
      URL url = findBundleResource(bundlePath);
      if (url == null) {
        bundlePath = name + "-template" + mat.group(5); // path in deployed mode
        url = findBundleResource(bundlePath);
      }
      if (url != null) {
        LOG.info("replacing " + pathInfo + " by live processing /" + bundlePath);
        handleScriptTemplate(req, resp, url, bundlePath);
        return true;
      }

      //is there a uncompressed library version of this script?
      bundlePath = "libjs/" + mat.group(3) + mat.group(5);
      url = findBundleResource(bundlePath);
      if (url != null) {
        LOG.info("replacing " + pathInfo + " by the uncompressed /" + bundlePath);
        handleScriptLibrary(req, resp, url, bundlePath);
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the name without the version suffix.
   */
  protected String extractName(Matcher matcher) {
    String name = matcher.group(3);
    int versionStart = name.lastIndexOf('-');
    if (versionStart > -1) {
      String version = name.substring(versionStart + 1);
      Matcher versionMatcher = VERSION_PATTERN.matcher(version);
      if (versionMatcher.matches()) {
        return name.substring(0, versionStart);
      }
    }
    return name;
  }

  protected boolean checkDebugEnabled(HttpServletRequest req, HttpServletResponse resp) {
    HttpSession session = req.getSession(true); //FIXME this is probably NOT the right place to create new sessions
    if (session == null) {
      return false;
    }
    String flag = req.getParameter(DEBUG_PARAM);
    if (flag != null) {
      session.setAttribute(SESSION_ATTR_ENABLED, "true".equals(flag));
    }
    Boolean active = (Boolean) session.getAttribute(SESSION_ATTR_ENABLED);
    if (active != null) {
      return active.booleanValue();
    }
    if (Platform.inDevelopmentMode()) {
      return true;
    }
    return false;
  }

  protected URL findBundleResource(String bundlePath) {
    for (IServletResourceProvider provider : SERVICES.getServices(IServletResourceProvider.class)) {
      URL url = provider.resolveBundleResource(bundlePath);
      if (url != null) {
        return url;
      }
    }
    return null;
  }

  protected void handleScriptTemplate(HttpServletRequest req, HttpServletResponse resp, final URL url, String bundlePath) throws IOException, ServletException {
    String input = TextFileUtil.readUTF8(url);
    ScriptProcessor processor = new ScriptProcessor();
    processor.setInput(bundlePath, input);
    processor.setIncludeFileLoader(new ITextFileLoader() {
      @Override
      public String read(String path) throws IOException {
        URL includeUrl = findBundleResource(path);
        if (includeUrl == null) {
          throw new FileNotFoundException(path);
        }
        return TextFileUtil.readUTF8(includeUrl);
      }
    });
    if (bundlePath.endsWith(".js")) {
      processor.setShowLineNumbers(true);
    }
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

  protected void handleScriptLibrary(HttpServletRequest req, HttpServletResponse resp, URL url, String bundlePath) throws IOException, ServletException {
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
