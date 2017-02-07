package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.GlobalHttpResourceCache;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResourceCache;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiPrebuildFilesProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiPrebuildProperty;
import org.eclipse.scout.rt.ui.html.UiThemeUtility;
import org.eclipse.scout.rt.ui.html.res.loader.HtmlFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pre-builds files (HTML, JS, CSS) and puts them into the HTTP resource cache, when the server starts up. This prevents
 * that the first user must wait until all files are parsed during the first request. Pre-building only happens when
 * application is NOT in development mode. The list of files which are pre-built by this class is configured with the
 * config property {@link UiPrebuildFilesProperty}.
 *
 * @see GlobalHttpResourceCache
 * @since 6.0
 */
public class PrebuildFiles implements IPlatformListener {

  private static final Logger LOG = LoggerFactory.getLogger(PrebuildFiles.class);

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.PlatformStarted && CONFIG.getPropertyValue(UiPrebuildProperty.class)) {
      LOG.info("Pre-building of web resources is enabled");
      buildResources();
    }
  }

  /**
   * Pre-builds the HTML files and the referenced scripts (JS, CSS) in the HTML document. Each document is put into the
   * HTTP resource cache, so we don't have to build them again later. However, we have to make a few assumptions here.
   * Since we have no HTTP request or session at this point, we assume:
   * <ul>
   * <li>the default locale</li>
   * <li>the default theme</li>
   * <li>minifying is enabled</li>
   * <li>caching is enabled</li>
   * </ul>
   */
  protected void buildResources() {
    long t0 = System.nanoTime();
    List<String> files = CONFIG.getPropertyValue(UiPrebuildFilesProperty.class);
    IHttpResourceCache httpResourceCache = BEANS.get(GlobalHttpResourceCache.class);
    for (String file : files) {
      LOG.info("Pre-building resource '{}'...", file);
      try {
        HttpCacheObject cacheObject = loadResource(file);
        httpResourceCache.put(cacheObject);
      }
      catch (IOException e) {
        LOG.error("Failed to load resource", e);
      }
    }
    LOG.info("Finished pre-building of {} web resources {} ms", files.size(), StringUtility.formatNanos(System.nanoTime() - t0));
  }

  protected HttpCacheObject loadResource(String file) throws IOException {
    String defaultTheme = UiThemeUtility.getConfiguredTheme();
    HtmlFileLoader loader = new HtmlFileLoader(defaultTheme, true, true);
    HttpCacheKey cacheKey = loader.createCacheKey(file);
    return loader.loadResource(cacheKey);
  }
}
