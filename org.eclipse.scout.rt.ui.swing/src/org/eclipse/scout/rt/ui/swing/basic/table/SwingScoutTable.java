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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.ui.swing.SwingPopupWorker;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.action.SwingScoutAction;
import org.eclipse.scout.rt.ui.swing.basic.ColorUtility;
import org.eclipse.scout.rt.ui.swing.basic.SwingScoutComposite;
import org.eclipse.scout.rt.ui.swing.dnd.TransferHandlerEx;
import org.eclipse.scout.rt.ui.swing.ext.HtmlViewCache;
import org.eclipse.scout.rt.ui.swing.ext.JScrollPaneEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableEx;
import org.eclipse.scout.rt.ui.swing.ext.JTableHeaderEx;
import org.eclipse.scout.rt.ui.swing.ext.MouseClickedBugFix;
import org.eclipse.scout.rt.ui.swing.icons.CheckboxIcon;

/**
 * The prefix SwingScout... denotes a model COMPOSITION between a swing and a
 * scout component The prefix Swing... denotes a SUBCLASS of a swing component
 * Base table class without selection handling.
 */
public class SwingScoutTable extends SwingScoutComposite<ITable> implements ISwingScoutTable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SwingScoutTable.class);

  private P_ScoutTableListener m_scoutTableListener;
  private JTableHeader m_swingTableHeader;
  private JScrollPane m_swingScrollPane;
  private HtmlViewCache m_htmlViewCache;
  private ClientSyncJob m_storeColumnWidthsJob;

  // cache
  private IKeyStroke[] m_installedScoutKs;

  private int m_cachedScoutDragTransferTypes;
  private int m_cachedScoutDropTransferTypes;

  // DND
  private TransferHandler m_emptySpaceTransferHandler;
  private P_SwingRowTransferHandler m_rowTransferHandler;
  private boolean m_dndInstalled;

  // keyboard navigation
  private TableKeyboardNavigationSupport m_keyboardNavigationSupport;

  public SwingScoutTable() {
    super();
  }

  @Override
  protected void initializeSwing() {
    m_htmlViewCache = new HtmlViewCache();
    JTableEx table = new P_SwingTable();
    m_swingScrollPane = new JScrollPaneEx(table);
    m_swingScrollPane.setBackground(table.getBackground());
    setSwingField(table);
    m_swingTableHeader = table.getTableHeader();
    if (m_swingTableHeader == null) {
      m_swingTableHeader = new JTableHeaderEx();
      table.setTableHeader(m_swingTableHeader);
    }
    // swing properties
    table.setAutoCreateColumnsFromModel(false);
    m_swingTableHeader.setReorderingAllowed(true);
    // header renderer must be set before models
    m_swingTableHeader.setDefaultRenderer(new SwingTableHeaderCellRenderer(m_swingTableHeader.getDefaultRenderer(), this));
    // models
    table.setAutoCreateColumnsFromModel(false);
    table.setColumnModel(new SwingTableColumnModel(getSwingEnvironment(), this));
    table.setModel(new SwingTableModel(getSwingEnvironment(), this));
    //editors
    SwingScoutTableCellEditor editor = new SwingScoutTableCellEditor(this);
    editor.initialize();
    //disable auto-start editing
    table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
    table.setSelectionModel(new DefaultListSelectionModel());
    table.getSelectionModel().setAnchorSelectionIndex(0);
    // listeners
    table.getSelectionModel().addListSelectionListener(new P_SwingSelectionListener());
    // re-attach observer when selection model changes
    table.addPropertyChangeListener("selectionModel", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        getSwingTable().getSelectionModel().addListSelectionListener(new P_SwingSelectionListener());
      }
    });
    m_swingTableHeader.addMouseListener(new P_SwingHeadMouseListener());
    table.addMouseListener(new P_SwingRowMouseListener());
    //add hyperlink click listener
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if (!e.isPopupTrigger()) {
          TableHtmlLinkDetector detector = new TableHtmlLinkDetector();
          if (detector.detect((JTable) e.getComponent(), e.getPoint())) {
            handleSwingHyperlinkAction(detector.getRowIndex(), detector.getColumnIndex(), detector.getHyperlink());
          }
        }
      }
    });
    m_swingScrollPane.getViewport().addMouseListener(new P_SwingEmptySpaceMouseListener());
    //ticket 87030
    m_swingScrollPane.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        ITable t = getScoutObject();
        if (t != null && t.isScrollToSelection()) {
          if (e.getComponent().isShowing()) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                scrollToSelection();
              }
            });
          }
        }
      }
    });
    //add context menu key stroke
    table.getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("CONTEXT_MENU"), "contextMenu");
    table.getActionMap().put("contextMenu", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (getUpdateSwingFromScoutLock().isAcquired()) return;
        //
        if (getScoutObject() != null) {
          int[] rowIndexes = getSwingTable().getSelectedRows();
          Point p = new Point(0, 0);
          if (rowIndexes != null && rowIndexes.length > 0) {
            p = getSwingTable().getCellRect(rowIndexes[0], 0, false).getLocation();
          }
          p.translate(2, 2);
          // notify Scout
          final Component compF = getSwingTable();
          final Point pFinal = p;
          Runnable t = new Runnable() {
            @Override
            public void run() {
              IMenu[] scoutMenus = getScoutObject().getUIFacade().fireRowPopupFromUI();
              // call swing menu
              new SwingPopupWorker(getSwingEnvironment(), compF, pFinal, scoutMenus).enqueue();
            }
          };
          getSwingEnvironment().invokeScoutLater(t, 5678);
          // end notify
        }
      }
    });
  }

  public JTableEx getSwingTable() {
    return (JTableEx) getSwingField();
  }

  protected SwingTableModel getSwingTableModel() {
    return (SwingTableModel) getSwingTable().getModel();
  }

  protected SwingTableColumnModel getSwingTableColumnModel() {
    return (SwingTableColumnModel) getSwingTable().getColumnModel();
  }

  protected ListSelectionModel getSwingTableSelectionModel() {
    return (ListSelectionModel) getSwingTable().getSelectionModel();
  }

  public JScrollPane getSwingScrollPane() {
    return m_swingScrollPane;
  }

  protected void setContextColumnFromSwing(int viewIndex) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    registerColumnHeaderPopupOwner(viewIndex);
    //
    if (getScoutObject() != null) {
      final IColumn scoutCol = (getSwingTableColumnModel().swingToScoutColumn(viewIndex));
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setContextColumnFromUI(scoutCol);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      if (m_scoutTableListener == null) {
        m_scoutTableListener = new P_ScoutTableListener();
        getScoutObject().addPriorityTableListener(m_scoutTableListener);
      }
      setMultiSelectFromScout(getScoutObject().isMultiSelect());
      setMultilineTextFromScout(getScoutObject().isMultilineText());
      setKeyboardNavigationFromScout();
      setHeaderVisibleFromScout(getScoutObject().isHeaderVisible());
      setColumnsAutoResizeFromScout(getScoutObject().isAutoResizeColumns());
      setKeyStrokesFromScout();
      updateDragTransferTypesFromScout();
      updateDropTransferTypesFromScout();
      setSelectionFromScout();
      // add checkable key mappings
      if (getScoutObject().isCheckable()) {
        getSwingTable().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("SPACE"), "toggleRow");
        getSwingTable().getActionMap().put("toggleRow", new AbstractAction() {
          private static final long serialVersionUID = 1L;

          @Override
          public void actionPerformed(ActionEvent e) {
            handleSwingRowClick(getSwingTable().getSelectedRow());
          }
        });
      }
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    if (getScoutObject() != null) {
      if (m_scoutTableListener != null) {
        getScoutObject().removeTableListener(m_scoutTableListener);
        m_scoutTableListener = null;
      }
    }
  }

  protected void setMultiSelectFromScout(boolean on) {
    if (on) {
      getSwingTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    else {
      getSwingTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
  }

  protected void setScrollToSelectionFromScout() {
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  protected void setMultilineTextFromScout(boolean on) {
    getSwingTable().setDynamicRowHeight(on);
  }

  protected void setKeyboardNavigationFromScout() {
    boolean hasKeyboardNavigation = getScoutObject().hasKeyboardNavigation();
    if (hasKeyboardNavigation) {
      if (m_keyboardNavigationSupport == null) {
        m_keyboardNavigationSupport = new P_KeyboardNavigationSupport(getSwingTable());
      }
    }
    else {
      if (m_keyboardNavigationSupport != null) {
        m_keyboardNavigationSupport.dispose();
        m_keyboardNavigationSupport = null;
      }
    }
  }

  protected void setHeaderVisibleFromScout(boolean on) {
    if (on) {
      getSwingTable().setTableHeader(m_swingTableHeader);
    }
    else {
      getSwingTable().setTableHeader(null);
    }
  }

  protected void setColumnsAutoResizeFromScout(boolean on) {
    getSwingTable().setAutoResizeMode(on ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
  }

  protected void setKeyStrokesFromScout() {
    JComponent component = getSwingContainer();
    if (component == null) {
      component = getSwingField();
    }
    if (component != null) {
      // remove old key strokes
      if (m_installedScoutKs != null) {
        for (int i = 0; i < m_installedScoutKs.length; i++) {
          IKeyStroke scoutKs = m_installedScoutKs[i];
          KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
          //
          InputMap imap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          imap.remove(swingKs);
          ActionMap amap = component.getActionMap();
          amap.remove(scoutKs.getActionId());
        }
      }
      m_installedScoutKs = null;
      // add new key strokes
      IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
      for (IKeyStroke scoutKs : scoutKeyStrokes) {
        int swingWhen = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
        KeyStroke swingKs = SwingUtility.createKeystroke(scoutKs);
        SwingScoutAction<IAction> action = new SwingScoutAction<IAction>();
        action.createField(scoutKs, getSwingEnvironment());
        //
        InputMap imap = component.getInputMap(swingWhen);
        imap.put(swingKs, scoutKs.getActionId());
        ActionMap amap = component.getActionMap();
        amap.put(scoutKs.getActionId(), action.getSwingAction());
      }
      m_installedScoutKs = scoutKeyStrokes;
    }
  }

  protected void setSelectionFromScout() {
    if (getScoutObject() == null) return;
    //
    ITableRow[] scoutRows = getScoutObject().getSelectedRows();
    ListSelectionModel lsm = getSwingTableSelectionModel();
    //
    int[] oldSwingRows = getSwingTable().getSelectedRows();
    int[] newSwingRows = scoutToSwingRows(scoutRows);
    Arrays.sort(oldSwingRows);
    Arrays.sort(newSwingRows);
    // restore selection
    if (!CompareUtility.equals(oldSwingRows, newSwingRows)) {
      HashSet<Integer> addSet = new HashSet<Integer>();
      HashSet<Integer> removeSet = new HashSet<Integer>();
      for (int index : newSwingRows) {
        addSet.add(index);
      }
      for (int index : oldSwingRows) {
        addSet.remove(index);
        removeSet.add(index);
      }
      for (int index : newSwingRows) {
        removeSet.remove(index);
      }
      try {
        lsm.setValueIsAdjusting(true);
        //
        for (int index : addSet) {
          lsm.addSelectionInterval(index, index);
        }
        for (int index : removeSet) {
          lsm.removeSelectionInterval(index, index);
        }
      }
      finally {
        lsm.setValueIsAdjusting(false);
      }
    }
    //ticket 96051
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  protected void setSelectionFromSwing(final int[] swingRows) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      if (getSwingTable().getSelectionModel().getValueIsAdjusting()) {
        return;
      }
      final ITableRow[] scoutRows = swingToScoutRows(swingRows);
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
            //
            getScoutObject().getUIFacade().setSelectedRowsFromUI(scoutRows);
          }
          finally {
            removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
          }
        }
      };

      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void updateDragTransferTypesFromScout() {
    int scoutDragTransfer = getScoutObject().getDragType();
    if (scoutDragTransfer != 0) {
      m_cachedScoutDragTransferTypes = scoutDragTransfer;
      // ensure installed
      if (!m_dndInstalled) {
        if (m_emptySpaceTransferHandler == null) {
          m_emptySpaceTransferHandler = new P_SwingEmptySpaceTransferHandler();
        }
        if (m_rowTransferHandler == null) {
          m_rowTransferHandler = new P_SwingRowTransferHandler();
        }
        m_swingScrollPane.getViewport().setTransferHandler(m_emptySpaceTransferHandler);
        JTableEx table = getSwingTable();
        table.setTransferHandler(m_rowTransferHandler);
        table.setDragEnabled(true);
        m_dndInstalled = true;
      }
    }
    else {
      if (m_cachedScoutDropTransferTypes == 0 && m_dndInstalled) {
        // uninstall
        m_swingScrollPane.getViewport().setTransferHandler(null);
        JTableEx table = getSwingTable();
        table.setTransferHandler(null);
        table.setDragEnabled(false);
        m_dndInstalled = false;
      }
    }
  }

  protected void updateDropTransferTypesFromScout() {
    int scoutDropTransfer = getScoutObject().getDropType();
    if (scoutDropTransfer != 0) {
      m_cachedScoutDropTransferTypes = scoutDropTransfer;
      // ensure installed
      if (!m_dndInstalled) {

        if (m_emptySpaceTransferHandler == null) {
          m_emptySpaceTransferHandler = new P_SwingEmptySpaceTransferHandler();
        }
        if (m_rowTransferHandler == null) {
          m_rowTransferHandler = new P_SwingRowTransferHandler();
        }
        m_swingScrollPane.getViewport().setTransferHandler(m_emptySpaceTransferHandler);
        JTableEx table = getSwingTable();
        table.setTransferHandler(m_rowTransferHandler);
        table.setDragEnabled(true);
        m_dndInstalled = true;
      }
    }
    else {
      if (m_cachedScoutDragTransferTypes == 0 && m_dndInstalled) {
        // uninstall
        m_swingScrollPane.getViewport().setTransferHandler(null);
        JTableEx table = getSwingTable();
        table.setTransferHandler(null);
        table.setDragEnabled(false);
        m_dndInstalled = false;
      }
    }
  }

  /**
   * ticket 88564, do NOT use {@link #scrollToSelection()} when setting scout rows to swing, since this can lead to
   * confusing effects when doing multiple selection on a table
   */
  protected void scrollToSelection() {
    int[] swingRows = getSwingTable().getSelectedRows();
    if (swingRows.length > 0) {
      Rectangle viewRect = getSwingTable().getVisibleRect();
      if (viewRect.isEmpty()) {
        //if component is not yet fully layouted
        return;
      }
      Rectangle selBeginRect = getSwingTable().getCellRect(swingRows[0], 0, true);
      Rectangle selEndRect = getSwingTable().getCellRect(swingRows[swingRows.length - 1], 0, true);
      Rectangle selRect = selBeginRect.union(selEndRect);
      Rectangle rootRect = new Rectangle(selRect.x, 0, selRect.width, selRect.y + selRect.height);
      if (rootRect.height <= viewRect.height) {
        getSwingTable().scrollRectToVisible(rootRect);
      }
      else if (selRect.height <= viewRect.height) {
        getSwingTable().scrollRectToVisible(selRect);
      }
      else if (selBeginRect.height <= viewRect.height) {
        getSwingTable().scrollRectToVisible(selBeginRect);
      }
    }
  }

  protected void storeColumnWidthsFromSwing(List<TableColumn> swingColumns) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    // store gui index and width of column
    SwingTableColumnModel cm = getSwingTableColumnModel();
    int n = cm.getColumnCount();
    final IColumn[] scoutCols = new IColumn[swingColumns.size()];
    final int[] scoutColWidths = new int[swingColumns.size()];
    for (int index = 0; index < swingColumns.size(); index++) {
      TableColumn swingCol = swingColumns.get(index);
      for (int i = 0; i < n; i++) {
        if (cm.getColumn(i) == swingCol) {
          scoutCols[index] = ((SwingTableColumn) swingCol).getScoutColumn();
          scoutColWidths[index] = swingCol.getPreferredWidth();
          break;
        }
      }
    }
    if (m_storeColumnWidthsJob != null) {
      m_storeColumnWidthsJob.cancel();
    }
    m_storeColumnWidthsJob = new ClientSyncJob("Store column widths", getSwingEnvironment().getScoutSession()) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        if (getScoutObject() != null) {
          for (int i = 0; i < scoutCols.length; i++) {
            if (scoutCols[i] != null) {
              getScoutObject().getUIFacade().setColumnWidthFromUI(scoutCols[i], scoutColWidths[i]);
            }
          }
        }
        return Status.OK_STATUS;
      }
    };
    m_storeColumnWidthsJob.schedule(400);
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String propName, Object newValue) {
    if (propName.equals(ITable.PROP_MULTI_SELECT)) {
      setMultiSelectFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_MULTILINE_TEXT)) {
      setMultilineTextFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_HEADER_VISIBLE)) {
      setHeaderVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_AUTO_RESIZE_COLUMNS)) {
      setColumnsAutoResizeFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_KEY_STROKES)) {
      setKeyStrokesFromScout();
    }
    else if (propName.equals(ITable.PROP_DRAG_TYPE)) {
      updateDragTransferTypesFromScout();
    }
    else if (propName.equals(ITable.PROP_DROP_TYPE)) {
      updateDropTransferTypesFromScout();
    }
    else if (propName.equals(ITable.PROP_KEYBOARD_NAVIGATION)) {
      setKeyboardNavigationFromScout();
    }
    else if (propName.equals(ITable.PROP_SCROLL_TO_SELECTION)) {
      setScrollToSelectionFromScout();
    }
  }

  /**
   * scout table observer
   */
  protected boolean isHandleScoutTableEvent(TableEvent[] a) {
    for (int i = 0; i < a.length; i++) {
      switch (a[i].getType()) {
        case TableEvent.TYPE_REQUEST_FOCUS:
        case TableEvent.TYPE_ROWS_INSERTED:
        case TableEvent.TYPE_ROWS_UPDATED:
        case TableEvent.TYPE_ROWS_DELETED:
        case TableEvent.TYPE_ALL_ROWS_DELETED:
        case TableEvent.TYPE_ROW_ORDER_CHANGED:
        case TableEvent.TYPE_ROW_FILTER_CHANGED:
        case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
        case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
        case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
        case TableEvent.TYPE_ROWS_SELECTED: {
          return true;
        }
      }
    }
    return false;
  }

  protected void handleScoutTableEventInSwing(TableEvent e) {
    SwingTableModel swingTableModel = (SwingTableModel) getSwingTable().getModel();
    int newRowCount = getScoutObject().getFilteredRowCount();
    /*
     * check the scout observer to filter all events that are used here
     * @see isHandleScoutTableEvent()
     */
    switch (e.getType()) {
      case TableEvent.TYPE_REQUEST_FOCUS: {
        getSwingTable().requestFocus();
        break;
      }
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ALL_ROWS_DELETED: {
        swingTableModel.updateModelState(newRowCount);
        break;
      }
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        swingTableModel.updateModelState(newRowCount);
        break;
      }
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROW_FILTER_CHANGED: {
        swingTableModel.updateModelState(newRowCount);
        break;
      }
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        // re-install columns
        SwingTableColumnModel swingColModel = (SwingTableColumnModel) getSwingTable().getColumnModel();
        swingColModel.initializeColumns();
        swingTableModel.updateModelState(newRowCount);
        break;
      }
      case TableEvent.TYPE_ROWS_SELECTED: {
        break;
      }
    }
    switch (e.getType()) {
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED:
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
      case TableEvent.TYPE_ROWS_SELECTED:
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        setSelectionFromScout();
        getSwingTable().repaint();
        break;
      }
    }
    switch (e.getType()) {
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED: {
        m_htmlViewCache = new HtmlViewCache();
        break;
      }
    }
  }

  protected void handleSwingEmptySpacePopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireEmptySpacePopupFromUI();
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  protected void handleSwingDropAction(int rowIndex, Transferable swingTransferable) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      if (swingTransferable != null) {
        final TransferObject scoutTransferable = SwingUtility.createScoutTransferable(swingTransferable);
        final ITableRow scoutRow = swingToScoutRow(rowIndex);
        if (scoutTransferable != null) {
          // notify Scout (asynchronous !)
          Runnable t = new Runnable() {
            @Override
            public void run() {
              getScoutObject().getUIFacade().fireRowDropActionFromUI(scoutRow, scoutTransferable);
            }
          };
          getSwingEnvironment().invokeScoutLater(t, 0);
          // end notify
        }
      }
    }
  }

  protected void handleSwingRowPopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireRowPopupFromUI();
          // call swing menu
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  protected void handleSwingRowClick(int rowIndex) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null && rowIndex >= 0) {
      final ITableRow scoutRow = swingToScoutRow(rowIndex);
      if (scoutRow != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowClickFromUI(scoutRow);
          }
        };

        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleSwingHyperlinkAction(int rowIndex, int colIndex, final URL url) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null && rowIndex >= 0 && colIndex >= 0) {
      final ITableRow scoutRow = swingToScoutRow(rowIndex);
      final IColumn scoutCol = getSwingTableColumnModel().swingToScoutColumn(colIndex);
      if (scoutRow != null && scoutCol != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireHyperlinkActionFromUI(scoutRow, scoutCol, url);
          }
        };

        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleSwingRowAction(int rowIndex) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    if (getScoutObject() != null && rowIndex >= 0) {
      final ITableRow scoutRow = swingToScoutRow(rowIndex);
      if (scoutRow != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowActionFromUI(scoutRow);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleSwingHeaderSort(final int sortIndex, final boolean shiftDown, final boolean controlDown) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    setContextColumnFromSwing(sortIndex);
    if (sortIndex >= 0) {
      // notify Scout
      if (getScoutObject() != null) {
        Runnable t = new Runnable() {
          @Override
          public void run() {
            IColumn<?> scoutCol = getScoutObject().getColumnSet().getVisibleColumn(sortIndex);
            getScoutObject().getUIFacade().setContextColumnFromUI(scoutCol);
            getScoutObject().getUIFacade().fireHeaderSortFromUI(scoutCol, controlDown);
          }
        };
        getSwingEnvironment().invokeScoutLater(t, 200);
      }
      // end notify
    }
  }

  protected void handleSwingHeaderPopup(final MouseEvent e) {
    if (getUpdateSwingFromScoutLock().isAcquired()) return;
    //
    final int sortIndex = getSwingTable().getTableHeader().columnAtPoint(e.getPoint());
    setContextColumnFromSwing(sortIndex);
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          // call swing menu
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireHeaderPopupFromUI();
          new SwingPopupWorker(getSwingEnvironment(), e.getComponent(), e.getPoint(), scoutMenus).enqueue();
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 5678);
      // end notify
    }
  }

  private void registerColumnHeaderPopupOwner(int columnViewIndex) {
    getSwingEnvironment().setPopupOwner(null, null);
    if (columnViewIndex < 0) {
      return;
    }
    //register the popup location in case ui is interested in it
    JTableHeader header = getSwingTable().getTableHeader();
    if (header == null) {
      return;
    }
    Point p = header.getLocationOnScreen();
    Point dp = header.getHeaderRect(columnViewIndex).getLocation();
    final Rectangle headerCellRect = new Rectangle(p.x + dp.x, p.y + dp.y + header.getHeight(), 0, 0);
    getSwingEnvironment().setPopupOwner(getSwingTable(), headerCellRect);
  }

  protected Transferable handleSwingDragRequest() {
    if (getUpdateSwingFromScoutLock().isAcquired()) return null;
    //
    final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireRowsDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getSwingEnvironment().invokeScoutLater(t, 20000).join(20000);
      }
      catch (InterruptedException e) {
        //nop
      }
      // end notify
    }
    TransferObject scoutTransferable = result.getValue();
    Transferable swingTransferable = null;
    swingTransferable = SwingUtility.createSwingTransferable(scoutTransferable);
    return swingTransferable;
  }

  protected void handleKeyboardNavigationFromSwing(int rowIndex) {
    if (getScoutObject().isCheckable()) {
      //nop
      return;
    }
    ListSelectionModel selectionModel = getSwingTable().getSelectionModel();
    selectionModel.setSelectionInterval(rowIndex, rowIndex);
    scrollToSelection();
  }

  protected ITableRow swingToScoutRow(int rowIndex) {
    ITable table = getScoutObject();
    if (table != null && rowIndex >= 0) {
      return table.getFilteredRow(rowIndex);
    }
    return null;
  }

  protected ITableRow[] swingToScoutRows(int[] swingRows) {
    if (swingRows == null || swingRows.length == 0) return new ITableRow[0];
    //
    ITable table = getScoutObject();
    if (table != null) {
      int mismatchCount = 0;
      ITableRow[] rows = new ITableRow[swingRows.length];
      for (int i = 0; i < rows.length; i++) {
        ITableRow row = table.getFilteredRow(swingRows[i]);
        if (row != null) {
          rows[i] = row;
        }
        else {
          rows[i] = null;
          mismatchCount++;
        }
      }
      if (mismatchCount > 0) {
        ITableRow[] newRows = new ITableRow[rows.length - mismatchCount];
        int index = 0;
        for (ITableRow row : rows) {
          if (row != null) {
            newRows[index] = row;
            index++;
          }
        }
        rows = newRows;
      }
      return rows;
    }
    return new ITableRow[0];
  }

  protected int scoutToSwingRow(ITableRow row) {
    ITable table = getScoutObject();
    if (table != null && row != null) {
      return table.getFilteredRowIndex(row);//row.getRowIndex();
    }
    return -1;
  }

  protected int[] scoutToSwingRows(ITableRow[] rows) {
    if (rows == null || rows.length == 0) return new int[0];
    //
    ITable table = getScoutObject();
    if (table != null) {
      int mismatchCount = 0;
      int[] swingRows = new int[rows.length];
      for (int i = 0; i < swingRows.length; i++) {
        int scoutIndex = table.getFilteredRowIndex(rows[i]);
        if (scoutIndex >= 0) {
          swingRows[i] = scoutIndex;
        }
        else {
          swingRows[i] = -1;
          mismatchCount++;
        }
      }
      if (mismatchCount > 0) {
        int[] newSwingRows = new int[swingRows.length - mismatchCount];
        int index = 0;
        for (int row : swingRows) {
          if (row >= 0) {
            newSwingRows[index] = row;
            index++;
          }
        }
        swingRows = newSwingRows;
      }
      return swingRows;
    }
    return new int[0];
  }

  private class P_SwingTable extends SwingTable {
    private static final long serialVersionUID = 1L;

    /**
     * override to decorate cells
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
      // try to reset before super call
      if (renderer instanceof JComponent) {
        JComponent jc = ((JComponent) renderer);
        //use html view cache. must set html view to null, otherwise the cached view is reset every time (by setting all view.parent=null in BasicHtml.updateRenderer)
        jc.putClientProperty(BasicHTML.propertyKey, null);
        jc.putClientProperty("html.disable", Boolean.TRUE);
        jc.setBackground(getBackground());
        jc.setForeground(getForeground());
        jc.setFont(getFont());
      }
      JComponent c = (JComponent) super.prepareRenderer(renderer, row, column);
      boolean isSelected = isCellSelected(row, column);
      // scout cell
      JTable swingTable = P_SwingTable.this;
      ITable scoutTable = getScoutObject();
      IColumn scoutCol = ((SwingTableColumn) swingTable.getColumnModel().getColumn(column)).getScoutColumn();
      ITableRow scoutRow = swingToScoutRow(row);
      if (scoutTable != null) {
        ICell cell = scoutTable.getCell(scoutRow, scoutCol);
        if (cell != null) {
          // enabled
          c.setEnabled(scoutTable.isEnabled() && scoutRow.isEnabled() && cell.isEnabled());
          // align
          if (c instanceof JLabel) {
            int align = cell.getHorizontalAlignment();
            // first column always left-aligned
            if (column == 0 && (!StringUtility.isNullOrEmpty(scoutRow.getIconId()) || !StringUtility.isNullOrEmpty(cell.getIconId()))) {
              align = -1;
            }
            if (align > 0) {
              ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
            }
            else if (align == 0) {
              ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            }
            else {
              ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
            }
          }
          // icon
          Icon icon = null;
          if (scoutTable.isCheckable() && column == 0) {
            icon = new CheckboxIcon();
            ((CheckboxIcon) icon).setSelected(scoutRow.isChecked());
            ((CheckboxIcon) icon).setEnabled(c.isEnabled());
          }
          else if (scoutCol.getDataType() == Boolean.class && (!(scoutCol instanceof ISmartColumn) || ((ISmartColumn) scoutCol).getLookupCall() == null)) {
            icon = new CheckboxIcon();
            Boolean b = (Boolean) cell.getValue();
            ((CheckboxIcon) icon).setSelected(b != null && b.booleanValue());
            ((CheckboxIcon) icon).setEnabled(c.isEnabled());
          }
          else if (cell.getIconId() != null) {
            icon = getSwingEnvironment().getIcon(cell.getIconId());
          }
          else if (column == 0) {
            icon = getSwingEnvironment().getIcon(scoutRow.getIconId());
          }

          if (cell.isEditable()) {
            icon = new P_IconWithMarker(icon);
          }

          if (c instanceof JLabel) {
            ((JLabel) c).setIcon(icon);
            ((JLabel) c).setDisabledIcon(icon);
          }
          // foreground
          //TODO use row fg if cells value is null
          if (cell.getForegroundColor() != null) {
            Color color = SwingUtility.createColor(cell.getForegroundColor());
            if (isSelected) {
              color = color.brighter();
            }
            c.setForeground(color);
          }
          // font (must be set before text, otherwise html view is null again)
          //TODO use row font if cells value is null
          if (cell.getFont() != null) {
            Font oldf = getFont();
            Font newf = SwingUtility.createFont(cell.getFont(), oldf);
            if (oldf != null) {// only override font style, not size and face
              c.setFont(new Font(oldf.getName(), newf.getStyle(), oldf.getSize()));
            }
          }
          // text
          String text = cell.getText();
          boolean wrapText = (scoutCol instanceof IStringColumn && ((IStringColumn) scoutCol).isTextWrap());
          if (scoutCol.getDataType() == Boolean.class && (!(scoutCol instanceof ISmartColumn) || ((ISmartColumn) scoutCol).getLookupCall() == null)) {
            text = null;
          }
          if (wrapText || (scoutTable.isMultilineText() && SwingUtility.isMultilineLabelText(text))) {
            text = SwingUtility.createHtmlLabelText(text, wrapText);
          }
          else {
            // make single line
            if (text != null) {
              text = text.replaceAll("[\\n\\r]+", " ");
            }
          }
          if (c instanceof JLabel) {
            ((JLabel) c).setText(text);
            if (m_htmlViewCache != null) {
              m_htmlViewCache.updateHtmlView((JLabel) c);
            }
          }
          // tooltip
          //TODO use row tt if cells value is null
          String s = cell.getTooltipText();
          if (s != null && s.length() == 0) {
            s = null;
          }
          s = SwingUtility.createHtmlLabelText(s, true);
          c.setToolTipText(s);
          // background
          //TODO use row bg if cells value is null
          if (cell.getBackgroundColor() != null) {
            Color color = SwingUtility.createColor(cell.getBackgroundColor());
            if (isSelected) {
              if (c.getBackground() != null) {
                // bsh 2010-10-08: if possible, merge colors instead of just using a darker version
                color = ColorUtility.mergeColors(c.getBackground(), 0.5f, color, 0.5f);
              }
              else {
                color = color.darker();
              }
            }
            c.setBackground(color);
          }
          else {
            // bsh 2010-10-08: consider table background color as well (e.g. in ListBoxes)
            if (swingTable.getBackground() != null && isSelected) {
              Color color = swingTable.getBackground();
              if (c.getBackground() != null) {
                color = ColorUtility.mergeColors(c.getBackground(), 0.5f, color, 0.5f);
              }
              else {
                color = color.darker();
              }
              c.setBackground(color);
            }
          }
        }
      }
      return c;
    }
  }

  private class P_IconWithMarker implements Icon {

    private static final int MARKER_SIZE = 5;
    private Icon m_icon;

    private P_IconWithMarker(Icon icon) {
      m_icon = icon;
      if (m_icon == null) {
        // in case of no custom icon is set
        m_icon = new ImageIcon();
      }
    }

    @Override
    public int getIconHeight() {
      return m_icon.getIconHeight();
    }

    @Override
    public int getIconWidth() {
      return m_icon.getIconWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      // draw marker into icon
      Color editableCellMarkerColor = UIManager.getColor("Table.cell.markerColorEditableCell");
      if (editableCellMarkerColor == null) {
        editableCellMarkerColor = Color.GRAY;
      }
      g.setColor(editableCellMarkerColor);
      g.fillPolygon(new int[]{0, MARKER_SIZE, 0}, new int[]{0, 0, MARKER_SIZE}, 3);
      m_icon.paintIcon(c, g, x, y);
    }
  }

  /**
   * Implementation of Sort and Popup Functionality for table header
   */
  private class P_SwingHeadMouseListener extends MouseAdapter {
    private Point m_pressedPoint;// to avoid sorting on column move
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
      m_pressedPoint = e.getPoint();
      JTableEx table = getSwingTable();
      int colIndex = table.getTableHeader().getColumnModel().getColumnIndexAtX(e.getX());
      if (colIndex >= 0) {
        table.setColumnSelectionInterval(colIndex, colIndex);
      }
      // Mac popup
      if (e.isPopupTrigger()) {
        handleSwingHeaderPopup(e);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      JTableEx table = getSwingTable();
      if (e.isPopupTrigger()) {
        handleSwingHeaderPopup(e);
      }
      else {
        // sorting only if there was no dragging
        if (m_pressedPoint == null) m_pressedPoint = e.getPoint();
        m_pressedPoint.translate(-e.getX(), -e.getY());
        int r = Math.abs(m_pressedPoint.x) + Math.abs(m_pressedPoint.y);
        if (r < 4 && e.getClickCount() == 1) {
          if (table.getTableHeader().getCursor().getType() == Cursor.DEFAULT_CURSOR) {
            int index = table.getTableHeader().columnAtPoint(e.getPoint());
            if (index >= 0) {
              int scoutViewIndex = index;
              handleSwingHeaderSort(scoutViewIndex, e.isShiftDown(), e.isControlDown());
            }
          }
        }
      }
      fix.mouseReleased(this, e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) return;
      if (e.getClickCount() == 2) {
        if (getSwingTable().getTableHeader().getCursor().getType() != Cursor.DEFAULT_CURSOR) {
          final int[] oldColumnWidths = new int[getSwingTableColumnModel().getColumnCount()];
          for (int i = 0; i < oldColumnWidths.length; i++) {
            oldColumnWidths[i] = getSwingTableColumnModel().getColumn(i).getWidth();
          }
          // optimize column widths
          ((P_SwingTable) getSwingTable()).setOptimalColumnWidths();
          // build delta
          ArrayList<TableColumn> list = new ArrayList<TableColumn>();
          for (int i = 0; i < oldColumnWidths.length; i++) {
            TableColumn tc = getSwingTableColumnModel().getColumn(i);
            int newWidth = tc.getPreferredWidth();
            if (newWidth != oldColumnWidths[i]) {
              list.add(tc);
            }
          }
          if (list.size() > 0) {
            storeColumnWidthsFromSwing(list);
          }
        }
      }
    }
  }// end class

  /**
   * Click behaviour for checkable tables
   */
  private class P_SwingRowMouseListener extends MouseInputAdapter {
    MouseClickedBugFix fix;

    @Override
    public void mousePressed(MouseEvent e) {
      fix = new MouseClickedBugFix(e);
      //
      JTableEx table = getSwingTable();
      int pressedRow = table.rowAtPoint(e.getPoint());
      // if selection is empty or click outside selection then make section of
      // this row
      // IMO 3.6.03 multi-selection with Ctrl can deselect rows
      // right click down
      // ONLY when enabled (see: SwingScoutListbox selections=checked row)
      if (e.isMetaDown()) {
        if (table.isEnabled()) {
          boolean autoSelectRow = true;
          if (table.getSelectionModel().getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
            if (e.isControlDown() || e.isShiftDown()) {
              if (table.getSelectedRowCount() > 0) {
                autoSelectRow = false;
              }
            }
          }
          if (autoSelectRow) {
            if (pressedRow >= 0 && !table.isRowSelected(pressedRow)) {
              table.setRowSelectionInterval(pressedRow, pressedRow);
            }
          }
        }
      }
      // doubleclick
      if (e.getClickCount() == 2 && pressedRow >= 0) {
        if (table.isEnabled()) {
          if (pressedRow >= 0 && table.getSelectedRowCount() <= 1 && (!table.isRowSelected(pressedRow))) {
            table.setRowSelectionInterval(pressedRow, pressedRow);
          }
        }
      }
      int swingViewIndex = getSwingTable().columnAtPoint(e.getPoint());
      setContextColumnFromSwing(swingViewIndex);
      // Mac popup
      if (e.isPopupTrigger()) {
        handleSwingRowPopup(e);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handleSwingRowPopup(e);
      }
      fix.mouseReleased(this, e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (fix.mouseClicked()) return;
      if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
        int pressedRow = getSwingTable().rowAtPoint(e.getPoint());
        if (pressedRow >= 0) {
          handleSwingRowClick(pressedRow);
        }
      }
      else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
        int pressedRow = getSwingTable().rowAtPoint(e.getPoint());
        if (pressedRow >= 0) {
          handleSwingRowAction(pressedRow);
        }
      }
    }
  }// end private class

  /**
   * Implementation of Popup Functionality for table rows
   */
  private class P_SwingEmptySpaceMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      // Mac popup
      if (e.isPopupTrigger()) {
        handleSwingEmptySpacePopup(e);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handleSwingEmptySpacePopup(e);
      }
    }
  }// end private class

  private class P_ScoutTableListener implements TableListener {
    public void tableChanged(final TableEvent e) {
      if (isHandleScoutTableEvent(new TableEvent[]{e})) {
        if (isIgnoredScoutEvent(TableEvent.class, "" + e.getType())) return;
        //
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateSwingFromScoutLock().acquire();
              //
              handleScoutTableEventInSwing(e);
            }
            finally {
              getUpdateSwingFromScoutLock().release();
            }
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }

    public void tableChangedBatch(final TableEvent[] a) {
      if (isHandleScoutTableEvent(a)) {
        final ArrayList<TableEvent> filteredList = new ArrayList<TableEvent>();
        for (int i = 0; i < a.length; i++) {
          if (!isIgnoredScoutEvent(TableEvent.class, "" + a[i].getType())) {
            filteredList.add(a[i]);
          }
        }
        if (filteredList.size() == 0) return;
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateSwingFromScoutLock().acquire();
              //
              for (TableEvent e : filteredList) {
                handleScoutTableEventInSwing(e);
              }
            }
            finally {
              getUpdateSwingFromScoutLock().release();
            }
          }
        };
        getSwingEnvironment().invokeSwingLater(t);
      }
    }
  }// end private class

  /**
   * Implementation of DropSource's DragGestureListener support for drag/drop
   * 
   * @since Build 202
   */
  private class P_SwingRowTransferHandler extends TransferHandlerEx {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean canDrag() {
      return m_cachedScoutDragTransferTypes != 0;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
      return handleSwingDragRequest();
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      return SwingUtility.isSupportedTransfer(m_cachedScoutDropTransferTypes, transferFlavors);
    }

    @Override
    public boolean importDataEx(JComponent comp, Transferable t, Point location) {
      if (location != null) {
        int droppingRow = getSwingTable().rowAtPoint(location);
        handleSwingDropAction(droppingRow, t);
        return true;
      }
      return false;
    }
  }// end private class

  private class P_SwingEmptySpaceTransferHandler extends TransferHandlerEx {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      return SwingUtility.isSupportedTransfer(m_cachedScoutDropTransferTypes, transferFlavors);
    }

    @Override
    public boolean importDataEx(JComponent comp, Transferable t, Point location) {
      int selectedRow = -1;
      handleSwingDropAction(selectedRow, t);
      return true;
    }

    @Override
    public boolean canDrag() {
      // always false
      return false;
    }

    @Override
    public Transferable createTransferable(JComponent c) {
      return null;
    }

  }// end private class

  private class P_KeyboardNavigationSupport extends TableKeyboardNavigationSupport {
    public P_KeyboardNavigationSupport(JTableEx table) {
      super(table);
    }

    @Override
    void handleKeyboardNavigation(int rowIndex) {
      handleKeyboardNavigationFromSwing(rowIndex);
    }

  } // end class P_KeyboardNavigationSupport

  private class P_SwingSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (!getSwingTable().getSelectionModel().getValueIsAdjusting()) {
        int[] swingRows = getSwingTable().getSelectedRows();
        setSelectionFromSwing(swingRows);
      }
    }
  }// end private class

}
