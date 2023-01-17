/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

/**
 * Grid Cell as container helper for dynamic layouting in scout
 */
@SuppressWarnings({"squid:S00116", "squid:ClassVariableVisibilityCheck"})
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

  @SuppressWarnings("squid:S00107")
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

  public GridData withX(int newX) {
    this.x = newX;
    return this;
  }

  public GridData withY(int newY) {
    this.y = newY;
    return this;
  }

  public GridData withW(int newW) {
    this.w = newW;
    return this;
  }

  public GridData withH(int newH) {
    this.h = newH;
    return this;
  }

  public GridData withWeightX(double newWeightX) {
    this.weightX = newWeightX;
    return this;
  }

  public GridData withWeightY(double newWeightY) {
    this.weightY = newWeightY;
    return this;
  }

  public GridData withUseUiWidth(boolean newUseUiWidth) {
    this.useUiWidth = newUseUiWidth;
    return this;
  }

  public GridData withUseUiHeight(boolean newUseUiHeight) {
    this.useUiHeight = newUseUiHeight;
    return this;
  }

  public GridData withHorizontalAlignment(int newHorizontalAlignment) {
    this.horizontalAlignment = newHorizontalAlignment;
    return this;
  }

  public GridData withVerticalAlignment(int newVerticalAlignment) {
    this.verticalAlignment = newVerticalAlignment;
    return this;
  }

  public GridData withFillHorizontal(boolean newFillHorizontal) {
    this.fillHorizontal = newFillHorizontal;
    return this;
  }

  public GridData withFillVertical(boolean newFillVertical) {
    this.fillVertical = newFillVertical;
    return this;
  }

  public GridData withWidthInPixel(int newWidthInPixel) {
    this.widthInPixel = newWidthInPixel;
    return this;
  }

  public GridData withHeightInPixel(int newHeightInPixel) {
    this.heightInPixel = newHeightInPixel;
    return this;
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

  public GridData copy() {
    return new GridData(this);
  }
}
