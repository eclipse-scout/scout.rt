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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTable;

import org.eclipse.scout.rt.ui.swing.basic.AbstractHtmlLinkDetector;

/**
 * Detects a hyperlink in a JTable
 */
public class TableHtmlLinkDetector extends AbstractHtmlLinkDetector<JTable> {
  private int m_rowIndex = -1;
  private int m_columnIndex = -1;

  @Override
  public boolean detect(JTable table, Point p) {
    boolean found = super.detect(table, p);
    if (found) {
      m_rowIndex = getRow(p);
      m_columnIndex = getColumn(p);
    }
    else {
      m_rowIndex = -1;
      m_columnIndex = -1;
    }
    return found;
  }

  @Override
  protected Rectangle getCellRectangle(Point p) {
    return getContainer().getCellRect(getRow(p), getColumn(p), false);
  }

  @Override
  protected Component getComponent(Point p) {
    int row = getRow(p);
    int col = getColumn(p);
    Component c = getContainer().prepareRenderer(getContainer().getCellRenderer(row, col), row, col);
    return c;
  }

  private int getRow(Point p) {
    return getContainer().rowAtPoint(p);
  }

  private int getColumn(Point p) {
    return getContainer().columnAtPoint(p);
  }

  public int getRowIndex() {
    return m_rowIndex;
  }

  public int getColumnIndex() {
    return m_columnIndex;
  }
}
