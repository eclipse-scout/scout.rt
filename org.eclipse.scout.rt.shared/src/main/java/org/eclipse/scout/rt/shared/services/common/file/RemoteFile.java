/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.FileUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * file path with / as delimiter
 */
// content encoded in utf-8 and then compressed!!!

public class RemoteFile implements Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteFile.class);

  private static final int ONE_HUNDRED_KILO_BYTE = 102400;
  public static final long DEFAULT_MAX_BLOCK_SIZE = 20000000; // 20MB
  private static final String DEFAULT_CHARSETNAME = StandardCharsets.UTF_8.name();
  private static final long serialVersionUID = 1L;
  private String m_dir;
  private String m_name;
  private String m_contentType;
  private int m_contentLength = -1;
  private int m_partStartPosition = -1;
  private long m_lastModified = -1;
  private Locale m_locale;
  private boolean m_exists;
  private long m_crc = -1;
  private byte[] m_compressedData;
  private String m_charsetName = DEFAULT_CHARSETNAME;

  public RemoteFile(URL url, boolean ignoreFolders) {
    this(url, ignoreFolders, DEFAULT_CHARSETNAME);
  }

  public RemoteFile(URL url, boolean ignoreFolders, String charsetName) {
    if (charsetName != null) {
      m_charsetName = charsetName;
    }
    if (url != null) {
      String path = url.getPath();
      int lastPart = path.lastIndexOf('/');
      if (lastPart >= 0) {
        m_dir = path.substring(0, lastPart + 1);
        m_name = path.substring(lastPart + 1);
      }
      else {
        m_name = path;
      }
      if (ignoreFolders) {
        m_dir = null;
      }
      m_locale = NlsLocale.get();
      // data
      URLConnection conn;
      try {
        conn = url.openConnection();
        m_lastModified = conn.getLastModified();
        m_contentType = conn.getContentType();
        m_contentLength = conn.getContentLength();
        readData(conn.getInputStream());
        m_exists = true;
      }
      catch (IOException e) { // NOSONAR
        m_exists = false;
      }
    }
    else {
      m_exists = false;
    }
  }

  public RemoteFile(String dir, String name, Locale locale, long lastModified) {
    this(dir, name, locale, lastModified, DEFAULT_CHARSETNAME);
  }

  public RemoteFile(String dir, String name, Locale locale, long lastModified, String charsetName) {
    if (charsetName != null) {
      m_charsetName = charsetName;
    }
    if (StringUtility.hasText(dir)) {
      dir = dir.replace('\\', '/').trim();
      if (!dir.endsWith("/")) {
        dir = dir + "/";
      }
      m_dir = dir;
    }
    m_name = name;
    m_lastModified = lastModified;
    m_locale = locale;
  }

  public RemoteFile(String dir, String name, long lastModified) {
    this(dir, name, lastModified, DEFAULT_CHARSETNAME);
  }

  public RemoteFile(String dir, String name, long lastModified, String charsetName) {
    this(dir, name, null, lastModified, charsetName);
  }

  public RemoteFile(String path, long lastModified) {
    this(path, lastModified, DEFAULT_CHARSETNAME);
  }

  public RemoteFile(String path, long lastModified, String charsetName) {
    if (charsetName != null) {
      m_charsetName = charsetName;
    }
    int i = path.replace('\\', '/').lastIndexOf('/');
    if (i >= 0) {
      m_dir = path.substring(0, i);
      m_name = path.substring(i);
    }
    else {
      m_dir = null;
      m_name = path;
    }
    m_lastModified = lastModified;
  }

  public RemoteFile(BinaryResource res) {
    this(null, res.getFilename(), res.getLastModified());
    setContentType(res.getContentType());
    if (res.getContent() != null) {
      try {
        readData(new ByteArrayInputStream(res.getContent()));
      }
      catch (IOException e) {
        throw new PlatformException("Cannot read binary data", e);
      }
    }
  }

  public BinaryResource toBinaryResource() {
    try {
      return BinaryResources.create()
          .withFilename(getName())
          .withContentType(getContentType())
          .withContent(extractData())
          .withLastModified(getLastModified())
          .build();
    }
    catch (IOException e) {
      throw new PlatformException("Cannot write binary data", e);
    }
  }

  /**
   * @return directory only ending with / or null
   */
  public String getDirectory() {
    return m_dir;
  }

  /**
   * @return name only
   */
  public String getName() {
    return m_name;
  }

  /**
   * @return directory/name
   */
  public String getPath() {
    StringBuilder b = new StringBuilder();
    if (m_dir != null) {
      b.append(m_dir);
    }
    b.append(m_name);
    return b.toString();
  }

  public Locale getLocale() {
    return m_locale;
  }

  public long getLastModified() {
    return m_lastModified;
  }

  public void setLastModified(long l) {
    m_lastModified = l;
  }

  public boolean exists() {
    return m_exists;
  }

  public void setExists(boolean exists) {
    m_exists = exists;
  }

  public String getCharsetName() {
    return m_charsetName;
  }

  public void setCharsetName(String charsetName) {
    if (charsetName != null) {
      m_charsetName = charsetName;
    }
  }

  public long getCRC() {
    return m_crc;
  }

  /**
   * @return true if this is a large file that could not be transfered in one block. Use
   *         {@link IRemoteFileService#getRemoteFilePart(RemoteFile, long)} to get the next block of the large server
   *         file.
   */
  public boolean hasMoreParts() {
    return getContentLength() == RemoteFile.DEFAULT_MAX_BLOCK_SIZE;
  }

  public int getPartStartPosition() {
    return m_partStartPosition;
  }

  public void setPartStartPosition(int position) {
    m_partStartPosition = position;
  }

  public int getContentLength() {
    return m_contentLength;
  }

  public void setContentLength(int len) {
    m_contentLength = len;
  }

  protected byte[] getCompressedData() {
    return m_compressedData;
  }

  protected void setCompressedData(byte[] compressedData, long crc) {
    m_compressedData = compressedData;
    m_crc = crc;
  }

  public String getContentType() {
    return m_contentType;
  }

  public void setContentType(String contentType) {
    m_contentType = contentType;
  }

  public boolean hasContent() {
    return m_compressedData != null;
  }

  public Writer getCompressedWriter() throws IOException {
    return new CompressedWriter(this, m_charsetName);
  }

  public Reader getDecompressedReader() throws IOException {
    return new DecompressedReader(this, m_charsetName);
  }

  public OutputStream getCompressedOutputStream() throws IOException {
    return new CompressedOutputStream(this);
  }

  public InputStream getDecompressedInputStream() throws IOException {
    return new DecompressedInputStream(this);
  }

  @SuppressWarnings("resource")
  public long/* crc */ writeData(File f) throws IOException {
    return writeData(new FileOutputStream(f));
  }

  public long/* crc */ writeData(Writer w) throws IOException {
    Reader in = null;
    BufferedWriter out = null;
    try {
      in = getDecompressedReader();
      out = new BufferedWriter(w);
      char[] b = new char[ONE_HUNDRED_KILO_BYTE];
      int len;
      while ((len = in.read(b)) > 0) {
        out.write(b, 0, len);
      }
      out.flush();
    }
    finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
    return getCRC();
  }

  /**
   * Write data and close stream
   *
   * @param os
   * @return
   * @throws IOException
   */
  public long/* crc */ writeData(OutputStream os) throws IOException {
    InputStream in = null;
    BufferedOutputStream out = null;
    try {
      in = getDecompressedInputStream();
      out = new BufferedOutputStream(os);
      byte[] b = new byte[ONE_HUNDRED_KILO_BYTE];
      int len;
      while ((len = in.read(b)) > 0) {
        out.write(b, 0, len);
      }
      out.flush();
    }
    finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
    return getCRC();
  }

  /**
   * Read data from file
   *
   * @param f
   * @return
   * @throws IOException
   */
  @SuppressWarnings("resource")
  public long/* crc */ readData(File f) throws IOException {
    return readData(new FileInputStream(f));
  }

  /**
   * Read data and close stream
   *
   * @param f
   * @return
   * @throws IOException
   */
  public long/* crc */ readData(Reader r) throws IOException {
    Writer out = null;
    BufferedReader in = null;
    try {
      in = new BufferedReader(r);
      out = getCompressedWriter();
      char[] b = new char[ONE_HUNDRED_KILO_BYTE];
      int len;
      while ((len = in.read(b)) > 0) {
        out.write(b, 0, len);
      }
      out.flush();
      m_exists = true;
    }
    finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
    return getCRC();
  }

  /**
   * Read data and close stream
   *
   * @param is
   * @return
   * @throws IOException
   */
  public long/* crc */ readData(InputStream is) throws IOException {
    return readData(is, 0, -1);
  }

  /**
   * Read data and close stream
   *
   * @param is
   * @param startPosition
   * @param maxReadSize
   * @return
   * @throws IOException
   */
  public long/* crc */ readData(InputStream is, long startPosition, long maxReadSize) throws IOException {
    OutputStream out = null;
    BufferedInputStream in = null;
    setPartStartPosition((int) startPosition);
    long readSize = 0;
    try {
      long skippedBytes = is.skip(startPosition);
      if (skippedBytes != startPosition) {
        LOG.warn("Skipped less bytes than requested [requested={}, skipped={}]", startPosition, skippedBytes);
      }
      in = new BufferedInputStream(is);
      out = getCompressedOutputStream();
      int bufferSize = ONE_HUNDRED_KILO_BYTE;
      byte[] b = new byte[bufferSize];
      int len;
      int maxBufferBoundary;
      while ((readSize < maxReadSize || maxReadSize == -1) && (len = in.read(b)) > 0) {
        maxBufferBoundary = len;
        if (maxReadSize > -1 && readSize + len > maxReadSize) {
          maxBufferBoundary = (int) (maxReadSize - readSize);
        }
        if (maxBufferBoundary < len) {
          len = maxBufferBoundary;
        }
        out.write(b, 0, len);
        readSize += len;
      }
      out.flush();
      m_exists = true;
    }
    finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
    return getCRC();
  }

  public byte[] extractData() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(m_compressedData.length * 3);
    writeData(bos);
    return bos.toByteArray();
  }

  /**
   * If the remote file is a zip archive, unpack its content to the directory see
   * {@link #readZipContentFromDirectory(File)}
   */
  public void writeZipContentToDirectory(File directory) throws IOException {
    directory.mkdirs();
    File tmp = File.createTempFile("tmp", ".zip");
    writeData(tmp);
    FileUtility.extractArchive(tmp, directory);
  }

  /**
   * Read all files from the directory and pack them as zip, so this remote file represents a zip archive see
   * {@link #writeZipContentToDirectory(File)}
   */
  public void readZipContentFromDirectory(File directory) throws IOException {
    File tmp = File.createTempFile("tmp", ".zip");
    FileUtility.compressArchive(directory, tmp);
    readData(tmp);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[dir=" + m_dir + ", name=" + m_name + ", lastModified=" + m_lastModified + "]";
  }

  /**
   * @ince 2.7
   */
  public void setContentTypeByExtension(String ext) {
    setContentType(getContentTypeForExtension(ext));
  }

  /**
   * Static extension to mimetype mapper
   *
   * @since 2.7
   */
  private static final Map<String, String> FILE_EXTENSION_TO_MIME_TYPE_MAP;

  static {
    FILE_EXTENSION_TO_MIME_TYPE_MAP = new HashMap<String, String>();
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ai", "application/postscript");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("aif", "audio/x-aiff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("aifc", "audio/x-aiff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("aiff", "audio/x-aiff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("asc", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("au", "audio/basic");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("avi", "video/x-msvideo");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("bcpio", "application/x-bcpio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("bin", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("c", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("cc", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ccad", "application/clariscad");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("cdf", "application/x-netcdf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("class", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("cpio", "application/x-cpio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("cpt", "application/mac-compactpro");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("csh", "application/x-csh");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("css", "text/css");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dcr", "application/x-director");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dir", "application/x-director");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dms", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("doc", "application/msword");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("drw", "application/drafting");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dvi", "application/x-dvi");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dwg", "application/acad");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dxf", "application/dxf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("dxr", "application/x-director");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("eps", "application/postscript");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("etx", "text/x-setext");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("exe", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ez", "application/andrew-inset");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("f", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("f90", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("fli", "video/x-fli");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("gif", "image/gif");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("gtar", "application/x-gtar");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("gz", "application/x-gzip");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("h", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("hdf", "application/x-hdf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("hh", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("hqx", "application/mac-binhex40");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("htm", "text/html");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("html", "text/html");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ice", "x-conference/x-cooltalk");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ief", "image/ief");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("iges", "model/iges");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("igs", "model/iges");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ips", "application/x-ipscript");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ipx", "application/x-ipix");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("jpe", "image/jpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("jpeg", "image/jpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("jpg", "image/jpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("js", "application/x-javascript");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("kar", "audio/midi");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("latex", "application/x-latex");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("lha", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("lsp", "application/x-lisp");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("lzh", "application/octet-stream");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("m", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("man", "application/x-troff-man");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("me", "application/x-troff-me");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mesh", "model/mesh");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mid", "audio/midi");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("midi", "audio/midi");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mif", "application/vnd.mif");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mime", "www/mime");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mov", "video/quicktime");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("movie", "video/x-sgi-movie");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mp2", "audio/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mp3", "audio/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mpe", "video/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mpeg", "video/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mpg", "video/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("mpga", "audio/mpeg");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ms", "application/x-troff-ms");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("msh", "model/mesh");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("msi", "application/x-msi");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("nc", "application/x-netcdf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("oda", "application/oda");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pbm", "image/x-portable-bitmap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pdb", "chemical/x-pdb");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pdf", "application/pdf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pgm", "image/x-portable-graymap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pgn", "application/x-chess-pgn");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("png", "image/png");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pnm", "image/x-portable-anymap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pot", "application/mspowerpoint");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ppm", "image/x-portable-pixmap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pps", "application/mspowerpoint");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ppt", "application/mspowerpoint");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ppz", "application/mspowerpoint");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("pre", "application/x-freelance");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("prt", "application/pro_eng");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ps", "application/postscript");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("qt", "video/quicktime");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ra", "audio/x-realaudio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ram", "audio/x-pn-realaudio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ras", "image/cmu-raster");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("rgb", "image/x-rgb");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("rm", "audio/x-pn-realaudio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("roff", "application/x-troff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("rpm", "audio/x-pn-realaudio-plugin");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("rtf", "text/rtf");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("rtx", "text/richtext");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("scm", "application/x-lotusscreencam");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("set", "application/set");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sgm", "text/sgml");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sgml", "text/sgml");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sh", "application/x-sh");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("shar", "application/x-shar");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("silo", "model/mesh");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sit", "application/x-stuffit");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("skd", "application/x-koan");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("skm", "application/x-koan");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("skp", "application/x-koan");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("skt", "application/x-koan");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("smi", "application/smil");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("smil", "application/smil");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("snd", "audio/basic");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sol", "application/solids");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("spl", "application/x-futuresplash");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("src", "application/x-wais-source");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("step", "application/STEP");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("stl", "application/SLA");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("stp", "application/STEP");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sv4cpio", "application/x-sv4cpio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("sv4crc", "application/x-sv4crc");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("swf", "application/x-shockwave-flash");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("t", "application/x-troff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tar", "application/x-tar");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tcl", "application/x-tcl");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tex", "application/x-tex");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("texi", "application/x-texinfo");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("texinfo", "application/x-texinfo");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tif", "image/tiff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tiff", "image/tiff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tr", "application/x-troff");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tsi", "audio/TSP-audio");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tsp", "application/dsptype");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("tsv", "text/tab-separated-values");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("txt", "text/plain");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("unv", "application/i-deas");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("ustar", "application/x-ustar");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("vcd", "application/x-cdlink");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("vda", "application/vda");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("viv", "video/vnd.vivo");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("vivo", "video/vnd.vivo");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("vrml", "model/vrml");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("wav", "audio/x-wav");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("wrl", "model/vrml");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xbm", "image/x-xbitmap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlc", "application/vnd.ms-excel");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xll", "application/vnd.ms-excel");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlm", "application/vnd.ms-excel");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xlw", "application/vnd.ms-excel");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xml", "text/xml");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xpm", "image/x-xpixmap");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xwd", "image/x-xwindowdump");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("xyz", "chemical/x-pdb");
    FILE_EXTENSION_TO_MIME_TYPE_MAP.put("zip", "application/zip");
  }

  /**
   * @since 2.7
   */
  public static String getContentTypeForExtension(String ext) {
    if (ext.startsWith(".")) {
      ext = ext.substring(1);
    }
    ext = ext.toLowerCase();
    return FILE_EXTENSION_TO_MIME_TYPE_MAP.get(ext);
  }
}
