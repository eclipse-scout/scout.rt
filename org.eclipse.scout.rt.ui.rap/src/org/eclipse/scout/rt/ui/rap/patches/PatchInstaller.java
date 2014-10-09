package org.eclipse.scout.rt.ui.rap.patches;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;

/**
 * Helper class to install additional JavaScript patches at runtime.
 */
public final class PatchInstaller {

  private PatchInstaller() {
  }

  /**
   * Installs the given JavaScript file.
   *
   * @param url
   *          {@link URL} to the JavaScript file.
   */
  public static void install(URL url) {
    ensureRegistered(url);
    ensureLoaded(url);
  }

  private static void ensureRegistered(URL url) {
    String identifier = toIdentifier(url);

    if (!RWT.getResourceManager().isRegistered(identifier)) {
      try {
        InputStream is = url.openStream();
        try {
          RWT.getResourceManager().register(identifier, is);
        }
        finally {
          is.close();
        }
      }
      catch (IOException e) {
        throw new RuntimeException("Failed to register patch: " + url, e);
      }
    }
  }

  private static void ensureLoaded(URL url) {
    JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
    loader.require(RWT.getResourceManager().getLocation(toIdentifier(url)));
  }

  /**
   * @return identifier to address the patch.
   */
  private static String toIdentifier(URL url) {
    return url.getPath();
  }
}
