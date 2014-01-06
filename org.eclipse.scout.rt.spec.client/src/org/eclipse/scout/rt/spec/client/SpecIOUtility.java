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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.spec.client.internal.Activator;
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
   * Copy all files in sourcefolder to destination
   * 
   * @param filter
   * @param sourceDir
   * @param destDir
   * @throws ProcessingException
   */
  public static void copyAll(File sourceDir, File destDir, FilenameFilter filter) throws ProcessingException {
    for (File file : sourceDir.listFiles(filter)) {
      File destFile = new File(destDir, file.getName());
      copy(file, destFile);
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
        stream = FileLocator.openStream(Activator.getDefault().getBundle(), new Path(path), true);
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

}
