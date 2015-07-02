package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

/**
 * Default implementation of a {@link IWebContentService} that searches on the classpath.
 */
public class WebContentService implements IWebContentService {

  protected String stripLeadingSlash(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  @Override
  public URL getScriptSource(String path) {
    if (path == null) {
      return null;
    }
    return getResourceImpl(stripLeadingSlash(path));
  }

  @Override
  public URL getWebContentResource(String path) {
    if (path == null) {
      return null;
    }
    return getResourceImpl("WebContent/" + stripLeadingSlash(path));
  }

  protected URL getResourceImpl(String resourcePath) {
    //disable hacker attacks using '..'
    if (resourcePath.contains("..")) {
      throw new IllegalArgumentException("path must not contain any '..'");
    }
    return getClass().getClassLoader().getResource(resourcePath);
  }
}
