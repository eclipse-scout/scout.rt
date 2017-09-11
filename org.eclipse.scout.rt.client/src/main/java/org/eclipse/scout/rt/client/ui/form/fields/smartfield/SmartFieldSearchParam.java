package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class SmartFieldSearchParam<LOOKUP_KEY> implements ISmartFieldSearchParam<LOOKUP_KEY> {

  private final String m_searchText;
  private final LOOKUP_KEY m_parentKey;
  private final boolean m_isByParentSearch;
  private final boolean m_selectCurrentValue;
  private final String m_wildcard;

  /**
   * @param wildcard
   * @param searchText
   *          Original search text as typed in the UI
   * @param parentKey
   * @param isByParentSearch
   * @param selectCurrentValue
   */
  SmartFieldSearchParam(String wildcard, String searchText, LOOKUP_KEY parentKey, boolean isByParentSearch, boolean selectCurrentValue) {
    m_wildcard = wildcard;
    m_searchText = searchText;
    m_parentKey = parentKey;
    m_isByParentSearch = isByParentSearch;
    m_selectCurrentValue = selectCurrentValue;
  }

  protected String toSearchText(String text) {
    return m_searchText;
  }

  @Override
  public String getSearchText() {
    return m_searchText;
  }

  @Override
  public String getSearchQuery() {
    return StringUtility.isNullOrEmpty(m_searchText) ? m_wildcard : m_searchText;
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
  public String getWildcard() {
    return m_wildcard;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_isByParentSearch ? 1231 : 1237);
    result = prime * result + ((m_parentKey == null) ? 0 : m_parentKey.hashCode());
    result = prime * result + ((m_searchText == null) ? 0 : m_searchText.hashCode());
    result = prime * result + (m_selectCurrentValue ? 1231 : 1237);
    result = prime * result + ((m_wildcard == null) ? 0 : m_wildcard.hashCode());
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
    SmartFieldSearchParam other = (SmartFieldSearchParam) obj;
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
    if (m_wildcard == null) {
      if (other.m_wildcard != null) {
        return false;
      }
    }
    else if (!m_wildcard.equals(other.m_wildcard)) {
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
        .attr("wildcard", m_wildcard)
        .toString();
  }

  public static <LOOKUP_KEY> ISmartFieldSearchParam<LOOKUP_KEY> createTextParam(String wildcard, String searchText, boolean selectCurrentValue) {
    return new SmartFieldSearchParam<LOOKUP_KEY>(wildcard, searchText, null, false, selectCurrentValue);
  }

  public static <LOOKUP_KEY> ISmartFieldSearchParam<LOOKUP_KEY> createParentParam(LOOKUP_KEY parentKey, boolean selectCurrentValue) {
    return new SmartFieldSearchParam<LOOKUP_KEY>(null, null, parentKey, true, selectCurrentValue);
  }

}
