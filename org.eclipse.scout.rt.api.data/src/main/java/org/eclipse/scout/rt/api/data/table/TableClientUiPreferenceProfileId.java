/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.table;

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Profile Id used to identify different profile for {@link TableClientUiPreferenceProfileDo}.
 * <p>
 * A profile might be based on some information from the user agent.
 */
@IdTypeName("scout.TableClientUiPreferenceProfileId")
public final class TableClientUiPreferenceProfileId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  /**
   * Key for global preferences of a table, i.e. preferences that are not stored to a specific settings profile.
   */
  public static final TableClientUiPreferenceProfileId GLOBAL = new TableClientUiPreferenceProfileId("global-a134390b-bfef-4b9e-a14e-425df161e768");

  /**
   * Factory settings for a bookmark. If a bookmark is reset to factory settings, the bookmarked settings are restored.
   */
  public static final TableClientUiPreferenceProfileId BOOKMARK = new TableClientUiPreferenceProfileId("bookmark-aebcacd2-ddb6-4b7f-8673-d1585701d388");

  private TableClientUiPreferenceProfileId(String id) {
    super(id);
  }

  public static TableClientUiPreferenceProfileId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new TableClientUiPreferenceProfileId(id);
  }
}
