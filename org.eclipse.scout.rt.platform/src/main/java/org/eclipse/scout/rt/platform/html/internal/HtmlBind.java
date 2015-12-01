package org.eclipse.scout.rt.platform.html.internal;

import java.util.Map;

import org.eclipse.scout.rt.platform.html.IHtmlBind;
import org.eclipse.scout.rt.platform.util.Assertions;

public class HtmlBind implements IHtmlBind {

  private String m_name;

  public HtmlBind(String name) {
    m_name = Assertions.assertNotNull(name);
  }

  @Override
  public int length() {
    return m_name.length();
  }

  @Override
  public char charAt(int index) {
    return m_name.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return m_name.subSequence(end, end);
  }

  @Override
  public String toString() {
    return m_name;
  }

  @Override
  public void replaceBinds(Map<String, String> bindMap) {
    String newBind = bindMap.get(m_name);
    if (newBind != null) {
      m_name = newBind;
    }
  }

}
