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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Column ID used within preferences to identify a table column.
 * <p>
 * Represents the class ID of a table column ({@link ITypeWithClassId#classId}) or a dynamically created {@link UUID}.
 */
@IdTypeName("scout.TableColumnId")
public final class TableColumnId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private TableColumnId(String id) {
    super(id);
  }

  /**
   * Only to be used for dynamically created table columns.
   */
  public static TableColumnId create() {
    return new TableColumnId(UUID.randomUUID().toString());
  }

  public static TableColumnId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new TableColumnId(id);
  }
}
