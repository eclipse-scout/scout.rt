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

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

public class ActivateBookmarkMenu extends AbstractMenu {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ActivateBookmarkMenu.class);

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
  @ConfigOperation
  @Order(10)
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
  protected void execAction() throws ProcessingException {
    if (m_bookmark != null) {
      try {
        ClientSyncJob.getCurrentSession().getDesktop().activateBookmark(m_bookmark);
      }
      catch (Throwable t) {
        LOG.error(null, t);
      }
    }
  }
}
