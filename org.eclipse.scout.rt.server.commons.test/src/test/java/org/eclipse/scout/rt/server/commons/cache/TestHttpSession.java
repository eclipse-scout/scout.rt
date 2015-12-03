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
package org.eclipse.scout.rt.server.commons.cache;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Test session recording attributes
 */
@SuppressWarnings("deprecation")
public class TestHttpSession implements HttpSession {

  private final HashMap<String, Object> m_sessionAttributes = new HashMap<String, Object>();

  @Override
  public long getCreationTime() {
    return 0;
  }

  @Override
  public String getId() {
    return null;
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

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
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
  }

  @Override
  public void removeAttribute(String name) {
    m_sessionAttributes.remove(name);
  }

  @Override
  public void removeValue(String name) {
  }

  @Override
  public void invalidate() {
  }

  @Override
  public boolean isNew() {
    return false;
  }
}
