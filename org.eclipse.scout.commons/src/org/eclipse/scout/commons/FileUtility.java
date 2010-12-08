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
package org.eclipse.scout.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Utility class for managing directories and files
 * 
 * @author BSI AG
 * @since 1.0
 */
public final class FileUtility {

  private FileUtility() {
  }

  public static void extractArchive(File archiveFile, File destinationDir) throws IOException {
    destinationDir.mkdirs();
    destinationDir.setLastModified(archiveFile.lastModified());
    String localFile = destinationDir.getName();
    JarFile jar = new JarFile(archiveFile);
    try {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry file = entries.nextElement();
        String name = file.getName();
        if (name.startsWith(localFile)) {
          name = name.substring(localFile.length());
        }
        while (name.startsWith("/") || name.startsWith("\\")) {
          name = name.substring(1);
        }
        File f = new File(destinationDir, name);
        if (file.isDirectory()) { // if its a directory, create it
          f.mkdirs();
          if (file.getTime() >= 0) {
            f.setLastModified(file.getTime());
          }
          continue;
        }
        else {
          f.getParentFile().mkdirs();
          InputStream is = null;
          FileOutputStream fos = null;
          try {
            is = jar.getInputStream(file);
            fos = new FileOutputStream(f);
            // Copy the bits from instream to outstream
            byte[] buf = new byte[102400];
            int len;
            while ((len = is.read(buf)) > 0)
              fos.write(buf, 0, len);
          }
          finally {
            if (fos != null) fos.close();
            if (is != null) is.close();
          }
          if (file.getTime() >= 0) {
            f.setLastModified(file.getTime());
          }
        }
      }
    }
    finally {
      if (jar != null) {
        try {
          jar.close();
        }
        catch (Throwable t) {
        }
      }
    }
  }

  /**
   * Copies one file to another. source must exist and be readable cannot copy a
   * directory to a file will not copy if timestamps and filesize match, will
   * overwrite otherwise
   * 
   * @param source
   *          the source file
   * @param dest
   *          the destination file
   * @throws IOException
   *           if an error occurs during the copy operation
   */
  public static void copyFile(File source, File dest) throws IOException {
    if (!source.exists()) {
      throw new FileNotFoundException(source.getAbsolutePath());
    }
    if (!source.canRead()) {
      throw new IOException("cannot read " + source);
    }

    if (dest.exists() && !dest.canWrite()) {
      throw new IOException("cannot write " + dest);
    }

    if (source.isDirectory()) {
      // source can not be a directory
      throw new IOException("source is a directory: " + source);
    }

    // source is a file
    if (dest.isDirectory()) {
      String sourceFileName = source.getName();
      copyFile(source, new File(dest, sourceFileName));
    }
    // both source and dest are files
    boolean needCopy = true;
    if (dest.exists()) {
      needCopy = (dest.length() != source.length()) || (dest.lastModified() != source.lastModified());
    }
    if (needCopy) {
      // Copies the file
      FileChannel input = null;
      FileChannel output = null;
      try {
        // magic number for Windows, 64Mb - 32Kb
        //
        int mbCount = 64;
        boolean done = false;
        // java.io.IOException: Insufficient system resources exist to complete
        // the requested service
        while (!done) {
          input = new FileInputStream(source).getChannel();
          if (!dest.exists()) {
            dest.getParentFile().mkdirs();
          }
          output = new FileOutputStream(dest).getChannel();

          try {
            int maxCount = (mbCount * 1024 * 1024) - (32 * 1024);
            long size = input.size();
            long position = 0;
            while (position < size) {
              position +=
                  input.transferTo(position, maxCount, output);
            }
            done = true;
          }
          catch (IOException ioXcp) {
            // getLog().warn(ioXcp);
            if (ioXcp.getMessage().contains("Insufficient system resources exist to complete the requested service")) {
              mbCount--;
              // getLog().debug( "Dropped resource count down to ["+mbCount+"]"
              // );
              if (mbCount == 0) {
                done = true;
              }
              if (input != null) {
                input.close();
              }
              if (output != null) {
                output.close();
              }
            }
            else {
              throw ioXcp;
            }
          }
        }
      }
      finally {
        if (input != null) {
          input.close();
        }
        if (output != null) {
          output.close();
        }
      }

      if (dest.exists() && source.exists()) {
        dest.setLastModified(source.lastModified());
      }
    }
  }

  public static byte[] readFile(File source) throws IOException {
    if (!source.exists()) {
      throw new FileNotFoundException(source.getAbsolutePath());
    }
    if (!source.canRead()) {
      throw new IOException("cannot read " + source);
    }
    if (source.isDirectory()) {
      // source can not be a directory
      throw new IOException("source is a directory: " + source);
    }
    FileInputStream input = null;
    try {
      input = new FileInputStream(source);
      byte[] data = new byte[(int) source.length()];
      int n = 0;
      while (n < data.length) {
        n += input.read(data, n, data.length - n);
      }
      return data;
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (Throwable e) {
        }
      }
    }
  }

  public static void copyTree(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdirs();
        targetLocation.setLastModified(sourceLocation.lastModified());
      }

      String[] children = sourceLocation.list();
      for (int i = 0; i < children.length; i++) {
        copyTree(new File(sourceLocation, children[i]), new File(
            targetLocation, children[i]));
      }
    }
    else copyFile(sourceLocation, targetLocation);
  }

  public static List<File> listTree(File f, boolean includeFiles, boolean includeFolders) throws IOException {
    ArrayList<File> list = new ArrayList<File>();
    listTreeRec(f, list, includeFiles, includeFolders);
    return list;
  }

  private static void listTreeRec(File f, List<File> list, boolean includeFiles, boolean includeFolders) throws IOException {
    if (f.isDirectory()) {
      if (includeFolders) {
        list.add(f);
      }
      String[] children = f.list();
      for (int i = 0; i < children.length; i++) {
        listTreeRec(new File(f, children[i]), list, includeFiles, includeFolders);
      }
    }
    else {
      if (includeFiles) {
        list.add(f);
      }
    }
  }

  public static void compressArchive(File srcDir, File archiveFile) throws IOException {
    JarOutputStream zOut = null;
    try {
      archiveFile.delete();
      zOut = new JarOutputStream(new FileOutputStream(archiveFile));
      addFolderToJar(srcDir, srcDir, zOut);
    }
    finally {
      if (zOut != null) try {
        zOut.close();
      }
      catch (Throwable t) {
      }
    }
  }

  private static void addFolderToJar(File baseDir, File srcdir, JarOutputStream zOut) throws IOException {
    if ((!srcdir.exists()) || (!srcdir.isDirectory())) {
      throw new IOException("source directory " + srcdir + " does not exist or is not a folder");
    }
    for (File f : srcdir.listFiles()) {
      if (f.exists() && (!f.isHidden())) {
        if (f.isDirectory()) {
          addFolderToJar(baseDir, f, zOut);
        }
        else {
          addFileToJar(baseDir, f, zOut);
        }
      }
    }
  }

  private static void addFileToJar(File baseDir, File src, JarOutputStream zOut) throws IOException {
    String name = src.getAbsolutePath();
    String prefix = baseDir.getAbsolutePath();
    if (prefix.endsWith("/") || prefix.endsWith("\\")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    name = name.substring(prefix.length() + 1);
    name = name.replace('\\', '/');
    byte[] data = readFile(src);
    addFileToJar(name, data, zOut);
  }

  private static void addFileToJar(String name, byte[] data, JarOutputStream zOut) throws IOException {
    zOut.putNextEntry(new ZipEntry(name));
    zOut.write(data);
    zOut.closeEntry();
  }

}
