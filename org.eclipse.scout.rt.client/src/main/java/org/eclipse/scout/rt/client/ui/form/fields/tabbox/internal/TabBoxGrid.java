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
package org.eclipse.scout.rt.client.ui.form.fields.tabbox.internal;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;

/**
 * Grid (model) layout of tab box only visible process-buttons are used
 */
public class TabBoxGrid {
  private ITabBox m_tabBox = null;
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  public TabBoxGrid(ITabBox tabBox) {
    m_tabBox = tabBox;
  }

  public void validate() {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    ArrayList<IFormField> list = new ArrayList<IFormField>();
    // filter
    for (IFormField f : m_tabBox.getGroupBoxes()) {
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
    for (int i = 0; i < m_fields.length; i++) {
      GridData data = GridDataBuilder.createFromHints(m_fields[i], 1);
      m_gridRows = Math.max(m_gridRows, data.h);
      m_gridColumns = Math.max(m_gridColumns, data.w);
    }
    for (int i = 0; i < m_fields.length; i++) {
      GridData data = GridDataBuilder.createFromHints(m_fields[i], m_gridColumns);
      data.x = 0;
      data.y = 0;
      m_fields[i].setGridDataInternal(data);
    }
  }

  public int getGridColumnCount() {
    return m_gridColumns;
  }

  public int getGridRowCount() {
    return m_gridRows;
  }
}
