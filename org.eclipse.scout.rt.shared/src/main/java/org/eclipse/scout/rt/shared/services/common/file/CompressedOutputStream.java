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
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class CompressedOutputStream extends OutputStream {
  private final RemoteFile m_remoteFile;
  private final ByteArrayOutputStream m_buffer;
  private final Deflater m_deflater;
  private final DeflaterOutputStream m_deflaterOutputStream;
  private final CRC32 m_crc32;

  public CompressedOutputStream(RemoteFile f) {
    m_remoteFile = f;
    m_buffer = new ByteArrayOutputStream();
    m_deflater = new Deflater(Deflater.BEST_COMPRESSION);
    m_deflaterOutputStream = new DeflaterOutputStream(m_buffer, m_deflater); // schreibt
    // die
    // komprimierten
    // Daten
    // in
    // den
    // Stream
    // m_buffer
    m_crc32 = new CRC32();
  }

  @Override
  public void close() throws IOException {
    flush();
    m_deflater.finish();
    m_deflaterOutputStream.finish();
    m_deflater.end();
    m_deflaterOutputStream.close();
    m_buffer.close();
    m_remoteFile.setCompressedData(m_buffer.toByteArray(), m_crc32.getValue());
  }

  @Override
  public void flush() throws IOException {
    m_deflaterOutputStream.flush();
    m_buffer.flush();
  }

  @Override
  public void write(int b) throws IOException {
    m_crc32.update(b);
    m_deflaterOutputStream.write(b);
  }

  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    // wandle bytes in String um mit richtiger codierung/charset
    m_crc32.update(buf, off, len);
    m_deflaterOutputStream.write(buf, off, len);
    flush();
  }
}
