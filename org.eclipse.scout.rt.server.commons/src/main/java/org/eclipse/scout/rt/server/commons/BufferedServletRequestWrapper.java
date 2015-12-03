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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

  private final BufferedServletInputStream m_buf;

  public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
    super(request);
    m_buf = new BufferedServletInputStream(request.getInputStream(), request.getContentLength());
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return m_buf;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(m_buf));
  }

  public byte[] getData() {
    return m_buf.getData();
  }
}
