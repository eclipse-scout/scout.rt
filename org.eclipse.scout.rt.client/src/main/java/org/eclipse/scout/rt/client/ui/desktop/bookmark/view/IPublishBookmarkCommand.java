/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.bookmark.view;

import org.eclipse.scout.rt.client.ui.desktop.bookmark.AbstractBookmarkTreeField;
import org.eclipse.scout.rt.shared.services.common.bookmark.BookmarkFolder;
import org.eclipse.scout.rt.shared.services.common.bookmark.IBookmarkStorageService;

/**
 * Injection command for {@link BookmarkViewForm#getUserBookmarkTreeField()} and
 * {@link AbstractBookmarkTreeField#injectPublishBookmarkCommand(IPublishBookmarkCommand)}
 */
@FunctionalInterface
public interface IPublishBookmarkCommand {

  /**
   * Obtains the target group for the bookmark to be published to
   * <p>
   * and calls {@link IBookmarkStorageService#publishBookmarkData(BookmarkFolder, java.util.Map)}
   */
  void publishBookmark(BookmarkFolder publishFolder);
}
