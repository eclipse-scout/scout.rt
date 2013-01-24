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
package org.eclipse.scout.rt.client.ui.form.fields.snapbox.internal;

import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.snapbox.ISnapBox;

/**
 * Grid (model) layout of snap box only visible process-buttons are used
 */
public class SnapBoxGrid {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SnapBoxGrid.class);

  private ISnapBox m_snapBox = null;
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  public SnapBoxGrid(ISnapBox snapBox) {
    m_snapBox = snapBox;
  }

  public void validate() {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    ArrayList<IFormField> list = new ArrayList<IFormField>();
    // filter
    for (IFormField f : m_snapBox.getFields()) {
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
    int y = 0;
    for (int i = 0; i < m_fields.length; i++) {
      GridData data = GridDataBuilder.createFromHints(m_fields[i], 1);
      data.x = 0;
      data.y = y;
      m_fields[i].setGridDataInternal(data);
      y = y + data.h;
      m_gridColumns = Math.max(m_gridColumns, data.w);
    }
    m_gridRows = y;
  }

  public int getGridColumnCount() {
    return m_gridColumns;
  }

  public int getGridRowCount() {
    return m_gridRows;
  }
}
