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
 * Table ID used within preferences to identify a table.
 * <p>
 * Represents the class ID of a table ({@link org.eclipse.scout.rt.platform.classid.ITypeWithClassId#classId}).
 */
@IdTypeName("scout.TableId")
public final class TableId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private TableId(String id) {
    super(id);
  }

  public static TableId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new TableId(id);
  }
}
