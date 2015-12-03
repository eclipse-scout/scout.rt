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
package org.eclipse.scout.rt.server.jdbc.fixture;

import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

/**
 * Table generated as {@link AbstractTableFieldData} (before Luna)
 */
public class TableFieldData extends AbstractTableFieldData {

  private static final long serialVersionUID = 1L;
  public static final int ACTIVE_COLUMN_ID = 0;
  public static final int STATE_COLUMN_ID = 1;
  public static final int NAME_COLUMN_ID = 2;

  public TableFieldData() {
  }

  public Boolean getActive(int row) {
    return (Boolean) getValueInternal(row, ACTIVE_COLUMN_ID);
  }

  public void setActive(int row, Boolean active) {
    setValueInternal(row, ACTIVE_COLUMN_ID, active);
  }

  public String getName(int row) {
    return (String) getValueInternal(row, NAME_COLUMN_ID);
  }

  public void setName(int row, String name) {
    setValueInternal(row, NAME_COLUMN_ID, name);
  }

  public Integer getState(int row) {
    return (Integer) getValueInternal(row, STATE_COLUMN_ID);
  }

  public void setState(int row, Integer state) {
    setValueInternal(row, STATE_COLUMN_ID, state);
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public Object getValueAt(int row, int column) {
    switch (column) {
      case ACTIVE_COLUMN_ID:
        return getActive(row);
      case STATE_COLUMN_ID:
        return getState(row);
      case NAME_COLUMN_ID:
        return getName(row);
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(int row, int column, Object value) {
    switch (column) {
      case ACTIVE_COLUMN_ID:
        setActive(row, (Boolean) value);
        break;
      case STATE_COLUMN_ID:
        setState(row, (Integer) value);
        break;
      case NAME_COLUMN_ID:
        setName(row, (String) value);
        break;
    }
  }
}
