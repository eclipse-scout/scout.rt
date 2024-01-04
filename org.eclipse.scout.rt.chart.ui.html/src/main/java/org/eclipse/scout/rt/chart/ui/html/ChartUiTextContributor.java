/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.chart.ui.html;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.scout.rt.ui.html.IUiTextContributor;

public class ChartUiTextContributor implements IUiTextContributor {

  @Override
  public void contribute(Set<String> textKeys) {
    textKeys.addAll(Arrays.asList(
        "ui.Bio",
        "ui.Brd",
        "ui.Chart",
        "ui.FulfillmentChartAriaDescription",
        "ui.Mio",
        "ui.Mrd",
        "ui.SpeedoChartAriaDescription",
        "ui.TooMuchData",
        "ui.Tri",
        "ui.Trd",
        "ui.Value",
        "ui.bar",
        "ui.groupedByWeekday",
        "ui.groupedByMonth",
        "ui.groupedByYear",
        "ui.groupedByDate",
        "ui.line"));
  }
}
