/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public final class SandboxClassLoaderBuilder {

  private static final Logger LOG = Logger.getLogger(SandboxClassLoaderBuilder.class.getName());

  private static final int ANY_SIZE = 10240;

  private final Map<String, URL> m_urls = new LinkedHashMap<String, URL>();
  private final JarLocator m_jarLocator;
  private final ClassLoader m_originalClassLoader = SandboxClassLoaderBuilder.class.getClassLoader();

  public SandboxClassLoaderBuilder() {
    this(null);
  }

  public SandboxClassLoaderBuilder(JarLocator jarLocator) {
    m_jarLocator = (jarLocator != null ? jarLocator : new JarLocator(SandboxClassLoaderBuilder.class));
  }

  public SandboxClassLoaderBuilder addJar(URL url) {
    if (url == null) {
      throw new IllegalArgumentException("Argument 'url' must not be null");
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public SandboxClassLoaderBuilder addLocalJar(String path) {
    URL url = m_jarLocator.getResource(path);
    if (url == null) {
      throw new IllegalStateException("Could not resolve URL for path '" + path + "'");
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public SandboxClassLoaderBuilder addJarContaining(Class<?> clazz) {
    if (clazz == null) {
      throw new IllegalStateException("Argument 'clazz' must not be null");
    }
    URL url = m_jarLocator.getJarContaining(clazz);
    if (url == null) {
      throw new IllegalStateException("Could not resolve URL for class " + clazz.getName());
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public URLClassLoader build(final ClassLoader parent) {
    return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
      @Override
      public URLClassLoader run() {
        return new P_ScriptClassLoader(m_urls.values().toArray(new URL[0]), parent);
      }
    });
  }

  protected static URL unwrapNestedJar(URL url) {
    if (!url.getPath().endsWith(".jar")) {
      return url;
    }

    String tempDir = System.getProperty("java.io.tmpdir");
    if (!(tempDir.endsWith("/") || tempDir.endsWith("\\"))) {
      tempDir += System.getProperty("file.separator");
    }

    try {
      String name = url.getPath();
      int i = name.lastIndexOf('/');
      if (i >= 0) {
        name = name.substring(i + 1);
      }

      String sha1;
      try (InputStream inputStream = url.openStream()) {
        sha1 = getSha1(inputStream);
      }

      String filenameWithoutExtension = name.substring(0, name.lastIndexOf('.'));
      String targetFilename = filenameWithoutExtension + "-" + sha1 + ".jar";
      File targetFile = new File(tempDir, targetFilename);
      if (targetFile.exists()) {
        String targetFileSha1;
        try (InputStream inputStream = new FileInputStream(targetFile)) {
          targetFileSha1 = getSha1(inputStream);
        }

        if (!targetFileSha1.equals(sha1)) {
          // sha1 hash of existing file doesn't match
          // use newly created temporary file as fallback
          File f = File.createTempFile("jar-sandbox-", name);
          LOG.log(Level.SEVERE, "Target file " + targetFile.getAbsolutePath() + " already exists but has wrong sha1 hash [" + targetFileSha1 + "]. Using new temporary file instead " + f.getAbsolutePath() + " [" + sha1 + "].");
          targetFile = f;
          targetFile.deleteOnExit();
          writeContent(url, targetFile);
        }
      }
      else {
        // existing file doesn't exist
        // write content to file
        targetFile.deleteOnExit();
        writeContent(url, targetFile);
      }

      return targetFile.toURI().toURL();
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-1 algorithm is missing but required to verify jar for classloader", e);
    }
    catch (IOException e) {
      throw new IllegalArgumentException("JAR " + url + " could not be extracted to temp directory", e);
    }
  }

  /**
   * Determines the sha1 hash (hex representation) of the data provided by the input stream.
   */
  protected static String getSha1(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
    MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
    byte[] buf = new byte[ANY_SIZE];
    int n;
    while ((n = inputStream.read(buf)) > 0) {
      sha1Digest.update(buf, 0, n);
    }

    return new HexBinaryAdapter().marshal(sha1Digest.digest());
  }

  /**
   * Writes the content from the url output stream to the provided file.
   */
  protected static void writeContent(URL url, File f) throws IOException {
    try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(f)) {
      byte[] buf = new byte[ANY_SIZE];
      int n;
      while ((n = in.read(buf)) > 0) {
        out.write(buf, 0, n);
      }
    }
  }

  /**
   * This class loader has only access to resources in the given JARs (provided by urls parameter) and also to script
   * resources (CSS, JS) from the parent class loader. The later is required, because the LessCompiler looks on the
   * current classpath to find a resource which is imported with the @import directive.
   */
  private final class P_ScriptClassLoader extends URLClassLoader {

    private P_ScriptClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
      String ext = getFileExtension(name);
      if ("css".equals(ext) || "js".equals(ext) || "less".equals(ext)) {
        return m_originalClassLoader.getResourceAsStream(name);
      }
      else {
        return super.getResourceAsStream(name);
      }
    }

    // Cannot use FileUtility here since the script-processor module has no dependency to Scout platform
    private String getFileExtension(String name) {
      int pos = name.lastIndexOf('.');
      if (pos > -1) {
        return name.substring(pos + 1);
      }
      else {
        return null;
      }
    }

  }
}
