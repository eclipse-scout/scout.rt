/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public abstract class AbstractGroupBoxBodyGrid implements IGroupBoxBodyGrid {

  private int m_gridRows;
  private int m_gridColumns;

  protected void setGridColumns(int gridColumns) {
    m_gridColumns = gridColumns;
  }

  @Override
  public int getGridColumnCount() {
    return m_gridColumns;
  }

  protected void setGridRows(int gridRows) {
    m_gridRows = gridRows;
  }

  @Override
  public int getGridRowCount() {
    return m_gridRows;
  }

  @Override
  public void validate(IGroupBox groupBox) {
    // reset old state
    setGridRows(0);
    // STEP 0: column count
    setGridColumns(computGridColumnCount(groupBox));
    int containingGridXYCount = 0;
    int notContainingGridXYCount = 0;
    // build
    List<IFormField> fieldsExceptProcessButtons = new ArrayList<>();
    for (IFormField formField : groupBox.getFields()) {
      if (formField.isVisible()) {
        if (!isProcessButton(formField)) {
          fieldsExceptProcessButtons.add(formField);
          GridData hints = formField.getGridDataHints();
          if (hints.x >= 0 && hints.y >= 0) {
            containingGridXYCount++;
          }
          else {
            notContainingGridXYCount++;
          }
        }
      }
      else {
        GridData data = GridDataBuilder.createFromHints(formField, 1);
        formField.setGridDataInternal(data);
      }
    }
    boolean isContainingXAndY = (containingGridXYCount > 0 && notContainingGridXYCount == 0);
    if (isContainingXAndY) {
      layoutAllStatic(fieldsExceptProcessButtons);
    }
    else {
      layoutAllDynamic(fieldsExceptProcessButtons);
    }
  }

  /**
   * @param formField
   * @return
   */
  protected boolean isProcessButton(IFormField formField) {
    return (formField instanceof IButton) && (((IButton) formField).isProcessButton());
  }

  /**
   * @param groupBox
   * @return
   */
  protected int computGridColumnCount(IGroupBox groupBox) {
    int gridColumns = -1;
    IGroupBox tmp = groupBox;
    while (gridColumns < 0 && tmp != null) {
      gridColumns = tmp.getGridColumnCount();
      tmp = tmp.getParentGroupBox();
    }
    if (gridColumns < 0) {
      gridColumns = 2;
    }
    return gridColumns;
  }

  /**
   * Make layout based on grid-x, grid-y, grid-w and grid-h No auto-layout
   */
  private void layoutAllStatic(List<IFormField> fields) {
    int totalGridW = 1;
    int totalGridH = 0;
    for (IFormField f : fields) {
      GridData hints = GridDataBuilder.createFromHints(f, 1);
      totalGridW = Math.max(totalGridW, hints.x + hints.w);
      totalGridH = Math.max(totalGridH, hints.y + hints.h);
    }

    for (IFormField f : fields) {
      GridData hints = GridDataBuilder.createFromHints(f, totalGridW);
      f.setGridDataInternal(hints);
    }
    setGridColumns(totalGridW);
    setGridRows(totalGridH);
  }

  protected abstract void layoutAllDynamic(List<IFormField> fields);

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getGridRowCount() + " " + getGridColumnCount() + "]";
  }

  /**
   * if grid w greater than group box column count. Grid w will be set to group box column count.
   */
  public static GridData getGridDataFromHints(IFormField field, int groupBoxColumnCount) {
    GridData data = GridDataBuilder.createFromHints(field, groupBoxColumnCount);
    data.w = Math.min(groupBoxColumnCount, data.w);
    return data;
  }
}
