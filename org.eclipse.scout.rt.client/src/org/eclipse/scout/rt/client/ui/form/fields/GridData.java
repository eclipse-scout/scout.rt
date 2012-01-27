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
package org.eclipse.scout.rt.client.ui.form.fields;

/**
 * Title: BSI Scout V3
 *  Copyright (c) 2001,2009 BSI AG
 * @version 3.x
 */

/**
 * Grid Cell as container helper for dynamic layouting in scout
 */
public final class GridData {

  public int x;
  public int y;
  public int w;
  public int h;
  public double weightX;
  public double weightY;
  public boolean useUiWidth;
  public boolean useUiHeight;
  /**
   * -1 left 0 center 1 right
   */
  public int horizontalAlignment;
  /**
   * -1 top 0 center 1 bottom
   */
  public int verticalAlignment;
  public boolean fillHorizontal;
  public boolean fillVertical;
  public int widthInPixel;
  public int heightInPixel;

  public GridData(GridData g) {
    this(g.x, g.y, g.w, g.h, g.weightX, g.weightY, g.useUiWidth, g.useUiHeight, g.horizontalAlignment, g.verticalAlignment, g.fillHorizontal, g.fillVertical, g.widthInPixel, g.heightInPixel);
  }

  public GridData(int x, int y, int w, int h, double weightX, double weightY) {
    this(x, y, w, h, weightX, weightY, false, false, -1, -1, true, true, 0, 0);
  }

  public GridData(int x, int y, int w, int h, double weightX, double weightY, boolean useUiWidth, boolean useUiHeight, int horizontalAlignment, int verticalAlignment, boolean fillHorizontal, boolean fillVertical, int widthInPixel, int heightInPixel) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.weightX = weightX;
    this.weightY = weightY;
    this.useUiWidth = useUiWidth;
    this.useUiHeight = useUiHeight;
    this.horizontalAlignment = horizontalAlignment;
    this.verticalAlignment = verticalAlignment;
    this.fillHorizontal = fillHorizontal;
    this.fillVertical = fillVertical;
    this.widthInPixel = widthInPixel;
    this.heightInPixel = heightInPixel;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", weightX=" + weightX + ", weightY=" + weightY + ", useUiWidth=" + useUiWidth + ", useUiHeight=" + useUiHeight + "]";
  }
}
