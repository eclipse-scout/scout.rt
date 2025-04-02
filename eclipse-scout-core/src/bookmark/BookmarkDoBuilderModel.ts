/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Desktop, Page} from '../index';

export interface BookmarkDoBuilderModel {
  desktop: Desktop;
  page?: Page;

  // --- builder options ---

  /**
   * Specifies whether the bookmark should include information about the path in an outline, e.g. to open it at its original location.
   * If this is not set, the bookmark creator only returns a {@link PageBookmarkDefinitionDo}. Otherwise, it tries to create a
   * {@link OutlineBookmarkDefinitionDo}.
   *
   * The default value is `true`.
   *
   * @see fallbackAllowed
   */
  createOutline?: boolean;

  /**
   * Specifies whether the bookmark should only include data that can safely be persisted to a database. When not set, the resulting
   * bookmark should only be used in-memory.
   *
   * The default value is `true`.
   */
  persistableRequired?: boolean;

  /**
   * Specifies whether the bookmark creator may return a {@link PageBookmarkDefinitionDo} when a {@link OutlineBookmarkDefinitionDo}
   * could not be created. If not, an error is thrown instead. This flag only has an effect if {@link createOutline} is `true`.
   *
   * The default value is `true`.
   *
   * @see createOutline
   */
  fallbackAllowed?: boolean;

  /**
   * Specifies whether the returned bookmark should include a human-readable title.
   *
   * The default value is `true`.
   */
  createTitle?: boolean;

  /**
   * Specifies whether the returned bookmark should include a human-readable description.
   *
   * The default value is `true`.
   */
  createDescription?: boolean;

  /**
   * Specifies whether the returned bookmark should include information about table preferences. This is typically needed if
   * the bookmark is stored in the database and has to be restored later. It is not needed for refreshes because that
   * information is coming from client preferences.
   *
   * The default value is `true`.
   */
  createTablePreferences?: boolean;

  /**
   * Specifies whether the returned bookmark should include information about selected rows. This is typically only needed
   * if using bookmarks for refreshing the outline.
   *
   * The default value is `true`.
   */
  createTableRowSelections?: boolean;
}
