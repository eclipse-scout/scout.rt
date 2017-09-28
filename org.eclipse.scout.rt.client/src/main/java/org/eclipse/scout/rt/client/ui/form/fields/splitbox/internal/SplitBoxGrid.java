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
package org.eclipse.scout.rt.client.ui.form.fields.splitbox.internal;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.ISplitBox;

/**
 * Grid (model) layout of split box only visible parts are used
 */
public class SplitBoxGrid implements ICompositeFieldGrid<ISplitBox> {
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  @Override
  public void validate(ISplitBox splitBox) {
    // reset
    m_gridColumns = 2;
    m_gridRows = 1;
    ArrayList<IFormField> list = new ArrayList<>();
    // filter
    for (IFormField f : splitBox.getFields()) {
      if (f.isVisible()) {
        list.add(f);
      }
      else {
        GridData data = GridDataBuilder.createFromHints(f, 1);
        f.setGridDataInternal(data);
      }
    }
    m_fields = list.toArray(new IFormField[list.size()]);
    layoutStatic();
  }

  private void layoutStatic() {
    int x = 0;
    for (IFormField m_field : m_fields) {
      GridData data = GridDataBuilder.createFromHints(m_field, 1);
      data.x = x;
      data.y = 0;
      if (data.weightX < 0) {
        data.weightX = data.w;
      }
      m_field.setGridDataInternal(data);
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
