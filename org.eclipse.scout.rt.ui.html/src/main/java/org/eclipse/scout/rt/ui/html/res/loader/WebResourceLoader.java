package org.eclipse.scout.rt.ui.html.res.loader;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.ui.html.res.IWebResourceHelper;
import org.eclipse.scout.rt.ui.html.res.WebResourceHelpers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class WebResourceLoader extends AbstractResourceLoader {

  private final boolean m_minify;
  private final boolean m_cacheEnabled;
  private final IWebResourceHelper m_helper;

  public WebResourceLoader(boolean minify, boolean cacheEnabled) {
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
    m_helper = WebResourceHelpers.create();
  }

  @Override
  public BinaryResource loadResource(String pathInfo) throws IOException {
    return findResource(pathInfo)
        .map(this::toBinaryResource)
        .map(br -> br.withFilename(pathInfo))
        .map(BinaryResources::build)
        .orElse(null);
  }

  protected BinaryResources toBinaryResource(Pair<URL, Integer> urlAndMaxAge) {
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

  protected Optional<Pair<URL, Integer>> findResource(String file) {
    return m_helper.getScriptResource(file, m_minify)
        .map(url -> Optional.<Pair<URL, Integer>> of(new ImmutablePair<>(url, HttpCacheControl.MAX_AGE_ONE_YEAR)))
        .orElseGet(() -> m_helper.getWebResource(file)
            .map(url -> new ImmutablePair<>(url, HttpCacheControl.MAX_AGE_4_HOURS)));

  }

  public boolean acceptFile(String file) {
    return findResource(file).isPresent();
  }

}
