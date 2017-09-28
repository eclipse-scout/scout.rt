/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.rt.client.clientnotification.AbstractObservableNotificationHandler;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;

public class BookmarkClientNotificationHandler extends AbstractObservableNotificationHandler<BookmarkChangedClientNotification> {

  @Override
  public void handleNotification(BookmarkChangedClientNotification notification, IClientNotificationAddress address) {
    ModelJobs.schedule(() -> BEANS.get(IBookmarkService.class).loadBookmarks(), ModelJobs.newInput(ClientRunContexts.copyCurrent()));
    super.handleNotification(notification, address);
  }

}
