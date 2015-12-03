/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.bookmark.menu;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOG.error(null, t);
      }
    }
  }
}
