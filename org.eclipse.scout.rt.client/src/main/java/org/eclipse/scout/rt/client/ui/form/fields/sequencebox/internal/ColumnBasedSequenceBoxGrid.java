/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox.internal;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.HorizontalGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.matrix.HorizontalGridMatrix;

/**
 * Alternative sequence box grid that behaves in the same way as {@link HorizontalGroupBoxBodyGrid}. <br>
 * Compared to the default SequenceBoxGrid, there is no special treatment for {@link IButton} regarding weightX and
 * useUiWidth. You can set the button's weightX to 0 manually but consider that it will affect every field in the same
 * column. <br>
 * Also you need to make sure the height will be correct, e.g. by setting useUiHeight to true (which is done by a
 * {@link IGroupBox} by default).
 */
public class ColumnBasedSequenceBoxGrid extends SequenceBoxGrid {
  private int m_gridColumnCount;

  public ColumnBasedSequenceBoxGrid(int gridColumnCount) {
    m_gridColumnCount = gridColumnCount;
  }

  @Override
  protected void layoutStatic(List<IFormField> fields) {
    HorizontalGridMatrix matrix = new HorizontalGridMatrix(m_gridColumnCount);
    matrix.computeGridData(fields);
    setGridRows(matrix.getRowCount());
    setGridColumns(matrix.getColumnCount());
  }
}
