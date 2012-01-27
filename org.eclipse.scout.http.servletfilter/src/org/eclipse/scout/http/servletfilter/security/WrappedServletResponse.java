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
package org.eclipse.scout.http.servletfilter.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class WrappedServletResponse extends HttpServletResponseWrapper {
  private static final int TYPE_ADD = 1;
  private static final int TYPE_SET = 2;
  private List<P_HeaderAttribute> m_headerAttributes;

  public WrappedServletResponse(HttpServletResponse response) {
    super(response);
    m_headerAttributes = new ArrayList<P_HeaderAttribute>();
  }

  @Override
  public void setHeader(String name, String value) {
    m_headerAttributes.add(new P_HeaderAttribute(name, value, TYPE_SET));
  }

  @Override
  public void addHeader(String name, String value) {
    m_headerAttributes.add(new P_HeaderAttribute(name, value, TYPE_ADD));
  }

  public String getHeaderValue(String name) {
    for (P_HeaderAttribute att : m_headerAttributes) {
      if (att.getName().equals(name)) {
        return att.getValue();
      }
    }
    return null;
  }

  public void clearHeader() {
    m_headerAttributes.clear();
  }

  @Override
  public void flushBuffer() throws IOException {

  }

  @Override
  public void setStatus(int sc) {
  }

  @Override
  public HttpServletResponse getResponse() {
    return (HttpServletResponse) super.getResponse();
  }

  public void applyHeader() {
    for (P_HeaderAttribute att : m_headerAttributes) {
      switch (att.getType()) {
        case TYPE_ADD:
          getResponse().addHeader(att.getName(), att.getValue());
          break;
        case TYPE_SET:
          getResponse().setHeader(att.getName(), att.getValue());
          break;
      }
    }
  }

  private class P_HeaderAttribute {
    private final String m_name;
    private final String m_value;
    private final int m_type;

    public P_HeaderAttribute(String name, String value, int type) {
      m_name = name;
      m_value = value;
      m_type = type;
    }

    public String getName() {
      return m_name;
    }

    public String getValue() {
      return m_value;
    }

    public int getType() {
      return m_type;
    }

  } // end class P_HeaderAttribute

}
