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
package org.eclipse.scout.rt.server.services.common.bookmark;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.notification.ICoalescer;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkChangedClientNotification;

public class BookmarkNotificationCoalescer implements ICoalescer<BookmarkChangedClientNotification> {

  @Override
  public List<BookmarkChangedClientNotification> coalesce(List<BookmarkChangedClientNotification> notifications) {
    // reduce to one
    return CollectionUtility.arrayList(CollectionUtility.firstElement(notifications));
  }

}
