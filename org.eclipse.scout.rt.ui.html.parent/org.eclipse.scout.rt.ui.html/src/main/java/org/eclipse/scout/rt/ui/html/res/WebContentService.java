package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Default implementation of a {@link IWebContentService} that searches in local osgi bundle
 */
public class WebContentService implements IWebContentService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(WebContentService.class);

  @Override
  public URL getScriptSource(String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    URL url = getResourceImpl(path);
    if (url != null) {
      return url;
    }
    url = getResourceImpl("/src/main/js" + path);
    if (url != null) {
      return url;
    }
    url = getResourceImpl("/src/test/js" + path);
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
    URL url = getResourceImpl("/WebContent" + path);
    if (url != null) {
      return url;
    }
    url = getResourceImpl("/src/main/resources/WebContent" + path);
    if (url != null) {
      return url;
    }
    url = getResourceImpl("/src/test/resources/WebContent" + path);
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
    if (Platform.isRunning()) {
      for (Bundle bundle : FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles()) {
        URL url = bundle.getEntry(resourcePath);
        if (url != null) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("locate resource '" + resourcePath + "' -> " + url);
          }
          return url;
        }
      }
    }
    else {
      return getClass().getClassLoader().getResource(resourcePath);
    }
    return null;
  }
}
