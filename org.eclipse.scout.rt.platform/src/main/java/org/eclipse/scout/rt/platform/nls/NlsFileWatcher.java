/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.nls;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NlsFileWatcher {
  private static final Logger LOG = LoggerFactory.getLogger(NlsFileWatcher.class);
  public static final String TEXT_RESOURCE_EXTENSION = "properties";

  private final Map<WatchKey, Path> m_watchKeys = new HashMap<>();
  private final ReentrantReadWriteLock m_watchReadWriteLock = new ReentrantReadWriteLock();
  private final Map<Path, List<NlsFileChangeHandler>> m_handlers = new HashMap<>();

  private WatchService m_watcher;

  @PreDestroy
  public void destroy() throws IOException {
    m_watcher.close();
  }

  /**
   * Registers a handler for a resource bundle. The handler will be called once a file in the folder of the resource
   * bundle is changed.
   *
   * @param resourceBundleName
   *          name of the resource bundle
   * @param onFileChangeConsumer
   *          called once a file has changed.
   * @param cl
   *          class loader that can load the resource bundle.
   * @throws IOException
   *           if unable to watch the desired directory.
   */
  public void watch(String resourceBundleName, Consumer<Path> onFileChangeConsumer, ClassLoader cl) throws IOException {
    String fileName = resourceBundleName.replace('.', '/') + '.' + TEXT_RESOURCE_EXTENSION;
    URL resource = cl.getResource(fileName);

    if (resource == null || !resource.getProtocol().equals("file")) {
      LOG.debug("Resource bundle {} not found or inside a jar. Files will not be watched.", resourceBundleName);
      return;
    }

    Path directory = new File(resource.getPath()).toPath().getParent();
    try {
      m_watchReadWriteLock.writeLock().lock();
      ensureStarted();
      registerDirectory(directory);
      List<NlsFileChangeHandler> handlers = m_handlers.computeIfAbsent(directory, (key) -> new ArrayList<>());
      handlers.add(new NlsFileChangeHandler(resourceBundleName, onFileChangeConsumer));
    }
    finally {
      m_watchReadWriteLock.writeLock().unlock();
    }
  }

  protected synchronized void ensureStarted() throws IOException {
    if (m_watcher != null) {
      return;
    }
    m_watcher = FileSystems.getDefault().newWatchService();

    Thread watcher = new Thread(() -> {
      try {
        WatchKey key;
        while ((key = m_watcher.take()) != null) {
          final Path keyPath;
          try {
            m_watchReadWriteLock.readLock().lock();
            keyPath = m_watchKeys.get(key);
          }
          finally {
            m_watchReadWriteLock.readLock().unlock();
          }
          if (keyPath == null) {
            LOG.error("Unregistered watch key '" + key + "'.");
            break;
          }
          for (WatchEvent<?> event : key.pollEvents()) {
            Path path = keyPath.resolve((Path) event.context());
            if (event.kind() == ENTRY_MODIFY) {
              handleFileChanged(path);
            }
          }
          key.reset();
        }
      }
      catch (ClosedWatchServiceException e) { //NOSONAR
        LOG.debug("Watcher stopped");
      }
      catch (InterruptedException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }, "NLS File Watcher");
    watcher.setDaemon(true);
    watcher.start();
  }

  protected void registerDirectory(Path directory) throws IOException {
    WatchKey key = directory.register(m_watcher, ENTRY_MODIFY);
    try {
      m_watchReadWriteLock.writeLock().lock();
      m_watchKeys.put(key, directory);
    }
    finally {
      m_watchReadWriteLock.writeLock().unlock();
    }
  }

  protected void handleFileChanged(Path path) {
    try {
      m_watchReadWriteLock.readLock().lock();
      List<NlsFileChangeHandler> handlers = m_handlers.get(path.getParent());
      if (!CollectionUtility.isEmpty(handlers)) {
        handlers.forEach(handler -> handler.fileChanged(path));
      }
    }
    finally {
      m_watchReadWriteLock.readLock().unlock();
    }
  }
}
