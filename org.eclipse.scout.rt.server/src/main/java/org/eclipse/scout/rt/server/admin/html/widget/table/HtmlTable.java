/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.admin.html.widget.table;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class HtmlTable extends HtmlComponent {
  private final String m_tableId;
  private final SortInfo m_sortInfo;
  private final List<VirtualRow> m_rows;
  // temporary
  private int m_temporaryColumn;

  public HtmlTable(HtmlComponent other, String tableId, SortInfo sortInfo) {
    super(other);
    m_tableId = tableId;
    m_sortInfo = sortInfo;
    m_rows = new ArrayList<>();
  }

  @Override
  public void tableHeaderCell(String content) {
    m_out.print("<th>");
    final int columnIndex = m_temporaryColumn;
    // next
    m_temporaryColumn++;
    String sortId;
    if (m_sortInfo.getColumnIndex() == columnIndex) {
      if (m_sortInfo.isAscending()) {
        sortId = m_tableId + ".sortCol." + columnIndex + ".down";
      }
      else {
        sortId = m_tableId + ".sortCol." + columnIndex + ".up";
      }
    }
    else {
      sortId = m_tableId + ".sortCol." + columnIndex;
    }
    startLinkAction(new AbstractHtmlAction(sortId) {

      @Override
      public void run() {
        if (m_sortInfo.getColumnIndex() == columnIndex) {
          m_sortInfo.setAscending(!m_sortInfo.isAscending());
        }
        else {
          m_sortInfo.setColumnIndex(columnIndex);
          m_sortInfo.setAscending(true);
        }
      }
    });
    if (m_sortInfo.getColumnIndex() == columnIndex) {
      m_out.print("<b>");
    }
    if (!StringUtility.hasText(content)) {
      m_out.print("&nbsp;");
    }
    else {
      print(content);
    }
    if (m_sortInfo.getColumnIndex() == columnIndex) {
      m_out.print("</b>");
    }
    endLinkAction();
    m_out.print("</th>");
  }

  public VirtualRow addVirtualRow() {
    VirtualRow v = new VirtualRow(this);
    m_rows.add(v);
    return v;
  }

  public void appendVirtualRows() {
    for (VirtualRow row : getSortedRows()) {
      append(row);
    }
  }

  private VirtualRow[] getSortedRows() {
    SortedMap<CompositeObject, VirtualRow> sortMap = new TreeMap<>();
    int rowIndex = 0;
    for (VirtualRow row : m_rows) {
      sortMap.put(new CompositeObject(row.getCellAt(m_sortInfo.getColumnIndex()), rowIndex), row);
      rowIndex++;
    }
    VirtualRow[] result = new VirtualRow[m_rows.size()];
    if (m_sortInfo.getColumnIndex() >= 0 && !m_sortInfo.isAscending()) {
      // reverse
      int i = m_rows.size() - 1;
      for (VirtualRow row : sortMap.values()) {
        result[i] = row;
        i--;
      }
    }
    else {
      // forward
      int i = 0;
      for (VirtualRow row : sortMap.values()) {
        result[i] = row;
        i++;
      }
    }
    return result;
  }

}
