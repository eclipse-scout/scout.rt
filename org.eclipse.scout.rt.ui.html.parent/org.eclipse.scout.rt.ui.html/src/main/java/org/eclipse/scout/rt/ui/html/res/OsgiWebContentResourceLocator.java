package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.SERVICES;

/**
 * Default implementation of a {@link IWebContentResourceLocator} that searches in local osgi bundle
 */
public class OsgiWebContentResourceLocator implements IWebContentResourceLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OsgiWebContentResourceLocator.class);

  @Override
  public URL getScriptSource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    URL url = getResourceImpl(path);
    if (url != null) {
      return url;
    }
    url = getResourceImpl("src/main/js/" + path);
    if (url != null) {
      return url;
    }
    return null;
  }

  @Override
  public URL getWebContentResource(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return getResourceImpl("WebContent/" + path);
  }

  protected URL getResourceImpl(String resourcePath) {
    for (OsgiWebContentService s : SERVICES.getServices(OsgiWebContentService.class)) {
      URL url = s.getBundle().getEntry(resourcePath);
      if (url != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("locate resource '" + resourcePath + "' -> " + url);
        }
        return url;
      }
    }
    return null;
  }
}
