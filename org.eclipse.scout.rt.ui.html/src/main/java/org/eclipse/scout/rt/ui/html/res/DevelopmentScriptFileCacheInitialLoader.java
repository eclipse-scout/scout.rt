/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.res;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.shared.ui.webresource.WebResourceResolvers;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.ScriptfileBuildProperty;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.ScriptfileBuilderDevCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentScriptFileCacheInitialLoader implements IPlatformListener {
  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentScriptFileCacheInitialLoader.class);

  private Path m_cacheFile;
  private boolean m_active;
  private Set<HttpCacheKey> m_cachedKeys;

  @PostConstruct
  public synchronized void init() {
    if (WebResourceResolvers.isNewMode()) {
      m_active = false;
    }
    else {
      m_active = Platform.get().inDevelopmentMode() && !CONFIG.getPropertyValue(ScriptfileBuildProperty.class);
    }
    if (!m_active) {
      return;
    }
    try {
      m_cacheFile = getCachePersistFile();
      if (m_cacheFile == null) {
        m_active = false;
      }
    }
    catch (IOException e) {
      m_active = false;
      LOG.warn("Could not initialize initial loader for scripts cached in development cache.", e);
    }

  }

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == IPlatform.State.PlatformStarted) {
      if (!m_active) {
        return;
      }
      try {
        DevelopmentScriptfileCache cache = BEANS.get(DevelopmentScriptfileCache.class);
        m_cachedKeys = loadCachedKeys();
        m_cachedKeys.forEach(key -> cache.scheduleBuildScriptFile(key));
      }
      catch (ClassNotFoundException | IOException e) {
        LOG.warn("Could not load cached resources (HttpCacheKeys) from user.home.", e);
      }
    }
  }

  protected synchronized Path getCachePersistFile() throws IOException {
    String persistKey = CONFIG.getPropertyValue(ScriptfileBuilderDevCacheKey.class);
    if (!StringUtility.hasText(persistKey)) {
      return null;
    }
    Path userHome = Paths.get(System.getProperty("user.home"));
    if (!Files.exists(userHome) || !Files.isDirectory(userHome)) {
      LOG.warn("Could not resolve user.home directory '{}'.", System.getProperty("user.home"));
      return null;
    }
    Path file = userHome.resolve(Paths.get(".eclipse", "org.eclipse.scout.dev", "scriptfilecache_" + persistKey + ".obj"));
    if (!Files.exists(file)) {
      Files.createDirectories(file.getParent());
      Files.createFile(file);
    }
    return file;
  }

  protected synchronized Set<HttpCacheKey> loadCachedKeys() throws IOException, ClassNotFoundException {
    ObjectInputStream objectinputstream = null;
    try {
      objectinputstream = new ObjectInputStream(Files.newInputStream(m_cacheFile));
      @SuppressWarnings("unchecked")
      Set<HttpCacheKey> loadedFiles = (Set<HttpCacheKey>) objectinputstream.readObject();
      if (LOG.isInfoEnabled()) {
        LOG.info("Load keys from persist cache in user.home '{}'.", loadedFiles
            .stream()
            .map(k -> k.getResourcePath())
            .collect(Collectors.joining(", ")));
      }
      return loadedFiles;
    }
    catch (EOFException e) { //NOSONAR
      // file is empty
      return CollectionUtility.emptyHashSet();
    }
    finally {
      if (objectinputstream != null) {
        objectinputstream.close();
      }
    }
  }

  public synchronized void storeInitialScriptfiles(Set<HttpCacheKey> keys) throws IOException {
    if (!m_active) {
      return;
    }
    if (CollectionUtility.equalsCollection(m_cachedKeys, keys)) {
      // already stored
      return;
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("Persist cache keys for development script cache '{}'.", keys
          .stream()
          .map(k -> k.getResourcePath())
          .collect(Collectors.joining(", ")));
    }
    ObjectOutputStream oos = null;
    try {
      oos = new ObjectOutputStream(Files.newOutputStream(m_cacheFile));
      oos.writeObject(keys);
      m_cachedKeys = CollectionUtility.hashSet(keys);
    }
    finally {
      if (oos != null) {
        oos.close();
      }
    }
  }
}
