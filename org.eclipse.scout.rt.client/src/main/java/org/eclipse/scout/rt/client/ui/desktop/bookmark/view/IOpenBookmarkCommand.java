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
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * Injection command for {@link BookmarkViewForm#getUserBookmarkTreeField()} and
 * {@link AbstractBookmarkTreeField#injectOpenBookmarkCommand(IInjectOpenBookmarkCommand)}
 */
@FunctionalInterface
public interface IOpenBookmarkCommand {

  /**
   * Opens the bookmark, it's up to the implementation to track additional features
   * <p>
   */
  void openBookmark(Bookmark bookmark);
}
