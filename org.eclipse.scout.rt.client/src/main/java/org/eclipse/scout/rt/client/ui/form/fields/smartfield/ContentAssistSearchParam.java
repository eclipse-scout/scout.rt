package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class ContentAssistSearchParam<LOOKUP_KEY> implements IContentAssistSearchParam<LOOKUP_KEY> {

  /**
   * This enum is required because we cannot simply check if key or parentKey is != null. Because some code is built
   * around logic where a "search by parent key" is started and null is passed as parentKey. In that case we must find
   * out what kind of search has been started.
   */
  enum SearchBy {
    TEXT,
    KEY,
    PARENT_KEY;
  }

  private final SearchBy m_searchBy;
  private String m_searchText;
  private LOOKUP_KEY m_parentKey;
  private LOOKUP_KEY m_key;
  private boolean m_selectCurrentValue;
  private String m_wildcard;

  ContentAssistSearchParam(String wildcard, String searchText, boolean selectCurrentValue) {
    m_searchBy = SearchBy.TEXT;
    m_wildcard = wildcard;
    m_searchText = searchText;
    m_selectCurrentValue = selectCurrentValue;
  }

  ContentAssistSearchParam(SearchBy searchBy, LOOKUP_KEY key, boolean selectCurrentValue) {
    switch (searchBy) {
      case KEY:
        m_key = key;
        break;
      case PARENT_KEY:
        m_parentKey = key;
        break;
      default:
        throw new IllegalArgumentException();
    }
    m_searchBy = searchBy;
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
  public boolean isByParentKeySearch() {
    return m_searchBy == SearchBy.PARENT_KEY;
  }

  @Override
  public boolean isSelectCurrentValue() {
    return m_selectCurrentValue;
  }

  @Override
  public String getWildcard() {
    return m_wildcard;
  }

  @Override
  public LOOKUP_KEY getKey() {
    return m_key;
  }

  @Override
  public boolean isByKeySearch() {
    return m_searchBy == SearchBy.KEY;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
    result = prime * result + ((m_parentKey == null) ? 0 : m_parentKey.hashCode());
    result = prime * result + ((m_searchBy == null) ? 0 : m_searchBy.hashCode());
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
    ContentAssistSearchParam other = (ContentAssistSearchParam) obj;
    if (m_key == null) {
      if (other.m_key != null) {
        return false;
      }
    }
    else if (!m_key.equals(other.m_key)) {
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
    if (m_searchBy != other.m_searchBy) {
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
        .attr("key", m_key)
        .attr("wildcard", m_wildcard)
        .toString();
  }

  public static <LOOKUP_KEY> IContentAssistSearchParam<LOOKUP_KEY> createTextParam(String wildcard, String searchText, boolean selectCurrentValue) {
    return new ContentAssistSearchParam<LOOKUP_KEY>(wildcard, searchText, selectCurrentValue);
  }

  public static <LOOKUP_KEY> IContentAssistSearchParam<LOOKUP_KEY> createKeyParam(LOOKUP_KEY key, boolean selectCurrentValue) {
    return new ContentAssistSearchParam<LOOKUP_KEY>(SearchBy.KEY, key, selectCurrentValue);
  }

  public static <LOOKUP_KEY> IContentAssistSearchParam<LOOKUP_KEY> createParentKeyParam(LOOKUP_KEY parentKey, boolean selectCurrentValue) {
    return new ContentAssistSearchParam<LOOKUP_KEY>(SearchBy.PARENT_KEY, parentKey, selectCurrentValue);
  }

}
