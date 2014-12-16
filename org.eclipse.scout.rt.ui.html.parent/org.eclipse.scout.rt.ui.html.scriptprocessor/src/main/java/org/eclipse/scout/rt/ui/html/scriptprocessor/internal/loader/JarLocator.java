package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JarLocator {
  private final Class<?> m_loaderClass;

  public JarLocator(Class<?> loaderClass) {
    m_loaderClass = loaderClass;
  }

  public URL getResource(String resourceName) {
    URL url = null;
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
    return url;
  }
}
