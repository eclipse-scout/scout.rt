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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * @deprecated This class will be removed in the next release because it is not used anymore.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class SmartColumnFilter<T> implements ITableColumnFilter<T> {
  private ISmartColumn<T> m_column;
  private Set<T> m_selectedValues;

  public SmartColumnFilter(ISmartColumn<T> column) {
    m_column = column;
  }

  @Override
  public IColumn<T> getColumn() {
    return m_column;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setColumn(IColumn column) {
    if (column instanceof ISmartColumn) {
      m_column = (ISmartColumn<T>) column;
    }
    else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public Set<T> getSelectedValues() {
    return m_selectedValues;
  }

  @Override
  public void setSelectedValues(Set<T> set) {
    m_selectedValues = set;
  }

  @Override
  public List<LookupRow> createHistogram() {
    TreeMap<String, LookupRow> hist = new TreeMap<String, LookupRow>();
    HashMap<String, Integer> countMap = new HashMap<String, Integer>();
    for (ITableRow row : m_column.getTable().getRows()) {
      T key = m_column.getValue(row);
      String text = m_column.getDisplayText(row);
      if (text != null && !hist.containsKey(text)) {
        FontSpec font = (row.isFilterAccepted() ? null : FontSpec.parse("italic"));
        hist.put(text, new LookupRow(key, text, null, null, null, null, font));
      }
      Integer count = countMap.get(text);
      countMap.put(text, count != null ? count + 1 : 1);
    }
    for (Map.Entry<String, LookupRow> e : hist.entrySet()) {
      Integer count = countMap.get(e.getKey());
      if (count != null && count > 1) {
        LookupRow row = e.getValue();
        row.setText(row.getText() + " (" + count + ")");
      }
    }
    ArrayList<LookupRow> list = new ArrayList<LookupRow>();
    list.addAll(hist.values());
    //
    Integer nullCount = countMap.get(null);
    list.add(new LookupRow(null, "(" + ScoutTexts.get("ColumnFilterNullText") + ")" + (nullCount != null && nullCount > 1 ? " (" + nullCount + ")" : "")));
    return list;
  }

  @Override
  public boolean isEmpty() {
    return m_selectedValues == null || m_selectedValues.isEmpty();
  }

  @Override
  public boolean accept(ITableRow row) {
    T value = m_column.getValue(row);
    if (m_selectedValues != null) {
      if (!m_selectedValues.contains(value)) {
        return false;
      }
    }
    return true;
  }

}
