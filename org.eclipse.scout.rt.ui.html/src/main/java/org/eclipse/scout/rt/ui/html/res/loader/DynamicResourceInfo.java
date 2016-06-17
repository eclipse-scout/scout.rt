package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

/**
 * <h3>{@link DynamicResourceInfo}</h3>
 */
public class DynamicResourceInfo {

  public static final String PATH_PREFIX = "dynamic";
  /**
   * Pattern to determine if the provided url path is a dynamic resource path.
   */
  public static final Pattern PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH = Pattern.compile("^/" + PATH_PREFIX + "/([^/]*)/([^/]*)/(.*)$");

  private final String m_adapterId;
  private final String m_fileName;
  private final IUiSession m_uiSession;

  public DynamicResourceInfo(IJsonAdapter<?> jsonAdapter, String fileName) {
    this(jsonAdapter.getUiSession(), jsonAdapter.getId(), fileName);
  }

  public DynamicResourceInfo(IUiSession uiSession, String adapterId, String fileName) {
    m_adapterId = adapterId;
    m_fileName = fileName;
    m_uiSession = uiSession;
  }

  public String toPath() {
    String encodedFilename = IOUtility.urlEncode(getFileName());
    // / was encoded by %2F, revert this encoding otherwise filename doesn't look nice in browser download
    // Example for filesnames containing a /:
    // - relative reference from a unzipped zip file
    // - another path segment was explicitly added to distinguish between same filenames
    encodedFilename = encodedFilename.replace("%2F", "/");
    return PATH_PREFIX + '/' + getUiSession().getUiSessionId() + '/' + getJsonAdapterId() + '/' + encodedFilename;
  }

  public IUiSession getUiSession() {
    return m_uiSession;
  }

  public String getJsonAdapterId() {
    return m_adapterId;
  }

  public String getFileName() {
    return m_fileName;
  }

  public static DynamicResourceInfo fromPath(HttpServletRequest req, String path) {
    if (path == null) {
      return null;
    }

    Matcher m = PATTERN_DYNAMIC_ADAPTER_RESOURCE_PATH.matcher(path);
    if (!m.matches()) {
      return null;
    }

    String uiSessionId = m.group(1);
    String adapterId = m.group(2);
    String filename = m.group(3);

    // lookup the UiSession on the current HttpSession to ensure the requested dynamic resource
    // is from one of the UiSessions of the currently authenticated user!
    IUiSession uiSession = UiSession.get(req, uiSessionId);
    if (uiSession == null) {
      return null;
    }

    return new DynamicResourceInfo(uiSession, adapterId, filename);
  }
}
