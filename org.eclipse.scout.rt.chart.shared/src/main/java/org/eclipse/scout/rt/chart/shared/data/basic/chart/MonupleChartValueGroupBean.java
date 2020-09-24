/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
