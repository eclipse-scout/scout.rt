/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.bookmark.menu;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

public class ActivateBookmarkKeyStroke extends KeyStroke {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActivateBookmarkKeyStroke.class);

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
  protected void execAction() throws ProcessingException {
    if (m_bookmark != null) {
      try {
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(m_bookmark, false);
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
    }
  }
}
