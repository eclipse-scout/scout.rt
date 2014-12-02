package org.eclipse.scout.rt.ui.html;

import java.net.URL;

import org.osgi.framework.Bundle;

/**
 * Default implementation of a {@link IWebContentResourceLocator} that searches in local osgi bundle
 */
public class OsgiWebContentResourceLocator implements IWebContentResourceLocator {
  private Bundle m_osgiBundle;

  public OsgiWebContentResourceLocator(Bundle osgiBundle) {
    m_osgiBundle = osgiBundle;
  }

  /**
   * relative script path inside folder <code>/src/main/js/</code>
   */
  @Override
  public Script getScriptSource(String scriptPath) {
    if (scriptPath.startsWith("/")) {
      scriptPath = scriptPath.substring(1);
    }
    URL url = getResourceImpl(scriptPath);
    if (url != null) {
      return new Script(scriptPath, url, this);
    }
    url = getResourceImpl("src/main/js/" + scriptPath);
    if (url != null) {
      return new Script(scriptPath, url, this);
    }
    return null;
  }

  @Override
  public URL getWebContentResource(String resourcePath) {
    if (resourcePath.startsWith("/")) {
      resourcePath = resourcePath.substring(1);
    }
    return getResourceImpl("WebContent/" + resourcePath);
  }

  protected URL getResourceImpl(String resourcePath) {
    if (m_osgiBundle != null) {
      return m_osgiBundle.getEntry(resourcePath);
    }
    return null;
  }
}
