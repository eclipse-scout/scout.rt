/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface IChartColumnParam {

  int AGGREGATION_COUNT = -1;
  int AGGREGATION_SUM = 1;
  int AGGREGATION_AVG = 2;

  int GROUP_BY_YEARS = 256;
  int GROUP_BY_MONTHS = 257;
  int GROUP_BY_WEEKDAYS = 258;
  int GROUP_BY_DATE = 259;

  int getColumnIndex();

  IColumn getColumn();

  int getColumnModifier();
}
