/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class BufferedServletOutputStream extends ServletOutputStream {

  private final ByteArrayOutputStream m_buf;

  public BufferedServletOutputStream(OutputStream out) throws IOException {
    m_buf = new ByteArrayOutputStream();
  }

  public byte[] getContent() {
    return m_buf.toByteArray();
  }

  /**
   * since servlet api 3.1
   */
  @Override
  public boolean isReady() {
    return true;
  }

  /**
   * since servlet api 3.1
   */
  @Override
  public void setWriteListener(javax.servlet.WriteListener writeListener) {
  }

  @Override
  public void write(byte[] b) throws IOException {
    m_buf.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    m_buf.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    m_buf.write(b);
  }

  @Override
  public void flush() throws IOException {
    m_buf.flush();
  }

  @Override
  public void close() throws IOException {
    m_buf.close();
  }
}
