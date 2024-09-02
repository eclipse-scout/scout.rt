/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;

/**
 * Utility class for managing directories and files
 *
 * @since 1.0
 */
@SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
public final class FileUtility {

  public static final String ZIP_PATH_SEPARATOR = "/";

  private static final int KILO_BYTE = 1024;

  private FileUtility() {
  }

  public static void extractArchive(File archiveFile, File targetDir) throws IOException {
    File destinationDir = targetDir.getCanonicalFile();
    Path destinationPath = destinationDir.toPath();
    destinationDir.mkdirs();
    destinationDir.setLastModified(archiveFile.lastModified());
    String localFile = destinationDir.getName();
    try (JarFile jar = new JarFile(archiveFile)) {
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
        File f = new File(destinationDir, name).getCanonicalFile();
        if (!f.toPath().startsWith(destinationPath)) {
          // security check (see https://github.com/snyk/zip-slip-vulnerability)
          throw new IllegalArgumentException("Entry is outside of the target dir: " + name);
        }

        if (file.isDirectory()) { // if its a directory, create it
          f.mkdirs();
          if (file.getTime() >= 0) {
            f.setLastModified(file.getTime());
          }
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
      // magic number for Windows, 64Mb - 32Kb
      int mbCount = 64;
      boolean done = false;
      // java.io.IOException: Insufficient system resources exist to complete
      // the requested service
      while (!done) {
        if (!dest.exists()) {
          dest.getParentFile().mkdirs();
        }
        try (FileInputStream in = new FileInputStream(source);
            FileChannel input = in.getChannel();
            FileOutputStream out = new FileOutputStream(dest);
            FileChannel output = out.getChannel()) {

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
          }
          else {
            throw ioXcp;
          }
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
      if (children != null && children.length > 0) {
        for (String aChildren : children) {
          copyTree(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
        }
      }
    }
    else {
      copyFile(sourceLocation, targetLocation);
    }
  }

  public static List<File> listTree(File f, boolean includeFiles, boolean includeFolders) {
    List<File> list = new ArrayList<>();
    listTreeRec(f, list, includeFiles, includeFolders);
    return list;
  }

  private static void listTreeRec(File f, List<File> list, boolean includeFiles, boolean includeFolders) {
    if (f.isDirectory()) {
      if (includeFolders) {
        list.add(f);
      }
      String[] children = f.list();
      if (children != null && children.length > 0) {
        for (String aChildren : children) {
          listTreeRec(new File(f, aChildren), list, includeFiles, includeFolders);
        }
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
    if (!srcdir.exists() || !srcdir.isDirectory()) {
      throw new IOException("source directory " + srcdir + " does not exist or is not a folder");
    }

    File[] files = srcdir.listFiles();
    if (files == null || files.length < 1) {
      return;
    }

    for (File f : files) {
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
      return getMimeType((Path) null);
    }
    if (!ext.isEmpty() && ext.charAt(0) == '.') {
      ext = ext.substring(1);
    }
    ext = ext.toLowerCase(Locale.US).trim();
    return getMimeType("file." + ext);
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
      return getMimeType((Path) null);
    }
    return getMimeType(Paths.get(f.toURI()));
  }

  /**
   * @param path
   *          may also be an invalid path
   *          <p>
   *          see {@link #getMimeType(Path)}
   */
  public static String getMimeType(String path) {
    try {
      return getMimeType(Paths.get(path));
    }
    catch (RuntimeException e1) { // NOSONAR
      try {
        return getMimeType(Paths.get("file." + getFileExtension(path)));
      }
      catch (RuntimeException e2) { // NOSONAR
        return getMimeType((Path) null);
      }
    }
  }

  /**
   * Finds the mime type of the given path.
   * <p>
   * The return value of this method is the string form of the value of a Multipurpose Internet Mail Extension (MIME)
   * content type as defined by <a href="http://www.ietf.org/rfc/rfc2045.txt"><i>RFC&nbsp;2045: Multipurpose Internet
   * Mail Extensions (MIME) Part One: Format of Internet Message Bodies</i></a>. The string is guaranteed to be parsable
   * according to the grammar in the RFC.
   * <p>
   * Loops over all {@link IMimeTypeDetector} and returns the first that decides.
   * <p>
   * If path is null or none of the {@link IMimeTypeDetector} decides then application/octet-stream is returned.
   *
   * @param path
   *          The path for which the content type should be returned (including content inspection).
   * @return The content type, never null
   */
  public static String getMimeType(Path path) {
    if (path == null) {
      return MimeType.APPLICATION_OCTET_STREAM.getType();
    }
    for (IMimeTypeDetector d : BEANS.all(IMimeTypeDetector.class)) {
      String m = d.getMimeType(path);
      if (m != null) {
        return m;
      }
    }
    return MimeType.APPLICATION_OCTET_STREAM.getType();
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

  private static final Pattern PAT_FILENAME_REMOVE_INVALID_CHARACTERS = Pattern.compile("[<>:\"/\\\\|?*\\x00-\\x1F]");
  private static final Pattern PAT_FILENAME_REMOVE_LEADING_CHARACTERS = Pattern.compile("^[\\s.]+([^\\s.].*)$");
  private static final Pattern PAT_FILENAME_REMOVE_TRAILING_CHARACTERS = Pattern.compile("^(.*[^\\s.])[\\s.]+$");
  private static final Pattern PAT_FILENAME_TRIM = Pattern.compile("^[\\s.]*$");
  private static final String DEFAULT_FILENAME = "_";

  /**
   * Validate a file name based on the <tt>filename</tt> by removing illegal characters, leading/trailing
   * dots/whitespace and omitting the file name being one of the reserved file names.
   * <p>
   * Never returns an empty String or null.
   * <p>
   * Chops filename to max length of 250 characters.
   *
   * @since 5.1
   */
  public static String toValidFilename(final String filename) {
    if (filename == null) {
      return DEFAULT_FILENAME;
    }
    String s = filename;
    s = PAT_FILENAME_REMOVE_INVALID_CHARACTERS.matcher(s).replaceAll("");
    try {
      Paths.get(s);
      //ok
    }
    catch (InvalidPathException ex) { // NOSONAR
      //nok, check every character in sandwich test
      StringBuilder buf = new StringBuilder();
      for (char ch : s.toCharArray()) {
        try {
          Paths.get("a" + ch + "a");
          buf.append(ch);
        }
        catch (InvalidPathException ex2) { // NOSONAR
          //skip ch
        }
      }
      s = buf.toString();
    }
    final String[] filenameParts = getFilenameParts(s);
    String name = filenameParts[0];
    //remove leading dots, whitespace from name part
    name = PAT_FILENAME_REMOVE_LEADING_CHARACTERS.matcher(name).replaceAll("$1");
    name = PAT_FILENAME_TRIM.matcher(name).replaceAll("");

    if (name.isEmpty()) {
      name = DEFAULT_FILENAME;
    }

    String ext = filenameParts[1];
    if (StringUtility.isNullOrEmpty(ext)) {
      //no extension found, remove trailing dots, whitespace from name part
      name = PAT_FILENAME_REMOVE_TRAILING_CHARACTERS.matcher(name).replaceAll("$1");
    }
    else {
      //remove trailing dots, whitespace from extension
      ext = PAT_FILENAME_REMOVE_TRAILING_CHARACTERS.matcher(ext).replaceAll("$1");
      ext = PAT_FILENAME_TRIM.matcher(ext).replaceAll("");
    }

    filenameParts[0] = name;
    filenameParts[1] = ext;
    s = StringUtility.join(".", filenameParts);
    s = PAT_FILENAME_TRIM.matcher(s).replaceAll("");

    // on some operating systems, the name may not be longer than 250 characters
    if (s.length() > 250) {
      int dot = s.lastIndexOf('.');
      String suffix = (dot > 0 ? s.substring(dot) : "");
      //suffix is at most 32 chars
      if (suffix.length() > 32) {
        suffix = "";
      }
      s = s.substring(0, 250 - suffix.length()) + suffix;
    }

    return s;
  }

  /**
   * Is the given <tt>filename</tt> a valid file name
   * <p>
   * Uses {@link #toValidFilename(String)} to check
   *
   * @return <tt>false</tt> if <tt>filename</tt> is not a valid filename<br/>
   *         <tt>true</tt> if <tt>filename</tt> is a valid filename
   */
  public static boolean isValidFilename(String filename) {
    return toValidFilename(filename).equals(filename);
  }

  /**
   * @return Returns <code>true</code> if the given file is a zip file, otherwise <code>false</code>.
   */
  public static boolean isZipFile(File file) {
    if (file == null || file.isDirectory() || !file.canRead() || file.length() < 4) {
      return false;
    }
    try (
        @SuppressWarnings("squid:S2095")
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      int test = in.readInt();
      return test == 0x504b0304; // magic number of a zip file
    }
    catch (IOException e) { // NOSONAR
    }
    return false;
  }

  /**
   * Creates a BinaryResource representing a zip file containing the provided BinaryResources.
   *
   * @param zipFilename
   *          Name of zip file.
   * @param resources
   *          BinaryResources to put in zip archive.
   * @return BinaryResource representing a zip archive.
   */
  public static BinaryResource zip(String zipFilename, Collection<BinaryResource> resources) {
    return zip(zipFilename, resources, false);
  }

  /**
   * Creates a BinaryResource representing a zip file containing the provided BinaryResources.
   *
   * @param zipFilename
   *          Name of zip file.
   * @param resources
   *          BinaryResources to put in zip archive.
   * @param avoidFileNameConflicts
   *          If set to <tt>true</tt>, unique file names will be used to avoid {@link IOException} due to file name
   *          conflicts. It may cause differences in file names and order between <tt>resources</tt> and the resulting
   *          zip file.
   * @return BinaryResource representing a zip archive.
   */
  public static BinaryResource zip(String zipFilename, Collection<BinaryResource> resources, boolean avoidFileNameConflicts) {
    File tempFile = IOUtility.createTempFile("tmp", ".zip", null);
    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempFile))) {
      Collection<BinaryResource> resourcesToZip = avoidFileNameConflicts ? getBinaryResourcesWithUniqueFileNames(resources) : resources;

      for (BinaryResource res : resourcesToZip) {
        if (res == null) {
          continue;
        }
        // make sure that filename within zip are valid filenames
        String validatedFilename = validateZipFilename(res.getFilename());

        zos.putNextEntry(new ZipEntry(validatedFilename));
        if (res.getContent() != null) {
          zos.write(res.getContent()); //TODO imo: extend BinaryResource to support .write(OutputStream)
        }
        zos.closeEntry();
      }
      zos.flush();

      zos.close(); // reading the bytes requires the zip output stream to be closed
      byte[] zipContent = IOUtility.getContent(tempFile);

      //TODO imo: BinaryResource extend to support a constructor with InputStream
      return BinaryResources.create()
          .withFilename(zipFilename)
          .withContentType("application/zip")
          .withContent(zipContent)
          .withLastModifiedNow()
          .build();
    }
    catch (IOException e) {
      throw new ProcessingException("could not create zip file", e);
    }
    finally {
      // delete no longer required temp file to free up disk space (318690)
      //noinspection ResultOfMethodCallIgnored
      tempFile.delete();
    }
  }

  /**
   * Unzips the given ZIP archive. Delegates to {@link IOUtility#unzip(byte[], Pattern)}.
   *
   * @see IOUtility#unzip(byte[], Pattern)
   */
  public static Collection<BinaryResource> unzip(BinaryResource zipArchive, Pattern filterPattern) {
    // TODO imo: BinaryResource create a .getInputStream(), also allow to pass an
    // InputStream to the constructor/builder of the BinaryResource.
    try {
      return IOUtility.unzip(zipArchive.getContent(), filterPattern);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not unzip archive", e);
    }
  }

  /**
   * Unzips the given ZIP archive.
   *
   * @return array of files contained in ZIP archive
   */
  public static File[] unzipArchive(File zipArchive) {
    List<File> files = new LinkedList<>();
    File tempDir = IOUtility.createTempDirectory("");
    try {
      FileUtility.extractArchive(zipArchive, tempDir);
      File[] list = tempDir.listFiles();
      if (list != null && list.length > 0) {
        Collections.addAll(files, list);
      }
      return files.toArray(new File[0]);
    }
    catch (IOException e) {
      throw new ProcessingException("Could not unzip archive", e);
    }
  }

  /**
   * Validates filename and removes illegal characters for file names within a zip archive.
   *
   * @see #toValidFilename(String) for details
   */
  public static String validateZipFilename(String origFilename) {
    String[] tokens = StringUtility.split(origFilename, ZIP_PATH_SEPARATOR);
    return Arrays.stream(tokens).map(FileUtility::toValidFilename).collect(Collectors.joining(ZIP_PATH_SEPARATOR));
  }

  /**
   * Creates a new list with BinaryResources with unique file names. The order won't be preserved.</br>
   * <p>
   * For BinaryResources with non-unique file names a new BinaryResource is created with a counting suffix: '(x)'.
   *
   * @param resources
   *          BinaryResources to put in zip archive.
   * @return BinaryResources with unique file names
   */
  public static Collection<BinaryResource> getBinaryResourcesWithUniqueFileNames(Collection<BinaryResource> resources) {
    List<BinaryResource> result = CollectionUtility.arrayList(resources);
    boolean hasDuplicates = true;
    while (hasDuplicates) {
      hasDuplicates = false;
      Map<String, List<BinaryResource>> nameMap = CollectionUtility.emptyHashMap();
      // group BinaryResources by file name
      for (BinaryResource binaryResource : result) {
        String filenameLowerCase = binaryResource.getFilename().toLowerCase();
        if (nameMap.containsKey(filenameLowerCase)) {
          nameMap.get(filenameLowerCase).add(binaryResource);
          hasDuplicates = true;
        }
        else {
          nameMap.put(filenameLowerCase, CollectionUtility.arrayList(binaryResource));
        }
      }
      result.clear();
      // add suffix for non-unique file names and add BinaryResources to result list
      for (Entry<String, List<BinaryResource>> entry : nameMap.entrySet()) {
        int elementCount = CollectionUtility.size(entry.getValue());
        if (elementCount > 1) {
          for (int i = 1; i <= elementCount; i++) {
            BinaryResource binaryResource = entry.getValue().get(i - 1);
            result.add(binaryResource.createAliasWithSameExtension(
                StringUtility.concatenateTokens(getFileName(binaryResource.getFilename(), false), "(", StringUtility.valueOf(i), ")")));
          }
        }
        else {
          result.addAll(entry.getValue());
        }
      }
    }
    return result;
  }

  /**
   * @return the name of the file only, optionally with included file extension
   */
  public static String getFileName(String filename, boolean includeFileExtension) {
    if (StringUtility.isNullOrEmpty(filename)) {
      return null;
    }

    String s = IOUtility.getFileName(filename);
    if (includeFileExtension) {
      return s;
    }
    if (s.lastIndexOf('.') != -1) {
      s = s.substring(0, s.lastIndexOf('.'));
    }
    return s;
  }
}
