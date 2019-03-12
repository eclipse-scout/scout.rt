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
package org.eclipse.scout.rt.ui.html;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClasspathFileWatch {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractClasspathFileWatch.class);

  private final WatchService m_watcher;
  private Map<WatchKey, Path> m_watchKeys = new HashMap<>();
  final ReentrantReadWriteLock m_watchKeyReadWriteLock = new ReentrantReadWriteLock();

  public AbstractClasspathFileWatch() throws IOException {
    m_watcher = FileSystems.getDefault().newWatchService();
    init();
    start();
  }

  public void init() throws IOException {
    for (Path p : getRootPaths()) {
      registerDirectoryRecursive(p);
    }
  }

  public void destroy() throws IOException {
    m_watcher.close();
  }

  /**
   * to handle file changes on any files on the classpath. Use this Service only for development reasons.
   */
  protected abstract void execFileChanged(Path path);

  protected void start() {
    Jobs.schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        try {
          WatchKey key;
          while ((key = m_watcher.take()) != null) {
            final Path keyPath;
            try {
              m_watchKeyReadWriteLock.readLock().lock();
              keyPath = m_watchKeys.get(key);
            }
            finally {
              m_watchKeyReadWriteLock.readLock().unlock();
            }
            if (keyPath == null) {
              LOG.error("Unregistered watch key '" + key + "'.");
              break;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
              Path path = keyPath.resolve((Path) event.context());
              if (Files.isDirectory(path)) {
                if (event.kind() == ENTRY_CREATE) {
                  registerDirectory(path);
                }
                else if (event.kind() == ENTRY_DELETE) {
                  unregisterDirectory(key, path);
                }
              }
              else if (event.kind() == ENTRY_MODIFY) {
                execFileChanged(path);
              }
            }
            key.reset();
          }
        }
        catch (ClosedWatchServiceException e) { //NOSONAR
          LOG.debug("Watcher stopped");
        }
      }
    }, Jobs.newInput()
        .withName("Less File change watch"));
  }

  protected Set<Path> getRootPaths() {
    Set<Path> result = new HashSet<>();
    StringTokenizer tokenizer = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
    while (tokenizer.hasMoreTokens()) {
      Path file = new File(tokenizer.nextToken()).toPath();
      if (Files.exists(file) && Files.isDirectory(file)) {
        result.add(file);
      }
    }
    return result;
  }

  protected void registerDirectoryRecursive(Path directory) throws IOException {
    // register directory and sub-directories
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        registerDirectory(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  protected void registerDirectory(Path directory) throws IOException {
    WatchKey key = directory.register(m_watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    try {
      m_watchKeyReadWriteLock.writeLock().lock();
      m_watchKeys.put(key, directory);
    }
    finally {
      m_watchKeyReadWriteLock.writeLock().unlock();
    }
  }

  protected void unregisterDirectory(WatchKey key, Path directory) {
    try {
      m_watchKeyReadWriteLock.writeLock().lock();
      m_watchKeys.remove(key);
    }
    finally {
      m_watchKeyReadWriteLock.writeLock().unlock();
    }
    key.cancel();
  }

}
