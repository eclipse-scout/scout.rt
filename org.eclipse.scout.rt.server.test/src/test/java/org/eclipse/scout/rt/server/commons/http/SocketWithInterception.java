/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.http;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWithInterception extends Socket {
  public interface ISocketReadInterceptor {
    int read(InputStream in) throws IOException;

    int read(InputStream in, byte[] b, int off, int len) throws IOException;
  }

  public interface ISocketWriteInterceptor {
    void write(OutputStream out, int b) throws IOException;

    void write(OutputStream out, byte[] b, int off, int len) throws IOException;
  }

  private ISocketReadInterceptor m_readInterceptor = null;
  private ISocketWriteInterceptor m_writeInterceptor = null;

  public SocketWithInterception withInterceptRead(ISocketReadInterceptor i) {
    m_readInterceptor = i;
    return this;
  }

  public SocketWithInterception withInterceptWrite(ISocketWriteInterceptor i) {
    m_writeInterceptor = i;
    return this;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return new FilterInputStream(super.getInputStream()) {
      @Override
      public int read() throws IOException {
        if (m_readInterceptor != null) {
          return m_readInterceptor.read(in);
        }
        else {
          return in.read();
        }
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
        if (m_readInterceptor != null) {
          return m_readInterceptor.read(in, b, off, len);
        }
        else {
          return in.read(b, off, len);
        }
      }
    };
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new FilterOutputStream(super.getOutputStream()) {
      @Override
      public void write(int b) throws IOException {
        if (m_writeInterceptor != null) {
          m_writeInterceptor.write(out, b);
        }
        else {
          out.write(b);
        }
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        if (m_writeInterceptor != null) {
          m_writeInterceptor.write(out, b, off, len);
        }
        else {
          out.write(b, off, len);
        }
      }
    };
  }
}
