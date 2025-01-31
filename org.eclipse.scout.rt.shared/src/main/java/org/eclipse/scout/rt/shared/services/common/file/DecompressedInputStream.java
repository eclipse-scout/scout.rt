/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.file;

/**
 * @version 3.x
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DecompressedInputStream extends InputStream {
  private final RemoteFile m_remoteFile;
  private final ByteArrayInputStream m_buffer;
  private final Inflater m_inflater;
  private final InflaterInputStream m_inflaterInputStream;

  public DecompressedInputStream(RemoteFile f) throws IOException {
    if (f.getCompressedData() == null) {
      throw new IOException(f + " has no content");
    }
    m_remoteFile = f;
    m_buffer = new ByteArrayInputStream(m_remoteFile.getCompressedData());
    m_inflater = new Inflater();
    m_inflaterInputStream = new InflaterInputStream(m_buffer, m_inflater);
  }

  @Override
  public void close() throws IOException {
    m_inflaterInputStream.close();
    m_inflater.end();
    m_buffer.close();
  }

  @Override
  public int read() throws IOException {
    return m_inflaterInputStream.read();
  }

  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    return m_inflaterInputStream.read(buf, off, len);
  }
}
