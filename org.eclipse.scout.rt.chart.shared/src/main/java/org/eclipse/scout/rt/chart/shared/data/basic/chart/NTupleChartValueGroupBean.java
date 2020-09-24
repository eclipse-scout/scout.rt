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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class NTupleChartValueGroupBean extends AbstractChartValueGroupBean implements INTupleChartValueGroupBean {
  private static final long serialVersionUID = 1L;

  private final List<Map<String, BigDecimal>> m_values = new ArrayList<>();
  private final int m_n;
  private final List<String> m_identifiers;

  public NTupleChartValueGroupBean(int n, String... identifiers) {
    if (n != identifiers.length) {
      throw new IllegalArgumentException("The number of identifiers is unequal to the dimension.");
    }
    List<String> identifiersList = CollectionUtility.arrayList(identifiers);
    if (identifiersList.contains(null)) {
      throw new IllegalArgumentException("At least one identifier is null.");
    }
    if (new HashSet<>(identifiersList).size() < identifiersList.size()) {
      throw new IllegalArgumentException("At least one identifier is not unique.");
    }
    m_n = n;
    m_identifiers = identifiersList;
  }

  @Override
  public int getN() {
    return m_n;
  }

  @Override
  public List<String> getIdentifiers() {
    return Collections.unmodifiableList(m_identifiers);
  }

  @Override
  public List<Map<String, BigDecimal>> getValues() {
    return Collections.unmodifiableList(m_values);
  }

  @Override
  public void add(BigDecimal... tupleValues) {
    if (m_n != tupleValues.length) {
      throw new IllegalArgumentException("The number of tuple values is unequal to the dimension.");
    }

    Map<String, BigDecimal> tuple = new HashMap<>();
    for (int i = 0; i < tupleValues.length; i++) {
      tuple.put(m_identifiers.get(i), tupleValues[i]);
    }

    m_values.add(tuple);
  }
}
