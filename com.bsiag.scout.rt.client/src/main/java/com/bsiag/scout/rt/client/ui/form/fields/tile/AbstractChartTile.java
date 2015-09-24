/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.scout.rt.client.ui.form.fields.tile;

import com.bsiag.scout.rt.client.ui.form.fields.chartfield.AbstractChart;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.AbstractChartField;
import com.bsiag.scout.rt.client.ui.form.fields.chartfield.IChartField;

/**
 * @since 5.2
 */
public abstract class AbstractChartTile extends AbstractTile<IChartField<AbstractChartTile.ChartField.Chart>> {

  public AbstractChartTile() {
    this(true);
  }

  public AbstractChartTile(boolean callInitializer) {
    super(callInitializer);
  }

  public class ChartField extends AbstractChartField<ChartField.Chart> {

    public class Chart extends AbstractChart {

    }
  }
}
