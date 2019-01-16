/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
