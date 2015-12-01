/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

import org.eclipse.scout.rt.platform.util.IOUtility;

public class BufferedServletInputStream extends ServletInputStream {

  private final ByteArrayInputStream m_data;
  private final byte[] m_rawData;
  private volatile Object m_readListener;
  private volatile boolean m_finished;

  public BufferedServletInputStream(InputStream source, int len) throws IOException {
    this(IOUtility.getContent(source, len)); // maybe read the data async?
  }

  public BufferedServletInputStream(InputStream source) throws IOException {
    this(IOUtility.getContent(source, -1));
  }

  public BufferedServletInputStream(byte[] data) {
    m_finished = false;
    m_rawData = data;
    m_data = new ByteArrayInputStream(data);
  }

  @SuppressWarnings("all")
  public boolean isFinished() {
    return m_finished;
  }

  public byte[] getData() {
    return m_rawData;
  }

  @SuppressWarnings("all")
  public boolean isReady() {
    return true; // always ready as data is buffered
  }

  @SuppressWarnings("all")
  public void setReadListener(javax.servlet.ReadListener readListener) {
    if (readListener == null) {
      throw new NullPointerException("readlistener may not be null."); // as per ServletInputStream spec
    }
    m_readListener = readListener;
    try {
      readListener.onDataAvailable();
    }
    catch (IOException e) {
      readListener.onError(e);
    }
  }

  @Override
  public int read() throws IOException {
    try {
      final int next = m_data.read();
      if (next < 0) {
        m_finished = true;
        javax.servlet.ReadListener listener = (javax.servlet.ReadListener) m_readListener;
        if (listener != null) {
          listener.onAllDataRead();
        }
      }
      return next;
    }
    catch (IOException e) {
      javax.servlet.ReadListener listener = (javax.servlet.ReadListener) m_readListener;
      if (listener != null) {
        listener.onError(e);
      }
      throw e;
    }
  }

  public int getLength() {
    return m_rawData.length;
  }

}
