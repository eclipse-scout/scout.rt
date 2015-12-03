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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IOUtility {
  private static final Logger LOG = LoggerFactory.getLogger(IOUtility.class);

  private IOUtility() {
  }

  /**
   * retrieve content as raw bytes
   */
  public static byte[] getContent(InputStream stream) {
    return getContent(stream, true);
  }

  /**
   * Gets the content of the given {@link InputStream} as {@link String}. The {@link Byte}s coming from the
   * {@link InputStream} are interpreted using the given charsetName.<br>
   * The {@link InputStream} is automatically closed.
   *
   * @param stream
   *          The stream to read from.
   * @param charsetName
   *          The name of the {@link Charset}.
   * @return A {@link String} containing the content of the given {@link InputStream}.
   */
  public static String getContent(InputStream stream, String charsetName) {
    try {
      return new String(IOUtility.getContent(stream), charsetName);
    }
    catch (UnsupportedEncodingException e) {
      throw new ProcessingException("Unsupported encoding: '" + charsetName + "'.", e);
    }
  }

  /**
   * Gets the content of the given {@link InputStream} as {@link String}. The content coming from the
   * {@link InputStream} must have the UTF-8 {@link Charset}.
   *
   * @param stream
   *          The {@link InputStream} to read from.
   * @return The content of the given {@link InputStream}.
   */
  public static String getContentUtf8(InputStream stream) {
    return getContent(stream, StandardCharsets.UTF_8.name());
  }

  public static byte[] getContent(InputStream in, int len) throws IOException {
    if (len >= 0) {
      byte[] buf = new byte[len];
      int count = 0;
      while (count < len) {
        count += in.read(buf, count, len - count);
      }
      return buf;
    }
    else {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int b;
      while ((b = in.read()) >= 0) {
        out.write(b);
      }
      return out.toByteArray();
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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(b)))) {
      int val;
      while ((val = in.read()) >= 0) {
        out.write(val);
      }
    }
    return out.toByteArray();
  }

  public static byte[] readFromUrl(URL url) throws IOException {
    URLConnection uc = url.openConnection();
    int len = uc.getContentLength();
    if (len >= 0) {
      try (InputStream in = uc.getInputStream()) {
        return getContent(in, len);
      }
    }
    else {
      try (BufferedInputStream in = new BufferedInputStream(uc.getInputStream())) {
        return getContent(in, -1);
      }
    }
  }

  public static byte[] getContent(InputStream stream, boolean autoClose) {
    BufferedInputStream in = null;
    try {
      in = new BufferedInputStream(stream);
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] b = new byte[10240];
      int len;
      while ((len = in.read(b)) > 0) {
        buffer.write(b, 0, len);
      }
      buffer.close();
      byte[] data = buffer.toByteArray();
      return data;
    }
    catch (IOException e) {
      throw new ProcessingException("input: " + stream, e);
    }
    finally {
      try {
        if (autoClose && in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + stream, e);
      }
    }
  }

  public static byte[] getContent(File file) {
    try {
      return getContent(new FileInputStream(file), true);
    }
    catch (FileNotFoundException e) {
      throw new ProcessingException("filename: " + file.getAbsolutePath(), e);
    }
  }

  public static byte[] getContent(String filename) {
    return getContent(toFile(filename));
  }

  /**
   * Reads the content of a file in the specified encoding (charset-name) e.g. "UTF-8" If no encoding is provided, the
   * system default encoding is used
   */
  public static String getContentInEncoding(String filepath, String encoding) {
    try {
      FileInputStream in = null;
      String content = null;
      try {
        in = new FileInputStream(filepath);
        if (StringUtility.hasText(encoding)) {
          content = getContent(new InputStreamReader(in, encoding));
        }
        else {
          content = getContent(new InputStreamReader(in));
        }
      }
      finally {
        if (in != null) {
          in.close();
        }
      }
      return content;
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * write content as raw bytes
   */
  public static void writeContent(OutputStream stream, byte[] data) {
    writeContent(stream, data, true);
  }

  public static void writeContent(OutputStream stream, byte[] data, boolean autoClose) {
    BufferedOutputStream out = null;
    try {
      out = new BufferedOutputStream(stream);
      out.write(data);
    }
    catch (IOException e) {
      throw new ProcessingException("output: " + stream, e);
    }
    finally {
      try {
        if (autoClose && out != null) {
          out.close();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("output: " + stream, e);
      }
    }
  }

  public static void writeContent(String filename, Object o) {
    File f = toFile(filename);
    try {
      if (o instanceof byte[]) {
        writeContent(new FileOutputStream(f), (byte[]) o);
      }
      else if (o instanceof char[]) {
        writeContent(new FileWriter(f), new String((char[]) o));
      }
      else if (o != null) {
        writeContent(new FileWriter(f), o.toString());
      }
    }
    catch (FileNotFoundException n) {
      throw new ProcessingException("filename: " + filename, n);
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + filename, e);
    }
  }

  public static void writeContent(Writer stream, String text) {
    writeContent(stream, text, true);
  }

  public static void writeContent(Writer stream, String text, boolean autoClose) {
    try {
      stream.write(text);
    }
    catch (IOException e) {
      throw new ProcessingException("output: " + stream, e);
    }
    finally {
      try {
        if (autoClose && stream != null) {
          stream.close();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("output: " + stream, e);
      }

    }
  }

  /**
   * retrieve content as string (correct character conversion)
   */
  public static String getContent(Reader stream) {
    return getContent(stream, true);
  }

  public static String getContent(Reader stream, boolean autoClose) {
    BufferedReader in = null;
    try {
      in = new BufferedReader(stream);
      StringWriter buffer = new StringWriter();
      char[] b = new char[10240];
      int len;
      while ((len = in.read(b)) > 0) {
        buffer.write(b, 0, len);
      }
      buffer.close();
      return buffer.toString();
    }
    catch (IOException e) {
      throw new ProcessingException("input: " + stream, e);
    }

    finally {
      try {
        if (autoClose && in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        throw new ProcessingException("input: " + stream, e);
      }
    }
  }

  /**
   * Directory browsing including subtree
   */
  public static File[] listFilesInSubtree(File dir, FileFilter filter) {
    ArrayList<File> list = new ArrayList<File>();
    listFilesRec(dir, filter, list);
    return list.toArray(new File[list.size()]);
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
   * creates a temporary directory with a random name and the given suffix
   */
  public static File createTempDirectory(String dirSuffix) {
    try {
      if (dirSuffix != null) {
        dirSuffix = dirSuffix.replaceAll("[:*?\\\"<>|]*", "");
      }
      File tmp = File.createTempFile("dir", dirSuffix);
      tmp.delete();
      tmp.mkdirs();
      tmp.deleteOnExit();
      return tmp;
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
   *          If no or an empty filename is given, a random fileName will be created.
   * @param content
   *          If no content is given, an empty file will be created
   * @return A new temporary file with specified content
   */
  public static File createTempFile(String fileName, byte[] content) {
    try {
      if (fileName != null) {
        fileName = fileName.replaceAll("[\\\\/:*?\\\"<>|]*", "");
      }
      if (fileName == null || fileName.length() == 0) {
        fileName = getTempFileName(".tmp");
      }
      File f = File.createTempFile("tmp", ".tmp");
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
        writeContent(new FileOutputStream(f2), content);
      }
      else {
        f2.createNewFile();
      }
      return f2;
    }
    catch (IOException e) {
      throw new ProcessingException("filename: " + fileName, e);
    }
  }

  /**
   * Creates a temp file from an input stream.
   *
   * @param content
   * @param filename
   * @param extension
   * @return new file
   * @throws ProcessingException
   */
  public static File createTempFile(InputStream content, String filename, String extension) throws ProcessingException {
    File temp = null;
    FileOutputStream fo = null;
    try {
      temp = File.createTempFile(filename, extension);
      fo = new FileOutputStream(temp);
      IOUtility.writeContent(fo, IOUtility.getContent(content));
    }
    catch (IOException e) {
      throw new ProcessingException("Error creating temp file", e);
    }
    finally {
      if (fo != null) {
        try {
          fo.close();
        }
        catch (IOException e) {
          // nop
        }
      }
    }
    return temp;
  }

  /**
   * Convenience method for creating temporary files with content. Note, the temporary file will be automatically
   * deleted when the virtual machine terminates. The temporary file will look like this: <i>prefix</i>2093483323922923
   * <i>.suffix</i>
   *
   * @param prefix
   *          The prefix of the temporary file
   * @param suffix
   *          The suffix of the temporary file. Don't forget the colon, for example <b>.tmp</b>
   * @param content
   * @return A new temporary file with the specified content
   */
  public static File createTempFile(String prefix, String suffix, byte[] content) {
    File f = null;
    try {
      f = File.createTempFile(prefix, suffix);
      f.deleteOnExit();
      if (content != null) {
        writeContent(new FileOutputStream(f), content);
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
   * @param directory
   * @return true if the directory is successfully deleted or does not exists; false otherwise
   * @throws SecurityException
   *           - If a security manager exists and its check methods deny read or delete access
   */
  public static boolean deleteDirectory(File dir) {
    if (dir != null && dir.exists()) {
      File[] a = dir.listFiles();
      for (int i = 0; a != null && i < a.length; i++) {
        if (a[i].isDirectory()) {
          deleteDirectory(a[i]);
        }
        else {
          a[i].delete();
        }
      }
      return dir.delete();
    }
    return true;
  }

  public static boolean deleteDirectory(String dir) {
    File f = toFile(dir);
    if (f.exists()) {
      return deleteDirectory(f);
    }
    else {
      return false;
    }
  }

  public static boolean createDirectory(String dir) {
    if (dir != null) {
      dir = dir.replaceAll("[*?\\\"<>|]*", "");
      File f = toFile(dir);
      return f.mkdirs();
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
    else {
      File f = toFile(filepath);
      return getFileSize(f);
    }
  }

  public static long getFileSize(File filepath) {
    if (filepath == null) {
      return 0;
    }
    else {
      if (filepath.exists()) {
        return filepath.length();
      }
      else {
        return 0;
      }
    }
  }

  public static long getFileLastModified(String filepath) {
    if (filepath == null) {
      return 0;
    }
    else {
      File f = toFile(filepath);
      if (f.exists()) {
        return f.lastModified();
      }
      else {
        return 0;
      }
    }
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
    else {
      return new File(s.replace('\\', File.separatorChar).replace('/', File.separatorChar));
    }
  }

  /**
   * @return the extension of the file
   */
  public static String getFileExtension(String filename) {
    if (filename == null) {
      return null;
    }
    int i = filename.lastIndexOf('.');
    if (i < 0) {
      return null;
    }
    else {
      return filename.substring(i + 1);
    }
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
   *          the URL string which shall be encoded
   * @return the encoded URL string
   */
  public static String urlEncode(String url) {
    if (url == null) {
      return null;
    }

    String s = url.toString().trim();
    if (s.length() == 0) {
      return "";
    }

    try {
      s = URLEncoder.encode(s, StandardCharsets.UTF_8.name());
      s = StringUtility.replace(s, "+", "%20");
    }
    catch (UnsupportedEncodingException e) {
      LOG.error("Unsupported encoding", e);
    }
    return s;
  }

  /**
   * a null-safe variant for calling {@link URLDecoder#decode(String, String)}. This method returns null if the given
   * <code>url</code> is null or an empty string respectively. Any leading / trailing whitespaces are omitted.
   *
   * @param encodedUrl
   *          the encoded URL string which shall be decoded
   * @return the decoded URL string
   */
  public static String urlDecode(String encodedUrl) {
    if (encodedUrl == null) {
      return null;
    }

    String s = encodedUrl.toString().trim();
    if (s.length() == 0) {
      return "";
    }

    try {
      s = URLDecoder.decode(s, StandardCharsets.UTF_8.name());
    }
    catch (UnsupportedEncodingException e) {
      LOG.error("Unsupported encoding", e);
    }
    return s;
  }

  /**
   * The text passed to this method is tried to wellform as an URL. If the text can not be transformed into an URL the
   * method returns null.
   *
   * @param urlText
   */
  public static URL urlTextToUrl(String urlText) {
    String text = urlText;
    URL url = null;
    if (text != null && text.length() > 0) {
      try {
        url = new URL(text);
      }
      catch (Exception e) {
        if (text.contains("@")) {
          text = "mailto:" + text;
        }
        else {
          text = "http://" + text;
        }
        try {
          url = new URL(text);
        }
        catch (Exception e1) {
          LOG.debug("Could not create url from : " + text + ":" + e1);
        }
      }
    }
    return url;
  }

  /**
   * Append a file to another. The provided {@link PrintWriter} will neither be flushed nor closed.
   * <p>
   * ATTENTION: Appending a file to itself using an autoflushing PrintWriter, will lead to an endless loop. (Appending a
   * file to itself using a Printwriter without autoflushing is safe.)
   *
   * @param writer
   *          a PrintWriter for the destination file
   * @param file
   *          source file
   * @throws ProcessingException
   *           if an {@link IOException} occurs (e.g. if file does not exists)
   */
  public static void appendFile(PrintWriter writer, File file) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null) {
        writer.println(line);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Error appending file: " + file.getName(), e);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }

  }

  /**
   * @param file
   * @param charsetName
   *          The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
   * @return List containing all lines of the file as Strings
   * @throws ProcessingException
   *           if an {@link IOException} occurs (e.g. if file does not exists)
   */
  public static List<String> readLines(File file, String charsetName) {
    ArrayList<String> lines;
    lines = new ArrayList<String>();
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        lines.add(line);
      }
    }
    catch (IOException e) {
      throw new ProcessingException("Error reading config file.", e);
    }
    finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        }
        catch (IOException e) {
          // ignore
        }
      }
    }
    return lines;
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
