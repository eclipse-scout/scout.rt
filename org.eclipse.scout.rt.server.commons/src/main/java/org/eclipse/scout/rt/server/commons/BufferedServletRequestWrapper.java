/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

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
