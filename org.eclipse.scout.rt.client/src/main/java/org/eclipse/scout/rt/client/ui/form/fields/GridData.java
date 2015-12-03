/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

  public GridData(int x, int y, int w, int h, double weightX, double weightY, boolean useUiWidth, boolean useUiHeight, int horizontalAlignment, int verticalAlignment, boolean fillHorizontal, boolean fillVertical, int widthInPixel,
      int heightInPixel) {
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (fillHorizontal ? 1231 : 1237);
    result = prime * result + (fillVertical ? 1231 : 1237);
    result = prime * result + h;
    result = prime * result + heightInPixel;
    result = prime * result + horizontalAlignment;
    result = prime * result + (useUiHeight ? 1231 : 1237);
    result = prime * result + (useUiWidth ? 1231 : 1237);
    result = prime * result + verticalAlignment;
    result = prime * result + w;
    long temp;
    temp = Double.doubleToLongBits(weightX);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(weightY);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + widthInPixel;
    result = prime * result + x;
    result = prime * result + y;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GridData other = (GridData) obj;
    if (fillHorizontal != other.fillHorizontal) {
      return false;
    }
    if (fillVertical != other.fillVertical) {
      return false;
    }
    if (h != other.h) {
      return false;
    }
    if (heightInPixel != other.heightInPixel) {
      return false;
    }
    if (horizontalAlignment != other.horizontalAlignment) {
      return false;
    }
    if (useUiHeight != other.useUiHeight) {
      return false;
    }
    if (useUiWidth != other.useUiWidth) {
      return false;
    }
    if (verticalAlignment != other.verticalAlignment) {
      return false;
    }
    if (w != other.w) {
      return false;
    }
    if (Double.doubleToLongBits(weightX) != Double.doubleToLongBits(other.weightX)) {
      return false;
    }
    if (Double.doubleToLongBits(weightY) != Double.doubleToLongBits(other.weightY)) {
      return false;
    }
    if (widthInPixel != other.widthInPixel) {
      return false;
    }
    if (x != other.x) {
      return false;
    }
    if (y != other.y) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", weightX=" + weightX + ", weightY=" + weightY + ", useUiWidth=" + useUiWidth + ", useUiHeight=" + useUiHeight + "]";
  }
}
