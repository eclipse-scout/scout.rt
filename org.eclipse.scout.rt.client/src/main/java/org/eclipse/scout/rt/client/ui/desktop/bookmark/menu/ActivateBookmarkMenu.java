/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bookmark.menu;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("24d7ddcd-7656-487b-af5a-caf7b5d3791b")
public class ActivateBookmarkMenu extends AbstractMenu {
  private static final Logger LOG = LoggerFactory.getLogger(ActivateBookmarkMenu.class);

  private final Bookmark m_bookmark;

  public ActivateBookmarkMenu(Bookmark b) {
    super(false);
    m_bookmark = b;
    callInitializer();
  }

  public Bookmark getBookmark() {
    return m_bookmark;
  }

  @Override
  protected void execInitAction() {
    if (m_bookmark != null) {
      setText(m_bookmark.getTitle());
      setIconId(m_bookmark.getIconId());
      setKeyStroke(m_bookmark.getKeyStroke());
    }
    else {
      setText("...");
    }
  }

  @Override
  protected void execAction() {
    if (m_bookmark != null) {
      try {
        ClientSessionProvider.currentSession().getDesktop().activateBookmark(m_bookmark);
      }
      catch (Exception t) {
        LOG.error("Could not activate bookmark {}", m_bookmark, t);
      }
    }
  }
}
