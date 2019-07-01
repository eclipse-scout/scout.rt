/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.http;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWithInterception extends Socket {
  @FunctionalInterface
  public interface IStreamInterceptor {
    int intercept(int b) throws IOException;
  }

  private IStreamInterceptor m_readInterceptor = b -> b;
  private IStreamInterceptor m_writeInterceptor = b -> b;

  public SocketWithInterception withInterceptRead(IStreamInterceptor i) throws IOException {
    if (i != null) {
      m_readInterceptor = i;
    }
    return this;
  }

  public SocketWithInterception withInterceptWrite(IStreamInterceptor i) throws IOException {
    if (i != null) {
      m_writeInterceptor = i;
    }
    return this;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FilterInputStream(super.getInputStream()) {
      private boolean m_eof;

      @Override
      public int read() throws IOException {
        if (m_eof) {
          return -1;
        }
        int i = in.read();
        if (i >= 0) {
          i = m_readInterceptor.intercept(i);
        }
        if (i < 0) {
          m_eof = true;
        }
        return i;
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        if (m_eof) {
          return -1;
        }
        int n = in.read(b, off, len);
        if (n < 0) {
          m_eof = true;
        }
        for (int k = 0; k < n; k++) {
          int i = ((int) b[off + k]) & 0xff;
          i = m_readInterceptor.intercept(i);
          if (i < 0) {
            //fixture EOF
            m_eof = true;
            return k > 0 ? k : -1;
          }
          b[off + k] = (byte) i;
        }
        return n;
      }
    };
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new FilterOutputStream(super.getOutputStream()) {
      @Override
      public void write(int b) throws IOException {
        b = ((int) m_writeInterceptor.intercept((byte) b)) & 0xff;
        out.write(b);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        for (int k = 0; k < len; k++) {
          int i = ((int) b[off + k]) & 0xff;
          i = m_readInterceptor.intercept(i);
          b[off + k] = (byte) i;
        }
        out.write(b, off, len);
      }
    };
  }
}
