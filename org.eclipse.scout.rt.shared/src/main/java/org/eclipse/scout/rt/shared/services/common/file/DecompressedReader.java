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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecompressedReader extends Reader {
  private static final Logger LOG = LoggerFactory.getLogger(DecompressedReader.class);

  private final RemoteFile m_remoteFile;
  private final ByteArrayInputStream m_buffer;
  private final Inflater m_inflater;
  private final InflaterInputStream m_inflaterInputStream;
  private InputStreamReader m_inputReader;

  public DecompressedReader(RemoteFile f) {
    this(f, StandardCharsets.UTF_8.name());
  }

  public DecompressedReader(RemoteFile f, String charsetName) {
    m_remoteFile = f;
    m_buffer = new ByteArrayInputStream(m_remoteFile.getCompressedData()); // enthält
    // komprimierte
    // daten
    m_inflater = new Inflater();
    m_inflaterInputStream = new InflaterInputStream(m_buffer, m_inflater);
    try {
      m_inputReader = new InputStreamReader(m_inflaterInputStream, charsetName);
    }
    catch (UnsupportedEncodingException e) {
      LOG.warn("unsupporeted encoding '{}'", charsetName, e);
      m_inputReader = new InputStreamReader(m_inflaterInputStream);
    }
  }

  @Override
  public void close() throws IOException {
    m_inputReader.close();
    m_inflaterInputStream.close();
    m_inflater.end();
    m_buffer.close();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return m_inputReader.read(cbuf, off, len);
  }

}
