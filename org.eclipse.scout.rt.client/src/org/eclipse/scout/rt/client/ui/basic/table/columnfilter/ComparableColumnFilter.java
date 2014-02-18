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

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 *
 */
public class ComparableColumnFilter<T extends Comparable<T>> implements ITableColumnFilter<T>, Serializable {
  private static final long serialVersionUID = 1L;
  private IColumn<T> m_column;
  private Set<T> m_selectedValues;
  private T m_minimumValue;
  private T m_maximumValue;

  public ComparableColumnFilter(IColumn<T> column) {
    m_column = column;
  }

  @Override
  public IColumn<T> getColumn() {
    return m_column;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setColumn(IColumn column) {
    m_column = column;
  }

  @Override
  public Set<T> getSelectedValues() {
    return m_selectedValues;
  }

  @Override
  public void setSelectedValues(Set<T> set) {
    m_selectedValues = set;
  }

  public T getMinimumValue() {
    return m_minimumValue;
  }

  public void setMinimumValue(T minimumValue) {
    m_minimumValue = minimumValue;
  }

  public T getMaximumValue() {
    return m_maximumValue;
  }

  public void setMaximumValue(T maximumValue) {
    m_maximumValue = maximumValue;
  }

  @Override
  public List<LookupRow<T>> createHistogram() {
    Map<T, LookupRow<T>> hist = new TreeMap<T, LookupRow<T>>();
    Map<T, Integer> countMap = new HashMap<T, Integer>();
    for (ITableRow row : m_column.getTable().getRows()) {
      T key = m_column.getValue(row);
      String text = m_column.getDisplayText(row);
      if (key != null && !hist.containsKey(key)) {
        FontSpec font = (row.isFilterAccepted() ? null : FontSpec.parse("italic"));
        hist.put(key, new LookupRow<T>(key, text, null, null, null, null, font));
      }
      Integer count = countMap.get(key);
      countMap.put(key, count != null ? count + 1 : 1);
    }
    for (Map.Entry<T, LookupRow<T>> e : hist.entrySet()) {
      Integer count = countMap.get(e.getKey());
      if (count != null && count > 1) {
        e.getValue().setText(e.getValue().getText() + " (" + count + ")");
      }
    }
    List<LookupRow<T>> list = new ArrayList<LookupRow<T>>();
    list.addAll(hist.values());
    //
    Integer nullCount = countMap.get(null);
    list.add(new LookupRow<T>(null, "(" + ScoutTexts.get("ColumnFilterNullText") + ")" + (nullCount != null && nullCount > 1 ? " (" + nullCount + ")" : "")));
    return list;
  }

  @Override
  public boolean isEmpty() {
    return (m_selectedValues == null || m_selectedValues.isEmpty()) && m_minimumValue == null && m_maximumValue == null;
  }

  @Override
  public boolean accept(ITableRow row) {
    T value = m_column.getValue(row);
    if (m_minimumValue != null) {
      if (value == null) {
        return false;
      }
      if (value.compareTo(m_minimumValue) < 0) {
        return false;
      }
    }
    if (m_maximumValue != null) {
      if (value == null) {
        return false;
      }
      if (value.compareTo(m_maximumValue) > 0) {
        return false;
      }
    }
    if (m_selectedValues != null) {
      if (!m_selectedValues.contains(value)) {
        return false;
      }
    }
    return true;
  }

}
