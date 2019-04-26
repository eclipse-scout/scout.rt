package org.eclipse.scout.rt.ui.html.res;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;

@Bean
public class WebResourceHelpers {

  public static IWebResourceHelper create() {
    return BEANS.get(WebResourceHelpers.class).get();
  }

  protected IWebResourceHelper get() {
    if (Platform.get().inDevelopmentMode()) {
      return BEANS.get(FilesystemWebResourceHelper.class);
    }
    return BEANS.get(ClasspathWebResourceHelper.class);
  }
}
