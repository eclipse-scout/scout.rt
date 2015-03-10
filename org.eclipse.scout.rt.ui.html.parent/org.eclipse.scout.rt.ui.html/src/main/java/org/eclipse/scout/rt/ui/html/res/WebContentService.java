package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

/**
 * Default implementation of a {@link IWebContentService} that searches in local osgi bundle
 */
public class WebContentService implements IWebContentService {

  @Override
  public URL getScriptSource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    URL url = getResourceImpl(path);
    if (url != null) {
      return url;
    }
    return null;
  }

  @Override
  public URL getWebContentResource(String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    URL url = getResourceImpl("WebContent" + path);
    if (url != null) {
      return url;
    }
    return null;
  }

  protected URL getResourceImpl(String resourcePath) {
    //disable hacker attacks using '..'
    if (resourcePath.contains("..")) {
      throw new IllegalArgumentException("path must not contain any '..'");
    }
    return getClass().getClassLoader().getResource(resourcePath);
  }
}
