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
package org.eclipse.scout.rt.ui.swing.ext;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.scout.rt.ui.swing.SwingLayoutUtility;
import org.eclipse.scout.rt.ui.swing.SwingUtility;

/**
 *
 */
public class JTableHeaderEx extends JTableHeader {
  private static final long serialVersionUID = 1L;

  private int m_preferredHeight;

  public void updatePreferredHeight() {
    int h = 0;
    for (Enumeration<TableColumn> en = getColumnModel().getColumns(); en.hasMoreElements();) {
      TableColumn col = en.nextElement();
      Object value = col.getHeaderValue();
      if (("" + value).indexOf("\n") >= 0) {
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
          renderer = getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(getTable(), value, false, false, 0, col.getModelIndex());
        Dimension d;
        if (comp instanceof JLabel) {
          ((JLabel) comp).setVerticalAlignment(SwingConstants.TOP);
          d = SwingLayoutUtility.getPreferredLabelSize((JLabel) comp, col.getWidth() - getColumnModel().getColumnMargin());
        }
        else {
          d = comp.getPreferredSize();
        }
        Insets insets = getInsets();
        d.height += insets.top + insets.bottom;
        h = Math.max(h, d.height);
      }
    }
    if (m_preferredHeight != h) {
      m_preferredHeight = h;
      resizeAndRepaint();
    }
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    if (m_preferredHeight > 0) {
      d.height = m_preferredHeight;
    }
    return d;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (m_preferredHeight > 0) {
      d.height = m_preferredHeight;
    }
    return d;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension d = super.getMaximumSize();
    if (m_preferredHeight > 0) {
      d.height = m_preferredHeight;
    }
    return d;
  }

  @Override
  public Point getToolTipLocation(MouseEvent e) {
    return SwingUtility.getAdjustedToolTipLocation(e, this, getTopLevelAncestor());
  }
}
