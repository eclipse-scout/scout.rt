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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;

/**
 * Grid (model) layout of sequence box only visible process-buttons are used
 */
public class SequenceBoxGrid implements ICompositeFieldGrid<ISequenceBox> {
  private int m_gridColumns;
  private int m_gridRows;

  @Override
  public void validate(ISequenceBox sequenceBox) {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    List<IFormField> list = new ArrayList<>();
    // filter
    for (IFormField f : sequenceBox.getFields()) {
      if (f.isVisible()) {
        list.add(f);
      }
      else {
        GridData data = GridDataBuilder.createFromHints(f, 1);
        f.setGridDataInternal(data);
      }
    }
    layoutStatic(list);
  }

  private void layoutStatic(List<IFormField> fields) {
    int x = 0;
    for (IFormField field : fields) {
      GridData data = GridDataBuilder.createFromHints(field, 1);
      data.x = x;
      data.y = 0;
      if (data.weightX < 0) {
        if (field instanceof IButton) {
          data.useUiWidth = true;
          data.weightX = 0;
        }
        else {
          data.weightX = data.w;
        }
      }
      field.setGridDataInternal(data);
      x = x + data.w;
      m_gridRows = Math.max(m_gridRows, data.h);
    }
    m_gridColumns = x;
  }

  @Override
  public int getGridColumnCount() {
    return m_gridColumns;
  }

  @Override
  public int getGridRowCount() {
    return m_gridRows;
  }
}
