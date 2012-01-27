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
package org.eclipse.scout.rt.ui.swing.form.fields.chartbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JComponent;

public class SampleChart extends JComponent implements ISwingChartProvider {
  private static final long serialVersionUID = 1L;

  public SampleChart() {
  }

  @Override
  public JComponent createChart(ISwingScoutChartBox swingParent) {
    return this;
  }

  @Override
  public JComponent refreshChart(JComponent chartPane) {
    // nop
    return this;
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(60, 60);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(300, 300);
  }

  @Override
  public Dimension getMaximumSize() {
    return new Dimension(10240, 10240);
  }

  @Override
  protected void paintComponent(Graphics g) {
    int h = getHeight();
    int w = getWidth();
    for (int y = 0; y <= h; y++) {
      int k = y * 55 / h + 200;
      g.setColor(new Color(k, k, 255));
      g.drawLine(0, y, w, y);
    }
    g.setColor(new Color(255, 100, 0));
    Random r = new Random();
    int y = h / 2;
    for (int x = 0; x < w; x++) {
      int yold = y;
      y = y + r.nextInt(7) - 3;
      g.drawLine(x, yold, x, y);
    }
  }

}
