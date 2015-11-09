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
package org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.internal;

import java.util.ArrayList;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grid (model) layout of radio button group only visible process-buttons are used
 */
public class RadioButtonGroupGrid {
  private static final Logger LOG = LoggerFactory.getLogger(RadioButtonGroupGrid.class);

  private ICompositeField m_group = null;
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  public RadioButtonGroupGrid(ICompositeField group) {
    m_group = group;
  }

  public void validate() {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    ArrayList<IFormField> list = new ArrayList<IFormField>();
    // filter
    for (IFormField f : m_group.getFields()) {
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
    GridData parentData = m_group.getGridData();
    if (parentData.h <= 0) {
      LOG.error("{} has gridData.h={}; expected value>0", m_group.getClass().getName(), parentData.h);
      m_gridRows = 1;
    }
    else if (m_fields.length <= 0) {
      LOG.error("{} has fieldCount={}; expected value>0", m_group.getClass().getName(), m_fields.length);
      m_gridRows = 1;
    }
    else {
      m_gridRows = Math.min(parentData.h, m_fields.length);
    }
    m_gridColumns = (m_fields.length + m_gridRows - 1) / m_gridRows;
    int i = 0;
    for (int r = 0; r < m_gridRows; r++) {
      for (int c = 0; c < m_gridColumns; c++) {
        if (i < m_fields.length) {
          GridData data = GridDataBuilder.createFromHints(m_fields[i], 1);
          data.x = c;
          data.y = r;
          m_fields[i].setGridDataInternal(data);
          i++;
        }
        else {
          break;
        }
      }
    }
  }

  public int getGridColumnCount() {
    return m_gridColumns;
  }

  public int getGridRowCount() {
    return m_gridRows;
  }
}
