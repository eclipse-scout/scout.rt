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
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal;

import static org.eclipse.scout.rt.platform.util.NumberUtility.divideAndCeil;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;

/**
 * Grid (model) layout of radio button group only visible process-buttons are used. This class distributes all buttons
 * (= fields of the group) over the available space, which is the gridH attribute of the group. For each button gridW
 * and gridH is set to 1, other configured values for these properties are ignored. Also gridX and gridY is set on the
 * field.
 */
public class RadioButtonGroupGrid implements ICompositeFieldGrid<ICompositeField> {

  private IRadioButtonGroup<?> m_group = null;
  private List<IFormField> m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  @Override
  public void validate(ICompositeField compositeField) {
    // reset
    m_group = (IRadioButtonGroup<?>) compositeField;
    m_gridColumns = 0;
    m_gridRows = 0;
    List<IFormField> fields = m_group.getFields();

    // filter
    List<IFormField> list = new ArrayList<>(fields.size());
    for (IFormField f : fields) {
      if (f.isVisible()) {
        list.add(f);
      }
      else {
        GridData data = GridDataBuilder.createFromHints(f, 1);
        f.setGridDataInternal(data);
      }
    }

    m_fields = list;
    applyGridData();
  }

  /**
   * Sets GridData on each button (= field) of the group.
   */
  protected void applyGridData() {
    m_gridColumns = m_group.getGridColumnCount();
    if (m_gridColumns == 0) {
      m_gridRows = 0;
    }
    else {
      m_gridRows = divideAndCeil(m_fields.size(), m_gridColumns);
    }
    int i = 0;
    for (int r = 0; r < m_gridRows; r++) {
      for (int c = 0; c < m_gridColumns; c++) {
        if (i < m_fields.size()) {
          GridData data = GridDataBuilder.createFromHints(m_fields.get(i), 1);
          data.x = c;
          data.y = r;
          data.w = 1;
          data.h = 1;
          m_fields.get(i).setGridDataInternal(data);
          i++;
        }
        else {
          break;
        }
      }
    }
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
