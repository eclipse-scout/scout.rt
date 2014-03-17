package org.eclipse.scout.rt.ui.json.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.FileUtility;
import org.osgi.framework.Bundle;

/**
 * Serve files from the bundles 'WebContent' folder
 */
public class ResourceHandler {
  private static final long serialVersionUID = 1L;
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final String ETAG = "ETag"; //$NON-NLS-1$

  private final Bundle m_bundle;
  private final String m_bundleWebContentFolder;

  /**
   * @param webContentFolder
   *          by default "WebContent"
   */
  public ResourceHandler(Bundle bundle, String webContentFolder) {
    m_bundle = bundle;
    m_bundleWebContentFolder = webContentFolder;
  }

  public boolean handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    URL url = resolveBundleResource(pathInfo);
    if (url == null) {
      return false;
    }
    URLConnection connection = url.openConnection();
    long lastModified = connection.getLastModified();
    int contentLength = connection.getContentLength();
    int lastDot = pathInfo.lastIndexOf('.');
    String contentType = FileUtility.getContentTypeForExtension(lastDot >= 0 ? pathInfo.substring(lastDot + 1) : pathInfo);
    if (setResponseHeaders(req, resp, contentType, lastModified, contentLength) == HttpServletResponse.SC_NOT_MODIFIED) {
      return true;
    }
    byte[] content = fileContent(url);
    resp.setContentLength(content.length);
    resp.getOutputStream().write(content);
    return true;
  }

  protected Bundle getBundle() {
    return m_bundle;
  }

  protected String getBundleWebContentFolder() {
    return m_bundleWebContentFolder;
  }

  /**
   * resolve a web path /res/scout.css to a bundle resource WebContent/res/scout.css
   */
  protected URL resolveBundleResource(String pathInfo) {
    if (pathInfo == null) {
      return null;
    }
    if (pathInfo.equals("/")) {
      pathInfo = "/index.html";
    }
    return getBundle().getEntry(getBundleWebContentFolder() + pathInfo);
  }

  protected int setResponseHeaders(final HttpServletRequest req, final HttpServletResponse resp, String contentType, long lastModified, int contentLength) {
    String etag = null;
    if (lastModified != -1 && contentLength != -1)
    {
      etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return HttpServletResponse.SC_NOT_MODIFIED;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the
      // fidelity
      // of the IMS header generally doesn't include milli-seconds
      if (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + 999)) {
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return HttpServletResponse.SC_NOT_MODIFIED;
      }
    }

    // return the full contents regularly
    if (contentLength != -1) {
      resp.setContentLength(contentLength);
    }

    if (contentType != null) {
      resp.setContentType(contentType);
    }

    if (lastModified > 0) {
      resp.setDateHeader(LAST_MODIFIED, lastModified);
    }

    if (etag != null) {
      resp.setHeader(ETAG, etag);
    }
    return HttpServletResponse.SC_ACCEPTED;
  }

  protected byte[] fileContent(URL url) throws IOException {
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
    return os.toByteArray();
  }

}
