/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
