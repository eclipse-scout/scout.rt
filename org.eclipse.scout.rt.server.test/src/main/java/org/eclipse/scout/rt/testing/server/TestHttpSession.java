/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.server;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;
import jakarta.servlet.http.HttpSessionContext;

import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Test session recording attributes
 */
@SuppressWarnings("deprecation")
public class TestHttpSession implements HttpSession {

  private final Map<String, Object> m_sessionAttributes = new HashMap<>();
  private final String m_id;

  public TestHttpSession() {
    this(null);
  }

  public TestHttpSession(String id) {
    if (!StringUtility.hasText(id)) {
      m_id = TestHttpSession.class.getName();
    }
    else {
      m_id = id;
    }
  }

  @Override
  public long getCreationTime() {
    return 0;
  }

  @Override
  public String getId() {
    return m_id;
  }

  @Override
  public long getLastAccessedTime() {
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public void setMaxInactiveInterval(int interval) {
    //NOP
  }

  @Override
  public int getMaxInactiveInterval() {
    return 0;
  }

  @Override
  public HttpSessionContext getSessionContext() {
    return null;
  }

  @Override
  public Object getAttribute(String name) {
    return m_sessionAttributes.get(name);
  }

  @Override
  public Object getValue(String name) {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Enumeration getAttributeNames() {
    return Collections.enumeration(m_sessionAttributes.keySet());
  }

  @Override
  public String[] getValueNames() {
    return new String[]{};
  }

  @Override
  public void setAttribute(String name, Object value) {
    m_sessionAttributes.put(name, value);
  }

  @Override
  public void putValue(String name, Object value) {
    //NOP
  }

  @Override
  public void removeAttribute(String name) {
    m_sessionAttributes.remove(name);
  }

  @Override
  public void removeValue(String name) {
    //NOP
  }

  @Override
  public void invalidate() {
    for (Object v : m_sessionAttributes.values()) {
      if (v instanceof HttpSessionBindingListener) {
        ((HttpSessionBindingListener) v).valueUnbound(new HttpSessionBindingEvent(this, v.toString()));
      }
    }
  }

  @Override
  public boolean isNew() {
    return false;
  }
}
