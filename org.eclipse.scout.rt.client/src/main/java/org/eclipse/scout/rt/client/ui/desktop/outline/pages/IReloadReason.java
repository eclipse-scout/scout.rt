/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

/**
 * Constants used in {@link IPage#reloadPage(IReloadReason)}
 *
 * @since 16.1
 */
public interface IReloadReason {
  /**
   * No specificv reason, just reload data using the current search settings, the current row limits and the current
   * filter (Default)
   */
  String UNSPECIFIED = "unspecified";

  /**
   * Some search parameters changed or the search was reset and the search was triggered
   */
  String SEARCH = "search";

  /**
   * The user requested loading more data than his soft limit, up to the application specific hard limit
   */
  String OVERRIDE_ROW_LIMIT = "overrideRowLimit";

  /**
   * The user requested loading no more data than his soft limit;
   */
  String RESET_ROW_LIMIT = "resetRowLimit";

  /**
   * The column structure of the table was changed
   */
  String ORGANIZE_COLUMNS = "organizeColumns";

  /**
   * Any call to {@link IPage#dataChanged(Object...)}
   */
  String DATA_CHANGED_TRIGGER = "dataChangedTrigger";
}
