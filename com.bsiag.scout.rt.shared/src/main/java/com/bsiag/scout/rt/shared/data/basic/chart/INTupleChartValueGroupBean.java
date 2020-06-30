/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.shared.data.basic.chart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface INTupleChartValueGroupBean extends IChartValueGroupBean {

  int getN();

  List<String> getIdentifiers();

  List<Map<String, BigDecimal>> getValues();

  void add(BigDecimal... tupleValues);
}
