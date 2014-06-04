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
package org.eclipse.scout.rt.client.ui.html.client.ext;

public class ChartTableControl extends AbstractTableControl implements IChartTableControl {

  public ChartTableControl() {
    this(true);
  }

  public ChartTableControl(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setLabel("Diagramm");
    setCssClass("control-chart");
    setGroup("Anzeige");
  }

}
