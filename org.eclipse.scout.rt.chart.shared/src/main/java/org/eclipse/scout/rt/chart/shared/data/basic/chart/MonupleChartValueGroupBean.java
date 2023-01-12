/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.shared.data.basic.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 5.2
 */
public class MonupleChartValueGroupBean extends AbstractChartValueGroupBean implements IMonupleChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private final List<BigDecimal> m_values = new ArrayList<>();

  @Override
  public List<BigDecimal> getValues() {
    return m_values;
  }
}
