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

import java.util.stream.Stream;

/**
 * Determines what parts of the chart data is colored with the same colors.
 */
public enum ColorMode {
  /**
   * Uses one of the other options depending on the chart type.
   */
  AUTO("auto"),
  /**
   * Uses a different color for each dataset.
   */
  DATASET("dataset"),
  /**
   * Uses a different color for each datapoint in a dataset but the n-th datapoint in each dataset will be colored using the same color.
   */
  DATA("data");

  private final String m_value;

  ColorMode(String value) {
    m_value = value;
  }

  public String getValue() {
    return m_value;
  }

  public static ColorMode parse(String value) {
    return Stream.of(values())
        .filter(colorMode -> colorMode.getValue().equals(value))
        .findFirst()
        .orElse(null);
  }

  /**
   * Utility method returning the "opposite" color mode for this mode.
   */
  public ColorMode toggle() {
    switch (this) {
      case DATASET:
        return DATA;
      case DATA:
        return DATASET;
      default:
        return this; // unknown mode cannot be toggled
    }
  }
}
