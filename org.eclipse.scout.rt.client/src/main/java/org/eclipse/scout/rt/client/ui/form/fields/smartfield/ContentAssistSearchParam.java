package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class ContentAssistSearchParam<LOOKUP_KEY> implements IContentAssistSearchParam<LOOKUP_KEY> {

  private final String m_searchText;
  private final LOOKUP_KEY m_parentKey;
  private final boolean m_isByParentSearch;
  private final boolean m_selectCurrentValue;

  ContentAssistSearchParam(String searchText, LOOKUP_KEY parentKey, boolean isByParentSearch, boolean selectCurrentValue) {
    m_parentKey = parentKey;
    m_selectCurrentValue = selectCurrentValue;
    m_searchText = searchText;
    m_isByParentSearch = isByParentSearch;
  }

  @Override
  public String getSearchText() {
    return m_searchText;
  }

  @Override
  public LOOKUP_KEY getParentKey() {
    return m_parentKey;
  }

  @Override
  public boolean isSelectCurrentValue() {
    return m_selectCurrentValue;
  }

  @Override
  public boolean isByParentSearch() {
    return m_isByParentSearch;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_isByParentSearch ? 1231 : 1237);
    result = prime * result + ((m_parentKey == null) ? 0 : m_parentKey.hashCode());
    result = prime * result + ((m_searchText == null) ? 0 : m_searchText.hashCode());
    result = prime * result + (m_selectCurrentValue ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ContentAssistSearchParam other = (ContentAssistSearchParam) obj;
    if (m_isByParentSearch != other.m_isByParentSearch) {
      return false;
    }
    if (m_parentKey == null) {
      if (other.m_parentKey != null) {
        return false;
      }
    }
    else if (!m_parentKey.equals(other.m_parentKey)) {
      return false;
    }
    if (m_searchText == null) {
      if (other.m_searchText != null) {
        return false;
      }
    }
    else if (!m_searchText.equals(other.m_searchText)) {
      return false;
    }
    if (m_selectCurrentValue != other.m_selectCurrentValue) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .attr("searchText", m_searchText)
        .attr("parentKey", m_parentKey)
        .attr("selectCurrentValue", m_selectCurrentValue)
        .toString();
  }

  public static <LOOKUP_KEY> IContentAssistSearchParam<LOOKUP_KEY> createTextParam(String searchText, boolean selectCurrentValue) {
    return new ContentAssistSearchParam<LOOKUP_KEY>(searchText, null, false, selectCurrentValue);
  }

  public static <LOOKUP_KEY> IContentAssistSearchParam<LOOKUP_KEY> createParentParam(LOOKUP_KEY parentKey, boolean selectCurrentValue) {
    return new ContentAssistSearchParam<LOOKUP_KEY>(null, parentKey, true, selectCurrentValue);
  }

}
