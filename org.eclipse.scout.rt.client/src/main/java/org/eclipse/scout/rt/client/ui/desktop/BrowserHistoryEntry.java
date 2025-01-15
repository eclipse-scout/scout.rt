/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import java.net.URI;
import java.util.Objects;

/**
 * This class is used to populate the browser history with history entries.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History">API/History</a>
 * @since 6.0
 */
public class BrowserHistoryEntry {

  private final String m_title;
  private final URI m_path;
  private final String m_deepLinkPath;
  private final boolean m_pathVisible;

  /**
   * @param path
   *     relative path which is displayed in the address bar of the browser. Example
   *     <code>./?deeplink=outline-12345</code>
   * @param title
   *     displayed in the browser window
   * @param deepLinkPath
   *     stored in the History.state object of the browser. This path is sent back to the UI server when the user
   *     clicks on the navigate buttons in the browser. Example <code>outline-12345</code>
   * @param pathVisible
   *     flag to show or hide the path in the URL. In some cases we want to suppress deep-link parameters
   *     on startup, but we still need the deep-link-path.
   */
  public BrowserHistoryEntry(URI path, String title, String deepLinkPath, boolean pathVisible) {
    m_path = path;
    m_title = title;
    m_deepLinkPath = deepLinkPath;
    m_pathVisible = pathVisible;
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

  public boolean isPathVisible() {
    return m_pathVisible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BrowserHistoryEntry that = (BrowserHistoryEntry) o;
    return m_pathVisible == that.m_pathVisible &&
        m_title.equals(that.m_title) &&
        m_path.equals(that.m_path) &&
        m_deepLinkPath.equals(that.m_deepLinkPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_title, m_path, m_deepLinkPath, m_pathVisible);
  }
}
