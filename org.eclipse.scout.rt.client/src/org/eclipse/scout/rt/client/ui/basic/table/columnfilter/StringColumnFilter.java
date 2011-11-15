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
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 *
 */
public class StringColumnFilter implements ITableColumnFilter<String> {
  private IColumn<String> m_column;
  private Set<String> m_selectedValues;
  private String m_pattern;
  //cache
  private transient Pattern m_regexPat;

  public StringColumnFilter(IColumn<String> column) {
    m_column = column;
  }

  @Override
  public IColumn<String> getColumn() {
    return m_column;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setColumn(IColumn column) {
    m_column = column;
  }

  @Override
  public Set<String> getSelectedValues() {
    return m_selectedValues;
  }

  @Override
  public void setSelectedValues(Set<String> set) {
    m_selectedValues = set;
  }

  public String getPattern() {
    return m_pattern;
  }

  public void setPattern(String pattern) {
    m_pattern = pattern;
    m_regexPat = null;
  }

  @Override
  public List<LookupRow> createHistogram() {
    TreeMap<String, LookupRow> hist = new TreeMap<String, LookupRow>();
    HashMap<String, Integer> countMap = new HashMap<String, Integer>();
    for (ITableRow row : m_column.getTable().getRows()) {
      String s = m_column.getDisplayText(row);
      if (!StringUtility.hasText(s)) {
        s = null;
      }
      if (s != null && !hist.containsKey(s)) {
        FontSpec font = (row.isFilterAccepted() ? null : FontSpec.parse("italic"));
        hist.put(s, new LookupRow(s, s, null, null, null, null, font));
      }
      Integer count = countMap.get(s);
      countMap.put(s, count != null ? count + 1 : 1);
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
    return (m_selectedValues == null || m_selectedValues.isEmpty()) && m_pattern == null;
  }

  @Override
  public boolean accept(ITableRow row) {
    String value = m_column.getDisplayText(row);
    if (!StringUtility.hasText(value)) {
      value = null;
    }
    if (m_pattern != null) {
      if (m_regexPat == null) {
        m_regexPat = Pattern.compile(StringUtility.toRegExPattern("*" + m_pattern.toLowerCase() + "*"));
      }
      if (value == null) {
        return false;
      }
      if (!m_regexPat.matcher(value.toLowerCase()).matches()) {
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
