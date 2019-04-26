package org.eclipse.scout.rt.ui.html.res;

import java.net.URL;

public class ClasspathWebResourceHelper extends AbstractWebResourceHelper {
  @Override
  protected URL getResourceImpl(String resourcePath) {
    return getClass().getClassLoader().getResource(resourcePath);
  }
}
