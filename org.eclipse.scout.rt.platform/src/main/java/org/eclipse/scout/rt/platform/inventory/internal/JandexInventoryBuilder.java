package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.PlatformException;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;

public class JandexInventoryBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JandexInventoryBuilder.class);
  private static final String SCOUT_XML_PATH = "META-INF/scout.xml";
  private static final String JANDEX_INDEX_PATH = "META-INF/jandex.idx";
  private static final String SYSTEM_PROPERTY_JANDEX_REBUILD = "jandex.rebuild";

  private final ArrayList<IndexView> m_indexList = new ArrayList<>();
  private final boolean m_forceRebuildFolderIndexes;

  public JandexInventoryBuilder() {
    m_forceRebuildFolderIndexes = ConfigIniUtility.getProperty(SYSTEM_PROPERTY_JANDEX_REBUILD, "false").equals("true");
  }

  public void scanAllModules() throws PlatformException {
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
    try {
      indexUri = new URI(urlText.substring(0, urlText.length() - SCOUT_XML_PATH.length()) + JANDEX_INDEX_PATH);
    }
    catch (URISyntaxException ex) {
      throw new PlatformException("create URI from: " + urlText + "/../../" + JANDEX_INDEX_PATH, ex);
    }
    //check for prepared index
    if (!(m_forceRebuildFolderIndexes && urlText.startsWith("file:"))) {
      try (InputStream in = indexUri.toURL().openStream()) {
        Index index = new IndexReader(in).read();
        m_indexList.add(index);
        if (LOG.isDebugEnabled()) {
          LOG.debug("found pre-built " + indexUri);
        }
        return;
      }
      catch (Exception ex) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("reading " + indexUri, ex);
        }
      }
    }
    //scan location
    LOG.info("found no pre-built " + indexUri + "; scanning location...");
    try {
      if (urlText.startsWith("jar:file:")) {
        Indexer indexer = new Indexer();
        File jarFile = new File(urlText.substring(9, urlText.lastIndexOf("!")));
        Index index = JarIndexer.createJarIndex(jarFile, indexer, false, false, false).getIndex();
        m_indexList.add(index);
        return;
      }
      if (urlText.startsWith("file:")) {
        Indexer indexer = new Indexer();
        File scoutXmlFolder = new File(urlText.substring(5));
        File classesFolder = scoutXmlFolder.getParentFile().getParentFile();
        Index index = JandexFolderIndexer.createFolderIndex(classesFolder, indexer);
        m_indexList.add(index);
        saveIndexFile(new File(classesFolder, JANDEX_INDEX_PATH), index);
        return;
      }
      //unknown protocol
      throw new Exception("unknown protocol");
    }
    catch (Exception ex) {
      LOG.error("Cannot scan location '" + urlText + "' with jandex", ex);
    }
  }

  protected void saveIndexFile(File file, Index index) {
    try (FileOutputStream out = new FileOutputStream(file)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("writing jandex index file: " + file);
      }
      new IndexWriter(out).write(index);
    }
    catch (Exception ex) {
      LOG.warn("Cannot write jandex index file: " + file, ex);
    }
  }

  public IndexView finish() {
    return CompositeIndex.create(m_indexList);
  }

}
