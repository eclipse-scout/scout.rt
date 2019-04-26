package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;
import java.util.Optional;

public abstract class AbstractWebResourceHelper implements IWebResourceHelper {

  protected static final String DEV_FOLDER_NAME = "dev";
  protected static final String MIN_FOLDER_NAME = "prod";
  protected static final String WEB_RESOURCE_FOLDER_NAME = "res";

  public static String stripLeadingSlash(String path) {
    if (path == null) {
      return null;
    }
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  protected String getWebResourceFolder(boolean minified) {
    return minified ? MIN_FOLDER_NAME : DEV_FOLDER_NAME;
  }

  @Override
  public Optional<URL> getScriptResource(String path, boolean minified) {
    if (path == null) {
      return Optional.empty();
    }
    URL url = getResourceImpl(getWebResourceFolder(minified) + "/" + stripLeadingSlash(path));
    return Optional.ofNullable(url);
  }

  @Override
  public Optional<URL> getWebResource(String path) {
    if (path == null) {
      return Optional.empty();
    }
    URL url = getResourceImpl(WEB_RESOURCE_FOLDER_NAME + "/" + stripLeadingSlash(path));
    return Optional.ofNullable(url);
  }

  /**
   * @return The {@link URL} or {@code null}.
   */
  protected abstract URL getResourceImpl(String resourcePath);
}
