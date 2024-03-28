/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import static org.eclipse.scout.rt.platform.util.CollectionUtility.arrayList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface INTupleChartValueGroupBean extends IChartValueGroupBean {

  int getN();

  List<String> getIdentifiers();

  List<Map<String, BigDecimal>> getValues();

  default void add(BigDecimal... tupleValues) {
    add(arrayList(tupleValues));
  }

  void add(List<BigDecimal> tupleValues);
}
