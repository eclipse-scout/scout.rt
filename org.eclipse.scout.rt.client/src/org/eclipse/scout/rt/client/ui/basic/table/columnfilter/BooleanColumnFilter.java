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
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * Checkboxes, Boolean values
 */
public class BooleanColumnFilter<T extends Comparable<Boolean>> implements ITableColumnFilter<Boolean>, Serializable {
  private static final long serialVersionUID = 1L;
  private IColumn<Boolean> m_column;
  private Set<Boolean> m_selectedValues;

  public BooleanColumnFilter(IColumn<Boolean> column) {
    m_column = column;
  }

  @Override
  public IColumn<Boolean> getColumn() {
    return m_column;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setColumn(IColumn column) {
    m_column = column;
  }

  @Override
  public Set<Boolean> getSelectedValues() {
    return m_selectedValues;
  }

  @Override
  public void setSelectedValues(Set<Boolean> set) {
    m_selectedValues = set;
  }

  @Override
  public List<LookupRow> createHistogram() {
    TreeMap<Boolean, LookupRow> hist = new TreeMap<Boolean, LookupRow>();
    HashMap<Boolean, Integer> countMap = new HashMap<Boolean, Integer>();
    hist.put(true, new LookupRow(true, "(" + ScoutTexts.get("ColumnFilterCheckedText") + ")"));
    hist.put(false, new LookupRow(false, "(" + ScoutTexts.get("ColumnFilterUncheckedText") + ")"));
    for (ITableRow row : m_column.getTable().getRows()) {
      Boolean key = BooleanUtility.nvl(m_column.getValue(row), false);
      Integer count = countMap.get(key);
      countMap.put(key, count != null ? count + 1 : 1);
    }
    for (Map.Entry<Boolean, LookupRow> e : hist.entrySet()) {
      Integer count = countMap.get(e.getKey());
      if (count != null && count > 1) {
        LookupRow row = e.getValue();
        row.setText(row.getText() + " (" + count + ")");
      }
    }
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    list.addAll(hist.values());
    return list;
  }

  @Override
  public boolean isEmpty() {
    return (m_selectedValues == null || m_selectedValues.isEmpty());
  }

  @Override
  public boolean accept(ITableRow row) {
    Boolean value = BooleanUtility.nvl(m_column.getValue(row), false);
    if (m_selectedValues != null) {
      if (!m_selectedValues.contains(value)) {
        return false;
      }
    }
    return true;
  }

}
