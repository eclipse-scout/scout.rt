package org.eclipse.scout.rt.ui.html.script;

import java.net.URL;

import org.eclipse.scout.service.SERVICES;

/**
 * Default implementation of a {@link IWebArchiveResourceLocator} that searches in local osgi bundle
 */
public class OsgiWebArchiveResourceLocator implements IWebArchiveResourceLocator {

  @Override
  public URL getScriptResource(String path) {
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
    for (OsgiWebArchiveService s : SERVICES.getServices(OsgiWebArchiveService.class)) {
      URL url = s.getBundle().getEntry(resourcePath);
      if (url != null) {
        return url;
      }
    }
    return null;
  }
}
