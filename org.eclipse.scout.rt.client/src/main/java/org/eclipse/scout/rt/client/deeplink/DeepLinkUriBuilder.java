/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.deeplink;

import java.net.URI;
import java.net.URL;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.BrowserHistoryEntry;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.UriBuilder;
import org.eclipse.scout.rt.shared.deeplink.DeepLinkUrlParameter;
import org.eclipse.scout.rt.shared.deeplink.DeepLinkUtility;

public final class DeepLinkUriBuilder {

  private final UriBuilder m_builder;
  private String m_info;
  private String m_path;
  private boolean m_pathVisible = true;

  private DeepLinkUriBuilder(UriBuilder builder) {
    m_builder = builder;
  }

  public static DeepLinkUriBuilder createAbsolute() {
    IClientSession clientSession = ClientSessionProvider.currentSession();
    return new DeepLinkUriBuilder(new UriBuilder(clientSession.getBrowserURI()));
  }

  public static DeepLinkUriBuilder createRelative() {
    return new DeepLinkUriBuilder(new UriBuilder("./"));
  }

  public DeepLinkUriBuilder parameterPath(String path) {
    m_path = path;
    m_builder.parameter(DeepLinkUrlParameter.DEEP_LINK, path);
    return this;
  }

  /**
   * Sets the 'info' field used to create a BrowserHistoryEntry. Use this method if you want to set the info but don't
   * want to add an info-parameter to the URL.
   */
  public DeepLinkUriBuilder info(String info) {
    m_info = info;
    return this;
  }

  public DeepLinkUriBuilder parameterInfo(String info) {
    if (StringUtility.hasText(info)) {
      m_info = info;
      m_builder.parameter(DeepLinkUrlParameter.INFO, DeepLinkUtility.toSlug(info));
    }
    return this;
  }

  public DeepLinkUriBuilder parameter(String name, String value) {
    m_builder.parameter(name, value);
    return this;
  }

  public DeepLinkUriBuilder pathVisible(boolean pathVisible) {
    m_pathVisible = pathVisible;
    return this;
  }

  public BrowserHistoryEntry createBrowserHistoryEntry() {
    if (m_path == null) {
      throw new IllegalStateException("Cannot create BrowserHistoryEntry without deep-link path");
    }
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    StringBuilder title = new StringBuilder(desktop.getTitle());
    if (StringUtility.hasText(m_info)) {
      title.append(" - ").append(m_info);
    }
    return new BrowserHistoryEntry(m_builder.createURI(), title.toString(), m_path, m_pathVisible);
  }

  public URL createURL() {
    return m_builder.createURL();
  }

  public URI createURI() {
    return m_builder.createURI();
  }

}
