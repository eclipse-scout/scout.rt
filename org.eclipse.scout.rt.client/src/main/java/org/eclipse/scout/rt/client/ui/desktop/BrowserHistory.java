package org.eclipse.scout.rt.client.ui.desktop;

/**
 * @since 6.0
 */
public class BrowserHistory {

  private String m_title;

  private String m_path;

  public BrowserHistory(String path, String title) {
    m_path = path;
    m_title = title;
  }

  public String getTitle() {
    return m_title;
  }

  public void setTitle(String title) {
    m_title = title;
  }

  public String getPath() {
    return m_path;
  }

  public void setPath(String path) {
    m_path = path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
    result = prime * result + ((m_title == null) ? 0 : m_title.hashCode());
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
    BrowserHistory other = (BrowserHistory) obj;
    if (m_path == null) {
      if (other.m_path != null) {
        return false;
      }
    }
    else if (!m_path.equals(other.m_path)) {
      return false;
    }
    if (m_title == null) {
      if (other.m_title != null) {
        return false;
      }
    }
    else if (!m_title.equals(other.m_title)) {
      return false;
    }
    return true;
  }

}
