/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tabbox.internal;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * Grid (model) layout of tab box only visible process-buttons are used
 */
public class TabBoxGrid implements ICompositeFieldGrid<ITabBox> {
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  @Override
  public void validate(ITabBox tabBox) {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    ArrayList<IFormField> list = new ArrayList<>();
    // filter
    for (IFormField f : tabBox.getGroupBoxes()) {
      if (f.isVisible()) {
        list.add(f);
      }
      else {
        GridData data = GridDataBuilder.createFromHints(f, 1);
        f.setGridDataInternal(data);
      }
    }
    m_fields = list.toArray(new IFormField[0]);
    layoutStatic();
  }

  private void layoutStatic() {
    for (IFormField m_field1 : m_fields) {
      GridData data = GridDataBuilder.createFromHints(m_field1, 1);
      m_gridRows = Math.max(m_gridRows, data.h);
      m_gridColumns = Math.max(m_gridColumns, data.w);
    }
    for (IFormField m_field : m_fields) {
      GridData data = GridDataBuilder.createFromHints(m_field, m_gridColumns);
      data.x = 0;
      data.y = 0;
      m_field.setGridDataInternal(data);
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
