/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.basic.table;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.RowIndexComparator;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.AbstractOpenMenuJob;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor;
import org.eclipse.scout.rt.ui.rap.core.util.BrowserInfo;
import org.eclipse.scout.rt.ui.rap.core.util.UiRedrawHandler;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.ext.table.TableViewerEx;
import org.eclipse.scout.rt.ui.rap.ext.table.util.TableRolloverSupport;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <h3>RwtScoutTable</h3> ...
 * knownIssues - multi column sorting is not supported, unable to get any key
 * mask in the selection event.
 * <p>
 * - multi line support in headers is not supported by rwt.
 * <p>
 * - multi line support in row texts is not supported so far. Might probably be done by customized table rows.
 * 
 * @since 3.7.0 June 2011
 */
public class RwtScoutTable extends RwtScoutComposite<ITable> implements IRwtScoutTableForPatch {
  private P_ScoutTableListener m_scoutTableListener;
  private UiRedrawHandler m_redrawHandler;

  private Listener m_autoResizeColumnListener;
  private Listener m_columnListener = new P_TableColumnListener();
  private SelectionListener m_columnSortListener = new P_ColumnSortListener();
  private TableColumnManager m_uiColumnManager = new TableColumnManager();
  private RwtScoutTableCellEditor m_uiCellEditorComposite;
  private int[] m_uiColumnOrder;
  private Menu m_contextMenu;
  private Menu m_headerMenu;
  private TableViewer m_uiViewer;
  private IRwtKeyStroke[] m_uiKeyStrokes;
  private ClientSyncJob m_storeColumnWidthsJob;

  private String m_variant = "";

  private AbstractTableKeyboardNavigationSupport m_keyboardNavigationSupport;

  public RwtScoutTable() {
  }

  public RwtScoutTable(String variant) {
    m_variant = variant;
  }

  @Override
  protected void initializeUi(Composite parent) {
    m_redrawHandler = new UiRedrawHandler(parent);
    int style;
    if (getScoutObject() != null && getScoutObject().isMultiSelect()) {
      style = SWT.MULTI;
    }
    else {
      style = SWT.SINGLE;
    }
    style |= SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION;
    TableEx table = getUiEnvironment().getFormToolkit().createTable(parent, style);
    if (StringUtility.hasText(m_variant)) {
      table.setData(WidgetUtil.CUSTOM_VARIANT, m_variant);
    }
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    new TableRolloverSupport(table);
    table.addDisposeListener(new DisposeListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void widgetDisposed(DisposeEvent e) {
        if (m_headerMenu != null && !m_headerMenu.isDisposed()) {
          m_headerMenu.dispose();
        }
        if (m_contextMenu != null && !m_contextMenu.isDisposed()) {
          m_contextMenu.dispose();
        }
      }
    });
    TableViewer viewer = new TableViewerEx(table);
    viewer.setUseHashlookup(true);
    setUiTableViewer(viewer);
    setUiField(table);
    //cell editing support
    m_uiCellEditorComposite = new RwtScoutTableCellEditor(this);
    // header menu
    m_headerMenu = new Menu(viewer.getTable().getShell(), SWT.POP_UP);
    table.addMenuDetectListener(new P_RwtHeaderMenuDetectListener());

    //columns
    initializeUiColumns();

    RwtScoutTableModel tableModel = createUiTableModel();
    tableModel.setMultiline(getScoutObject().isMultilineText());
    viewer.setContentProvider(tableModel);
    viewer.setLabelProvider(tableModel);
    viewer.setInput(tableModel);

    // ui listeners
    viewer.addSelectionChangedListener(new P_RwtSelectionListener());
    P_RwtTableListener rwtTableListener = new P_RwtTableListener();
    table.addListener(SWT.MouseDown, rwtTableListener);
    table.addListener(SWT.MouseUp, rwtTableListener);
    table.addListener(SWT.MouseDoubleClick, rwtTableListener);
    table.addListener(SWT.MenuDetect, rwtTableListener);
    table.addListener(SWT.KeyUp, rwtTableListener);
    table.addListener(SWT.Resize, rwtTableListener);

