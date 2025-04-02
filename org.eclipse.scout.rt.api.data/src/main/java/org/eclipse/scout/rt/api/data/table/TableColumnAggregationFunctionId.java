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
 * Wraps a string value from {@code INumberColumn.AggregationFunction}
 */
@IdTypeName("scout.TableColumnAggregationFunctionId")
public final class TableColumnAggregationFunctionId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private TableColumnAggregationFunctionId(String id) {
    super(id);
  }

  public static TableColumnAggregationFunctionId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new TableColumnAggregationFunctionId(id);
  }
}
