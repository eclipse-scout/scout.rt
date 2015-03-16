package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.cdi.Bean;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * scan classpath for {@link Bean}
 */
public abstract class AbstractInventoryBuilder {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractInventoryBuilder.class);
  public static final String CLASS_EXT = ".class";

  public AbstractInventoryBuilder() {
  }

  public void scanAllModules() throws IOException {
    if (Platform.isOsgiRunning()) {
      String path = "META-INF/scout.xml";
      for (Bundle b : FrameworkUtil.getBundle(getClass()).getBundleContext().getBundles()) {
        if (b.getResource("/" + path) == null) {
          continue;
        }
        for (Enumeration en = b.findEntries("/", "*.class", true); en.hasMoreElements();) {
          URL url = ((URL) en.nextElement());
          handleClass(url);
        }
      }
    }
    else {
      String path = "META-INF/scout.xml";
      for (Enumeration<URL> en = getClass().getClassLoader().getResources(path); en.hasMoreElements();) {
        String url = en.nextElement().toExternalForm();
        if (url.startsWith("jar:file:")) {
          scanModule(new File(url.substring(9, url.lastIndexOf("!"))));
        }
        else if (url.startsWith("file:")) {
          scanModule(new File(url.substring(5)).getParentFile().getParentFile().getParentFile());
        }
      }
    }
  }

  public void scanModule(File file) {
    if (!file.exists()) {
      return;
    }
    try {
      LOG.debug("Handle path: " + file);
      if (file.isDirectory()) {
        scanDirectory(file, null);
      }
      else {
        scanJar(file);
      }
    }
    catch (IOException e) {
      LOG.warn("Could not handle path: " + file, e);
      e.printStackTrace();
    }
  }

  protected void scanDirectory(File file, String parentPath) throws IOException {
    File[] files = file.listFiles();
    for (File child : files) {
      String path;
      if (parentPath != null) {
        path = parentPath + "/" + child.getName();
      }
      else {
        path = child.getName();
      }
      if (child.isDirectory()) {
        scanDirectory(child, path);
      }
      else {
        scanFile(child, path);
      }
    }
  }

  protected void scanFile(File file, String path) throws IOException {
    if (isClass(path)) {
      handleClass(file.toURI().toURL());
    }
  }

  protected void scanJar(File file) throws IOException {
    String archiveUrl = "jar:" + file.toURI().toURL().toExternalForm() + "!/";
    try (ZipFile zip = new ZipFile(file)) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        String path = entries.nextElement().getName();
        if (isClass(path)) {
          handleClass(new URL(archiveUrl + path));
        }
      }
    }
    catch (ZipException e) {
      throw new IOException("failed processing file " + file, e);
    }
  }

  protected boolean isClass(String name) {
    return name.endsWith(CLASS_EXT);
  }

  protected abstract void handleClass(URL url);

  public abstract void finish();

}