    // context menu
    Menu contextMenu = new Menu(viewer.getTable().getShell(), SWT.POP_UP);
    contextMenu.addMenuListener(new P_ContextMenuListener());
    m_contextMenu = contextMenu;
  }

  @Override
  public boolean isUiDisposed() {
    return getUiField() == null || getUiField().isDisposed();
  }

  protected RwtScoutTableModel createUiTableModel() {
    return new RwtScoutTableModel(getScoutObject(), this, m_uiColumnManager);
  }

  @Override
  public TableColumnManager getUiColumnManager() {
    return m_uiColumnManager;
  }

  @Override
  public void initializeUiColumns() {
    m_redrawHandler.pushControlChanging();
    try {
      for (TableColumn col : getUiField().getColumns()) {
        col.dispose();
      }
      /*
       * bug: rwt table first column can not be aligned nor an image can be set.
       * see also RwtScoutTableCellEditor
       */
      TableColumn dummyCol = new TableColumn(getUiField(), SWT.LEFT);
      dummyCol.setWidth(0);
      dummyCol.setResizable(false);
      dummyCol.setMoveable(false);
      boolean sortEnabled = false;
      IColumn<?>[] scoutColumnsOrdered;
      if (getScoutObject() != null) {
        scoutColumnsOrdered = getScoutObject().getColumnSet().getVisibleColumns();
        sortEnabled = getScoutObject().isSortEnabled();
      }
      else {
        scoutColumnsOrdered = new IColumn[0];
      }
      if (m_uiColumnManager == null) {
        m_uiColumnManager = new TableColumnManager();
      }
      m_uiColumnManager.initialize(scoutColumnsOrdered);
      boolean multilineHeaders = false;
      for (IColumn<?> scoutColumn : scoutColumnsOrdered) {
        IHeaderCell cell = scoutColumn.getHeaderCell();
        String cellText = cell.getText();
        if (cellText == null) {
          cellText = "";
        }
        if (cellText.indexOf("\n") >= 0) {
          multilineHeaders = true;
        }
        int style = RwtUtility.getHorizontalAlignment(cell.getHorizontalAlignment());
        TableColumn rwtCol = new TableColumn(getUiField(), style);
        rwtCol.setData(KEY_SCOUT_COLUMN, scoutColumn);
        rwtCol.setMoveable(true);
        rwtCol.setToolTipText(cell.getTooltipText());
        updateHeaderText(rwtCol, scoutColumn);
        rwtCol.setWidth(scoutColumn.getWidth());
        if (cell.isSortActive()) {
          getUiField().setSortColumn(rwtCol);
          getUiField().setSortDirection(cell.isSortAscending() ? SWT.UP : SWT.DOWN);
        }
        if (sortEnabled) {
          rwtCol.addSelectionListener(m_columnSortListener);
        }
        rwtCol.addListener(SWT.Move, m_columnListener);
        rwtCol.addListener(SWT.Resize, m_columnListener);
      }
      //multiline header settings
      if (multilineHeaders) {
        getUiField().setData("multiLineHeader", Boolean.TRUE);
      }
      m_uiColumnOrder = getUiField().getColumnOrder();
      //update cell editors
      m_uiCellEditorComposite.initializeUi();
    }
    finally {
      m_redrawHandler.popControlChanging();
    }
  }

  @Override
  protected void attachScout() {
    super.attachScout();
    if (getScoutObject() != null) {
      if (m_scoutTableListener == null) {
        m_scoutTableListener = new P_ScoutTableListener();
        getScoutObject().addUITableListener(m_scoutTableListener);
      }
      setHeaderVisibleFromScout(getScoutObject().isHeaderVisible());
      setSelectionFromScout(getScoutObject().getSelectedRows());
      setKeyStrokeFormScout();
      setRowHeightFromScout();
      setKeyboardNavigationFromScout();
      updateAutoResizeColumnsFromScout();
      // dnd support
      new P_DndSupport(getScoutObject(), getScoutObject(), getUiField());
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

  @Override
  public TableEx getUiField() {
    return (TableEx) super.getUiField();
  }

  @Override
  public TableViewer getUiTableViewer() {
    return m_uiViewer;
  }

  @Override
  public void setUiTableViewer(TableViewer uiViewer) {
    m_uiViewer = uiViewer;
  }

  @Override
  public ITableRow getUiSelectedRow() {
    ITableRow[] rows = getUiSelectedRows();
    if (rows.length > 0) {
      return rows[0];
    }
    return null;
  }

  @Override
  public ITableRow[] getUiSelectedRows() {
    StructuredSelection uiSelection = (StructuredSelection) getUiTableViewer().getSelection();
    TreeSet<ITableRow> sortedRows = new TreeSet<ITableRow>(new RowIndexComparator());
    if (uiSelection != null && !uiSelection.isEmpty()) {
      for (Object o : uiSelection.toArray()) {
        ITableRow row = (ITableRow) o;
        sortedRows.add(row);
      }
    }
    return sortedRows.toArray(new ITableRow[sortedRows.size()]);
  }

  protected void setKeyStrokeFormScout() {
    // remove old
    if (m_uiKeyStrokes != null) {
      for (IRwtKeyStroke rwtKeyStroke : m_uiKeyStrokes) {
        getUiEnvironment().removeKeyStroke(getUiField(), rwtKeyStroke);
      }
    }
    // add new
    ArrayList<IRwtKeyStroke> newRwtKeyStrokes = new ArrayList<IRwtKeyStroke>();
    IKeyStroke[] scoutKeyStrokes = getScoutObject().getKeyStrokes();
    for (IKeyStroke scoutKeyStroke : scoutKeyStrokes) {
      if (scoutKeyStroke.isEnabled()) {
        IRwtKeyStroke[] rwtStrokes = RwtUtility.getKeyStrokes(scoutKeyStroke, getUiEnvironment());
        for (IRwtKeyStroke rwtStroke : rwtStrokes) {
          getUiEnvironment().addKeyStroke(getUiField(), rwtStroke, false);
          newRwtKeyStrokes.add(rwtStroke);
        }
      }
    }
    m_uiKeyStrokes = newRwtKeyStrokes.toArray(new IRwtKeyStroke[newRwtKeyStrokes.size()]);
  }

  protected void setRowHeightFromScout() {
    int h = getScoutObject().getRowHeightHint();
    if (h <= 0 && getScoutObject().isMultilineText()) {
      h = 40; // Enough for 2 lines fully visible (further lines are cut off) --> cannot be dynamic at the moment, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=346768
    }
    if (h >= 0) {
      getUiField().setData(Table.ITEM_HEIGHT, h);
    }
    if (isCreated()) {
      getUiTableViewer().refresh();
    }
  }

  protected void setKeyboardNavigationFromScout() {
    if (getScoutObject().hasKeyboardNavigation()) {
      if (m_keyboardNavigationSupport == null) {
        m_keyboardNavigationSupport = new P_KeyBoardNavigationSupport(getUiField());
      }
    }
    else {
      if (m_keyboardNavigationSupport != null) {
        m_keyboardNavigationSupport.dispose();
        m_keyboardNavigationSupport = null;
      }
    }
  }

  private void updateAutoResizeColumnsFromScout() {
    if (getUiField() != null && !getUiField().getParent().isDisposed()) {
      Composite parent = getUiField().getParent();
      if (getScoutObject().isAutoResizeColumns()) {
        if (m_autoResizeColumnListener == null) {
          m_autoResizeColumnListener = new P_RwtResizeListener();
          parent.addListener(SWT.Resize, m_autoResizeColumnListener);
        }
      }
      else {
        if (m_autoResizeColumnListener != null) {
          parent.removeListener(SWT.Resize, m_autoResizeColumnListener);
          m_autoResizeColumnListener = null;
        }
      }
    }
  }

  /**
   * scout property observer
   */
  @Override
  protected void handleScoutPropertyChange(String propName, Object newValue) {
    if (propName.equals(ITable.PROP_HEADER_VISIBLE)) {
      setHeaderVisibleFromScout(((Boolean) newValue).booleanValue());
    }
    else if (propName.equals(ITable.PROP_KEY_STROKES)) {
      setKeyStrokeFormScout();
    }
    else if (propName.equals(ITable.PROP_ROW_HEIGHT_HINT)) {
      setRowHeightFromScout();
    }
    else if (propName.equals(ITable.PROP_KEYBOARD_NAVIGATION)) {
      setKeyboardNavigationFromScout();
    }
    else if (propName.equals(ITable.PROP_AUTO_RESIZE_COLUMNS)) {
      updateAutoResizeColumnsFromScout();
    }
  }

  /**
   * scout table observer
   */
  protected boolean isHandleScoutTableEvent(TableEvent[] a) {
    for (TableEvent element : a) {
      switch (element.getType()) {
        case TableEvent.TYPE_REQUEST_FOCUS:
        case TableEvent.TYPE_REQUEST_FOCUS_IN_CELL:
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

  protected void handleScoutTableEventInUi(TableEvent e) {
    if (isUiDisposed()) {
      return;
    }
    RwtScoutTableEvent uiTableEvent = null;
    /*
     * check the scout observer to filter all events that are used here
     * @see isHandleScoutTableEvent()
     */
    switch (e.getType()) {
      case TableEvent.TYPE_REQUEST_FOCUS: {
        getUiField().setFocus();
        break;
      }
      case TableEvent.TYPE_REQUEST_FOCUS_IN_CELL: {
        //start editing
        int swtCol = -1;
        TableColumn[] swtColumns = getUiField().getColumns();
        for (int c = 0; c < swtColumns.length; c++) {
          if (swtColumns[c].getData(KEY_SCOUT_COLUMN) == e.getFirstColumn()) {
            swtCol = c;
            break;
          }
        }
        ITableRow scoutRow = e.getFirstRow();
        if (scoutRow != null && swtCol >= 0) {
          getUiTableViewer().editElement(scoutRow, swtCol);
        }
        break;
      }
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        uiTableEvent = new RwtScoutTableEvent();
        break;
      }
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
        break;
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
        headerUpdateFromScout();
        break;
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        // re-install columns
        initializeUiColumns();
        if (getScoutObject().isAutoResizeColumns()) {
          handleAutoSizeColumns();
        }

        uiTableEvent = new RwtScoutTableEvent();
        break;
      }
      case TableEvent.TYPE_ROWS_SELECTED: {
        setSelectionFromScout(e.getRows());
        break;
      }
    }
    //
    if (uiTableEvent != null) {
      ((RwtScoutTableModel) getUiTableViewer().getContentProvider()).consumeTableModelEvent(uiTableEvent);
      getUiTableViewer().refresh();
    }
    // refresh selection, indexes might have changed
    switch (e.getType()) {
      case TableEvent.TYPE_ROW_FILTER_CHANGED:
        // Update column title if filter changed (mark column as filtered)
        for (TableColumn swtCol : getUiField().getColumns()) {
          updateHeaderText(swtCol);
        }
        setSelectionFromScout(e.getTable().getSelectedRows());
        break;
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_ROW_ORDER_CHANGED: {
        setSelectionFromScout(e.getTable().getSelectedRows());
        break;
      }
    }
  }

  private void updateHeaderText(TableColumn swtCol) {
    if (swtCol == null) {
      return;
    }
    Object data = swtCol.getData(KEY_SCOUT_COLUMN);
    if (data instanceof IColumn<?>) {
      updateHeaderText(swtCol, (IColumn<?>) data);
    }
  }

  private void updateHeaderText(TableColumn swtCol, IColumn<?> scoutCol) {
    updateHeaderText(swtCol, scoutCol, false);
  }

  private void updateHeaderText(TableColumn swtCol, IColumn<?> scoutCol, boolean indicateSortOrder) {
    IHeaderCell cell = scoutCol.getHeaderCell();
    String text = cell.getText();
    if (text == null) {
      text = "";
    }
    if (scoutCol instanceof ICustomColumn) {
      text = "[+] " + text;
    }
    if (scoutCol.isColumnFilterActive()) {
      text = "(*) " + text;
    }
    if (indicateSortOrder) {
      if (scoutCol.isSortAscending()) {
        text = "[a-z] " + text;
      }
      else {
        text = "[z-a] " + text;
      }
    }
    swtCol.setText(text);
  }

  protected void setHeaderVisibleFromScout(boolean headerVisible) {
    getUiField().setHeaderVisible(headerVisible);
  }

  @Override
  public void setEnabledFromScout(boolean enabledFromScout) {
    getUiField().setEnabled(!enabledFromScout);
    // <Workaround>
    // Because RAP seems to ignore the default ":disabled" state,
    // we apply a custom variant to all header cells. Otherwise
    // the "normal" style would be used for disabled cells.
    for (TableColumn column : getUiField().getColumns()) {
      column.setData(WidgetUtil.CUSTOM_VARIANT, (enabledFromScout ? null : VARIANT_TABLE_COLUMN_DISABLED));
    }
    // </Workaround>
  }

  protected void setSelectionFromScout(ITableRow[] selectedRows) {
    if (getUiField().isDisposed()) {
      return;
    }
    ITableRow[] uiSelection = getUiSelectedRows();
    if (CompareUtility.equals(uiSelection, selectedRows)) {
      // no change
      return;
    }
    else {
      if (selectedRows == null) {
        selectedRows = new ITableRow[0];
      }
      getUiTableViewer().setSelection(new StructuredSelection(selectedRows), true);

      if (selectedRows.length > 0) {
        getUiTableViewer().reveal(selectedRows[0]);
      }
    }
  }

  protected void setContextColumnFromUi(TableColumn uiColumn) {
    if (getScoutObject() != null) {
      //try to find correct location, since TableColumn has NO x,y and is not a Control!
      Point pDisp = getUiField().toDisplay(-getUiField().getHorizontalBar().getSelection(), 0);
      for (TableColumn c : getUiField().getColumns()) {
        if (c == uiColumn) {
          break;
        }
        pDisp.x += c.getWidth();
      }
      getUiEnvironment().setPopupOwner(getUiField(), new Rectangle(pDisp.x - 2, pDisp.y, 1, getUiField().getHeaderHeight()));
      // notify Scout
      IColumn scoutColumn = null;
      if (uiColumn != null) {
        scoutColumn = (IColumn<?>) uiColumn.getData(KEY_SCOUT_COLUMN);
      }
      final IColumn finalCol = scoutColumn;
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setContextColumnFromUI(finalCol);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  private int getVisualCellIndex(TableItem tableItem, int columnIndex) {
    int visualCellIndex = columnIndex;
    final int[] columnOrder = tableItem.getParent().getColumnOrder();
    for (int element : columnOrder) {
      if (element == columnIndex) {
        visualCellIndex = columnIndex;
      }
    }
    return visualCellIndex;
  }

  protected void setSelectionFromUi(final StructuredSelection selection) {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    //
    if (getScoutObject() != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
            //
            getScoutObject().getUIFacade().setSelectedRowsFromUI(RwtUtility.getItemsOfSelection(ITableRow.class, selection));
          }
          finally {
            removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_ROWS_SELECTED);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void headerUpdateFromScout() {
    // Because SWT can only indicate one sort column, we will use the first (i.e. the column
    // with the lowest sort index) user sort column that is visible for that purpose. Further
    // sort columns will be indicated by a special header text (see updateHeaderText() method).
    int minSortIndex = -1;
    TableColumn minUiSortColumn = null;
    IColumn<?> minScoutSortColumn = null;
    for (TableColumn col : getUiField().getColumns()) {
      Object data = col.getData(KEY_SCOUT_COLUMN);
      if (data instanceof IColumn<?>) {
        IColumn<?> cell = (IColumn<?>) data;
        if (cell.isSortExplicit() && (minSortIndex == -1 || cell.getSortIndex() < minSortIndex)) {
          minSortIndex = cell.getSortIndex();
          minUiSortColumn = col;
          minScoutSortColumn = cell;
        }
      }
    }

    if (minUiSortColumn != null && minScoutSortColumn != null) {
      getUiField().setSortColumn(minUiSortColumn);
      getUiField().setSortDirection(minScoutSortColumn.isSortAscending() ? SWT.UP : SWT.DOWN);
    }
    for (TableColumn col : getUiField().getColumns()) {
      Object data = col.getData(KEY_SCOUT_COLUMN);
      if (data instanceof IColumn<?>) {
        IColumn<?> cell = (IColumn<?>) data;
        boolean indicateSortOrder = (cell.isSortExplicit() && cell != minScoutSortColumn);
        updateHeaderText(col, cell, indicateSortOrder);
      }
    }
  }

  protected void handleUiRowClick(final ITableRow row) {
    if (getScoutObject() != null) {
      if (row != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowClickFromUI(row);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleUiRowAction(final ITableRow row) {
    if (getScoutObject() != null) {
      if (!getScoutObject().isCheckable() && row != null) {
        // notify Scout
        Runnable t = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireRowActionFromUI(row);
          }
        };
        getUiEnvironment().invokeScoutLater(t, 0);
        // end notify
      }
    }
  }

  protected void handleUiHyperlinkAction(String urlText) {
    if (getScoutObject() != null) {
      final URL url;
      try {
        url = new URL(urlText);
      }
      catch (MalformedURLException e) {
        //nop
        return;
      }
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          ITable table = getScoutObject();
          table.getUIFacade().fireHyperlinkActionFromUI(table.getSelectedRow(), table.getContextColumn(), url);
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  /**
   * Distributes the table width to the columns considered column weights of
   * model. Empty space will be distributed weighted.
   */
  protected void handleAutoSizeColumns() {
    int totalWidth = getUiField().getClientArea().width;
    /* fixed in rwt
    if (getUiField().getVerticalBar() != null && getUiField().getVerticalBar().getVisible()) {
      totalWidth -= getUiField().getVerticalBar().getSize().x;
    }
    */
    if (totalWidth < 32) {
      //either not showing or not yet layouted
      return;
    }
    int totalWeight = 0;
    HashMap<TableColumn, Integer> columnWeights = new HashMap<TableColumn, Integer>();
    for (TableColumn col : getUiField().getColumns()) {
      if (col == null || col.isDisposed()) {
        continue;
      }
      Object data = col.getData(RwtScoutTable.KEY_SCOUT_COLUMN);
      if (data instanceof IColumn<?>) {
        int width = ((IColumn<?>) data).getInitialWidth();
        columnWeights.put(col, width);
        totalWeight += width;
      }
      else {
        totalWidth -= col.getWidth();
      }
    }
    double factor = (double) totalWidth / (double) totalWeight;
    int i = 0;
    for (Entry<TableColumn, Integer> entry : columnWeights.entrySet()) {
      if (i < columnWeights.size() - 1) {
        int width = (int) (factor * entry.getValue().intValue());
        entry.getKey().setWidth(width);
        totalWidth -= width;
        i++;
      }
      else {
        //Column must not get smaller than initial width.
        int width = Math.max(totalWidth, entry.getValue());
        entry.getKey().setWidth(width);
      }
    }
  }

  protected void handleUiColumnResized(TableColumn column) {
    if (column.isDisposed()) {
      return;
    }
    if (!column.getParent().isVisible()) {
      return;
    }
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    //
    final int width = column.getWidth();
    final IColumn<?> scoutColumn = (IColumn<?>) column.getData(KEY_SCOUT_COLUMN);
    if (scoutColumn != null) {
      if (scoutColumn.getWidth() != width) {

        //Cancel already scheduled resize job to protect the model from too many resize events.
        if (m_storeColumnWidthsJob != null) {
          m_storeColumnWidthsJob.cancel();
        }

        /*
         * imo, 04.06.2009: added swt-side optimistic lock check Method
         * autoSizeColumns indirectly calls this code by setting the width on a
         * table column if this code calls scout using synchronizer it may
         * invoke pending swt jobs while waiting, which can execute a dispose
         * form job. After that job all columns are disposed. This results in
         * WidgetDisposed exceptions on line with entry.getKey().setWidth(...)
         */
        m_storeColumnWidthsJob = new ClientSyncJob("Store column widths", getUiEnvironment().getClientSession()) {
          @Override
          protected IStatus runStatus(IProgressMonitor monitor) {
            try {
              addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
              //
              getScoutObject().getUIFacade().setColumnWidthFromUI(scoutColumn, width);
            }
            finally {
              removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
            }

            return Status.OK_STATUS;
          }
        };
        m_storeColumnWidthsJob.schedule(400);
      }
    }
  }

  protected void handleUiColumnMoved() {
    if (getUpdateUiFromScoutLock().isAcquired()) {
      return;
    }
    int[] uiColumnOrder = getUiField().getColumnOrder();
    // do not allow to reorder icon and dummy column
    if (uiColumnOrder[0] != 0) {
      getUiField().setColumnOrder(m_uiColumnOrder);
      return;
    }
    // if column with icon has changed position
    if (uiColumnOrder[1] != m_uiColumnOrder[1]
        && getScoutObject().getRowCount() > 0
        && StringUtility.hasText(getScoutObject().getRow(0).getIconId())) {
      getUiTableViewer().refresh();
    }
    int[] truncatedColOrder = new int[uiColumnOrder.length - 1];
    for (int i = 0; i < truncatedColOrder.length; i++) {
      truncatedColOrder[i] = uiColumnOrder[i + 1] - 1;
    }
    final IColumn<?>[] newOrder = m_uiColumnManager.getOrderedColumns(truncatedColOrder);
    if (m_uiColumnManager.applyNewOrder(newOrder)) {
      m_uiColumnOrder = uiColumnOrder;
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          try {
            addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_ORDER_CHANGED);
            addIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
            //
            getScoutObject().getUIFacade().fireVisibleColumnsChangedFromUI(newOrder);
          }
          finally {
            removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_ORDER_CHANGED);
            removeIgnoredScoutEvent(TableEvent.class, "" + TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
          }
        }
      };
      getUiEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  protected void handleKeyboardNavigationFromUi(TableItem item) {
//    if (getScoutObject().isCheckable()) {
//      //nop
//      return;
//    }
    getUiField().setSelection(item);
    Event selectionEvent = new Event();
    selectionEvent.type = SWT.DefaultSelection;
    selectionEvent.widget = getUiField();
    for (Listener l : getUiField().getListeners(SWT.DefaultSelection)) {
      l.handleEvent(selectionEvent);
    }
  }

  private class P_ScoutTableListener implements TableListener {
    @Override
    public void tableChanged(final TableEvent e) {
      if (isHandleScoutTableEvent(new TableEvent[]{e})) {
        if (isIgnoredScoutEvent(TableEvent.class, "" + e.getType())) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            try {
              getUpdateUiFromScoutLock().acquire();
              //
              handleScoutTableEventInUi(e);
            }
            finally {
              getUpdateUiFromScoutLock().release();
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }

    @Override
    public void tableChangedBatch(final TableEvent[] a) {
      if (isHandleScoutTableEvent(a)) {
        final ArrayList<TableEvent> filteredList = new ArrayList<TableEvent>();
        for (int i = 0; i < a.length; i++) {
          if (!isIgnoredScoutEvent(TableEvent.class, "" + a[i].getType())) {
            filteredList.add(a[i]);
          }
        }
        if (filteredList.size() == 0) {
          return;
        }
        Runnable t = new Runnable() {
          @Override
          public void run() {
            if (isUiDisposed()) {
              return;
            }
            m_redrawHandler.pushControlChanging();
            try {
              try {
                getUpdateUiFromScoutLock().acquire();
                //
                for (TableEvent element : filteredList) {
                  handleScoutTableEventInUi(element);
                }
              }
              finally {
                getUpdateUiFromScoutLock().release();
              }
            }
            finally {
              m_redrawHandler.popControlChanging();
            }
          }
        };
        getUiEnvironment().invokeUiLater(t);
      }
    }
  }// end P_ScoutTableListener

  private void showMenu(Point eventPosition) {
    Point pt = getUiField().getDisplay().map(null, getUiField(), eventPosition);
    Rectangle clientArea = getUiField().getClientArea();
    boolean header = clientArea.y <= pt.y && pt.y < clientArea.y + getUiField().getHeaderHeight();
    getUiField().setMenu(header ? m_headerMenu : m_contextMenu);
    getUiField().getMenu().addMenuListener(new MenuAdapter() {
      private static final long serialVersionUID = 1L;

      @Override
      public void menuHidden(MenuEvent e) {
        getUiField().setMenu(null);
        ((Menu) e.getSource()).removeMenuListener(this);
      }
    });
    getUiField().getMenu().setLocation(eventPosition);
    getUiField().getMenu().setVisible(true);
  }

  private final class P_OpenMenuJob extends AbstractOpenMenuJob {

    public P_OpenMenuJob(Control UiField) {
      super(UiField);
    }

    @Override
    public void showMenu(Point pt) {
      RwtScoutTable.this.showMenu(pt);
    }
  }

  private class P_RwtTableListener implements Listener {
    private static final long serialVersionUID = 1L;

    private Boolean m_doubleClicked = Boolean.FALSE;

    private long m_mouseDownTime = 0;
    private P_OpenMenuJob m_openMenuJob = new P_OpenMenuJob(getUiField());

    @Override
    public void handleEvent(Event event) {
      Point eventPosition = new Point(event.x, event.y);
      switch (event.type) {
        case SWT.MouseDown: {
          setContextColumnFromUi(RwtUtility.getRwtColumnAt(getUiTableViewer().getTable(), eventPosition));
          if (getUiField().getItem(eventPosition) == null) {
            getUiTableViewer().setSelection(null);
            setSelectionFromUi(new StructuredSelection());
          }
          m_mouseDownTime = new Date().getTime();
          m_openMenuJob.startOpenJob(eventPosition);
          break;
        }
        case SWT.MouseUp: {
          synchronized (m_doubleClicked) {
            if (m_doubleClicked == Boolean.TRUE) {
              m_doubleClicked = Boolean.FALSE;
              break;
            }
          }
          StructuredSelection selection = (StructuredSelection) getUiTableViewer().getSelection();
          BrowserInfo browserInfo = RwtUtility.getBrowserInfo();
          if ((browserInfo.isTablet()
              || browserInfo.isMobile())
              && event.button == 1) {
            long mouseUpTime = new Date().getTime();
            if (mouseUpTime - m_mouseDownTime <= 500L) {
              m_openMenuJob.stopOpenJob();
              if (selection != null && selection.size() == 1) {
                handleUiRowAction((ITableRow) selection.getFirstElement());
              }
            }
          }
          else {
            if (selection != null && selection.size() == 1) {
              handleUiRowClick((ITableRow) selection.getFirstElement());
            }
          }
          break;
        }
        case SWT.MouseDoubleClick: {
          synchronized (m_doubleClicked) {
            m_doubleClicked = Boolean.TRUE;
          }
          StructuredSelection selection = (StructuredSelection) getUiTableViewer().getSelection();
          if (selection != null && selection.size() == 1) {
            handleUiRowAction((ITableRow) selection.getFirstElement());
          }
          break;
        }
        case SWT.Resize: {
          //lazy column auto-fit
          if (getScoutObject().isAutoResizeColumns()) {
            if (getUiField() != null && !getUiField().isDisposed()) {
              getUiField().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                  if (getUiField() != null && !getUiField().isDisposed()) {
                    handleAutoSizeColumns();
                  }
                }
              });
            }
          }
          break;
        }
        case SWT.MenuDetect: {
          showMenu(eventPosition);
          break;
        }
        case SWT.KeyUp: {
          if (event.doit && getScoutObject().isCheckable()) {
            if (event.stateMask == 0) {
              switch (event.keyCode) {
                case ' ':
                case SWT.CR:
                  ITableRow[] selectedRows = RwtUtility.getItemsOfSelection(ITableRow.class, (StructuredSelection) getUiTableViewer().getSelection());
                  if (selectedRows != null && selectedRows.length > 0) {
                    handleUiRowClick(selectedRows[0]);
                  }
                  event.doit = false;
                  break;
              }
            }
          }
          break;
        }
      }
    }
  }

  private class P_RwtResizeListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      //lazy column auto-fit
      if (getUiField() != null && !getUiField().isDisposed()) {
        getUiField().getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (getUiField() != null && !getUiField().isDisposed()) {
              handleAutoSizeColumns();
            }
          }
        });
      }
    }
  } // end class P_SwtResizeListener

  public class P_RwtSelectionListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      setSelectionFromUi((StructuredSelection) event.getSelection());
    }
  }

  private class P_TableColumnListener implements Listener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.Move:
          handleUiColumnMoved();
          break;
        case SWT.Resize:
          if (event.widget instanceof TableColumn) {
            handleUiColumnResized((TableColumn) event.widget);
          }

      }
    }

  } // end class P_TableColumnMoveListener

  private class P_ColumnSortListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent e) {
      TableColumn col = (TableColumn) e.getSource();
      setContextColumnFromUi(col);
      final IColumn<?> newColumn = (IColumn<?>) col.getData(KEY_SCOUT_COLUMN);
      final boolean ctrlKeyPressed = ((e.stateMask & SWT.CONTROL) > 0);
      if (getScoutObject() != null) {
        Runnable job = new Runnable() {
          @Override
          public void run() {
            getScoutObject().getUIFacade().fireHeaderSortFromUI(newColumn, ctrlKeyPressed);
          }
        };
        getUiEnvironment().invokeScoutLater(job, 0);
      }
    }
  } // end class P_ColumnSortListener

  private class P_ContextMenuListener extends MenuAdapterEx {

    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener() {
      super(RwtScoutTable.this.getUiTableViewer().getTable(), RwtScoutTable.this.getUiTableViewer().getTable().getParent());
    }

    @Override
    protected Menu getContextMenu() {
      return m_contextMenu;
    }

    @Override
    protected void setContextMenu(Menu contextMenu) {
      m_contextMenu = contextMenu;
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      final boolean emptySelection = getUiTableViewer().getSelection().isEmpty();
      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          if (emptySelection) {
            scoutMenusRef.set(getScoutObject().getUIFacade().fireEmptySpacePopupFromUI());
          }
          else {
            scoutMenusRef.set(getScoutObject().getUIFacade().fireRowPopupFromUI());
          }
        }
      };
      JobEx job = RwtScoutTable.this.getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden
      // within the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), RwtScoutTable.this.getUiEnvironment(), m_contextMenu);
      }
    }

  } // end class P_ContextMenuListener

  private class P_RwtHeaderMenuDetectListener implements MenuDetectListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void menuDetected(MenuDetectEvent event) {
      Table table = getUiField();
      Point pTable = table.getDisplay().map(null, table, new Point(event.x, event.y));
      Rectangle clientArea = table.getClientArea();
      boolean header = clientArea.y <= pTable.y && pTable.y < clientArea.y + table.getHeaderHeight();
      if (!header) {
        return;
      }
      // clear all previous
      // Windows BUG: fires menu hide before the selection on the menu item is
      // propagated.
      if (m_headerMenu != null) {
        for (MenuItem item : m_headerMenu.getItems()) {
          disposeMenuItem(item);
        }
      }
      setContextColumnFromUi(RwtUtility.getRwtColumnAt(getUiTableViewer().getTable(), pTable));
      final AtomicReference<IMenu[]> scoutMenusRef = new AtomicReference<IMenu[]>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          IMenu[] scoutMenus = getScoutObject().getUIFacade().fireHeaderPopupFromUI();
          scoutMenusRef.set(scoutMenus);
        }
      };
      JobEx job = getUiEnvironment().invokeScoutLater(t, 1200);
      try {
        job.join(1200);
      }
      catch (InterruptedException ex) {
        //nop
      }
      // grab the actions out of the job, when the actions are providden
      // within the scheduled time the popup will be handled.
      if (scoutMenusRef.get() != null) {
        RwtMenuUtility.fillContextMenu(scoutMenusRef.get(), getUiEnvironment(), m_headerMenu);
      }
    }

    protected void disposeMenuItem(MenuItem item) {
      Menu menu = item.getMenu();
      if (menu != null) {
        for (MenuItem childItem : menu.getItems()) {
          disposeMenuItem(childItem);
        }
        menu.dispose();
      }
      item.dispose();
    }
  } // end class P_HeaderMenuListener

  private class P_DndSupport extends AbstractRwtScoutDndSupport {
    public P_DndSupport(IPropertyObserver scoutObject, IDNDSupport scoutDndSupportable, Control control) {
      super(scoutObject, scoutDndSupportable, control, RwtScoutTable.this.getUiEnvironment());
    }

    @Override
    protected TransferObject handleUiDragRequest() {
      final Holder<TransferObject> result = new Holder<TransferObject>(TransferObject.class, null);
      Runnable t = new Runnable() {
        @Override
        public void run() {
          TransferObject scoutTransferable = getScoutObject().getUIFacade().fireRowsDragRequestFromUI();
          result.setValue(scoutTransferable);
        }
      };
      try {
        getUiEnvironment().invokeScoutLater(t, 20000).join(20000);
      }
      catch (InterruptedException e) {
        //nop
      }
      return result.getValue();
    }

    @Override
    protected void handleUiDropAction(DropTargetEvent event, final TransferObject scoutTransferObject) {
      Object dropTarget = event.item != null ? event.item.getData() : null;
      final ITableRow row = dropTarget instanceof ITableRow ? (ITableRow) dropTarget : null;
      Runnable job = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().fireRowDropActionFromUI(row, scoutTransferObject);
        }
      };
      getUiEnvironment().invokeScoutLater(job, 200);
    }
  }// end class P_DndSupport

  private class P_KeyBoardNavigationSupport extends AbstractTableKeyboardNavigationSupport {
    /**
     * @param table
     * @param delay
     */
    public P_KeyBoardNavigationSupport(TableEx table) {
      super(table);
    }

    @Override
    public void handleKeyboardNavigation(TableItem tableItem) {
      handleKeyboardNavigationFromUi(tableItem);
    }
  } // P_KeyBoardNavigationSupport

}
