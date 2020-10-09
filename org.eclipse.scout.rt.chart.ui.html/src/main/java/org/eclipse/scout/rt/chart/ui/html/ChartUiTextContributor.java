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
package org.eclipse.scout.rt.chart.ui.html;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.scout.rt.ui.html.IUiTextContributor;

public class ChartUiTextContributor implements IUiTextContributor {

  @Override
  public void contributeUiTextKeys(Set<String> textKeys) {
    textKeys.addAll(Arrays.asList(
        "ui.Bio",
        "ui.Brd",
        "ui.Chart",
        "ui.Mio",
        "ui.Mrd",
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
