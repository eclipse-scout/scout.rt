package org.eclipse.scout.rt.ui.html.json.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * serve a file as a servlet resource using caches
 */
public class StaticResourceHandler {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(StaticResourceHandler.class);
  private static final String LAST_MODIFIED = "Last-Modified"; //$NON-NLS-1$
  private static final String IF_MODIFIED_SINCE = "If-Modified-Since"; //$NON-NLS-1$
  private static final String IF_NONE_MATCH = "If-None-Match"; //$NON-NLS-1$
  private static final int IF_MODIFIED_SINCE_DELTA = 999;
  private static final String ETAG = "ETag"; //$NON-NLS-1$
  private static final int ANY_SIZE = 8192;

  public void handle(AbstractJsonServlet servlet, HttpServletRequest req, HttpServletResponse resp, URL url) throws ServletException, IOException {
    URLConnection connection = url.openConnection();
    long lastModified = connection.getLastModified();
    int contentLength = connection.getContentLength();
    int status = processCacheHeaders(req, resp, lastModified, contentLength);
    if (status == HttpServletResponse.SC_NOT_MODIFIED) {
      resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      return;
    }

    //Return file regularly if the client (browser) does not already have it or if the file has changed in the meantime
    byte[] content = fileContent(url);
    resp.setContentLength(content.length);

    //Prefer mime type mapping from container
    String path = url.getPath();
    int lastSlash = path.lastIndexOf('/');
    int lastDot = path.lastIndexOf('.');
    String fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    String fileExtension = lastDot >= 0 ? path.substring(lastDot + 1) : path;

    String contentType = servlet.getServletContext().getMimeType(fileName);
    if (contentType == null) {
      contentType = getMsOfficeMimeTypes(fileExtension);
    }
    if (contentType == null) {
      contentType = FileUtility.getContentTypeForExtension(fileExtension);
    }
    if (contentType == null) {
      LOG.warn("Could not determine content type of file " + path);
    }
    else {
      resp.setContentType(contentType);
    }

    resp.getOutputStream().write(content);
  }

  /**
   * TODO AWE: (scout) In org.eclipse.scout.commons.FileUtility hinzufügen
   * see: http://stackoverflow.com/questions/4212861/what-is-a-correct-mime-type-for-docx-pptx-etc
   */
  private static final Map<String, String> EXT_TO_MIME_TYPE_MAP = new HashMap<>();

  static {
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    EXT_TO_MIME_TYPE_MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    EXT_TO_MIME_TYPE_MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    EXT_TO_MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
    EXT_TO_MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    EXT_TO_MIME_TYPE_MAP.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
    EXT_TO_MIME_TYPE_MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
  }

  private String getMsOfficeMimeTypes(String fileExtension) {
    return EXT_TO_MIME_TYPE_MAP.get(fileExtension.toLowerCase());
  }

  /**
   * Checks whether the file needs to be returned or not, depending on the request headers and file modification state.
   * Also writes cache headers (last modified and etag) if the file needs to be returned.
   *
   * @return {@link HttpServletResponse#SC_NOT_MODIFIED} if the file hasn't changed in the meantime or
   *         {@link HttpServletResponse#SC_ACCEPTED} if the content of the file needs to be returned.
   */
  protected int processCacheHeaders(final HttpServletRequest req, final HttpServletResponse resp, long lastModified, int contentLength) {
    resp.setHeader("cache-control", "private, max-age=0, no-cache, no-store, must-revalidate");

    String etag = null;
    if (lastModified != -1L && contentLength != -1L) {
      etag = "W/\"" + contentLength + "-" + lastModified + "\""; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    // Check for cache revalidation.
    // We should prefer ETag validation as the guarantees are stronger and all
    // HTTP 1.1 clients should be using it
    String ifNoneMatch = req.getHeader(IF_NONE_MATCH);
    if (ifNoneMatch != null && etag != null && ifNoneMatch.indexOf(etag) != -1) {
      return HttpServletResponse.SC_NOT_MODIFIED;
    }
    else {
      long ifModifiedSince = req.getDateHeader(IF_MODIFIED_SINCE);
      // for purposes of comparison we add 999 to ifModifiedSince since the
      // fidelity
      // of the IMS header generally doesn't include milli-seconds
      if (ifModifiedSince > -1 && lastModified > 0 && lastModified <= (ifModifiedSince + IF_MODIFIED_SINCE_DELTA)) {
        return HttpServletResponse.SC_NOT_MODIFIED;
      }
    }

    // File needs to be returned regularly, write cache headers
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
      byte[] buffer = new byte[ANY_SIZE];
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
