package org.eclipse.scout.rt.client.ui.form.fields.smartfield.result;

import org.eclipse.scout.rt.platform.util.StringUtility;

public class ByTextQueryParam implements IQueryParam {

  private final String m_wildcard;

  private final String m_text;

  public ByTextQueryParam(String wildcard, String text) {
    this.m_wildcard = wildcard;
    this.m_text = text;
  }

  public String getText() {
    return m_text;
  }

  public String getWildcard() {
    return m_wildcard;
  }

  public boolean isBrowseAll() {
    return StringUtility.isNullOrEmpty(m_text) || m_text.equals(m_wildcard);
  }

}
