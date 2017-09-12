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
package org.eclipse.scout.rt.shared.servicetunnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Serialization override<br>
 * used partly to make Date's timezone independent using StaticDate class
 */
public class DebugInputStream extends InputStream {
  private final InputStream m_in;
  private final ByteArrayOutputStream m_debugOut;
  private int m_lastReadCharacter;
  private Throwable m_lastThrownException;

  public DebugInputStream(InputStream in) {
    m_in = in;
    m_debugOut = new ByteArrayOutputStream();
  }

  public String getContent(String encoding) throws UnsupportedEncodingException {
    return new String(m_debugOut.toByteArray(), encoding);
  }

  @Override
  public int available() throws IOException {
    return m_in.available();
  }

  @Override
  public void close() throws IOException {
    m_in.close();
    m_debugOut.close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    m_in.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return m_in.markSupported();
  }

  @Override
  public int read() throws IOException {
    try {
      m_lastReadCharacter = m_in.read();
      m_debugOut.write((char) m_lastReadCharacter);
      return m_lastReadCharacter;
    }
    catch (IOException ioe) {
      m_lastThrownException = ioe;
      throw ioe;
    }
  }

  @Override
  public synchronized void reset() throws IOException {
    m_in.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return m_in.skip(n);
  }

  public int getLastReadCharacter() {
    return m_lastReadCharacter;
  }

  public Throwable getLastThrownException() {
    return m_lastThrownException;
  }

}
