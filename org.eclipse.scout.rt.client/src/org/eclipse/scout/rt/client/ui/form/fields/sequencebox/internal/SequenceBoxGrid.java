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
package org.eclipse.scout.rt.client.ui.form.fields.sequencebox.internal;

import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;

/**
 * Grid (model) layout of sequence box only visible process-buttons are used
 */
public class SequenceBoxGrid {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SequenceBoxGrid.class);

  private ISequenceBox m_rangeBox = null;
  private IFormField[] m_fields;
  private int m_gridColumns;
  private int m_gridRows;

  public SequenceBoxGrid(ISequenceBox rangeBox) {
    m_rangeBox = rangeBox;
  }

  public void validate() {
    // reset
    m_gridColumns = 0;
    m_gridRows = 0;
    ArrayList<IFormField> list = new ArrayList<IFormField>();
    // filter
    for (IFormField f : m_rangeBox.getFields()) {
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
    for (int i = 0; i < m_fields.length; i++) {
      GridData data = GridDataBuilder.createFromHints(m_fields[i], 1);
      data.x = x;
      data.y = 0;
      if (data.weightX < 0) {
        if (m_fields[i] instanceof IButton) {
          data.weightX = 0;
        }
        else {
          data.weightX = data.w;
        }
      }
      m_fields[i].setGridDataInternal(data);
      x = x + data.w;
      m_gridRows = Math.max(m_gridRows, data.h);
    }
    m_gridColumns = x;
  }

  public int getGridColumnCount() {
    return m_gridColumns;
  }

  public int getGridRowCount() {
    return m_gridRows;
  }
}
