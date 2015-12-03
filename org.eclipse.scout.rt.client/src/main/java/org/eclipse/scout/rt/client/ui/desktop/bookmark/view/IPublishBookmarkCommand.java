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
package org.eclipse.scout.rt.client.ui.desktop.bookmark.view;

import org.eclipse.scout.rt.client.ui.desktop.bookmark.AbstractBookmarkTreeField;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;

/**
 * Injection command for {@link BookmarkViewForm#getUserBookmarkTreeField()} and
 * {@link AbstractBookmarkTreeField#injectPublishBookmarkCommand(IPublishBookmarkCommand)}
 */
public interface IPublishBookmarkCommand {

  /**
   * Obtains the target group for the bookmark to be published to
   * <p>
   * and calls
   * {@link IBookmarkStorageService#publishBookmarkData(org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder, java.util.Map)}
   */
  void publishBookmark(BookmarkFolder publishFolder);

}
