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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class BufferedServletInputStream extends ServletInputStream {

  private final ByteArrayInputStream m_buf;
  private final int m_len;

  public BufferedServletInputStream(byte[] bytes) throws IOException {
    m_buf = new ByteArrayInputStream(bytes);
    m_len = bytes.length;
  }

  public int getLength() {
    return m_len;
  }

  @Override
  public int read() throws IOException {
    return m_buf.read();
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
  }
}
