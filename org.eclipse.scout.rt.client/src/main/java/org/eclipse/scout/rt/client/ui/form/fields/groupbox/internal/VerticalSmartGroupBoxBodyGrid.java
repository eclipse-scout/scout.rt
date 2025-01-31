/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal;

import static org.eclipse.scout.rt.platform.util.NumberUtility.divideAndCeil;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix.VerticalGridMatrix;

/**
 * @author Andreas Hoegger
 * @since 4.0.0 M6 25.02.2014
 */
public class VerticalSmartGroupBoxBodyGrid extends AbstractGroupBoxBodyGrid {

  @Override
  protected void layoutAllDynamic(List<IFormField> fields) {
    // calculate the used cells
    int cellCount = 0;
    for (IFormField f : fields) {
      GridData hints = getGridDataFromHints(f, getGridColumnCount());
      cellCount += hints.w * hints.h;
    }

    int rowCount = divideAndCeil(cellCount, getGridColumnCount());
    VerticalGridMatrix matrix = new VerticalGridMatrix(getGridColumnCount(), rowCount);
    while (!matrix.computeGridData(fields)) {
      matrix.resetAll(getGridColumnCount(), ++rowCount);
    }
    // setGridData
    for (IFormField f : fields) {
      GridData data = matrix.getGridData(f);
      f.setGridDataInternal(data);
    }
    setGridRows(matrix.getRowCount());
  }
}
