/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link LogicalGridData}
 */
public class LogicalGridDataTest {

  private static final boolean INIT_FILL_HORIZONTAL = true;
  private static final boolean INIT_FILL_VERTICAL = true;
  private static final int INIT_GRID_H = 1;
  private static final int INIT_GRID_W = 1;
  private static final int INIT_GRID_X = 0;
  private static final int INIT_GRID_Y = 0;
  private static final int INIT_HEIGHT_HINT = 0;
  private static final int INIT_HORIZONTAL_ALIGNMENT = -1;
  private static final int INIT_TOP_INSET = 0;
  private static final boolean INIT_USE_UI_HEIGHT = false;
  private static final boolean INIT_USE_UI_WIDTH = false;
  private static final int INIT_VERTICAL_ALIGNMENT = -1;
  private static final double INIT_WEIGHT_X = 0.0;
  private static final double INIT_WEIGHT_Y = 0.0;
  private static final int INIT_WIDTH_HINT = 0;

  private static final boolean VAL_FILL_HORIZONTAL = false;
  private static final boolean VAL_FILL_VERTICAL = false;
  private static final int VAL_GRID_H = 2;
  private static final int VAL_GRID_W = 3;
  private static final int VAL_GRID_X = 1;
  private static final int VAL_GRID_Y = 2;
  private static final int VAL_HEIGHT_HINT = 9;
  private static final int VAL_HORIZONTAL_ALIGNMENT = 0;
  private static final int VAL_TOP_INSET = 2;
  private static final boolean VAL_USE_UI_HEIGHT = true;
  private static final boolean VAL_USE_UI_WIDTH = true;
  private static final int VAL_VERTICAL_ALIGNMENT = 1;
  private static final double VAL_WEIGHT_X = 5.6;
  private static final double VAL_WEIGHT_Y = 3.8;
  private static final int VAL_WIDTH_HINT = 1;

  /**
   * Test method for {@link org.eclipse.scout.rt.ui.swt.LogicalGridData#LogicalGridData()}.
   */
  @Test
  public void testLogicalGridData() {
    LogicalGridData data = new LogicalGridData();
    Assert.assertEquals("fillHorizontal", INIT_FILL_HORIZONTAL, data.fillHorizontal);
    Assert.assertEquals("fillVertical", INIT_FILL_VERTICAL, data.fillVertical);
    Assert.assertEquals("gridh", INIT_GRID_H, data.gridh);
    Assert.assertEquals("gridw", INIT_GRID_W, data.gridw);
    Assert.assertEquals("gridx", INIT_GRID_X, data.gridx);
    Assert.assertEquals("gridy", INIT_GRID_Y, data.gridy);
    Assert.assertEquals("heightHint", INIT_HEIGHT_HINT, data.heightHint);
    Assert.assertEquals("horizontalAlignment", INIT_HORIZONTAL_ALIGNMENT, data.horizontalAlignment);
    Assert.assertEquals("topInset", INIT_TOP_INSET, data.topInset);
    Assert.assertEquals("useUiHeight", INIT_USE_UI_HEIGHT, data.useUiHeight);
    Assert.assertEquals("useUiWidth", INIT_USE_UI_WIDTH, data.useUiWidth);
    Assert.assertEquals("verticalAlignment", INIT_VERTICAL_ALIGNMENT, data.verticalAlignment);
    Assert.assertEquals("weightx", INIT_WEIGHT_X, data.weightx, 0.1);
    Assert.assertEquals("weighty", INIT_WEIGHT_Y, data.weighty, 0.1);
    Assert.assertEquals("widthHint", INIT_WIDTH_HINT, data.widthHint);

  }

  /**
   * Test method for
   * {@link org.eclipse.scout.rt.ui.swt.LogicalGridData#LogicalGridData(org.eclipse.scout.rt.ui.swt.LogicalGridData)}.
   */
  @Test
  public void testLogicalGridDataLogicalGridData() {
    LogicalGridData d1 = new LogicalGridData();
    d1.fillHorizontal = VAL_FILL_HORIZONTAL;
    d1.fillVertical = VAL_FILL_VERTICAL;
    d1.gridh = VAL_GRID_H;
    d1.gridw = VAL_GRID_W;
    d1.gridx = VAL_GRID_X;
    d1.gridy = VAL_GRID_Y;
    d1.heightHint = VAL_HEIGHT_HINT;
    d1.horizontalAlignment = VAL_HORIZONTAL_ALIGNMENT;
    d1.topInset = VAL_TOP_INSET;
    d1.useUiHeight = VAL_USE_UI_HEIGHT;
    d1.useUiWidth = VAL_USE_UI_WIDTH;
    d1.verticalAlignment = VAL_VERTICAL_ALIGNMENT;
    d1.weightx = VAL_WEIGHT_X;
    d1.weighty = VAL_WEIGHT_Y;
    d1.widthHint = VAL_WIDTH_HINT;
    assertEqualsValue(d1);

    LogicalGridData d2 = new LogicalGridData(d1);
    Assert.assertNotSame(d1, d2);
    assertEqualsValue(d2);
  }

  private static void assertEqualsValue(LogicalGridData data) {
    Assert.assertEquals("fillHorizontal", VAL_FILL_HORIZONTAL, data.fillHorizontal);
    Assert.assertEquals("fillVertical", VAL_FILL_VERTICAL, data.fillVertical);
    Assert.assertEquals("gridh", VAL_GRID_H, data.gridh);
    Assert.assertEquals("gridw", VAL_GRID_W, data.gridw);
    Assert.assertEquals("gridx", VAL_GRID_X, data.gridx);
    Assert.assertEquals("gridy", VAL_GRID_Y, data.gridy);
    Assert.assertEquals("heightHint", VAL_HEIGHT_HINT, data.heightHint);
    Assert.assertEquals("horizontalAlignment", VAL_HORIZONTAL_ALIGNMENT, data.horizontalAlignment);
    Assert.assertEquals("topInset", VAL_TOP_INSET, data.topInset);
    Assert.assertEquals("useUiHeight", VAL_USE_UI_HEIGHT, data.useUiHeight);
    Assert.assertEquals("useUiWidth", VAL_USE_UI_WIDTH, data.useUiWidth);
    Assert.assertEquals("verticalAlignment", VAL_VERTICAL_ALIGNMENT, data.verticalAlignment);
    Assert.assertEquals("weightx", VAL_WEIGHT_X, data.weightx, 0.1);
    Assert.assertEquals("weighty", VAL_WEIGHT_Y, data.weighty, 0.1);
    Assert.assertEquals("widthHint", VAL_WIDTH_HINT, data.widthHint);
  }

}
