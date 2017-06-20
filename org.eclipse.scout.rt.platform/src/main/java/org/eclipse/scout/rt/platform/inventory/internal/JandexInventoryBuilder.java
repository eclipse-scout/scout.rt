/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JandexRebuildProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
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
    this(
        new JandexRebuildProperty().getValue()
            ? RebuildStrategy.ALWAYS
            : new PlatformDevModeProperty().getValue()
                ? RebuildStrategy.IF_MODIFIED
                : RebuildStrategy.IF_MISSING);
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
      for (Enumeration<URL> en = getClass().getClassLoader().getResources(SCOUT_XML_PATH); en.hasMoreElements();) {
        URL url = en.nextElement();
        URI indexUri = findIndexUri(url);
        scanModule(indexUri);
      }
    }
    catch (IOException ex) {
      throw new PlatformException("Error while reading resources '{}'", SCOUT_XML_PATH, ex);
    }
  }

  public Index scanModule(URI indexUri) {
    try {
      Index index = scanModuleUnsafe(indexUri);
      if (index != null) {
        m_indexList.add(index);
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
    File indexFile = new File(indexUri);
    File classesFolder = indexFile.getParentFile().getParentFile();
    IndexMetaData newMeta = null;
    Index index = null;
    boolean indexFileExists = indexFile.exists();
    if (indexFileExists) {
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
            indexFileExists = false;
            if (!indexFile.delete()) {
              LOG.warn("Cannot delete '{}'", indexFile);
            }
          }
          else {
            //pass 2: file count
            index = readIndex(indexUri);
            int oldFileCount = index.getKnownClasses().size();
            if (newMeta.fileCount() != oldFileCount) {
              LOG.info("Drop divergent index '{}'. Index file count {} != folder file count {}.", indexUri, oldFileCount, newMeta.fileCount());
              index = null;
              indexFileExists = false;
              if (!indexFile.delete()) {
                LOG.warn("Cannot delete '{}'", indexFile);
              }
            }
          }
          break;
        case ALWAYS:
          indexFileExists = false;
          if (!indexFile.delete()) {
            LOG.warn("Cannot delete '{}'", indexFile);
          }
          break;
        default:
          //nop
      }
    }

    if (index == null && indexFileExists) {
      index = readIndex(indexUri);
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
    saveIndex(indexFile, index);
    if (!indexFile.setLastModified(newMeta.lastModified())) {
      LOG.warn("Cannot set lastModified on '{}'", indexFile);
    }
    return index;
  }

  protected Index scanJar(URI indexUri) throws URISyntaxException, IOException {
    String s = indexUri.getRawSchemeSpecificPart();
    File jarFile = new File(new URI(s.substring(0, s.lastIndexOf("!"))));
    Index index = readIndex(indexUri);
    if (index != null) {
      return index;
    }
    LOG.info("Found no pre-built '{}'. Scanning location...", indexUri);
    Indexer indexer = new Indexer();
    return JarIndexer.createJarIndex(jarFile, indexer, false, false, false).getIndex();
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
    JandexFiles.walkFileTree(dir, new IJandexFileVisitor() {
      @Override
      public void visit(Path path, BasicFileAttributes attrs) throws IOException {
        if (!acceptPathForIndex(path)) {
          return;
        }
        fileCountRef.incrementAndGet();
        long t = attrs.lastModifiedTime().toMillis();
        if (t > lastModifiedRef.get()) {
          lastModifiedRef.set(t);
        }
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
   * @return index or null
   */
  protected Index readIndex(URI indexUri) {
    try (InputStream in = new BufferedInputStream(indexUri.toURL().openStream())) {
      Index index = new IndexReader(in).read();
      LOG.debug("Found pre-built {}", indexUri);
      return index;
    }
    catch (FileNotFoundException e) {
      LOG.debug("No pre-built index found: {}", indexUri, e);
      return null;
    }
    catch (Exception ex) {
      throw new PlatformException("Error reading index '{}'", indexUri, ex);
    }
  }

  protected Index createFolderIndex(Path dir, final Indexer indexer) throws IOException {
    JandexFiles.walkFileTree(dir, new IJandexFileVisitor() {
      @Override
      public void visit(Path path, BasicFileAttributes attrs) throws IOException {
        appendPathToIndex(path, indexer);
      }
    });
    return indexer.complete();
  }

  protected void appendPathToIndex(Path path, Indexer indexer) throws IOException {
    try (InputStream in = Files.newInputStream(path)) {
      indexer.index(in);
    }
  }

  protected void saveIndex(File file, Index index) {
    try (FileOutputStream out = new FileOutputStream(file)) {
      LOG.debug("Write jandex index file '{}'", file);
      new IndexWriter(out).write(index);
    }
    catch (Exception ex) {
      LOG.warn("Error while writing jandex index file '{}'", file, ex);
    }
  }

  public IndexView finish() {
    return CompositeIndex.create(m_indexList);
  }

  protected List<IndexView> getIndexList() {
    return m_indexList;
  }
}
