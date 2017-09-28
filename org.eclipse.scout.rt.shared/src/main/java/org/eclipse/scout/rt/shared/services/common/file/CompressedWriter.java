/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.file;

/**
 * @version 3.x
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class CompressedWriter extends Writer {
  private static final String DEFAULT_CHARSETNAME = StandardCharsets.UTF_8.name();
  private final RemoteFile m_remoteFile;
  private final ByteArrayOutputStream m_buffer;
  private final Deflater m_deflater;
  private final DeflaterOutputStream m_deflaterOutputStream;
  private final CRC32 m_crc32;
  private String m_charsetName = DEFAULT_CHARSETNAME;

  public CompressedWriter(RemoteFile f) {
    this(f, DEFAULT_CHARSETNAME);
  }

  public CompressedWriter(RemoteFile f, String charsetName) {
    m_remoteFile = f;
    m_charsetName = charsetName;
    m_buffer = new ByteArrayOutputStream();
    m_deflater = new Deflater(Deflater.BEST_COMPRESSION);
    m_deflaterOutputStream = new DeflaterOutputStream(m_buffer, m_deflater);
    m_crc32 = new CRC32();
  }

  @Override
  public void close() throws IOException {
    flush();
    m_deflater.finish();
    m_deflaterOutputStream.finish();
    m_deflaterOutputStream.close();
    m_deflater.end();
    m_buffer.close();
    m_remoteFile.setCompressedData(m_buffer.toByteArray(), m_crc32.getValue());
  }

  @Override
  public void flush() throws IOException {
    m_deflaterOutputStream.flush();
    m_buffer.flush();
  }

  @Override
  // written chars (to m_buffer) encoded in Charset.defaultCharset()
  public void write(char[] cbuf, int off, int len) throws IOException { // schreibt
    // chars
    // nach
    // m_buffer
    // (in
    // remote_file)
    String str = new String(cbuf, off, len);
    byte[] b = str.getBytes(m_charsetName);
    m_crc32.update(b, 0, b.length);
    m_deflaterOutputStream.write(b, 0, b.length);
    flush();
  }
}
