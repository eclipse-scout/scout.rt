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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JandexRebuildProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
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

  private static final String SCOUT_XML_PATH = "META-INF/scout.xml";
  private static final String JANDEX_INDEX_PATH = "META-INF/jandex.idx";

  private final List<IndexView> m_indexList;
  private final boolean m_forceRebuildFolderIndexes;

  public JandexInventoryBuilder() {
    m_forceRebuildFolderIndexes = new JandexRebuildProperty().getValue(); // do not use the CONFIG class here because the platform is not ready yet
    m_indexList = new ArrayList<>();
  }

  public void scanAllModules() {
    try {
      for (Enumeration<URL> en = getClass().getClassLoader().getResources(SCOUT_XML_PATH); en.hasMoreElements();) {
        URL url = en.nextElement();
        scanModule(url);
      }
    }
    catch (IOException ex) {
      throw new PlatformException("failed reading resources '" + SCOUT_XML_PATH + "'", ex);
    }
  }

  public void scanModule(URL url) {
    String urlText = url.toExternalForm();
    URI indexUri;
    String indexUrl = urlText.substring(0, urlText.length() - SCOUT_XML_PATH.length()) + JANDEX_INDEX_PATH;
    try {
      indexUri = new URI(indexUrl);
    }
    catch (URISyntaxException ex) {
      throw new PlatformException("Cannot create URI from: " + indexUrl, ex);
    }

    //check for prepared index
    if (!(m_forceRebuildFolderIndexes && urlText.startsWith("file:"))) {
      try (InputStream in = new BufferedInputStream(indexUri.toURL().openStream())) {
        Index index = new IndexReader(in).read();
        m_indexList.add(index);
        LOG.debug("found pre-built {}", indexUri);
        return;
      }
      catch (FileNotFoundException e) {
        LOG.debug("No pre-built index found: {}", indexUri, e);
      }
      catch (Exception ex) {
        throw new PlatformException("error reading index: " + indexUri, ex);
      }
    }

    //scan location
    if (m_forceRebuildFolderIndexes) {
      LOG.info("forcing rebuild of index '{}'. scanning location...", indexUri);
    }
    else {
      LOG.info("found no pre-built '{}'. scanning location...", indexUri);
    }
    try {
      if (urlText.startsWith("jar:file:")) {
        Indexer indexer = new Indexer();
        File jarFile = new File(new URI("file:" + urlText.substring(9, urlText.lastIndexOf("!"))));
        Index index = JarIndexer.createJarIndex(jarFile, indexer, false, false, false).getIndex();
        m_indexList.add(index);
        return;
      }
      if (urlText.startsWith("file:")) {
        Indexer indexer = new Indexer();
        File scoutXmlFolder = new File(url.toURI());
        File classesFolder = scoutXmlFolder.getParentFile().getParentFile();
        Index index = JandexFolderIndexer.createFolderIndex(classesFolder, indexer);
        m_indexList.add(index);
        saveIndexFile(new File(classesFolder, JANDEX_INDEX_PATH), index);
        return;
      }
      //unknown protocol
      throw new Exception("unknown protocol: " + urlText);
    }
    catch (Exception ex) {
      throw new PlatformException("Cannot scan location '" + urlText + "' with jandex", ex);
    }
  }

  protected void saveIndexFile(File file, Index index) {
    try (FileOutputStream out = new FileOutputStream(file)) {
      LOG.debug("writing jandex index file: {}", file);
      new IndexWriter(out).write(index);
    }
    catch (Exception ex) {
      LOG.warn("Cannot write jandex index file: {}", file, ex);
    }
  }

  public IndexView finish() {
    return CompositeIndex.create(m_indexList);
  }

  protected List<IndexView> getIndexList() {
    return m_indexList;
  }

  protected boolean isForceRebuildFolderIndexe() {
    return m_forceRebuildFolderIndexes;
  }
}
