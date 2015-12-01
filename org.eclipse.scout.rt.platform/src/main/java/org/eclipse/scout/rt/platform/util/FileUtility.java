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
package org.eclipse.scout.rt.platform.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.eclipse.scout.rt.platform.exception.ProcessingException;

/**
 * Utility class for managing directories and files
 *
 * @author BSI AG
 * @since 1.0
 */
public final class FileUtility {
  private static final int KILO_BYTE = 1024;

  private FileUtility() {
  }

  /**
   * Static extension to mimetype mapper (and reverse)
   */
  private static final Map<String, String> EXT_TO_MIME_TYPE_MAP;

  static {
    EXT_TO_MIME_TYPE_MAP = new HashMap<String, String>();
    EXT_TO_MIME_TYPE_MAP.put("ai", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("aif", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("aifc", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("aiff", "audio/x-aiff");
    EXT_TO_MIME_TYPE_MAP.put("asc", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("au", "audio/basic");
    EXT_TO_MIME_TYPE_MAP.put("avi", "video/x-msvideo");
    EXT_TO_MIME_TYPE_MAP.put("bcpio", "application/x-bcpio");
    EXT_TO_MIME_TYPE_MAP.put("bin", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("c", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("cc", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("ccad", "application/clariscad");
    EXT_TO_MIME_TYPE_MAP.put("cdf", "application/x-netcdf");
    EXT_TO_MIME_TYPE_MAP.put("class", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("cpio", "application/x-cpio");
    EXT_TO_MIME_TYPE_MAP.put("cpt", "application/mac-compactpro");
    EXT_TO_MIME_TYPE_MAP.put("csh", "application/x-csh");
    EXT_TO_MIME_TYPE_MAP.put("css", "text/css");
    EXT_TO_MIME_TYPE_MAP.put("dcr", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("dir", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("dms", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("doc", "application/msword");
    EXT_TO_MIME_TYPE_MAP.put("drw", "application/drafting");
    EXT_TO_MIME_TYPE_MAP.put("dvi", "application/x-dvi");
    EXT_TO_MIME_TYPE_MAP.put("dwg", "application/acad");
    EXT_TO_MIME_TYPE_MAP.put("dxf", "application/dxf");
    EXT_TO_MIME_TYPE_MAP.put("dxr", "application/x-director");
    EXT_TO_MIME_TYPE_MAP.put("eml", "message/rfc822");
    EXT_TO_MIME_TYPE_MAP.put("eps", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("etx", "text/x-setext");
    EXT_TO_MIME_TYPE_MAP.put("exe", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("ez", "application/andrew-inset");
    EXT_TO_MIME_TYPE_MAP.put("f", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("f90", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("fli", "video/x-fli");
    EXT_TO_MIME_TYPE_MAP.put("gif", "image/gif");
    EXT_TO_MIME_TYPE_MAP.put("gtar", "application/x-gtar");
    EXT_TO_MIME_TYPE_MAP.put("gz", "application/x-gzip");
    EXT_TO_MIME_TYPE_MAP.put("h", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("hdf", "application/x-hdf");
    EXT_TO_MIME_TYPE_MAP.put("hh", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("hqx", "application/mac-binhex40");
    EXT_TO_MIME_TYPE_MAP.put("htm", "text/html");
    EXT_TO_MIME_TYPE_MAP.put("html", "text/html");
    EXT_TO_MIME_TYPE_MAP.put("ice", "x-conference/x-cooltalk");
    EXT_TO_MIME_TYPE_MAP.put("ief", "image/ief");
    EXT_TO_MIME_TYPE_MAP.put("iges", "model/iges");
    EXT_TO_MIME_TYPE_MAP.put("igs", "model/iges");
    EXT_TO_MIME_TYPE_MAP.put("ini", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("ips", "application/x-ipscript");
    EXT_TO_MIME_TYPE_MAP.put("ipx", "application/x-ipix");
    EXT_TO_MIME_TYPE_MAP.put("jpe", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("jpg", "image/jpeg");
    EXT_TO_MIME_TYPE_MAP.put("js", "application/javascript");
    EXT_TO_MIME_TYPE_MAP.put("json", "application/json");
    EXT_TO_MIME_TYPE_MAP.put("kar", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("latex", "application/x-latex");
    EXT_TO_MIME_TYPE_MAP.put("lha", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("lsp", "application/x-lisp");
    EXT_TO_MIME_TYPE_MAP.put("lzh", "application/octet-stream");
    EXT_TO_MIME_TYPE_MAP.put("m", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("man", "application/x-troff-man");
    EXT_TO_MIME_TYPE_MAP.put("me", "application/x-troff-me");
    EXT_TO_MIME_TYPE_MAP.put("mesh", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("mid", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("midi", "audio/midi");
    EXT_TO_MIME_TYPE_MAP.put("mif", "application/vnd.mif");
    EXT_TO_MIME_TYPE_MAP.put("mime", "www/mime");
    EXT_TO_MIME_TYPE_MAP.put("mov", "video/quicktime");
    EXT_TO_MIME_TYPE_MAP.put("movie", "video/x-sgi-movie");
    EXT_TO_MIME_TYPE_MAP.put("mp2", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mp3", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpe", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpeg", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpg", "video/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("mpga", "audio/mpeg");
    EXT_TO_MIME_TYPE_MAP.put("ms", "application/x-troff-ms");
    EXT_TO_MIME_TYPE_MAP.put("msg", "application/vnd.ms-outlook");
    EXT_TO_MIME_TYPE_MAP.put("msh", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("nc", "application/x-netcdf");
    EXT_TO_MIME_TYPE_MAP.put("oda", "application/oda");
    EXT_TO_MIME_TYPE_MAP.put("pbm", "image/x-portable-bitmap");
    EXT_TO_MIME_TYPE_MAP.put("pdb", "chemical/x-pdb");
    EXT_TO_MIME_TYPE_MAP.put("pdf", "application/pdf");
    EXT_TO_MIME_TYPE_MAP.put("pgm", "image/x-portable-graymap");
    EXT_TO_MIME_TYPE_MAP.put("pgn", "application/x-chess-pgn");
    EXT_TO_MIME_TYPE_MAP.put("png", "image/png");
    EXT_TO_MIME_TYPE_MAP.put("pnm", "image/x-portable-anymap");
    EXT_TO_MIME_TYPE_MAP.put("pot", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppm", "image/x-portable-pixmap");
    EXT_TO_MIME_TYPE_MAP.put("pps", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppt", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("ppz", "application/mspowerpoint");
    EXT_TO_MIME_TYPE_MAP.put("pre", "application/x-freelance");
    EXT_TO_MIME_TYPE_MAP.put("prt", "application/pro_eng");
    EXT_TO_MIME_TYPE_MAP.put("ps", "application/postscript");
    EXT_TO_MIME_TYPE_MAP.put("qt", "video/quicktime");
    EXT_TO_MIME_TYPE_MAP.put("ra", "audio/x-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("ram", "audio/x-pn-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("ras", "image/cmu-raster");
    EXT_TO_MIME_TYPE_MAP.put("rgb", "image/x-rgb");
    EXT_TO_MIME_TYPE_MAP.put("rm", "audio/x-pn-realaudio");
    EXT_TO_MIME_TYPE_MAP.put("roff", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("rpm", "audio/x-pn-realaudio-plugin");
    EXT_TO_MIME_TYPE_MAP.put("rtf", "text/rtf");
    EXT_TO_MIME_TYPE_MAP.put("rtx", "text/richtext");
    EXT_TO_MIME_TYPE_MAP.put("scm", "application/x-lotusscreencam");
    EXT_TO_MIME_TYPE_MAP.put("set", "application/set");
    EXT_TO_MIME_TYPE_MAP.put("sgm", "text/sgml");
    EXT_TO_MIME_TYPE_MAP.put("sgml", "text/sgml");
    EXT_TO_MIME_TYPE_MAP.put("sh", "application/x-sh");
    EXT_TO_MIME_TYPE_MAP.put("shar", "application/x-shar");
    EXT_TO_MIME_TYPE_MAP.put("silo", "model/mesh");
    EXT_TO_MIME_TYPE_MAP.put("sit", "application/x-stuffit");
    EXT_TO_MIME_TYPE_MAP.put("skd", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skm", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skp", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("skt", "application/x-koan");
    EXT_TO_MIME_TYPE_MAP.put("smi", "application/smil");
    EXT_TO_MIME_TYPE_MAP.put("smil", "application/smil");
    EXT_TO_MIME_TYPE_MAP.put("snd", "audio/basic");
    EXT_TO_MIME_TYPE_MAP.put("sol", "application/solids");
    EXT_TO_MIME_TYPE_MAP.put("spl", "application/x-futuresplash");
    EXT_TO_MIME_TYPE_MAP.put("src", "application/x-wais-source");
    EXT_TO_MIME_TYPE_MAP.put("step", "application/STEP");
    EXT_TO_MIME_TYPE_MAP.put("stl", "application/SLA");
    EXT_TO_MIME_TYPE_MAP.put("stp", "application/STEP");
    EXT_TO_MIME_TYPE_MAP.put("sv4cpio", "application/x-sv4cpio");
    EXT_TO_MIME_TYPE_MAP.put("sv4crc", "application/x-sv4crc");
    EXT_TO_MIME_TYPE_MAP.put("swf", "application/x-shockwave-flash");
    EXT_TO_MIME_TYPE_MAP.put("t", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("tar", "application/x-tar");
    EXT_TO_MIME_TYPE_MAP.put("tcl", "application/x-tcl");
    EXT_TO_MIME_TYPE_MAP.put("tex", "application/x-tex");
    EXT_TO_MIME_TYPE_MAP.put("texi", "application/x-texinfo");
    EXT_TO_MIME_TYPE_MAP.put("texinfo", "application/x-texinfo");
    EXT_TO_MIME_TYPE_MAP.put("tif", "image/tiff");
    EXT_TO_MIME_TYPE_MAP.put("tiff", "image/tiff");
    EXT_TO_MIME_TYPE_MAP.put("tr", "application/x-troff");
    EXT_TO_MIME_TYPE_MAP.put("tsi", "audio/TSP-audio");
    EXT_TO_MIME_TYPE_MAP.put("tsp", "application/dsptype");
    EXT_TO_MIME_TYPE_MAP.put("tsv", "text/tab-separated-values");
    EXT_TO_MIME_TYPE_MAP.put("txt", "text/plain");
    EXT_TO_MIME_TYPE_MAP.put("unv", "application/i-deas");
    EXT_TO_MIME_TYPE_MAP.put("ustar", "application/x-ustar");
    EXT_TO_MIME_TYPE_MAP.put("vcd", "application/x-cdlink");
    EXT_TO_MIME_TYPE_MAP.put("vda", "application/vda");
    EXT_TO_MIME_TYPE_MAP.put("viv", "video/vnd.vivo");
    EXT_TO_MIME_TYPE_MAP.put("vivo", "video/vnd.vivo");
    EXT_TO_MIME_TYPE_MAP.put("vrml", "model/vrml");
    EXT_TO_MIME_TYPE_MAP.put("wav", "audio/x-wav");
    EXT_TO_MIME_TYPE_MAP.put("woff", "application/font-woff");
    EXT_TO_MIME_TYPE_MAP.put("wrl", "model/vrml");
    EXT_TO_MIME_TYPE_MAP.put("xbm", "image/x-xbitmap");
    EXT_TO_MIME_TYPE_MAP.put("xlc", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xll", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xlm", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xlw", "application/vnd.ms-excel");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    EXT_TO_MIME_TYPE_MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    EXT_TO_MIME_TYPE_MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    EXT_TO_MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
    EXT_TO_MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXT_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT_TO_MIME_TYPE_MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    EXT_TO_MIME_TYPE_MAP.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
    EXT_TO_MIME_TYPE_MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
    EXT_TO_MIME_TYPE_MAP.put("vsto", "application/x-ms-vsto");
    EXT_TO_MIME_TYPE_MAP.put("xml", "text/xml");
    EXT_TO_MIME_TYPE_MAP.put("xpm", "image/x-xpixmap");
    EXT_TO_MIME_TYPE_MAP.put("xwd", "image/x-xwindowdump");
    EXT_TO_MIME_TYPE_MAP.put("xyz", "chemical/x-pdb");
    EXT_TO_MIME_TYPE_MAP.put("zip", "application/zip");
  }

  public static void extractArchive(File archiveFile, File destinationDir) throws IOException {
    destinationDir.mkdirs();
    destinationDir.setLastModified(archiveFile.lastModified());
    String localFile = destinationDir.getName();
    try (JarFile jar = new JarFile(archiveFile);) {
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
            while ((len = is.read(buf)) > 0) {
              fos.write(buf, 0, len);
            }
          }
          finally {
            if (fos != null) {
              fos.close();
            }
            if (is != null) {
              is.close();
            }
          }
          if (file.getTime() >= 0) {
            f.setLastModified(file.getTime());
          }
        }
      }
    }
  }

  /**
   * Copies one file to another. Source must exist and be readable. Cannot copy a directory to a file. Will not copy if
   * time stamps and file size match, will overwrite otherwise.
   *
   * @param source
   *          the source file
   * @param dest
   *          the destination file
   * @throws IOException
   *           if an error occurs during the copy operation
   */
  @SuppressWarnings("resource")
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
            int maxCount = (mbCount * KILO_BYTE * KILO_BYTE) - (32 * KILO_BYTE);
            long size = input.size();
            long position = 0;
            while (position < size) {
              position +=
                  input.transferTo(position, maxCount, output);
            }
            done = true;
          }
          catch (IOException ioXcp) {
            if (ioXcp.getMessage().contains("Insufficient system resources exist to complete the requested service")) {
              mbCount--;
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

  private static byte[] readFile(File source) throws IOException {
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
    try (FileInputStream input = new FileInputStream(source)) {
      byte[] data = new byte[(int) source.length()];
      int n = 0;
      while (n < data.length) {
        n += input.read(data, n, data.length - n);
      }
      return data;
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
    else {
      copyFile(sourceLocation, targetLocation);
    }
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
    archiveFile.delete();
    try (JarOutputStream zOut = new JarOutputStream(new FileOutputStream(archiveFile))) {
      addFolderToJar(srcDir, srcDir, zOut);
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
    long timestamp = src.lastModified();
    byte[] data = readFile(src);
    addFileToJar(name, data, timestamp, zOut);
  }

  private static void addFileToJar(String name, byte[] data, long timestamp, JarOutputStream zOut) throws IOException {
    ZipEntry entry = new ZipEntry(name);
    entry.setTime(timestamp);
    zOut.putNextEntry(entry);
    zOut.write(data);
    zOut.closeEntry();
  }

  /**
   * @return the mime type for the specified extension
   */
  public static String getContentTypeForExtension(String ext) {
    if (ext == null) {
      return null;
    }
    if (ext.length() > 0 && ext.charAt(0) == '.') {
      ext = ext.substring(1);
    }
    ext = ext.toLowerCase();
    return EXT_TO_MIME_TYPE_MAP.get(ext);
  }

  /**
   * Gets the content type (MIME) of the given file.
   * <p>
   * The return value of this method is the string form of the value of a Multipurpose Internet Mail Extension (MIME)
   * content type as defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045: Multipurpose Internet
   * Mail Extensions (MIME) Part One: Format of Internet Message Bodies</i></a>. The string is guaranteed to be parsable
   * according to the grammar in the RFC.
   *
   * @param f
   *          The file for which the content type should be returned.
   * @return The content type or null if it could not be determined.
   */
  public static String getContentType(File f) {
    if (f == null || !f.exists()) {
      return null;
    }

    try {
      String contentType = Files.probeContentType(Paths.get(f.toURI()));
      if (contentType != null) {
        return contentType;
      }

      String fileName = f.getName();
      int i = fileName.lastIndexOf('.');
      if (i >= 0) {
        return FileUtility.getContentTypeForExtension(fileName.substring(i + 1));
      }

      return null;
    }
    catch (IOException e) {
      throw new ProcessingException("Unable to read content type of file '" + f.getAbsolutePath() + "'.", e);
    }
  }

  /**
   * @return Returns <code>true</code> if the given file is a zip file.
   */
  public static boolean isZipFile(File file) {
    if (file == null || file.isDirectory() || !file.canRead() || file.length() < 4) {
      return false;
    }
    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      int test = in.readInt();
      return test == 0x504b0304; // magic number of a zip file
    }
    catch (Exception e) {
      return false;
    }
  }

  public static byte[] removeByteOrderMark(File f) {
    return IOUtility.removeByteOrderMark(IOUtility.getContent(f.getAbsolutePath()));
  }

  /**
   * @param file
   * @return the file-extension of the given file or null when file has no file-extension. Example "foo.png" will return
   *         "png".
   */
  public static String getFileExtension(File file) {
    if (file == null) {
      return null;
    }
    return getFileExtension(file.getName());
  }

  /**
   * @param fileName
   * @return the file-extension of the given file or null when file has no file-extension. Example "foo.png" will return
   *         "png".
   */
  public static String getFileExtension(String fileName) {
    String[] parts = getFilenameParts(fileName);
    if (parts == null) {
      return null;
    }
    return parts[1];
  }

  /**
   * @param file
   * @return an array with two elements, [0] contains the file-name without extension [1] contains the file-extension
   */
  public static String[] getFilenameParts(File file) {
    if (file == null) {
      return null;
    }
    return getFilenameParts(file.getName());

  }

  /**
   * @param fileName
   * @return an array with two elements, [0] contains the file-name without extension [1] contains the file-extension
   */
  public static String[] getFilenameParts(String fileName) {
    if (fileName == null) {
      return null;
    }
    int index = fileName.lastIndexOf('.');
    if (index < 0) {
      return new String[]{
          fileName,
          null
      };
    }
    else {
      return new String[]{
          fileName.substring(0, index),
          fileName.substring(index + 1)
      };
    }
  }
}
