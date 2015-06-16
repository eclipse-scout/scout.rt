/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.notification.AbstractObservableNotificationHandler;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;

/**
 *
 */
public class BookmarkClientNotificationHandler extends AbstractObservableNotificationHandler<BookmarkChangedClientNotification> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BookmarkClientNotificationHandler.class);

  @Override
  public void handleNotification(BookmarkChangedClientNotification notification) {
    try {
      BEANS.get(IBookmarkService.class).loadBookmarks();
    }
    catch (ProcessingException e) {
      LOG.error("Could not reload bookmarks.", e);
    }
    super.handleNotification(notification);
  }
}
