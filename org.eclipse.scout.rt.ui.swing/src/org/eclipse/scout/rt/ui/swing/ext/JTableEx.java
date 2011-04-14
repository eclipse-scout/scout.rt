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
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.dnd.DefaultDropTarget;
import org.eclipse.scout.rt.ui.swing.dnd.TransferHandlerEx;

/**
 * Several bug fixes and enhancements to {@link JTable}
 * <p>
 * - keep tooltip manager registration even if table itself has null tooltip (maybe cells have!)
 * <p>
 * - DnD handling using {@link TransferHandlerEx} and {@link DefaultDropTarget}
 * <p>
 * - support for getPreferredContentSize()
 */
public class JTableEx extends JTable {
  private static final long serialVersionUID = 1L;

  private TableModelListener m_tableModelListener;
  private TableColumnModelListener m_tableColumnModelListener;
  /*
   * fix for jre 1.4+ static (bad) concept of dynamic row heights with no possibility of override or injection
   */
  private boolean m_dynamicRowHeight = false;
  private DynamicRowHeightCache m_dynamicRowHeightCache;

  public JTableEx() {
    super();
    if (UIManager.get("Table.rowHeight") != null) {
      @SuppressWarnings("hiding")
      int rowHeight = UIManager.getInt("Table.rowHeight");
      if (rowHeight > 0) {
        setRowHeight(rowHeight);
      }
    }
    setTableHeader(new JTableHeaderEx());
    if (UIManager.get("Table.showGrid") != null) {
      setShowGrid(UIManager.getBoolean("Table.showGrid"));
    }
    if (UIManager.get("Table.showHorizontalLines") != null) {
      setShowHorizontalLines(UIManager.getBoolean("Table.showHorizontalLines"));
    }
    if (UIManager.get("Table.showVerticalLines") != null) {
      setShowVerticalLines(UIManager.getBoolean("Table.showVerticalLines"));
    }
    adjustFocusHandling();
    // remove ESC and ENTER override
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "no-event");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "no-event");
    setVerifyInputWhenFocusTarget(true);
    setAutoscrolls(true);// used for page up/down end
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setColumnSelectionAllowed(false);
    setRowSelectionAllowed(true);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setOpaque(false);
    getTableHeader().setReorderingAllowed(false);
    m_tableColumnModelListener = new P_TableColumnModelListener();
    getColumnModel().addColumnModelListener(m_tableColumnModelListener);
    // add up/down helpers
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("HOME"), "selectFirstRow");
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("END"), "selectLastRow");
    // focus cell border repainter
    addFocusListener(new P_FocusListener());
  }

  /**
   * better defaults for vertical alignment. multiline: top, single-line: center
   */
  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    //
    if (c instanceof JLabel) {
      JLabel label = (JLabel) c;
      if (isDynamicRowHeight()) {
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setVerticalTextPosition(SwingConstants.TOP);
      }
      else {
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.CENTER);
      }
    }
    return c;
  }

  /**
   * Implementation of TableSelectionListener for fixing bug in Swing that does
   * not repaint when anchor/lead changes without
   */
  @Override
  public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    int oldAnchor = getSelectionModel().getAnchorSelectionIndex();
    int oldLead = getSelectionModel().getLeadSelectionIndex();
    super.changeSelection(rowIndex, columnIndex, toggle, extend);
    repaintFocusRow(oldAnchor, oldLead);
    repaintFocusRow(getSelectionModel().getAnchorSelectionIndex(), getSelectionModel().getLeadSelectionIndex());
  }

  protected void adjustFocusHandling() {
    SwingUtility.installDefaultFocusHandling(this);
    setFocusable(true);
    setFocusCycleRoot(false);
  }

  /**
   * bug in swing: tooltip flickers (show, hide, show, hide,...) when shown at exact mouse position.
   * <p>
   * Shift tooltip 4px right, 16px down
   */
  @Override
  public Point getToolTipLocation(MouseEvent event) {
    if (getToolTipText(event) != null) {
      return new Point(event.getX() + 4, event.getY() + 16);
    }
    else {
      return null;
    }
  }

  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    if (text == null) {
      // re-register
      ToolTipManager.sharedInstance().registerComponent(this);
    }
  }

  @Override
  public void setModel(TableModel newModel) {
    TableModel oldModel = getModel();
    // invalidate row heights
    super.setModel(newModel);
    if (oldModel != newModel) {
      if (oldModel != null && m_tableModelListener != null) {
        oldModel.removeTableModelListener(m_tableModelListener);
      }
      m_tableModelListener = null;
      if (newModel != null) {
        if (m_tableModelListener == null) {
          m_tableModelListener = new P_TableModelListener();
        }
        newModel.addTableModelListener(m_tableModelListener);
      }
    }
    if (isDynamicRowHeight()) {
      m_dynamicRowHeightCache = new DynamicRowHeightCache(this);
    }
    else {
      m_dynamicRowHeightCache = null;
    }
  }

  @Override
  public void setColumnModel(TableColumnModel newModel) {
    TableColumnModel oldModel = getColumnModel();
    super.setColumnModel(newModel);
    if (oldModel != newModel) {
      if (oldModel != null && m_tableColumnModelListener != null) {
        oldModel.removeColumnModelListener(m_tableColumnModelListener);
      }
      m_tableColumnModelListener = null;
      if (newModel != null) {
        if (m_tableColumnModelListener == null) {
          m_tableColumnModelListener = new P_TableColumnModelListener();
        }
        newModel.addColumnModelListener(m_tableColumnModelListener);
      }
    }
  }

  public void setDynamicRowHeight(boolean b) {
    if (m_dynamicRowHeight != b) {
      m_dynamicRowHeight = b;
      if (m_dynamicRowHeight) {
        m_dynamicRowHeightCache = new DynamicRowHeightCache(this);
      }
      else {
        m_dynamicRowHeightCache = null;
      }
      resizeAndRepaint();
    }
  }

  public boolean isDynamicRowHeight() {
    return m_dynamicRowHeight;
  }

  @Override
  public void setRowHeight(int height) {
    if (isDynamicRowHeight()) {
      m_dynamicRowHeightCache = new DynamicRowHeightCache(this, height);
    }
    else {
      m_dynamicRowHeightCache = null;
    }
    super.setRowHeight(height);
  }

  @Override
  public void setRowHeight(int row, int height) {
    if (isDynamicRowHeight()) {
      //no action when using dynamic row height
    }
    else {
      super.setRowHeight(row, height);
    }
  }

  @Override
  public int getRowHeight(int row) {
    if (isDynamicRowHeight()) {
      return m_dynamicRowHeightCache.getRowHeight(row);
    }
    else {
      return super.getRowHeight(row);
    }
  }

  public Dimension getPreferredContentSize(int maxRowCount) {
    Dimension max = new Dimension();
    for (int r = 0, nr = getRowCount(); r < nr && r < maxRowCount; r++) {
      int w = 0;
      int h = 0;
      for (int c = 0, nc = getColumnCount(); c < nc; c++) {
        TableCellRenderer renderer = getCellRenderer(r, c);
        JComponent comp = (JComponent) prepareRenderer(renderer, r, c);
        comp.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Dimension d = comp.getPreferredSize();
        w += d.width;
        h = Math.max(h, d.height);
      }
      h = Math.max(h, getRowHeight(r));
      max.height += h;
      max.width = Math.max(max.width, w);
    }
    return max;
  }

  @Override
  public int rowAtPoint(Point p) {
    if (isDynamicRowHeight()) {
      return m_dynamicRowHeightCache.getRowAtPoint(p);
    }
    else {
      return super.rowAtPoint(p);
    }
  }

  @Override
  public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
    Rectangle r = super.getCellRect(row, column, includeSpacing);
    if (isDynamicRowHeight()) {
      r.y = m_dynamicRowHeightCache.getPointForRow(row);
      if (r.height > 0 && !includeSpacing) {
        //re-apply row margins
        r.y = r.y + getRowMargin() / 2;
      }
    }
    return r;
  }

  /**
   * Implementation of TableModelListener for fixing bug in JRE13 with variable
   * row heights Workaround to support dynamic multi-line-dependent header
   * height
   */
  private class P_TableModelListener implements TableModelListener {
    @Override
    public void tableChanged(TableModelEvent e) {
      // headers
      JTableHeader header = getTableHeader();
      if (header instanceof JTableHeaderEx) {
        ((JTableHeaderEx) header).updatePreferredHeight();
      }
      if (isDynamicRowHeight()) {
        m_dynamicRowHeightCache = new DynamicRowHeightCache(JTableEx.this);
        resizeAndRepaint();
      }
      else {
        m_dynamicRowHeightCache = null;
      }
    }
  }// end class

  /**
   * Implementation of TableColumnModelListener for fixing bug in JRE13 with
   * variable row heights
   */
  private class P_TableColumnModelListener implements TableColumnModelListener {
    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
      // headers
      JTableHeader header = getTableHeader();
      if (header instanceof JTableHeaderEx) {
        ((JTableHeaderEx) header).updatePreferredHeight();
      }
      if (isDynamicRowHeight()) {
        m_dynamicRowHeightCache = new DynamicRowHeightCache(JTableEx.this);
        resizeAndRepaint();
      }
      else {
        m_dynamicRowHeightCache = null;
      }
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }
  }

  protected void repaintFocusRow(int anchor, int lead) {
    int r = anchor;
    if (r < 0) {
      r = lead;
    }
    if (r >= 0) {
      Rectangle rect1 = getCellRect(r, 0, false);
      Rectangle rectn = getCellRect(r, getColumnCount() - 1, false);
      repaint(rect1.union(rectn));
    }
  }

  @Override
  public void setTransferHandler(TransferHandler newHandler) {
    TransferHandler oldHandler = (TransferHandler) getClientProperty("TransferHandler");
    putClientProperty("TransferHandler", newHandler);
    DropTarget dropHandler = getDropTarget();
    if ((dropHandler == null) || (dropHandler instanceof UIResource)) {
      if (newHandler == null) {
        setDropTarget(null);
      }
      else if (!GraphicsEnvironment.isHeadless()) {
        setDropTarget(new DefaultDropTarget(this));
      }
    }
    firePropertyChange("transferHandler", oldHandler, newHandler);
  }

  @Override
  public TransferHandler getTransferHandler() {
    return (TransferHandler) getClientProperty("TransferHandler");
  }

  /**
   * Implementation of FocusListener for cell border repaint
   */
  private class P_FocusListener extends FocusAdapter {
    @Override
    public void focusGained(FocusEvent e) {
      repaintFocusRow(getSelectionModel().getAnchorSelectionIndex(), getSelectionModel().getLeadSelectionIndex());
    }

    @Override
    public void focusLost(FocusEvent e) {
      repaintFocusRow(getSelectionModel().getAnchorSelectionIndex(), getSelectionModel().getLeadSelectionIndex());
    }
  }// end class
}
