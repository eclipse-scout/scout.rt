package org.eclipse.scout.rt.ui.html.thirdparty.internal.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;

public class JarLocator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JarLocator.class);

  private Class<?> m_loaderClass;
  private Bundle m_osgiBundle;

  public JarLocator(Class<?> loaderClass, Bundle osgiBundle) {
    m_loaderClass = loaderClass;
    m_osgiBundle = osgiBundle;
  }

  public URL getResource(String resourceName) {
    URL url = null;
    if (m_osgiBundle != null) {
      //path in binary build
      url = m_osgiBundle.getEntry(resourceName);
      if (url != null) {
        return url;
      }
      //path in development workspace
      url = m_osgiBundle.getEntry("src/main/resources/" + resourceName);
      if (url != null) {
        return url;
      }
    }
    if (m_loaderClass != null) {
      //path in binary build
      url = m_loaderClass.getResource("/" + resourceName);
      if (url != null) {
        return url;
      }
      //path in development workspace
      url = m_loaderClass.getResource("/src/main/resources/" + resourceName);
      if (url != null) {
        return url;
      }
    }
    return null;
  }

  public URL getJarContaining(Class<?> clazz) {
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    if (url == null) {
      return url;
    }
    if (url.getPath().endsWith(".jar")) {
      return url;
    }
    //workspace
    try {
      URL fileUrl = new URL(url, "target/classes/");
      File f = new File(fileUrl.getPath());
      if (f.exists()) {
        return fileUrl;
      }
      fileUrl = url;
      f = new File(fileUrl.getPath());
      if (f.exists()) {
        return fileUrl;
      }
    }
    catch (MalformedURLException e) {
      //nop
    }
    LOG.warn("could not verify if the url exists: " + url);
    return url;
  }
}
