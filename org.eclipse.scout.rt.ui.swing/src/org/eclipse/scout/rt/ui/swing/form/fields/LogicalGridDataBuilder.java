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
package org.eclipse.scout.rt.ui.swing.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.LogicalGridData;
import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;

public final class LogicalGridDataBuilder {

  private LogicalGridDataBuilder() {
  }

  public static LogicalGridData createLabel(ISwingEnvironment env, GridData correspondingFieldData) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 0;
    data.gridy = 1;
    data.gridh = correspondingFieldData.h;
    data.weighty = 1.0;
    data.widthHint = env.getFieldLabelWidth();
    data.topInset = SwingLayoutUtility.getTextFieldTopInset();
    data.useUiWidth = true;
    data.useUiHeight = true;
    data.fillVertical = false;
    return data;
  }

  public static LogicalGridData createLabelOnTop(GridData correspondingFieldData) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 1;
    data.gridy = 0;
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
  public static LogicalGridData createField(ISwingEnvironment env, GridData correspondingFieldData) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 1;
    data.gridy = 1;
    data.weightx = 1.0;
    data.gridh = correspondingFieldData.h;
    if (correspondingFieldData.weightY == 0 || (correspondingFieldData.weightY < 0 && correspondingFieldData.h <= 1)) {
      data.weighty = 0;
    }
    else {
      data.weighty = 1.0;
    }
    data.useUiWidth = correspondingFieldData.useUiWidth;
    data.useUiHeight = correspondingFieldData.useUiHeight;
    return data;
  }

  public static LogicalGridData createButton1(ISwingEnvironment env) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 2;
    data.gridy = 1;
    data.fillVertical = false;
    data.useUiWidth = true;
    data.useUiHeight = true;
    return data;
  }

  public static LogicalGridData createButton2(ISwingEnvironment env) {
    LogicalGridData data = new LogicalGridData();
    data.gridx = 3;
    data.gridy = 1;
    data.useUiWidth = true;
    data.useUiHeight = true;
    data.fillVertical = false;
    return data;
  }

}
