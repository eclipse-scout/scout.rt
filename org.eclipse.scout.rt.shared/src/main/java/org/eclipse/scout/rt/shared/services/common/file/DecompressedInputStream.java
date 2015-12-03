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

/**
 * @version 3.x
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DecompressedInputStream extends InputStream {
  private RemoteFile m_remoteFile;
  private ByteArrayInputStream m_buffer;
  private Inflater m_inflater;
  private InflaterInputStream m_inflaterInputStream;

  public DecompressedInputStream(RemoteFile f) throws IOException {
    if (f.getCompressedData() == null) {
      throw new IOException("" + f + " has no content");
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
