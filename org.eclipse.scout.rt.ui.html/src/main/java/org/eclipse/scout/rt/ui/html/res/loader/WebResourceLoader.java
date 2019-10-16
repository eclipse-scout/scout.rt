package org.eclipse.scout.rt.ui.html.res.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.shared.ui.webresource.IWebResourceResolver;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceDescriptor;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceResolvers;

public class WebResourceLoader extends AbstractResourceLoader {

  private final boolean m_minify;
  private final boolean m_cacheEnabled;
  private final String m_theme;
  private final IWebResourceResolver m_helper;

  public WebResourceLoader(boolean minify, boolean cacheEnabled, String theme) {
    m_minify = minify;
    m_cacheEnabled = cacheEnabled;
    m_theme = theme;
    m_helper = WebResourceResolvers.create();
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

  public Optional<WebResourceDescriptor> resolveResource(String pathInfo) {
    return lookupResource(pathInfo).map(ImmutablePair::getLeft);
  }

  protected Optional<ImmutablePair<WebResourceDescriptor, Integer>> lookupResource(String file) {
    return m_helper.resolveScriptResource(file, m_minify, m_theme)
        .map(descriptor -> Optional.of(new ImmutablePair<>(descriptor, HttpCacheControl.MAX_AGE_ONE_YEAR)))
        .orElseGet(() -> m_helper.resolveWebResource(file, m_minify)
            .map(descriptor -> new ImmutablePair<>(descriptor, HttpCacheControl.MAX_AGE_4_HOURS)));
  }

  protected BinaryResources toBinaryResources(ImmutablePair<WebResourceDescriptor, Integer> res) {
    URL url = res.getLeft().getUrl();
    try {
      URLConnection connection = url.openConnection();
      byte[] bytes = IOUtility.readFromUrl(url);
      return BinaryResources.create()
          .withContent(bytes)
          .withCharset(StandardCharsets.UTF_8)
          .withLastModified(connection.getLastModified())
          .withCachingAllowed(m_cacheEnabled)
          .withCacheMaxAge(res.getRight());
    }
    catch (IOException e) {
      throw new PlatformException("Unable to read from url '{}'.", url, e);
    }
  }

}
