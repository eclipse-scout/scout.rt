/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import java.net.URI;

/**
 * This class is used to populate the browser history with history entries.
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/API/History
 * @since 6.0
 */
public class BrowserHistoryEntry {

  private final String m_title;

  private final URI m_path;

  private final String m_deepLinkPath;

  /**
   * @param path
   *          relative path which is displayed in the address bar of the browser. Example
   *          <code>./?deeplink=outline-12345</code>
   * @param title
   *          displayed in the browser window
   * @param deepLinkPath
   *          stored in the History.state object of the browser. This path is sent back to the UI server when the user
   *          clicks on the navigate buttons in the browser. Example <code>outline-12345</code>
   */
  public BrowserHistoryEntry(URI path, String title, String deepLinkPath) {
    m_path = path;
    m_title = title;
    m_deepLinkPath = deepLinkPath;
  }

  public String getTitle() {
    return m_title;
  }

  public String getPath() {
    if (m_path == null) {
      return null;
    }
    return m_path.toString();
  }

  public String getDeepLinkPath() {
    return m_deepLinkPath;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_deepLinkPath == null) ? 0 : m_deepLinkPath.hashCode());
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
    BrowserHistoryEntry other = (BrowserHistoryEntry) obj;
    if (m_deepLinkPath == null) {
      if (other.m_deepLinkPath != null) {
        return false;
      }
    }
    else if (!m_deepLinkPath.equals(other.m_deepLinkPath)) {
      return false;
    }
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
