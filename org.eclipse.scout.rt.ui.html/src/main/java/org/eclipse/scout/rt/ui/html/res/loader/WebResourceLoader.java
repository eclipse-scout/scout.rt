package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.shared.ui.webresource.IWebResourceHelper;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceHelpers;

public class WebResourceLoader extends AbstractResourceLoader {

  private final boolean m_minify;
  private final boolean m_cacheEnabled;
  private final String m_theme;
  private final IWebResourceHelper m_helper;

  public WebResourceLoader(boolean minify, boolean cacheEnabled, String theme) {
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
    m_theme = theme;
    m_helper = WebResourceHelpers.create();
  }

  @Override
  public BinaryResource loadResource(String pathInfo) {
    return lookupResource(pathInfo)
        .map(this::toBinaryResources)
        .map(br -> br.withFilename(pathInfo))
        .map(BinaryResources::build)
        .orElse(null);
  }

  public boolean acceptFile(String file) {
    return lookupResource(file).isPresent();
  }

  protected Optional<Pair<URL, Integer>> lookupResource(String file) {
    if (m_theme != null) {
      // If the theme is set to something other than 'default', check if a file with that theme exists.
      String[] parts = FileUtility.getFilenameParts(file);
      if (parts != null && "css".equals(parts[1])) {
        String themeFragmentPath = parts[0] + '-' + m_theme + '.' + parts[1];
        Optional<Pair<URL, Integer>> resource = resolveResource(themeFragmentPath);
        if (resource.isPresent()) {
          return resource;
        }
      }
    }
    return resolveResource(file);
  }

  protected Optional<Pair<URL, Integer>> resolveResource(String file) {
    return m_helper.getScriptResource(file, m_minify)
        .map(url -> Optional.<Pair<URL, Integer>> of(new ImmutablePair<>(url, HttpCacheControl.MAX_AGE_ONE_YEAR)))
        .orElseGet(() -> m_helper.getWebResource(file)
            .map(url -> new ImmutablePair<>(url, HttpCacheControl.MAX_AGE_4_HOURS)));
  }

  protected BinaryResources toBinaryResources(Pair<URL, Integer> urlAndMaxAge) {
    URL url = urlAndMaxAge.getLeft();
    try {
      URLConnection connection = url.openConnection();
      byte[] bytes = IOUtility.readFromUrl(url);
      return BinaryResources.create()
          .withContent(bytes)
          .withCharset(StandardCharsets.UTF_8)
          .withLastModified(connection.getLastModified())
          .withCachingAllowed(m_cacheEnabled)
          .withCacheMaxAge(urlAndMaxAge.getRight());
    }
    catch (IOException e) {
      throw new PlatformException("Unable to read from url '{}'.", url, e);
    }
  }
}
