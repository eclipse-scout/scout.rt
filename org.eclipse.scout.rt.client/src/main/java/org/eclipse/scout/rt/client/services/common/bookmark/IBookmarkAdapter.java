/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.bookmark;

import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

/**
 * Adapter for {@link IPage}. The {@link BookmarkUtility} asks the {@link IPage} for this type of adapter when creating
 * a {@link Bookmark}.
 */
public interface IBookmarkAdapter {

  String getIdentifier();

  String getTitle();

  String getText();

  String getIconId();

  String getOutlineClassName();

  String getOutlineTitle();
}
