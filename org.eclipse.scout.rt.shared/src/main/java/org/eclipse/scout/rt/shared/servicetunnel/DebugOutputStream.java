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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Serialization override<br>
 * used partly to make Date's timezone independent using StaticDate class
 */
public class DebugOutputStream extends OutputStream {
  private final OutputStream m_out;
  private final ByteArrayOutputStream m_debugOut;
  private int m_lastWrittenCharacter;
  private Throwable m_lastThrownException;

  public DebugOutputStream(OutputStream out) {
    m_out = out;
    m_debugOut = new ByteArrayOutputStream();
  }

  public String getContent(String encoding) throws UnsupportedEncodingException {
    return new String(m_debugOut.toByteArray(), encoding);
  }

  @Override
  public void write(int b) throws IOException {
    m_lastWrittenCharacter = b;
    try {
      m_out.write(m_lastWrittenCharacter);
      // debug
      m_debugOut.write((char) m_lastWrittenCharacter);
    }
    catch (IOException ioe) {
      m_lastThrownException = ioe;
    }
  }

  @Override
  public void flush() throws IOException {
    m_out.flush();
  }

  @Override
  public void close() throws IOException {
    m_out.close();
    m_debugOut.close();
  }

  public int getLastWrittenCharacter() {
    return m_lastWrittenCharacter;
  }

  public Throwable getLastThrownException() {
    return m_lastThrownException;
  }
}
