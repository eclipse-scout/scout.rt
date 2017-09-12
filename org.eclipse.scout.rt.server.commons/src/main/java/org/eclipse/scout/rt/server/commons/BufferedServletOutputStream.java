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
package org.eclipse.scout.rt.server.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.eclipse.scout.rt.platform.util.Assertions;

public class BufferedServletOutputStream extends ServletOutputStream {

  private final ByteArrayOutputStream m_buf;
  private volatile Object m_writeListener;

  public BufferedServletOutputStream() {
    m_buf = new ByteArrayOutputStream();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener(WriteListener writeListener) {
    m_writeListener = Assertions.assertNotNull(writeListener);
    try {
      writeListener.onWritePossible();
    }
    catch (IOException e) {
      writeListener.onError(e);
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    try {
      m_buf.write(b);
    }
    catch (IOException e) {
      WriteListener listener = (WriteListener) m_writeListener;
      if (listener != null) {
        listener.onError(e);
      }
      throw e;
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    m_buf.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    m_buf.write(b);
  }

  public byte[] getContent() {
    return m_buf.toByteArray();
  }
}
