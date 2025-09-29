/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.rt.platform.util.Assertions.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("findbugs:RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
public final class IOUtility {
  private static final Logger LOG = LoggerFactory.getLogger(IOUtility.class);

  private IOUtility() {
  }

  public static byte[] getContent(String filename) {
    return getContent(assertNotNull(toFile(filename)));
  }

  /**
   * @see Files#readAllBytes(Path)
   */
  public static byte[] getContent(File file) {
    try {
      return Files.readAllBytes(file.toPath());
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + file.getAbsolutePath(), e);
    }
  }

  /**
   * Reads the content of a file into a {@link String}. Use only for small files.
   *
   * @param filepath
   *     The file to read. Must not be {@code null}.
   * @param encoding
   *     An optional encoding to read the file (e.g. {@code StandardCharsets.UTF_8.name()}). If no encoding is given, the standard charset is used (see {@link Charset#defaultCharset()}).
   * @return The file content as {@link String}.
   * @throws ProcessingException
   *     in case there is an I/O error reading the file.
   */
  public static String getContentInEncoding(String filepath, String encoding) {
    return getContentInEncoding(toFile(filepath), encoding);
  }

  /**
   * Reads the content of a file into a {@link String}. Use only for small files.
   *
   * @param file
   *     The file to read. Must not be {@code null}.
   * @param encoding
   *     An optional encoding to read the file (e.g. {@code StandardCharsets.UTF_8.name()}). If no encoding is given, the standard charset is used (see {@link Charset#defaultCharset()}).
   * @return The file content as {@link String}.
   * @throws ProcessingException
   *     in case there is an I/O error reading the file.
   */
  public static String getContentInEncoding(File file, String encoding) {
    return getContentInEncoding(file.toPath(), encoding);
  }

  /**
   * Reads the content of a file into a {@link String}. Use only for small files.
   *
   * @param file
   *     The file to read. Must not be {@code null}.
   * @param encoding
   *     An optional encoding to read the file (e.g. {@code StandardCharsets.UTF_8.name()}). If no encoding is given, the standard charset is used (see {@link Charset#defaultCharset()}).
   * @return The file content as {@link String}.
   * @throws ProcessingException
   *     in case there is an I/O error reading the file.
   */
  public static String getContentInEncoding(Path file, String encoding) {
    try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
      return readString(in, encoding);
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * Reads bytes.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @return the content bytes
   */
  public static byte[] readBytes(InputStream in) {
    return readBytes(in, -1);
  }

  /**
   * Reads bytes.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @param len
   *     optional known length or -1 if unknown
   * @return the content bytes
   */
  public static byte[] readBytes(InputStream in, int len) {
    if (len >= 0) {
      try {
        byte[] buf = new byte[len];
        int count = 0;
        while (count < len) {
          int read = in.read(buf, count, len - count);
          if (read < 0) {
            return Arrays.copyOf(buf, count);
          }
          count += read;
        }
        return buf;
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + in, e);
      }
    }
    else {
      try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
        writeFromToStream(buffer, in);
        return buffer.toByteArray();
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + in, e);
      }
    }
  }

  /**
   * Reads string.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @param charset
   *     optional charset, if null is provided, the system default encoding is used
   * @return the content string
   */
  public static String readString(InputStream in, String charset) {
    return readString(in, charset, -1);
  }

  /**
   * Reads string from UTF-8 encoded stream.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @return the content string
   */
  public static String readStringUTF8(InputStream in) {
    return readString(in, StandardCharsets.UTF_8.name(), -1);
  }

  /**
   * Reads string.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @param charset
   *     optional charset, if null is provided, the system default encoding is used
   * @param len
   *     optional known length in bytes or -1 if unknown
   * @return the content string
   */
  public static String readString(InputStream in, String charset, int len) {
    if (StringUtility.hasText(charset)) {
      try {
        return readString(new InputStreamReader(in, charset), len);
      }
      catch (UnsupportedEncodingException e) {
        throw new PlatformException("charset {}", charset, e);
      }
    }
    else {
      return readString(new InputStreamReader(in), len);
    }
  }

  /**
   * Reads string.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @return the content string
   */
  public static String readString(Reader in) {
    return readString(in, -1);
  }

  /**
   * Reads string.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   *
   * @param in
   *     input reader
   * @param maxLen
   *     max number of characters to read or -1 if the whole stream should be read.
   * @return the content string
   */
  public static String readString(Reader in, int maxLen) {
    if (maxLen >= 0) {
      try {
        char[] buf = new char[maxLen];
        int count = 0;
        int nRead = in.read(buf, 0, maxLen);
        while (nRead != -1 && count < maxLen) {
          count += nRead;
          nRead = in.read(buf, count, maxLen - count);
        }
        return new String(buf, 0, count);
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + in, e);
      }
    }
    else {
      try (StringWriter buffer = new StringWriter()) {
        in.transferTo(buffer);
        return buffer.toString();
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + in, e);
      }
    }
  }

  public static byte[] compressGzip(byte[] b) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    try (GZIPOutputStream out = new GZIPOutputStream(buf)) {
      out.write(b);
    }
    return buf.toByteArray();
  }

  public static byte[] uncompressGzip(byte[] b) throws IOException {
    try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(b)); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      in.transferTo(out);
      return out.toByteArray();
    }
  }

  /**
   * Unzips the given ZIP archive as a collection of BinaryResources. Unzipping happens in-memory, no files are written.
   * The optional parameter <code>entryNameFilterPattern</code> allows to filter the ZIP for matching file entries.
   * Unzipping will be performed with the UTF-8 charset. If this fails, unzipping with the legacy charset Cp437 will
   * be performed. If this fails again or Cp437 is not supported by the JVM used, an <code>IllegalArgumentException</code>
   * is thrown.
   *
   * @param zipArchive
   *     file to unzip
   * @param filterPattern
   *     optional filter, may be null
   * @return A collection of binary resources contained in the ZIP archive
   */
  public static Collection<BinaryResource> unzip(byte[] zipArchive, Pattern filterPattern) throws IOException {
    try {
      return unzip(zipArchive, filterPattern, null);
    }
    catch (IllegalArgumentException e) {
      Charset charset;
      try {
        charset = Charset.forName("Cp437");
      }
      catch (UnsupportedCharsetException charsetException) { // NOSONAR
        throw e;
      }
      return unzip(zipArchive, filterPattern, charset);
    }
  }

  /**
   * Unzips the given ZIP archive as a collection of BinaryResources. Unzipping happens in-memory, no files are written.
   * The optional parameter <code>entryNameFilterPattern</code> allows to filter the ZIP for matching file entries.
   *
   * @param zipArchive
   *     binary data
   * @param charset
   *     the charset to use for the unzipping
   * @param filterPattern
   *     optional filter, may be null
   * @return A collection of binary resources contained in the ZIP archive
   */
  public static Collection<BinaryResource> unzip(byte[] zipArchive, Pattern filterPattern, Charset charset) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipArchive), charset == null ? StandardCharsets.UTF_8 : charset)) {
      List<BinaryResource> list = new ArrayList<>();
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();
        if (Paths.get(entryName).normalize().startsWith("..")) {
          // security check (see https://github.com/snyk/zip-slip-vulnerability)
          throw new IllegalArgumentException("Zip entry is outside of root dir: " + entryName);
        }

        if (filterPattern != null && !filterPattern.matcher(entryName).matches()) {
          continue;
        }

        // Skip directories, not relevant for binary resources
        if (entry.isDirectory()) {
          continue;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeFromToStream(bos, zis);
        bos.close();

        BinaryResource res = BinaryResources.create()
            .withFilename(entryName)
            .withContent(bos.toByteArray())
            .withLastModified(entry.getTime())
            .build();

        list.add(res);
      }
      return list;
    }
  }

  public static byte[] readFromUrl(URL url) throws IOException {
    URLConnection uc = url.openConnection();
    int len = uc.getContentLength();
    try (BufferedInputStream in = new BufferedInputStream(uc.getInputStream())) {
      return readBytes(in, len);
    }
  }

  /**
   * Reads all text lines from the {@link URL} specified.
   *
   * @param url
   *     The {@link URL} to read from. Must not be {@code null}.
   * @param charset
   *     The {@link Charset} used to read the url content. Must not be {@code null}.
   * @return A {@link List} with all lines
   */
  public static List<String> readAllLinesFromUrl(URL url, Charset charset) throws IOException {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(assertNotNull(url).openConnection().getInputStream(), charset))) {
      return in.lines().collect(toList());
    }
  }

  /**
   * Reads the content of the file specified by the given URL and returns it as a {@link BinaryResource}. The name of
   * the binary resource is extracted from the "file" part of the given URL. Use
   * {@link #readBinaryResource(URL, String)} to specify a different name.
   *
   * @param url
   *     URL pointing to the file's location
   * @return a {@link BinaryResource} with the content of the file specified by the given URL or {@code null} if the URL
   * is {@code null}.
   * @throws ProcessingException
   *     if reading the file content failed for some reason.
   */
  public static BinaryResource readBinaryResource(URL url) {
    if (url == null) {
      return null;
    }
    String targetName = new File(url.getPath()).getName();
    return readBinaryResource(url, targetName);
  }

  /**
   * Reads the content of the file specified by the given URL and returns it as a {@link BinaryResource}. The name of
   * the binary resource is set to the given "targetName".
   *
   * @param url
   *     URL pointing to the file's location
   * @param targetName
   *     File name to be used for the resulting binary resource, i.e. {@link BinaryResource#getFilename()}.
   * @return a {@link BinaryResource} with the content of the file specified by the given URL or {@code null} if the URL
   * is {@code null}.
   * @throws ProcessingException
   *     if reading the file content failed for some reason.
   */
  public static BinaryResource readBinaryResource(URL url, String targetName) {
    if (url == null) {
      return null;
    }
    try (InputStream in = url.openStream()) {
      byte[] content = readBytes(in);
      return new BinaryResource(targetName, content);
    }
    catch (IOException e) {
      throw new ProcessingException("Error while reading from URL {}", url, e);
    }
  }

  public static void writeContent(String filename, Object o) {
    File f = toFile(filename);
    try {
      if (o instanceof byte[]) {
        try (FileOutputStream out = new FileOutputStream(f)) {
          writeBytes(out, (byte[]) o);
        }
      }
      else if (o instanceof char[]) {
        try (FileWriter out = new FileWriter(f)) {
          writeString(out, new String((char[]) o));
        }
      }
      else if (o != null) {
        try (FileWriter out = new FileWriter(f)) {
          writeString(out, o.toString());
        }
      }
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + filename, e);
    }
  }

  /**
   * Write bytes to an output stream without throwing a checked exception. {@link ProcessingException} is thrown instead.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   */
  public static void writeBytes(OutputStream out, byte[] bytes) {
    try {
      out.write(bytes);
    }
    catch (IOException e) {
      throw new ProcessingException("output: " + out, e);
    }
  }

  /**
   * Writes from one stream into another.
   *
   * @return the number of bytes written
   * @since 6.1
   */
  public static long writeFromToStream(OutputStream out, InputStream in) throws IOException {
    return in.transferTo(out);
  }

  /**
   * Write string in UTF8 encoding. The given {@link OutputStream} is automatically closed.
   */
  public static void writeStringUTF8(OutputStream out, String s) {
    writeString(out, StandardCharsets.UTF_8.name(), s);
  }

  /**
   * Write string. The given {@link OutputStream} is automatically closed.
   */
  public static void writeString(OutputStream out, String charset, String s) {
    try (OutputStreamWriter w = new OutputStreamWriter(out, charset)) {
      w.write(s);
      w.flush();
    }
    catch (IOException e) {
      throw new ProcessingException("output: " + out, e);
    }
  }

  /**
   * Write string.
   * <p>
   * Stream is <em>not</em> closed. Use resource-try on streams created by caller.
   */
  public static void writeString(Writer out, String s) {
    try {
      out.write(s);
    }
    catch (IOException e) {
      throw new ProcessingException("output: " + out, e);
    }
  }

  /**
   * Directory browsing including subtree
   */
  public static File[] listFilesInSubtree(File dir, FileFilter filter) {
    ArrayList<File> list = new ArrayList<>();
    listFilesRec(dir, filter, list);
    return list.toArray(new File[0]);
  }

  private static void listFilesRec(File dir, FileFilter filter, ArrayList<File> intoList) {
    if (dir != null && dir.exists() && dir.isDirectory()) {
      File[] a = dir.listFiles(filter);
      for (int i = 0; a != null && i < a.length; i++) {
        if (a[i].isDirectory()) {
          listFilesRec(a[i], filter, intoList);
        }
        else {
          intoList.add(a[i]);
        }
      }
    }
  }

  /**
   * Creates a temporary directory with a random name and the given suffix. The new directory is empty and exists. It is
   * marked as {@link File#deleteOnExit()}.
   *
   * @return the new temporary directory
   */
  public static File createTempDirectory(String dirSuffix) {
    try {
      if (dirSuffix != null) {
        dirSuffix = dirSuffix.replaceAll("[:*?\"<>|]*", "");
      }
      File tempDirectory = File.createTempFile("dir", dirSuffix);
      assertTrue(tempDirectory.delete()); // File.createTempFile creates a new file which will be deleted here
      assertTrue(tempDirectory.mkdirs()); // the newly created file reference is now used as a directory and the directory is created here
      tempDirectory.deleteOnExit();
      return tempDirectory;
    }
    catch (IOException e) {
      throw new ProcessingException("dir: " + dirSuffix, e);
    }
  }

  /**
   * Convenience method for creating temporary files with content. Note, the temporary file will be automatically
   * deleted when the virtual machine terminates.
   *
   * @param fileName
   *     If no or an empty filename is given, a random fileName will be created.
   * @param content
   *     If no content is given, an empty file will be created
   * @return A new temporary file with specified content
   */
  public static File createTempFile(String fileName, byte[] content) {
    return createTempFile(fileName, (File) null, content);
  }

  /**
   * Convenience method for creating temporary files with content. Note, the temporary file will be automatically
   * deleted when the virtual machine terminates.
   *
   * @param fileName
   *     If no or an empty filename is given, a random fileName will be created.
   * @param content
   *     If no content is given, an empty file will be created
   * @param directory
   *     The directory in which the temporary file is to be created, or {@code null} if the default temp directory
   *     is to be used
   * @return A new temporary file with specified content
   */
  public static File createTempFile(String fileName, File directory, byte[] content) {
    try {
      if (fileName != null) {
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]*", "");
      }
      if (fileName == null || fileName.isEmpty()) {
        fileName = getTempFileName(".tmp");
      }
      File f = File.createTempFile("tmp", ".tmp", directory);
      File f2 = new File(f.getParentFile(), new File(fileName).getName());
      if (f2.exists() && !f2.delete()) {
        throw new IOException("File " + f2 + " exists and cannot be deleted");
      }
      boolean ok = f.renameTo(f2);
      if (!ok) {
        throw new IOException("failed renaming " + f + " to " + f2);
      }
      f2.deleteOnExit();
      if (content != null) {
        try (OutputStream out = new FileOutputStream(f2)) {
          writeBytes(out, content);
        }
      }
      else {
        // noinspection ResultOfMethodCallIgnored
        f2.createNewFile();
      }
      return f2;
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + fileName, e);
    }
  }

  /**
   * Creates a temporary file in the system temp folder from an input stream.
   * <p>
   * Content stream is closed automatically.
   *
   * @param content
   *     data to be written to the temporary file.
   * @param filename
   *     file name prefix (the system will automatically add an arbitrary identifier to this prefix to generate a
   *     unique file name)
   * @param extension
   *     file name suffix (must include the colon, e.g. {@code ".tmp"})
   * @return newly created temporary file
   * @throws ProcessingException
   *     if file creation failed
   */
  public static File createTempFile(InputStream content, String filename, String extension) {
    return createTempFile(content, filename, extension, null);
  }

  /**
   * Creates a temporary file in a certain folder from an input stream.
   * <p>
   * Content stream is closed automatically.
   *
   * @param content
   *     data to be written to the temporary file.
   * @param filename
   *     file name prefix (the system will automatically add an arbitrary identifier to this prefix to generate a
   *     unique file name)
   * @param extension
   *     file name suffix (must include the colon, e.g. {@code ".tmp"})
   * @param directory
   *     The directory in which the temporary file is to be created, or {@code null} if the default temp directory
   *     is to be used
   * @return newly created temporary file
   * @throws ProcessingException
   *     if file creation failed
   */
  public static File createTempFile(InputStream content, String filename, String extension, File directory) {
    try {
      File temp = File.createTempFile(filename, extension, directory);
      try (InputStream in = content) {
        Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      return temp;
    }
    catch (IOException e) {
      throw new ProcessingException("Error creating temp file", e);
    }
  }

  /**
   * Convenience method for creating temporary files with content. Note, the temporary file will be automatically
   * deleted when the virtual machine terminates. The temporary file will look like this: <i>prefix</i>2093483323922923
   * <i>.suffix</i> and will be located in the default temp folder.
   *
   * @param prefix
   *     The prefix of the temporary file
   * @param suffix
   *     The suffix of the temporary file. Don't forget the colon, for example <b>.tmp</b>
   * @param content
   *     data to be written to the temporary file.
   * @return A new temporary file with the specified content
   * @throws ProcessingException
   *     if file creation failed
   */
  public static File createTempFile(String prefix, String suffix, byte[] content) {
    return createTempFile(prefix, suffix, null, content);
  }

  /**
   * Convenience method for creating temporary files with content. Note, the temporary file will be automatically
   * deleted when the virtual machine terminates. The temporary file will look like this: <i>prefix</i>2093483323922923
   * <i>.suffix</i> and will be located in the specified directory.
   *
   * @param prefix
   *     The prefix of the temporary file
   * @param suffix
   *     The suffix of the temporary file. Don't forget the colon, for example <b>.tmp</b>
   * @param directory
   *     The directory in which the temporary file is to be created, or {@code null} if the default temp directory
   *     is to be used
   * @param content
   *     data to be written to the temporary file.
   * @return A new temporary file with the specified content
   * @throws ProcessingException
   *     if file creation failed
   */
  public static File createTempFile(String prefix, String suffix, File directory, byte[] content) {
    File f = null;
    try {
      f = File.createTempFile(prefix, suffix, directory);
      f.deleteOnExit();
      if (content != null) {
        try (OutputStream out = new FileOutputStream(f)) {
          writeBytes(out, content);
        }
      }
      return f;
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + f, e);
    }
  }

  /**
   * Delete a directory and all containing files and directories
   *
   * @param dir
   *     directory to be deleted
   * @return true if the directory is successfully deleted or does not exist; false otherwise
   * @throws SecurityException
   *     - If a security manager exists and its check methods deny read or delete access
   */
  public static boolean deleteDirectory(File dir) {
    if (dir != null && dir.exists()) {
      File[] a = dir.listFiles();
      for (int i = 0; a != null && i < a.length; i++) {
        if (a[i].isDirectory()) {
          deleteDirectory(a[i]);
        }
        else {
          // noinspection ResultOfMethodCallIgnored
          a[i].delete();
        }
      }
      return dir.delete();
    }
    return true;
  }

  public static boolean deleteDirectory(String dir) {
    File f = toFile(dir);
    if (f != null && f.exists()) {
      return deleteDirectory(f);
    }
    return false;
  }

  public static boolean createDirectory(String dir) {
    if (dir != null) {
      dir = dir.replaceAll("[*?\"<>|]*", "");
      File f = toFile(dir);
      return f != null && f.mkdirs();
    }
    return false;
  }

  public static boolean deleteFile(String filePath) {
    if (filePath != null) {
      File f = toFile(filePath);
      if (f.exists()) {
        return f.delete();
      }
    }
    return false;
  }

  /**
   * Null-safe file delete
   *
   * @return <code>true</code>, if deletion successful.
   */
  public static boolean deleteFile(File file) {
    if (file != null && file.exists()) {
      return file.delete();
    }
    return false;
  }

  /**
   * file handling
   */
  public static long getFileSize(String filepath) {
    if (filepath == null) {
      return 0;
    }
    File f = toFile(filepath);
    return getFileSize(f);
  }

  public static long getFileSize(File filepath) {
    if (filepath == null || !filepath.exists()) {
      return 0;
    }
    return filepath.length();
  }

  public static long getFileLastModified(String filepath) {
    if (filepath == null) {
      return 0;
    }
    File f = toFile(filepath);
    if (f.exists()) {
      return f.lastModified();
    }
    return 0;
  }

  /**
   * @return the name of the file only
   */
  public static String getFileName(String filepath) {
    if (filepath == null) {
      return null;
    }
    File f = toFile(filepath);
    return f.getName();
  }

  /**
   * @return a valid File representing s with support for both / and \ as path separators.
   */
  public static File toFile(String s) {
    if (s == null) {
      return null;
    }
    return new File(s.replace('\\', File.separatorChar).replace('/', File.separatorChar));
  }

  /**
   * @return the path of the file without its name
   */
  public static String getFilePath(String filepath) {
    if (filepath == null) {
      return null;
    }
    File f = toFile(filepath);
    return f.getParent();
  }

  public static boolean fileExists(String s) {
    if (s != null) {
      File f = toFile(s);
      return f.exists();
    }
    else {
      return false;
    }
  }

  public static String getTempFileName(String fileExtension) {
    try {
      File f = File.createTempFile("tmp", fileExtension);
      // noinspection ResultOfMethodCallIgnored
      f.delete();
      return f.getAbsolutePath();
    }
    catch (IOException e) {
      throw new ProcessingException("extension: " + fileExtension, e);
    }
  }

  /**
   * A null-safe variant for calling {@link URLEncoder#encode(String, String)}. This method returns null if the given
   * <code>url</code> is null or an empty string respectively. Any leading / trailing whitespaces are omitted.
   * Furthermore, "%20" is used to represent spaces instead of "+".
   *
   * @param url
   *     the URL string which shall be encoded
   * @return the encoded URL string
   */
  public static String urlEncode(String url) {
    if (url == null) {
      return null;
    }

    String s = url.trim();
    if (s.isEmpty()) {
      return "";
    }

    s = URLEncoder.encode(s, StandardCharsets.UTF_8);
    return StringUtility.replace(s, "+", "%20");
  }

  /**
   * a null-safe variant for calling {@link URLDecoder#decode(String, String)}. This method returns null if the given
   * <code>url</code> is null or an empty string respectively. Any leading / trailing whitespaces are omitted.
   *
   * @param encodedUrl
   *     the encoded URL string which shall be decoded
   * @return the decoded URL string
   */
  public static String urlDecode(String encodedUrl) {
    if (encodedUrl == null) {
      return null;
    }

    String s = encodedUrl.trim();
    if (s.isEmpty()) {
      return "";
    }

    return URLDecoder.decode(s, StandardCharsets.UTF_8);
  }

  /**
   * Tries to convert the text to a URL. If it is no url yet, it tries to convert to an email URL ({@code mailto:}) if it contains an '@' or a {@code https} URL otherwise.
   *
   * @return the {@link URL} or {@code null} if it cannot be converted.
   */
  public static URL urlTextToUrl(String urlText) {
    if (StringUtility.isNullOrEmpty(urlText)) {
      return null;
    }
    try {
      return new URI(urlText).toURL();
    }
    catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
      String text = urlText;
      if (text.indexOf('@') >= 0) {
        text = "mailto:" + text;
      }
      else {
        text = "https://" + text;
      }

      try {
        return new URI(text).toURL();
      }
      catch (MalformedURLException | URISyntaxException | IllegalArgumentException e1) {
        LOG.debug("Could not create url from '{}'.", urlText, e1);
      }
    }
    return null;
  }

  /**
   * Append a file to another. The provided {@link PrintWriter} will neither be flushed nor closed.
   * <p>
   * ATTENTION: Appending a file to itself using an auto flushing PrintWriter, will lead to an endless loop. (Appending a file to itself using a {@link PrintWriter} without auto flushing is safe.)
   *
   * @param writer
   *     a PrintWriter for the destination file
   * @param file
   *     source file
   * @throws ProcessingException
   *     if an {@link IOException} occurs (e.g. if file does not exist)
   */
  public static void appendFile(PrintWriter writer, File file) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        writer.println(line);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Error appending file: " + file.getName(), e);
    }
  }

  /**
   * @param file
   *     file to read from
   * @param charsetName
   *     The name of a supported {@link Charset </code>charset<code>}
   * @return List containing all lines of the file as Strings
   * @throws ProcessingException
   *     if an {@link IOException} occurs (e.g. if file does not exist)
   */
  public static List<String> readLines(File file, String charsetName) {
    try {
      return Files.readAllLines(file.toPath(), Charset.forName(charsetName));
    }
    catch (IOException e) {
      throw new ProcessingException("Error reading all lines of file '{}'", file, e);
    }
  }

  /**
   * This method removes the Byte Order Mark (BOM) from an array of bytes. The following Byte Order Marks of the
   * following encodings are checked and removed.
   * <ul>
   * <li>UTF-8
   * <li>UTF-16BE
   * <li>UTF-16LE
   * <li>UTF-32BE
   * <li>UTF-32LE
   * </ul>
   *
   * @return Returns a copy of the input array without the Byte Order Mark
   */
  public static byte[] removeByteOrderMark(final byte[] input) {
    if (input == null) {
      return null;
    }

    int skip = 0;

    // UTF-8
    if (input.length >= 3 && (input[0] == (byte) 0xEF) && (input[1] == (byte) 0xBB) && (input[2] == (byte) 0xBF)) {
      skip = 3;
    }

    // UTF-16BE
    else if (input.length >= 2 && (input[0] == (byte) 0xFE) && (input[1] == (byte) 0xFF)) {
      skip = 2;
    }

    // UTF-16LE
    else if (input.length >= 4 && (input[0] == (byte) 0xFF) && (input[1] == (byte) 0xFE) && (input[2] != (byte) 0x00) && (input[3] != (byte) 0x00)) {
      skip = 2;
    }

    // UTF-32BE
    else if (input.length >= 4 && (input[0] == (byte) 0x00) && (input[1] == (byte) 0x00) && (input[2] == (byte) 0xFE) && (input[3] == (byte) 0xFF)) {
      skip = 4;
    }

    // UTF-32LE
    else if (input.length >= 4 && (input[0] == (byte) 0xFF) && (input[1] == (byte) 0xFE) && (input[2] == (byte) 0x00) && (input[3] == (byte) 0x00)) {
      skip = 4;
    }

    if (skip > 0) {
      return Arrays.copyOfRange(input, skip, input.length);
    }
    return input;
  }
}
