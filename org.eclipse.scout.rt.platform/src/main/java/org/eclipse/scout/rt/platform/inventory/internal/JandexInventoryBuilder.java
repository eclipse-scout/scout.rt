/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JandexRebuildProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JandexInventoryBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(JandexInventoryBuilder.class);

  public static final String SCOUT_XML_PATH = "META-INF/scout.xml";
  public static final String JANDEX_INDEX_PATH = "META-INF/jandex.idx";

  public enum RebuildStrategy {
    IF_MISSING, IF_MODIFIED, ALWAYS
  }

  protected static class IndexMetaData {
    private final long m_lastModified;
    private final int m_fileCount;

    protected IndexMetaData(long lastModified, int fileCount) {
      m_lastModified = lastModified;
      m_fileCount = fileCount;
    }

    protected long lastModified() {
      return m_lastModified;
    }

    protected int fileCount() {
      return m_fileCount;
    }
  }

  private final RebuildStrategy m_rebuildStrategy;

  private final List<IndexView> m_indexList;

  public JandexInventoryBuilder() {
    // do not use the CONFIG class here because the platform is not ready yet
    this(new JandexRebuildProperty().getValue());
  }

  public JandexInventoryBuilder(RebuildStrategy rebuildStrategy) {
    m_rebuildStrategy = rebuildStrategy;
    m_indexList = new ArrayList<>();
  }

  public RebuildStrategy getRebuildStrategy() {
    return m_rebuildStrategy;
  }

  public void scanAllModules() {
    try {
      Collections.list(getClass().getClassLoader().getResources(SCOUT_XML_PATH))
          .parallelStream()
          .map(this::findIndexUri)
          .forEach(this::scanModule);
    }
    catch (IOException ex) {
      throw new PlatformException("Error while reading resources '{}'", SCOUT_XML_PATH, ex);
    }
  }

  public Index scanModule(URI indexUri) {
    try {
      Index index = scanModuleUnsafe(indexUri);
      if (index != null) {
        synchronized (m_indexList) {
          m_indexList.add(index);
        }
      }
      return index;
    }
    catch (PlatformException p) {
      throw p;
    }
    catch (Exception ex) {
      throw new PlatformException("Cannot scan location '{}' with jandex", indexUri, ex);
    }
  }

  protected Index scanModuleUnsafe(URI indexUri) throws IOException, URISyntaxException {
    if ("file".equals(indexUri.getScheme())) {
      return scanFolder(indexUri);
    }
    else if ("jar".equals(indexUri.getScheme())) {
      return scanJar(indexUri);
    }
    else {
      return scanOther(indexUri);
    }
  }

  protected Index scanFolder(URI indexUri) throws IOException {
    File indexFile0 = new File(indexUri);
    File classesFolder = indexFile0.getParentFile().getParentFile();
    try (LockedFile indexFile = new LockedFile(indexFile0)) {
      IndexMetaData newMeta = null;
      Index index = null;
      boolean canReadFromIndexFile = indexFile.exists();
      if (canReadFromIndexFile) {
        switch (m_rebuildStrategy) {
          case IF_MODIFIED:
            newMeta = indexMetaData(classesFolder.toPath());
            //pass 1: check modified
            long oldModified = indexFile.lastModified();
            if (newMeta.lastModified() > oldModified) {
              if (LOG.isInfoEnabled()) {
                LOG.info("Drop outdated index '{}'. Index timestamp {} is older than folder timestamp {}.", indexUri,
                    DateUtility.format(new Date(oldModified), "yyyy-MM-dd HH:mm:ss.SSS"),
                    DateUtility.format(new Date(newMeta.lastModified()), "yyyy-MM-dd HH:mm:ss.SSS"));
              }
              canReadFromIndexFile = false;
            }
            else {
              //pass 2: file count
              index = readIndex(indexUri, indexFile.newInputStream());
              int oldFileCount = index.getKnownClasses().size();
              if (newMeta.fileCount() != oldFileCount) {
                LOG.info("Drop divergent index '{}'. Index file count {} != folder file count {}.", indexUri, oldFileCount, newMeta.fileCount());
                index = null;
                canReadFromIndexFile = false;
              }
            }
            break;
          case ALWAYS:
            canReadFromIndexFile = false;
            break;
          default:
            //nop
        }
      }

      if (index == null && canReadFromIndexFile) {
        index = readIndex(indexUri, indexFile.newInputStream());
      }
      if (index != null) {
        return index;
      }

      if (newMeta == null) {
        newMeta = indexMetaData(classesFolder.toPath());
      }
      LOG.info("Rebuild index '{}'. Scanning location...", indexUri);
      Indexer indexer = new Indexer();
      index = createFolderIndex(classesFolder.toPath(), indexer);
      if (indexFile.isReadOnly()) {
        LOG.info("Cannot write index to file '{}'. File is read-only. Using newly created index without flushing it to the storage.", indexFile);
      }
      else {
        writeIndex(index, indexFile);
        if (!indexFile.setLastModified(newMeta.lastModified())) {
          LOG.warn("Cannot set lastModified on '{}'", indexFile);
        }
      }
      return index;
    }
  }

  protected Index scanJar(URI indexUri) throws URISyntaxException, IOException {
    String s = indexUri.getRawSchemeSpecificPart();
    Index index = readIndex(indexUri);
    if (index != null) {
      return index;
    }
    LOG.info("Found no pre-built '{}'. Scanning location...", indexUri);
    File jarFile = new File(new URI(s.substring(0, s.lastIndexOf('!'))));
    Indexer indexer = new Indexer();
    //use a temp file, we don't want jandex to create a file in .m2 directories!
    File tmp = File.createTempFile("jandex", ".idx");
    try {
      return JarIndexer.createJarIndex(jarFile, indexer, tmp, false, false, false).getIndex();
    }
    finally {
      //noinspection ResultOfMethodCallIgnored
      tmp.delete();
    }
  }

  protected Index scanOther(URI indexUri) {
    Index index = readIndex(indexUri);
    if (index != null) {
      return index;
    }
    throw new PlatformException("Unknown protocol in '{}'", indexUri);
  }

  protected IndexMetaData indexMetaData(Path dir) throws IOException {
    final AtomicLong lastModifiedRef = new AtomicLong();
    final AtomicInteger fileCountRef = new AtomicInteger();
    JandexFiles.walkFileTree(dir, (path, attrs) -> {
      if (!acceptPathForIndex(path)) {
        return;
      }
      fileCountRef.incrementAndGet();
      long t = attrs.lastModifiedTime().toMillis();
      if (t > lastModifiedRef.get()) {
        lastModifiedRef.set(t);
      }
    });
    return new IndexMetaData(lastModifiedRef.get(), fileCountRef.get());
  }

  protected boolean acceptPathForIndex(Path path) {
    return true;
  }

  protected URI findIndexUri(URL scoutXmlUrl) {
    String s = scoutXmlUrl.toExternalForm();
    try {
      return new URI(s.substring(0, s.length() - SCOUT_XML_PATH.length()) + JANDEX_INDEX_PATH);
    }
    catch (URISyntaxException ex) {
      throw new PlatformException("Cannot find index URI from '{}'", s, ex);
    }
  }

  /**
   * @param indexUri
   *          path to jandex.idx, typically ending in /META-INF/jandex.idx
   * @return index or null
   */
  protected Index readIndex(URI indexUri) {
    try (InputStream in = new BufferedInputStream(indexUri.toURL().openStream())) {
      return readIndex(indexUri, in);
    }
    catch (PlatformException e) {
      throw e;
    }
    catch (FileNotFoundException e) {
      LOG.debug("No pre-built index found: {}", indexUri, e);
      return null;
    }
    catch (Exception ex) {
      throw new PlatformException("Error reading index '{}'", indexUri, ex);
    }
  }

  /**
   * @param indexUri
   *          path to jandex.idx, typically ending in /META-INF/jandex.idx
   * @return index or null
   */
  protected Index readIndex(URI indexUri, InputStream in) {
    try {
      Index index = new IndexReader(in).read();
      LOG.debug("Found pre-built {}", indexUri);
      return index;
    }
    catch (Exception ex) {
      throw new PlatformException("Error reading index '{}'", indexUri, ex);
    }
  }

  protected Index createFolderIndex(Path dir, final Indexer indexer) throws IOException {
    JandexFiles.walkFileTree(dir, (path, attrs) -> appendPathToIndex(path, indexer));
    return indexer.complete();
  }

  protected void appendPathToIndex(Path path, Indexer indexer) throws IOException {
    try (InputStream in = Files.newInputStream(path)) {
      indexer.index(in);
    }
  }

  protected void writeIndex(Index index, LockedFile f) {
    try {
      LOG.debug("Write jandex index file '{}'", f);
      new IndexWriter(f.newOutputStream()).write(index);
    }
    catch (Exception ex) {
      LOG.warn("Error while writing jandex index file '{}'", f, ex);
    }
  }

  public IndexView finish() {
    return CompositeIndex.create(m_indexList);
  }

  protected List<IndexView> getIndexList() {
    return m_indexList;
  }
}
