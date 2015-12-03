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
package org.eclipse.scout.rt.server.admin.html.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.StringUtility;

public class VirtualRow extends HtmlComponent {
  private List<String> m_cells;
  // temporary
  private int m_cellBegin;

  public VirtualRow(HtmlComponent parent) {
    super(parent);
    m_cells = new ArrayList<String>();
  }

  public String getCellAt(int columnIndex) {
    if (columnIndex >= 0 && columnIndex < m_cells.size()) {
      return m_cells.get(columnIndex);
    }
    else {
      return null;
    }
  }

  @Override
  public void startTableCell(int rows, int cols, String color) {
    super.startTableCell(rows, cols, color);
    m_cellBegin = m_writer.getBuffer().length();
  }

  @Override
  public void endTableCell() {
    int end = m_writer.getBuffer().length();
    String s = StringUtility.removeTags(m_writer.getBuffer().substring(m_cellBegin, end));
    m_cells.add(s);
    super.endTableCell();
  }
}
