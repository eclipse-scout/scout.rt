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
package org.eclipse.scout.rt.ui.swing.basic.table;

import javax.swing.table.TableColumn;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

public class SwingTableColumn extends TableColumn {
  private static final long serialVersionUID = 1L;

  private IColumn m_scoutColumn;

  public SwingTableColumn(int swingModelIndex, IColumn scoutColumn) {
    super(swingModelIndex);
    m_scoutColumn = scoutColumn;
    int w = m_scoutColumn.getWidth();
    setMinWidth(0);
    setPreferredWidth(w);
    setWidth(w);
  }

  public IColumn getScoutColumn() {
    return m_scoutColumn;
  }

  /*
   * Dynamic text (might change anytime)
   */
  @Override
  public Object getHeaderValue() {
    Object o = m_scoutColumn.getHeaderCell().getText();
    if (o != null) {
      String text = o.toString();
      if (SwingUtility.isMultilineLabelText(text)) {
        text = SwingUtility.createHtmlLabelText(text, false);
      }
    }
    return o;
  }

}
