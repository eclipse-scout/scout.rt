/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.migration.ecma6.model.old;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

public class LineReaderWithPositionTracking implements AutoCloseable {
  private final Object lock = new Object();
  private PushbackReader in;
  private int m_startOfLine;
  private int m_endOfLine;
  private String m_eol;
  private boolean m_eof;
  private int m_pos;

  public LineReaderWithPositionTracking(Reader in) {
    this.in = new PushbackReader(in, 2);
  }

  public int getStartOfLine() {
    return m_startOfLine;
  }

  public int getEndOfLine() {
    return m_endOfLine;
  }

  public String getDetectedEOL() {
    return m_eol == null ? "\n" : m_eol;
  }

  public String readLine() throws IOException {
    m_startOfLine = m_pos;
    m_endOfLine = m_pos;
    if (m_eof) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    int ch = -1;
    while (true) {
      ch = in.read();
      if (ch < 0) {
        m_eof = true;
        if (buf.length() == 0) {
          return null;
        }
        else {
          //eof=eol
          break;
        }
      }
      m_pos++;
      if (ch == '\n' || ch == '\r') {
        //eol
        break;
      }
      buf.append((char) ch);
    }
    if (ch == '\n' || ch == '\r') {
      m_endOfLine = m_pos - 1;
      m_eol = String.valueOf((char) ch);
      if (ch == '\r') {
        int ch2 = in.read();
        if (ch2 == '\n') {
          m_pos++;
          m_eol += (char) ch2;
        }
        else {
          in.unread(ch2);
        }
      }
    }
    else {
      m_endOfLine = m_pos;
    }
    return buf.toString();
  }

  @Override
  public void close() throws IOException {
    synchronized (lock) {
      if (in == null)
        return;
      try {
        in.close();
      }
      finally {
        in = null;
      }
    }
  }
}
