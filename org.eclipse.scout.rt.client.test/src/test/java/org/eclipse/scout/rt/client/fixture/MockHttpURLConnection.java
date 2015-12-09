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
package org.eclipse.scout.rt.client.fixture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.util.SleepUtil;

public abstract class MockHttpURLConnection extends HttpURLConnection {
  private ByteArrayOutputStream m_out;
  private ByteArrayInputStream m_in;
  private String m_statusLine;

  protected MockHttpURLConnection(URL u) {
    super(u);
    m_out = new ByteArrayOutputStream();
  }

  @Override
  public void connect() throws IOException {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return m_out;
  }

  @Override
  public String getHeaderField(int n) {
    if (n == 0) {
      return m_statusLine;
    }
    return null;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (m_in == null) {
      final AtomicInteger scRef = new AtomicInteger(500);
      final ByteArrayOutputStream servletOut = new ByteArrayOutputStream();
      final ByteArrayInputStream servletIn = new ByteArrayInputStream(m_out.toByteArray());
      m_out = null;
      Thread t = new Thread() {
        @Override
        public void run() {
          try {
            mockHttpServlet(servletIn, servletOut);
            scRef.set(200);
          }
          catch (Exception e) {
            //nop
          }
        }
      };
      t.start();
      while (t.isAlive()) {
        SleepUtil.sleepSafe(100, TimeUnit.MILLISECONDS);
      }
      m_in = new ByteArrayInputStream(servletOut.toByteArray());
      int sc = scRef.get();
      m_statusLine = "HTTP/1.0 " + sc + " " + (sc == 200 ? "OK" : "NOK");
    }
    return m_in;
  }

  protected abstract void mockHttpServlet(InputStream servletIn, OutputStream servletOut) throws Exception;

}
