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
package org.eclipse.scout.rt.ui.rap.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.ext.StatusLabelEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;

public final class LogicalGridDataBuilder {
  /**
   * <p>
   * x-position of the field.
   * </p>
   * Position is 1 because there can be a label on the left side
   */
  public static final int FIELD_GRID_X = 1;

  /**
   * <p>
   * y-position of the field.
   * </p>
   * Position is 1 because there can be a label on top
   */
  public static final int FIELD_GRID_Y = 1;

  private LogicalGridDataBuilder() {
  }

  /**
   * label and status is combined in one component {@link StatusLabelEx}
   */
  public static LogicalGridData createLabel(GridData correspondingFieldData) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X - 1;
    data.gridy = FIELD_GRID_Y;
    data.gridh = correspondingFieldData.h;
    data.weighty = 1.0;
    data.widthHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldLabelWidth();
    data.useUiWidth = true;
    data.useUiHeight = true;
    data.fillVertical = false;
    data.topInset = 1;
    return data;
  }

  public static LogicalGridData createLabelOnTop(GridData correspondingFieldData) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X;
    data.gridy = FIELD_GRID_Y - 1;
    data.weighty = 0.0;
    data.weightx = 1.0;
    data.useUiWidth = true;
    data.useUiHeight = true;
    data.fillVertical = true;
    data.fillHorizontal = true;
    return data;
  }

  /**
   * @param gd
   *          is only used for the properties useUiWidth and useUiHeight and the
   *          weights
   */
  public static LogicalGridData createField(GridData gd) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X;
    data.gridy = FIELD_GRID_Y;
    data.weightx = 1.0;
    data.gridh = gd.h;
    if (gd.weightY == 0 || gd.weightY < 0 && gd.h <= 1) {
      data.weighty = 0;
    }
    else {
      data.weighty = 1.0;
    }
    data.useUiWidth = gd.useUiWidth;
    data.useUiHeight = gd.useUiHeight;
    return data;
  }

  public static LogicalGridData createButton1() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X + 1;
    data.gridy = FIELD_GRID_Y;
    data.heightHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonHeight();
    data.widthHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonWidth();
    data.fillVertical = false;
    data.fillHorizontal = false;
    return data;
  }

  public static LogicalGridData createButton2() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X + 2;
    data.gridy = FIELD_GRID_Y;
    data.heightHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonHeight();
    data.widthHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonWidth();
    data.fillVertical = false;
    data.fillHorizontal = false;
    return data;
  }

  public static LogicalGridData createSmartButton() {
    LogicalGridData data = new LogicalGridData();
    data.gridx = FIELD_GRID_X + 1;
    data.gridy = FIELD_GRID_Y;
    data.heightHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonHeight();
    data.widthHint = UiDecorationExtensionPoint.getLookAndFeel().getFormFieldActivationButtonWithMenuWidth();
    data.fillVertical = false;
    data.fillHorizontal = false;
    return data;
  }
}
