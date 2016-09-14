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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SandboxClassLoaderBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(SandboxClassLoaderBuilder.class);

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

  public SandboxClassLoaderBuilder addLocalJar(String path) {
    URL url = m_jarLocator.getResource(path);
    if (url == null) {
      throw new PlatformException("Could not resolve URL for path '{}'", path);
    }
    ByteArrayOutputStream jarData = new ByteArrayOutputStream();
    try {
      try (InputStream in = url.openStream()) {
        byte[] buf = new byte[ANY_SIZE];
        int n;
        while ((n = in.read(buf)) > 0) {
          jarData.write(buf, 0, n);
        }
      }
      URL jarUrl = createTemporaryJar(path, jarData.toByteArray());
      m_urls.put(jarUrl.toExternalForm(), jarUrl);
    }
    catch (Exception e) {
      throw new PlatformException("Cannot read content of {}", path, e);
    }
    return this;
  }

  public SandboxClassLoaderBuilder addClasses(String newJarFileName, String... classNames) {
    if (classNames == null || classNames.length == 0) {
      return this;
    }
    ByteArrayOutputStream jarData = new ByteArrayOutputStream();
    try {
      //create jar
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      try (JarOutputStream jar = new JarOutputStream(jarData, manifest)) {
        for (String className : classNames) {
          String classPath = (className.replace(".", "/")) + ".class";
          URL url = m_jarLocator.getResource(classPath);
          if (url == null) {
            throw new PlatformException("Could not resolve URL for class {}", className);
          }
          //add entry
          JarEntry entry = new JarEntry(classPath);
          entry.setTime(0L);
          jar.putNextEntry(entry);
          try (InputStream in = url.openStream()) {
            byte[] buf = new byte[ANY_SIZE];
            int n;
            while ((n = in.read(buf)) > 0) {
              jar.write(buf, 0, n);
            }
          }
          jar.closeEntry();
        }
        jar.finish();
      }
    }
    catch (IOException e) {
      throw new PlatformException("Cannot create jar for {}", Arrays.toString(classNames), e);
    }
    URL jarUrl = createTemporaryJar(newJarFileName, jarData.toByteArray());
    m_urls.put(jarUrl.toExternalForm(), jarUrl);
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

  static URL createTemporaryJar(String jarFilePath, byte[] data) {
    String jarFileName = new File(jarFilePath).getName();
    String tempDir = System.getProperty("java.io.tmpdir");
    if (!(tempDir.endsWith("/") || tempDir.endsWith("\\"))) {
      tempDir += System.getProperty("file.separator");
    }
    try {
      String sha1;
      try (InputStream inputStream = new ByteArrayInputStream(data)) {
        sha1 = readSha1(inputStream);
      }
      File targetFile = new File(tempDir, sha1 + "-" + jarFileName);
      if (targetFile.exists()) {
        String targetFileSha1;
        try (InputStream inputStream = new FileInputStream(targetFile)) {
          targetFileSha1 = readSha1(inputStream);
        }
        if (targetFileSha1.equals(sha1)) {
          return targetFile.toURI().toURL();
        }
        // sha1 hash of existing file doesn't match
        // use newly created temporary file as fallback
        File f = File.createTempFile("jar-sandbox-", jarFileName);
        LOG.error("Target file '{}' already exists but has wrong sha1 hash [{}]. Using new temporary file instead '{}' [{}].", targetFile.getAbsolutePath(), targetFileSha1, f.getAbsolutePath(), sha1);
        targetFile = f;
      }
      targetFile.deleteOnExit();
      writeContent(targetFile, data);
      return targetFile.toURI().toURL();
    }
    catch (NoSuchAlgorithmException e) {
      throw new PlatformException("SHA-1 algorithm is missing but required to verify jar for classloader", e);
    }
    catch (IOException e) {
      throw new PlatformException("JAR {} could not be extracted to temp directory", jarFilePath, e);
    }
  }

  /**
   * Determines the sha1 hash (hex representation) of the data provided by the input stream.
   */
  static String readSha1(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
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
  static void writeContent(File f, byte[] content) throws IOException {
    // ensure folder exists
    File folder = f.getParentFile();
    if (!folder.exists() && !folder.mkdirs()) {
      throw new PlatformException("unable to create folder '{}'", folder.getAbsolutePath());
    }
    // write content
    try (FileOutputStream out = new FileOutputStream(f)) {
      out.write(content);
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
