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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.RowIndexComparator;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.basic.RwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.basic.table.celleditor.RwtScoutTableCellEditor;
import org.eclipse.scout.rt.ui.rap.ext.MenuAdapterEx;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.ext.table.TableViewerEx;
import org.eclipse.scout.rt.ui.rap.ext.table.util.TableRolloverSupport;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.form.fields.AbstractRwtScoutDndSupport;
import org.eclipse.scout.rt.ui.rap.keystroke.IRwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.scout.rt.ui.rap.util.RwtUtility;
import org.eclipse.scout.rt.ui.rap.util.UiRedrawHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.MarkupValidator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * knownIssues - multi column sorting is not supported, unable to get any key
 * mask in the selection event.
 * <p>
 * - multi line support in headers is not supported by rwt.
 * <p>
 * - multi line support in row texts is not supported so far. Might probably be done by customized table rows.
 * 
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public class RwtScoutTable extends RwtScoutComposite<ITable> implements IRwtScoutTableForPatch {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutTable.class);

  private P_ScoutTableListener m_scoutTableListener;
  private UiRedrawHandler m_redrawHandler;

  private Listener m_autoResizeColumnListener;
  private Listener m_columnListener = new P_TableColumnListener();
  private SelectionListener m_columnSortListener = new P_ColumnSortListener();
  private TableColumnManager m_uiColumnManager = new TableColumnManager();
  private RwtScoutTableCellEditor m_uiCellEditorComposite;
  private int[] m_uiColumnOrder;
  private TableViewer m_uiViewer;
  private IRwtKeyStroke[] m_uiKeyStrokes;
  private ClientSyncJob m_storeColumnWidthsJob;

  private RwtScoutColumnModel m_columnModel = null;

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
      table.setData(RWT.CUSTOM_VARIANT, m_variant);
    }
    table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
    table.setData(MarkupValidator.MARKUP_VALIDATION_DISABLED, Boolean.TRUE);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    table.setTouchEnabled(RwtUtility.getBrowserInfo().isTablet() || RwtUtility.getBrowserInfo().isMobile());
    new TableRolloverSupport(table);
    TableViewer viewer = new TableViewerEx(table);
    ColumnViewerToolTipSupport.enableFor(viewer);
    viewer.setUseHashlookup(true);
    setUiTableViewer(viewer);
    setUiField(table);
    //cell editing support
    m_uiCellEditorComposite = new RwtScoutTableCellEditor(this);

    //columns
    initializeUiColumns();

    RwtScoutTableModel tableModel = createUiTableModel();
    viewer.setContentProvider(tableModel);
    viewer.setInput(tableModel);

    // ui listeners
    viewer.addSelectionChangedListener(new P_RwtSelectionChangedListener());
    P_RwtTableListener rwtTableListener = new P_RwtTableListener();
    table.addListener(SWT.MouseDown, rwtTableListener);
    table.addListener(SWT.MouseUp, rwtTableListener);
    table.addListener(SWT.MouseDoubleClick, rwtTableListener);
    table.addListener(SWT.MenuDetect, rwtTableListener);
    table.addListener(SWT.Resize, rwtTableListener);
    table.addSelectionListener(new P_RwtHyperlinkSelectionListener());

    getUiEnvironment().addKeyStroke(table, new RwtKeyStroke((int) ' ') {

      @Override
      public void handleUiAction(Event e) {
        handleUiToggleAcction(e);
      }
    }, false);
  }

  @Override
  public boolean isUiDisposed() {
    return getUiField() == null || getUiField().isDisposed();
  }

  protected RwtScoutTableModel createUiTableModel() {
    return new RwtScoutTableModel(getScoutObject(), this, m_uiColumnManager);
  }

  protected RwtScoutColumnModel getUiColumnModel() {
    if (m_columnModel == null) {
      m_columnModel = new RwtScoutColumnModel(getScoutObject(), this, m_uiColumnManager);
    }
    return m_columnModel;
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
      List<IColumn<?>> scoutColumnsOrdered;
      if (getScoutObject() != null) {
        scoutColumnsOrdered = getScoutObject().getColumnSet().getVisibleColumns();
        sortEnabled = getScoutObject().isSortEnabled();
      }
      else {
        scoutColumnsOrdered = CollectionUtility.emptyArrayList();
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
        boolean isHtml = HtmlTextUtility.isTextWithHtmlMarkup(cellText);
        if (!isHtml && cellText.indexOf("\n") >= 0) {
          multilineHeaders = true;
        }
        if (isHtml) {
          multilineHeaders = true;
          cellText = getUiEnvironment().adaptHtmlCell(RwtScoutTable.this, cellText);
        }
        int style = RwtUtility.getHorizontalAlignment(cell.getHorizontalAlignment());
        TableColumn rwtCol = new TableColumn(getUiField(), style);
        TableViewerColumn rwtViewerCol = new TableViewerColumn(getUiTableViewer(), rwtCol);
        rwtViewerCol.setLabelProvider(getUiColumnModel());
        rwtCol.setData(KEY_SCOUT_COLUMN, scoutColumn);
        if (scoutColumn instanceof IStringColumn) {
          rwtCol.setData(WRAPPED_COLUMN, ((IStringColumn) scoutColumn).isTextWrap());
        }
        rwtCol.setMoveable(true);
        rwtCol.setToolTipText(cell.getTooltipText());
        updateHeaderText(rwtCol, scoutColumn);
        rwtCol.setWidth(scoutColumn.getWidth());
        if (scoutColumn.isFixedWidth()) {
          rwtCol.setResizable(false);
        }
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
    if (getScoutObject() == null) {
      return;
    }
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
    attachDndSupport();
    handleEventsFromRecentHistory();
  }

  private void handleEventsFromRecentHistory() {
    final IEventHistory<TableEvent> h = getScoutObject().getEventHistory();
    if (h == null) {
      return;
    }

    getUiEnvironment().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        for (TableEvent e : h.getRecentEvents()) {
          handleScoutTableEventInUi(e);
        }
      }
    });
  }

  protected void attachDndSupport() {
    if (UiDecorationExtensionPoint.getLookAndFeel().isDndSupportEnabled()) {
      new P_DndSupport(getScoutObject(), getScoutObject(), getUiField());
    }
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    removeAutoResizeColumnListener();
    if (getScoutObject() == null) {
      return;
    }
    if (m_scoutTableListener != null) {
      getScoutObject().removeTableListener(m_scoutTableListener);
      m_scoutTableListener = null;
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
    return CollectionUtility.firstElement(getUiSelectedRows());
  }

  @Override
  public List<ITableRow> getUiSelectedRows() {
    StructuredSelection uiSelection = (StructuredSelection) getUiTableViewer().getSelection();
    TreeSet<ITableRow> sortedRows = new TreeSet<ITableRow>(new RowIndexComparator());
    if (uiSelection != null && !uiSelection.isEmpty()) {
      Iterator uiSelectionIt = uiSelection.iterator();
      while (uiSelectionIt.hasNext()) {
        sortedRows.add((ITableRow) uiSelectionIt.next());
      }
    }
    return new ArrayList<ITableRow>(sortedRows);
  }

  protected void setKeyStrokeFormScout() {
    // remove old
    if (m_uiKeyStrokes != null) {
      for (IRwtKeyStroke rwtKeyStroke : m_uiKeyStrokes) {
        getUiEnvironment().removeKeyStroke(getUiField(), rwtKeyStroke);
      }
    }
    // add new
    List<IRwtKeyStroke> newRwtKeyStrokes = new ArrayList<IRwtKeyStroke>();
    for (IKeyStroke scoutKeyStroke : getScoutObject().getKeyStrokes()) {
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
    if (h >= 0) {
      getUiField().setData(RWT.CUSTOM_ITEM_HEIGHT, h);
    }
    else {
      int defaultTableRowHeight = UiDecorationExtensionPoint.getLookAndFeel().getTableRowHeight();
      if (defaultTableRowHeight >= 0) {
        getUiField().setData(RWT.CUSTOM_ITEM_HEIGHT, defaultTableRowHeight);
      }
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
        //If the parent has already been resized no event will be fired anymore.
        //So it is necessary to request an auto resizing of the columns manually. (Bugzilla 355855)
        scheduleHandleAutoResizeColumn();
      }
    }
    else {
      removeAutoResizeColumnListener();
    }
  }

  private void scheduleHandleAutoResizeColumn() {
    getUiField().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        handleAutoSizeColumns();
      }
    });
  }

  private void removeAutoResizeColumnListener() {
    if (m_autoResizeColumnListener == null) {
      return;
    }

    Composite parent = getUiField().getParent();
    parent.removeListener(SWT.Resize, m_autoResizeColumnListener);
    m_autoResizeColumnListener = null;
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
    else if (propName.equals(ITable.PROP_SCROLL_TO_SELECTION)) {
      updateScrollToSelectionFromScout();
    }
  }

  /**
   * scout table observer
   */
  protected boolean isHandleScoutTableEvent(List<? extends TableEvent> events) {
    for (TableEvent element : events) {
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
        case TableEvent.TYPE_ROWS_SELECTED:
        case TableEvent.TYPE_SCROLL_TO_SELECTION: {
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
      case TableEvent.TYPE_SCROLL_TO_SELECTION: {
        scrollToSelection();
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
      getUiColumnModel().consumeColumnModelEvent(uiTableEvent);
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
    getUiField().setEnabled(true);
    // <Workaround>
    // Because RAP seems to ignore the default ":disabled" state,
    // we apply a custom variant to all header cells. Otherwise
    // the "normal" style would be used for disabled cells.
    for (TableColumn column : getUiField().getColumns()) {
      column.setData(RWT.CUSTOM_VARIANT, (enabledFromScout ? null : VARIANT_TABLE_COLUMN_DISABLED));
    }
    // </Workaround>
  }

  protected void setSelectionFromScout(List<ITableRow> selectedRows) {
    if (getUiField().isDisposed()) {
      return;
    }
    List<ITableRow> uiSelection = getUiSelectedRows();
    if (CompareUtility.equals(uiSelection, selectedRows)) {
      // no change
      return;
    }
    else {
      if (selectedRows == null) {
        selectedRows = Collections.emptyList();
      }
      getUiTableViewer().setSelection(new StructuredSelection(selectedRows), true);
      updateScrollToSelectionFromScout();
    }
  }

  private void updateScrollToSelectionFromScout() {
    if (getScoutObject().isScrollToSelection()) {
      scrollToSelection();
    }
  }

  protected void scrollToSelection() {
    if (getUiField() != null && !getUiField().isDisposed()) {
      getUiField().showSelection();
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
        updateHeaderText(col);
      }
    }

    if (minUiSortColumn != null && minScoutSortColumn != null) {
      getUiField().setSortColumn(minUiSortColumn);
      getUiField().setSortDirection(minScoutSortColumn.isSortAscending() ? SWT.UP : SWT.DOWN);
    }
    else {
      getUiField().setSortColumn(null);
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

  protected void handleUiHyperlinkAction(final ITableRow row, String urlText) {
    if (getScoutObject() != null) {
      final URL url;
      try {
        url = new URL(urlText);
      }
      catch (MalformedURLException e) {
        LOG.error("Hyperlink could not be activated", e);
        return;
      }

      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          ITable table = getScoutObject();
          table.getUIFacade().fireHyperlinkActionFromUI(row, table.getContextColumn(), url);
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
    if (getUiField() == null || getUiField().isDisposed()) {
      return;
    }

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
    int actualWidth = 0;
    HashMap<TableColumn, Integer> columnWeights = new HashMap<TableColumn, Integer>();
    for (TableColumn col : getUiField().getColumns()) {
      if (col == null || col.isDisposed()) {
        continue;
      }
      actualWidth += col.getWidth();
      Object data = col.getData(RwtScoutTable.KEY_SCOUT_COLUMN);
      if (data instanceof IColumn<?> && !((IColumn<?>) data).isFixedWidth()) {
        int width = ((IColumn<?>) data).getInitialWidth();
        columnWeights.put(col, width);
        totalWeight += width;
      }
      else {
        totalWidth -= col.getWidth();
      }
    }

    //If the columns already have the correct size there is no need to recalculate it
    if (actualWidth == totalWidth) {
      return;
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
        entry.getKey().setWidth(totalWidth);
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
    final List<IColumn<?>> newOrder = m_uiColumnManager.getOrderedColumns(truncatedColOrder);
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
    getUiField().setSelection(item);
    Event selectionEvent = new Event();
    selectionEvent.type = SWT.DefaultSelection;
    selectionEvent.widget = getUiField();
    for (Listener l : getUiField().getListeners(SWT.DefaultSelection)) {
      l.handleEvent(selectionEvent);
    }
  }

  protected void handleUiToggleAcction(Event e) {
    if (e.doit && getScoutObject().isCheckable()) {
      if (e.stateMask == 0) {
        switch (e.keyCode) {
          case ' ':
            List<ITableRow> selectedRows = RwtUtility.getItemsOfSelection(ITableRow.class, (StructuredSelection) getUiTableViewer().getSelection());
            if (CollectionUtility.hasElements(selectedRows)) {
              handleUiRowClick(CollectionUtility.firstElement(selectedRows));
            }
            e.doit = false;
            break;
        }
      }
    }
  }

  private class P_ScoutTableListener implements TableListener {
    @Override
    public void tableChanged(final TableEvent e) {
      if (isHandleScoutTableEvent(CollectionUtility.arrayList(e))) {
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
    public void tableChangedBatch(final List<? extends TableEvent> events) {
      if (isHandleScoutTableEvent(events)) {
        final List<TableEvent> filteredList = new ArrayList<TableEvent>();
        for (TableEvent event : events) {
          if (!isIgnoredScoutEvent(TableEvent.class, "" + event.getType())) {
            filteredList.add(event);
          }
        }
        if (CollectionUtility.hasElements(filteredList)) {
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
    }
  }// end P_ScoutTableListener

  private Menu createMenu(boolean headerMenu) {
    if (getUiField().getMenu() != null) {
      getUiField().getMenu().dispose();
      getUiField().setMenu(null);
    }
    Menu contextMenu = new Menu(getUiField().getShell(), SWT.POP_UP);
    contextMenu.addMenuListener(new P_ContextMenuListener(headerMenu));
    getUiField().setMenu(contextMenu);

    return contextMenu;
  }

  private void createAndShowMenu(Point location) {
    Point pt = getUiField().getDisplay().map(null, getUiField(), location);
    Rectangle clientArea = getUiField().getClientArea();
    boolean header = clientArea.y <= pt.y && pt.y < clientArea.y + getUiField().getHeaderHeight();

    Menu menu = createMenu(header);
    menu.setLocation(location);
    menu.setVisible(true);
  }

  private class P_RwtTableListener extends AbstractAvoidWrongDoubleClickListener {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleEventInternal(Event event) {
      Point eventPosition = new Point(event.x, event.y);
      TableViewer uiTableViewer = getUiTableViewer();
      switch (event.type) {
        case SWT.MouseDown: {
          //Close cell editor on empty space click
          if (uiTableViewer.getTable().getItem(new Point(event.y, event.y)) == null && uiTableViewer instanceof TableViewerEx) {
            ((TableViewerEx) uiTableViewer).applyEditorValue();
          }

          setContextColumnFromUi(RwtUtility.getRwtColumnAt(uiTableViewer.getTable(), eventPosition));
          if (getUiField().getItem(eventPosition) == null) {
            uiTableViewer.setSelection(null);
            setSelectionFromUi(new StructuredSelection());
          }
          break;
        }
        case SWT.MouseUp: {
          StructuredSelection selection = (StructuredSelection) uiTableViewer.getSelection();
          if (selection != null && selection.size() == 1) {
            handleUiRowClick((ITableRow) selection.getFirstElement());
          }
          break;
        }
        case SWT.MouseDoubleClick: {
          StructuredSelection selection = (StructuredSelection) uiTableViewer.getSelection();
          if (selection != null && selection.size() == 1) {
            handleUiRowAction((ITableRow) selection.getFirstElement());
          }
          break;
        }
        case SWT.Resize: {
          //lazy column auto-fit
          if (getUiField() != null && !getUiField().isDisposed()) {
            if (getScoutObject().isAutoResizeColumns()) {
              scheduleHandleAutoResizeColumn();
            }
            updateScrollToSelectionFromScout();
          }
          break;
        }
        case SWT.MenuDetect: {
          createAndShowMenu(eventPosition);
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
        if (getScoutObject().isAutoResizeColumns()) {
          scheduleHandleAutoResizeColumn();
        }
        updateScrollToSelectionFromScout();
      }
    }
  } // end class P_SwtResizeListener

  public class P_RwtSelectionChangedListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      setSelectionFromUi((StructuredSelection) event.getSelection());
    }
  }

  private class P_RwtHyperlinkSelectionListener extends SelectionAdapter {
    private static final long serialVersionUID = 1L;

    @Override
    public void widgetSelected(SelectionEvent event) {
      if (event.detail == RWT.HYPERLINK) {
        TableItem tableItem = (TableItem) event.item;
        ITableRow row = (ITableRow) tableItem.getData();
        handleUiHyperlinkAction(row, event.text);
      }
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
    private boolean m_header;

    private static final long serialVersionUID = 1L;

    public P_ContextMenuListener(boolean header) {
      super(RwtScoutTable.this.getUiTableViewer().getTable(), RwtScoutTable.this.getUiTableViewer().getTable());
      m_header = header;
    }

    @Override
    public void menuShown(MenuEvent e) {
      super.menuShown(e);

      List<IMenu> menus = null;
      if (m_header) {
        menus = collectHeaderMenus();
      }
      else {
        final boolean emptySelection = getUiTableViewer().getSelection().isEmpty();
        menus = RwtMenuUtility.collectMenus(getScoutObject(), emptySelection, !emptySelection, getUiEnvironment());
      }
      if (menus != null) {
        Menu menu = ((Menu) e.getSource());
        RwtMenuUtility.fillContextMenu(menus, getUiEnvironment(), menu);
      }
    }

    private List<IMenu> collectHeaderMenus() {
      final AtomicReference<List<IMenu>> scoutMenusRef = new AtomicReference<List<IMenu>>();
      Runnable t = new Runnable() {
        @Override
        public void run() {
          List<IMenu> scoutMenus = getScoutObject().getUIFacade().fireHeaderPopupFromUI();
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
        return scoutMenusRef.get();
      }

      return CollectionUtility.emptyArrayList();
    }
  }

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
