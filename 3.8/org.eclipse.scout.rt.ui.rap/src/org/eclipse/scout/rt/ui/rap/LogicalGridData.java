/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap;

public class LogicalGridData {
  public static final String CLIENT_PROPERTY_NAME = LogicalGridData.class.getName();

  public int gridx;
  public int gridy;
  public int gridw = 1;
  public int gridh = 1;
  public double weightx;
  public double weighty;
  public boolean useUiWidth;
  public boolean useUiHeight;
  public int widthHint;
  public int heightHint;
  /**
   * -1 left 0 center 1 right
   */
  public int horizontalAlignment = -1;
  /**
   * -1 top 0 center 1 bottom
   */
  public int verticalAlignment = -1;
  public boolean fillHorizontal = true;
  public boolean fillVertical = true;
  //
  public int topInset;

  public LogicalGridData() {
  }

  public LogicalGridData(LogicalGridData template) {
    gridx = template.gridx;
    gridy = template.gridy;
    gridw = template.gridw;
    gridh = template.gridh;
    weightx = template.weightx;
    weighty = template.weighty;
    useUiWidth = template.useUiWidth;
    useUiHeight = template.useUiHeight;
    widthHint = template.widthHint;
    heightHint = template.heightHint;
    horizontalAlignment = template.horizontalAlignment;
    verticalAlignment = template.verticalAlignment;
    fillHorizontal = template.fillHorizontal;
    fillVertical = template.fillVertical;
    topInset = template.topInset;
  }

  public void validate() {
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + gridx + ", " + gridy + ", " + gridw + ", " + gridh + ", " + weightx + ", " + weighty + ", useUiWidth=" + useUiWidth + ", useUiHeight=" + useUiHeight + ", widthHint=" + widthHint + ", heightHint=" + heightHint + ", fillHorizontal=" + fillHorizontal + ", fillVertical=" + fillVertical + "]";
  }

}
