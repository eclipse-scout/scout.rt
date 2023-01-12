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
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("29cd5135-9b4d-4de7-a16b-e5cea26901ef")
public class ActivateBookmarkKeyStroke extends KeyStroke {
  private static final Logger LOG = LoggerFactory.getLogger(ActivateBookmarkKeyStroke.class);

  private final Bookmark m_bookmark;

  public ActivateBookmarkKeyStroke(Bookmark b) {
    super(b.getKeyStroke());
    m_bookmark = b;
    callInitializer();
  }

  public Bookmark getBookmark() {
    return m_bookmark;
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
