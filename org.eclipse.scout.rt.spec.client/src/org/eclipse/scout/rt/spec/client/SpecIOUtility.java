/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.osgi.framework.Bundle;

/**
 * Some utilities for files
 */
public final class SpecIOUtility {
  public static final String ENCODING = "utf-8";

  private SpecIOUtility() {
  }

  public static String[] getRelativePaths(File[] files, File baseDir) {
    List<String> pathList = new ArrayList<String>();
    for (File f : files) {
      String relative = baseDir.toURI().relativize(f.toURI()).getPath();
      pathList.add(relative);
    }
    return CollectionUtility.toArray(pathList, String.class);
  }

  public static String[] addPrefix(String[] files, String pathPrefix) {
    String[] pathList = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      pathList[i] = pathPrefix + files[i];
    }
    return pathList;
  }

  /**
   * @param out
   * @param id
   * @param fileExtension
   * @return
   * @throws ProcessingException
   */
  public static File createNewFile(File out, String id, String fileExtension) throws ProcessingException {
    out.mkdirs();
    File file = new File(out, id + fileExtension);
    try {
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      return file;
    }
    catch (IOException e) {
      throw new ProcessingException("Error creating file.", e);
    }
  }

  public static Writer createWriter(File out, String id, String fileExtension) throws ProcessingException {
    File file = createNewFile(out, id, fileExtension);
    return createWriter(file);
  }

  public static Writer createWriter(File file) throws ProcessingException {
    try {
      FileOutputStream outputStream = new FileOutputStream(file.getPath(), true);
      return new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING));
    }
    catch (IOException e) {
      throw new ProcessingException("Error writing mediawiki file.", e);
    }
  }

  public static Properties loadProperties(File file) throws ProcessingException {
    Properties prop = new Properties();
    FileInputStream inStream = null;
    try {
      inStream = new FileInputStream(file);
      prop.load(inStream);
      return prop;
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error loading property file", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error loading property file", e);
    }
    finally {
      try {
        if (inStream != null) {
          inStream.close();
        }
      }
      catch (IOException e) {
        //nop
      }
    }
  }

  /**
   * Copies a file from a bundle with a given path inside this bundle (jar or source) to a destination directory
   * 
   * @param bundle
   * @param path
   *          path within the bundle
   * @param destDir
   *          destination directory
   * @throws ProcessingException
   */
  public static void copyFile(Bundle bundle, String path, File destFile) throws ProcessingException {
    ReadableByteChannel sourceChannel = null;
    FileChannel destChannel = null;
    FileOutputStream out = null;
    try {
      InputStream stream;
      try {
        stream = FileLocator.openStream(bundle, new Path(path), true);
        sourceChannel = Channels.newChannel(stream);
        out = new FileOutputStream(destFile);
        destChannel = out.getChannel();
        final long maxBytes = 1000000l;
        destChannel.transferFrom(sourceChannel, 0, maxBytes);
      }
      catch (IOException e) {
        throw new ProcessingException("Failed to copy.", e);
      }
    }
    finally {
      if (sourceChannel != null) {
        try {
          sourceChannel.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
      if (out != null) {
        try {
          out.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
      if (destChannel != null) {
        try {
          destChannel.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public static void copy(File source, File dest) throws ProcessingException {
    FileChannel sourceChannel = null;
    FileChannel destChannel = null;
    try {
      sourceChannel = new FileInputStream(source).getChannel();
      destChannel = new FileOutputStream(dest).getChannel();
      destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error copying file", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error copying file", e);
    }
    finally {
      if (sourceChannel != null) {
        try {
          sourceChannel.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
      if (destChannel != null) {
        try {
          destChannel.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public static void replaceAll(File in, Map<String, String> m) throws ProcessingException {
    FileReader reader = null;
    FileWriter writer = null;
    BufferedReader br = null;
    File temp = new File(in.getParent(), in.getName() + "_temp");

    try {
      reader = new FileReader(in);
      writer = new FileWriter(temp);
      br = new BufferedReader(reader);

      String line;
      while ((line = br.readLine()) != null) {
        String repl = replaceAll(line, m);
        writer.write(repl);
        writer.write(System.getProperty("line.separator"));
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (br != null) {
          br.close();
        }
      }
      catch (IOException e) {
        // NOP
      }

      try {
        if (writer != null) {
          writer.close();
        }
      }
      catch (IOException e) {
        // NOP
      }
    }
    SpecIOUtility.copy(temp, in);
    temp.delete();
  }

  private static String replaceAll(String s, Map<String, String> m) {
    for (Entry<String, String> e : m.entrySet()) {
      s = s.replaceAll(e.getKey(), e.getValue());
    }
    return s;
  }

  /**
   * List all files in a directory inside a bundle. Source and binary bundles are supported.
   * 
   * @param bundle
   * @param path
   *          Relative path inside bundle. Expects {@link File#separator} as path-separator.
   * @param filter
   *          When implementing {@link FilenameFilter#accept(File dir, String name)}, make sure not to depend on the
   *          <code>dir</code> parameter as this will be null in case of binary bundles.
   * @return
   * @throws ProcessingException
   */
  public static List<String> listFiles(Bundle bundle, String path, FilenameFilter filter) throws ProcessingException {
    if (!path.endsWith(File.separator)) {
      path = path + File.separator;
    }

    URL bundleRoot = bundle.getEntry("/");
    URI resolvedBundleUri = null;
    URL resolvedBundleUrl = null;
    try {
      resolvedBundleUrl = FileLocator.resolve(bundleRoot);
      resolvedBundleUri = resolvedBundleUrl.toURI();
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("Error reading directory", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error reading directory", e);
    }

    if ("jar".equals(resolvedBundleUri.getScheme())) {
      return listFilesBinary(path, resolvedBundleUrl, filter);
    }
    else if ("file".equals(resolvedBundleUri.getScheme())) {
      return listFilesSource(path, resolvedBundleUri, filter);
    }
    throw new ProcessingException("Error reading directory");
  }

  /**
   * @param relativePath
   * @param resolvedFileBundleUri
   * @param filter
   * @return
   */
  private static List<String> listFilesSource(String relativePath, URI resolvedFileBundleUri, FilenameFilter filter) {
    ArrayList<String> fileNames = new ArrayList<String>();
    File bundleRoot = new File(resolvedFileBundleUri);
    File dir = new File(bundleRoot, relativePath);
    File[] files = dir.listFiles(filter);
    for (File file : files) {
      if (file.isFile()) {
        fileNames.add(file.getName());
      }
    }
    return fileNames;
  }

  private static List<String> listFilesBinary(String relativePath, URL resolvedBinaryBundleUrl, FilenameFilter filter) throws ProcessingException {
    ArrayList<String> list = new ArrayList<String>();
    JarInputStream jis = null;
    try {
      JarURLConnection connection = (JarURLConnection) resolvedBinaryBundleUrl.openConnection();
      File jarFile = new File(connection.getJarFileURL().toURI());
      jis = new JarInputStream(new FileInputStream(jarFile));
      ZipEntry entry;
      while ((entry = jis.getNextEntry()) != null) {
        String name = entry.getName().replace("/", File.separator);
        if (name.startsWith(relativePath)
            && name.substring(relativePath.length()).length() > 0
            && !name.substring(relativePath.length()).contains(File.separator)
            && filter.accept(null, name.substring(relativePath.length()))) {
          list.add(name.substring(relativePath.length()));
        }
      }
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("Error reading directory", e);
    }
    catch (IOException e) {
      throw new ProcessingException("Error reading directory", e);
    }
    catch (URISyntaxException e) {
      throw new ProcessingException("Error reading directory", e);
    }
    finally {
      if (jis != null) {
        try {
          jis.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
    return list;
  }
}
