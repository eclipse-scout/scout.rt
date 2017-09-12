package org.eclipse.scout.rt.testing.server;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

/**
 * Test session recording attributes
 */
@SuppressWarnings("deprecation")
public class TestHttpSession implements HttpSession {

  private final Map<String, Object> m_sessionAttributes = new HashMap<>();

  @Override
  public long getCreationTime() {
    return 0;
  }

  @Override
  public String getId() {
    return TestHttpSession.class.getName();
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
