/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.res;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResourceCache;
import org.eclipse.scout.rt.ui.html.AbstractClasspathFileWatcher;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.ScriptfileBuildProperty;
import org.eclipse.scout.rt.ui.html.res.loader.ScriptFileLoader;
import org.eclipse.scout.rt.ui.html.script.ScriptFileBuilder;
import org.eclipse.scout.rt.ui.html.script.ScriptOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DevelopmentScriptfileCache extends HttpResourceCache {
  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentScriptfileCache.class);

  private final PathMatcher m_cssMatcher = FileSystems.getDefault().getPathMatcher("glob:**.css");
  private final PathMatcher m_jsMatcher = FileSystems.getDefault().getPathMatcher("glob:**.js");
  private final Map<HttpCacheKey, IFuture<HttpCacheObject>> m_pendingScriptFiles = new HashMap<>();
  private final Object m_rebuildJsLock = new Object();
  private final Object m_rebuildStylesheetLock = new Object();

  private boolean m_active;
  private AbstractClasspathFileWatcher m_scriptFileWatcher;
  private IFuture<Void> m_rebuildJsFuture;
  private IFuture<Void> m_rebuildStylesheetFuture;

  @PostConstruct
  public void init() {
    m_active = Platform.get().inDevelopmentMode() && !CONFIG.getPropertyValue(ScriptfileBuildProperty.class);
    if (!m_active) {
      return;
    }
    try {
      m_scriptFileWatcher = createScriptFileWatcher();
    }
    catch (IOException e) { //NOSONAR
      m_scriptFileWatcher = null;
      m_active = false;
      LOG.warn("Could not install watch service on classpath for less file rebuild. Use rebuild on every request!");
    }
  }

  @PreDestroy
  public void destroy() {
    try {
      m_scriptFileWatcher.destroy();
      m_scriptFileWatcher = null;
    }
    catch (IOException e) { //NOSONAR
      LOG.warn("Could not uninstall watch service on classpath for less file rebuild!");
    }
  }

  public boolean isActive() {
    return m_active;
  }

  @Override
  public HttpCacheObject get(HttpCacheKey cacheKey) {
    if (!isActive()) {
      return null;
    }
    if (!acceptKey(cacheKey)) {
      return null;
    }
    HttpCacheObject result = null;
    IFuture<HttpCacheObject> future = null;
    synchronized (getCacheLock()) {
      future = m_pendingScriptFiles.get(cacheKey);
      if (future == null) {
        result = super.get(cacheKey);
        if (result != null) {
          return result;
        }
        future = scheduleBuildScriptFile(cacheKey);
      }
    }
    try {
      return future.awaitDoneAndGet();
    }
    catch (FutureCancelledError ex) { //NOSONAR
      // try again
      return get(cacheKey);
    }
  }

  protected boolean acceptKey(HttpCacheKey cacheKey) {
    Path path = Paths.get(cacheKey.getResourcePath());
    return m_cssMatcher.matches(path) || m_jsMatcher.matches(path);
  }

  @Override
  public boolean put(HttpCacheObject obj) {
    boolean inserted = super.put(obj);
    if (inserted) {
      handleCacheChanged();
    }
    return inserted;
  }

  @Override
  public HttpCacheObject remove(HttpCacheKey cacheKey) {
    HttpCacheObject removed = super.remove(cacheKey);
    if (removed != null) {

      handleCacheChanged();
    }
    return removed;
  }

  protected void handleCacheChanged() {
    Jobs.schedule(() -> {
      try {
        BEANS.get(DevelopmentScriptFileCacheInitialLoader.class).storeInitialScriptfiles(getAllKeys());
      }
      catch (Exception e) {
        LOG.warn("Could not store cached scriptfiles in dev mode.", e);
      }
    }, Jobs.newInput().withName("development store cached script files."));
  }

  public void rebuildScripts(PathMatcher matcher) {
    synchronized (getCacheLock()) {
      m_pendingScriptFiles.forEach((ikey, future) -> future.cancel(true));
      m_pendingScriptFiles.clear();
      getAllKeys().stream()
          .filter(key -> matcher.matches(Paths.get(key.getResourcePath())))
          .forEach(key -> scheduleBuildScriptFile(key));
    }
  }

  protected IFuture<HttpCacheObject> scheduleBuildScriptFile(HttpCacheKey key) {
    synchronized (m_pendingScriptFiles) {
      IFuture<HttpCacheObject> feature = Jobs.schedule(createRecompileScriptJob(key), Jobs.newInput()
          .withName("Recompile scriptfile."));
      m_pendingScriptFiles.put(key, feature);
      return feature;
    }
  }

  protected void scheduleRebuildJsFiles() {
    LOG.debug("Rebuild all js files in development cache.");
    synchronized (m_rebuildJsLock) {
      if (m_rebuildJsFuture != null) {
        m_rebuildJsFuture.cancel(false);
        m_rebuildJsFuture = null;
      }
      m_rebuildJsFuture = Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            synchronized (m_rebuildJsLock) {
              if (IFuture.CURRENT.get().isCancelled()) {
                return;
              }
            }
            rebuildScripts(m_jsMatcher);
          }
          finally {
            synchronized (m_rebuildJsLock) {
              if (IFuture.CURRENT.get() == m_rebuildJsFuture) {
                m_rebuildJsFuture = null;
              }
            }
          }
        }
      }, Jobs.newInput()
          .withName("rebuild js files.")
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartIn(300, TimeUnit.MILLISECONDS)));
    }
  }

  protected void scheduleRebuildStylesheets() {
    LOG.debug("Rebuild all stylesheets files in development cache.");
    synchronized (m_rebuildStylesheetLock) {
      if (m_rebuildStylesheetFuture != null) {
        m_rebuildStylesheetFuture.cancel(false);
        m_rebuildStylesheetFuture = null;
      }
      m_rebuildStylesheetFuture = Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            synchronized (m_rebuildStylesheetLock) {
              if (IFuture.CURRENT.get().isCancelled()) {
                return;
              }
            }
            rebuildScripts(m_cssMatcher);
          }
          finally {
            synchronized (m_rebuildStylesheetLock) {
              if (IFuture.CURRENT.get() == m_rebuildStylesheetFuture) {
                m_rebuildStylesheetFuture = null;
              }
            }
          }
        }
      }, Jobs.newInput()
          .withName("rebuild stylesheets files.")
          .withExecutionTrigger(Jobs.newExecutionTrigger()
              .withStartIn(300, TimeUnit.MILLISECONDS)));
    }
  }

  protected AbstractClasspathFileWatcher createScriptFileWatcher() throws IOException {
    return new ScriptFileWatcher();
  }

  protected Callable<HttpCacheObject> createRecompileScriptJob(HttpCacheKey key) {
    return new RecompileScriptJob(key);
  }

  private class ScriptFileWatcher extends AbstractClasspathFileWatcher {

    private final PathMatcher m_lessMatcher = FileSystems.getDefault().getPathMatcher("glob:**.less");

    public ScriptFileWatcher() throws IOException {
      super();
    }

    @Override
    protected void execFileChanged(Path path) {
      if (m_lessMatcher.matches(path)) {
        scheduleRebuildStylesheets();
        return;
      }
      if (m_jsMatcher.matches(path)) {
        scheduleRebuildJsFiles();
      }
    }
  }

  private class RecompileScriptJob implements Callable<HttpCacheObject> {

    private final HttpCacheKey m_key;

    public RecompileScriptJob(HttpCacheKey key) {
      m_key = key;
    }

    @Override
    public HttpCacheObject call() throws Exception {
      try {
        ScriptFileBuilder builder = new ScriptFileBuilder(BEANS.get(IWebContentService.class), m_key.getAttribute(ScriptFileLoader.THEME_KEY), Boolean.parseBoolean(m_key.getAttribute(ScriptFileLoader.MINIFYED_KEY)));
        ScriptOutput out = builder.buildScript(m_key.getResourcePath());
        if (out == null) {
          return null;
        }
        if (IFuture.CURRENT.get().isCancelled()) {
          return null;
        }
        HttpCacheObject cacheObject = new HttpCacheObject(m_key,
            BinaryResources.create()
                .withFilename(ScriptFileLoader.translateLess(out.getPathInfo()))
                .withCharset(StandardCharsets.UTF_8)
                .withContent(out.getContent())
                .withLastModified(out.getLastModified())
                .withCachingAllowed(true)
                .withCacheMaxAge(HttpCacheControl.MAX_AGE_ONE_YEAR)
                .build());
        if (!IFuture.CURRENT.get().isCancelled()) {
          synchronized (getCacheLock()) {
            LOG.debug("put {} to cache.", m_key);
            put(cacheObject);
          }
        }
        return cacheObject;
      }
      finally {
        synchronized (getCacheLock()) {
          m_pendingScriptFiles.remove(m_key);
        }
      }
    }
  }
}
