/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.table.controls;

import org.eclipse.scout.rt.chart.shared.data.basic.chart.IChartType;
import org.eclipse.scout.rt.dataobject.id.AbstractStringId;
import org.eclipse.scout.rt.dataobject.id.IdTypeName;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Wraps a string value from {@link IChartType}.
 */
@IdTypeName("scout.ChartTableControlChartTypeId")
public final class ChartTableControlChartTypeId extends AbstractStringId {
  private static final long serialVersionUID = 1L;

  private ChartTableControlChartTypeId(String id) {
    super(id);
  }

  public static ChartTableControlChartTypeId of(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return new ChartTableControlChartTypeId(id);
  }
}
