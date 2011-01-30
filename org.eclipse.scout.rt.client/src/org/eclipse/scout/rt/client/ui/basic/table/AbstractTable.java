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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.DefaultTableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ILongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ITimeColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.AddCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.RemoveCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.internal.InternalTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.menus.CopyWidthsOfColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

/**
 * Columns are defined as inner classes<br>
 * for every inner column class there is a generated getXYColumn method directly
 * on the table
 */
public abstract class AbstractTable extends AbstractPropertyObserver implements ITable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTable.class);

  private boolean m_initialized;
  private final OptimisticLock m_initLock;
  protected ColumnSet m_columnSet;
  /**
   * synchronized list
   */
  private final List<ITableRow> m_rows;
  private final Object m_cachedRowsLock;
  private ITableRow[] m_cachedRows;
  private final HashMap<CompositeObject, ITableRow> m_deletedRows;
  private TreeSet<ITableRow/* ordered by rowIndex */> m_selectedRows = new TreeSet<ITableRow>(new RowIndexComparator());
  private IMenu[] m_menus;
  private ITableUIFacade m_uiFacade;
  private final ArrayList<ITableRowFilter> m_rowFilters;
  private String m_userPreferenceContext;
  // batch mutation
  private boolean m_autoDiscardOnDelete;
  private boolean m_sortEnabled;
  private boolean m_sortValid;
  private boolean m_initialMultiLineText;
  private int m_tableChanging;
  private ArrayList<TableEvent> m_tableEventBuffer = new ArrayList<TableEvent>();
  private final HashSet<P_CellLookup> m_cellLookupBuffer = new HashSet<P_CellLookup>();
  private HashSet<ITableRow> m_rowDecorationBuffer = new HashSet<ITableRow>();
  // key stroke buffer for select-as-you-type
  private final KeyStrokeBuffer m_keyStrokeBuffer;
  private final EventListenerList m_listenerList = new EventListenerList();
  //cell editing
  private P_CellEditorContext m_editContext;
  //auto filter
  private final Object m_cachedFilteredRowsLock;
  private ITableRow[] m_cachedFilteredRows;
  private ITableColumnFilterManager m_columnFilterManager;
  private ITableCustomizer m_tableCustomizer;

  public AbstractTable() {
    this(true);
  }

  public AbstractTable(boolean callInitializer) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTable(this);
    }
    m_cachedRowsLock = new Object();
    m_cachedFilteredRowsLock = new Object();
    m_rows = Collections.synchronizedList(new ArrayList<ITableRow>(1));
    m_deletedRows = new HashMap<CompositeObject, ITableRow>();
    m_keyStrokeBuffer = new KeyStrokeBuffer(500L);
    m_rowFilters = new ArrayList<ITableRowFilter>(1);
    m_initLock = new OptimisticLock();
    //add single observer listener
    addTableListener(new P_TableListener());
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    initConfig();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredTitle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredDefaultIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredMultiSelect() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(32)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredMultiCheck() {
    return true;
  }

  /**
   * The menu that is used on the ENTER / action key on a table row
   */
  @ConfigProperty(ConfigProperty.MENU_CLASS)
  @Order(35)
  @ConfigPropertyValue("null")
  protected Class<? extends IMenu> getConfiguredDefaultMenu() {
    return null;
  }

  /**
   * @return true: deleted nodes are automatically erased<br>
   *         false: deleted nodes are cached for later processing (service
   *         deletion)
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredSortEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  /**
   * @return true: all columns are resized so that the table never needs
   *         horizontal scrolling
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAutoResizeColumns() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredCheckable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  @ConfigPropertyValue("0")
  protected int getConfiguredDropType() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  @ConfigPropertyValue("0")
  protected int getConfiguredDragType() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredKeyboardNavigation() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredScrollToSelection() {
    return false;
  }

  /**
   * @return a transferable object representing the given rows
   */
  @ConfigOperation
  @Order(10)
  protected TransferObject execDrag(ITableRow[] rows) throws ProcessingException {
    return null;
  }

  /**
   * process drop action, row may be null (for empty space drop)
   */
  @ConfigOperation
  @Order(20)
  protected void execDrop(ITableRow row, TransferObject t) throws ProcessingException {
  }

  @ConfigOperation
  @Order(40)
  protected void execDecorateCell(Cell view, ITableRow row, IColumn col) throws ProcessingException {
  }

  /**
   * Table content changed, rows were added, removed or changed
   */
  @ConfigOperation
  @Order(45)
  protected void execContentChanged() throws ProcessingException {
  }

  @ConfigOperation
  @Order(50)
  protected void execInitTable() throws ProcessingException {
  }

  @ConfigOperation
  @Order(55)
  protected void execDisposeTable() throws ProcessingException {
  }

  @ConfigOperation
  @Order(80)
  protected void execDecorateRow(ITableRow row) throws ProcessingException {
  }

  /**
   * row is never null
   */
  @ConfigOperation
  @Order(60)
  protected void execRowClick(ITableRow row) throws ProcessingException {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_CLICK, new ITableRow[]{row});
    fireTableEventInternal(e);
  }

  /**
   * row is never null
   */
  @ConfigOperation
  @Order(62)
  protected void execRowAction(ITableRow row) throws ProcessingException {
    Class<? extends IMenu> defaultMenuType = getConfiguredDefaultMenu();
    if (defaultMenuType != null) {
      try {
        runMenu(defaultMenuType);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
    else {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_ACTION, new ITableRow[]{row});
      fireTableEventInternal(e);
    }
  }

  @ConfigOperation
  @Order(70)
  protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
  }

  /**
   * The hyperlink's table row is the selected row and the column is the context column {@link #getContextColumn()}
   * 
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(80)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IMenu.class);
  }

  private Class<? extends IColumn>[] getConfiguredColumns() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IColumn.class);
  }

  private Class<? extends IKeyStroke>[] getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
  }

  protected void initConfig() {
    m_uiFacade = createUIFacade();
    setTitle(getConfiguredTitle());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setSortEnabled(getConfiguredSortEnabled());
    setDefaultIconId(getConfiguredDefaultIconId());
    setHeaderVisible(getConfiguredHeaderVisible());
    setAutoResizeColumns(getConfiguredAutoResizeColumns());
    setCheckable(getConfiguredCheckable());
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setInitialMultilineText(getConfiguredMultilineText());
    setMultilineText(getConfiguredMultilineText());
    setKeyboardNavigation(getConfiguredKeyboardNavigation());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setScrollToSelection(getConfiguredScrollToSelection());
    if (getTableCustomizer() == null) {
      setTableCustomizer(createTableCustomizer());
    }
    // columns
    Class<? extends IColumn>[] ca = getConfiguredColumns();
    ArrayList<IColumn<?>> colList = new ArrayList<IColumn<?>>();
    for (int i = 0; i < ca.length; i++) {
      try {
        IColumn column = ConfigurationUtility.newInnerInstance(this, ca[i]);
        colList.add(column);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    try {
      injectColumnsInternal(colList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute columns.", e);
    }

    ArrayList<IColumn> completeList = new ArrayList<IColumn>();
    completeList.addAll(colList);
    m_columnSet = new ColumnSet(this, completeList);
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] ma = getConfiguredMenus();
    for (int i = 0; i < ma.length; i++) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, ma[i]);
        menuList.add(menu);
      }
      catch (Throwable t) {
        LOG.error("create " + ma[i].getName(), t);
      }
    }

    try {
      injectMenusInternal(menuList);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute menus.", e);
    }
    m_menus = menuList.toArray(new IMenu[0]);
    // key strokes
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>();
    Class<? extends IKeyStroke>[] ksArray = getConfiguredKeyStrokes();
    for (int i = 0; i < ksArray.length; i++) {
      try {
        IKeyStroke ks = ConfigurationUtility.newInnerInstance(this, ksArray[i]);
        ksList.add(ks);
      }
      catch (Throwable t) {
        LOG.error("create " + ksArray[i].getName(), t);
      }
    }
    //add ENTER key stroke when default menu is used or execRowAction has an override
    Class<? extends IMenu> defaultMenuType = getConfiguredDefaultMenu();
    if (defaultMenuType != null || ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowAction", new Class[]{ITableRow.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() throws ProcessingException {
          fireRowAction(getSelectedRow());
        }
      });
    }
    setKeyStrokes(ksList.toArray(new IKeyStroke[ksList.size()]));
    // add Convenience observer for drag & drop callbacks
    addTableListener(new TableAdapter() {
      @Override
      public void tableChanged(TableEvent e) {
        //dnd
        switch (e.getType()) {
          case TableEvent.TYPE_ROWS_DRAG_REQUEST: {
            if (e.getDragObject() == null) {
              try {
                e.setDragObject(execDrag(e.getRows()));
              }
              catch (ProcessingException ex) {
                SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROW_DROP_ACTION: {
            if (e.getDropObject() != null) {
              try {
                execDrop(e.getFirstRow(), e.getDropObject());
              }
              catch (ProcessingException ex) {
                SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ALL_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_INSERTED:
          case TableEvent.TYPE_ROWS_UPDATED: {
            try {
              execContentChanged();
            }
            catch (ProcessingException ex) {
              SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
            }
            break;
          }
        }
      }
    });
  }

  /**
   * Override this internal method only in order to make use of dynamic fields<br>
   * Used to manage column list and add/remove columns
   * 
   * @param columnList
   *          live and mutable list of configured columns, not yet initialized
   */
  protected void injectColumnsInternal(List<IColumn<?>> columnList) {
    ITableCustomizer c = getTableCustomizer();
    if (c != null) {
      c.injectCustomColumns(columnList);
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus
   * 
   * @param menuList
   *          live and mutable list of configured menus
   */
  protected void injectMenusInternal(List<IMenu> menuList) {
  }

  protected ITableUIFacade createUIFacade() {
    return new P_TableUIFacade();
  }

  /*
   * Runtime
   */

  public String getUserPreferenceContext() {
    return m_userPreferenceContext;
  }

  public void setUserPreferenceContext(String context) {
    m_userPreferenceContext = context;
    if (isTableInitialized()) {
      //re-initialize
      try {
        initTable();
      }
      catch (ProcessingException e) {
        LOG.error("Failed re-initializing table " + getClass().getName(), e);
      }
    }
  }

  /**
   * This is the init of the runtime model after the table and columns are built
   * and configured
   */
  public final void initTable() throws ProcessingException {
    try {
      if (m_initLock.acquire()) {
        try {
          setTableChanging(true);
          //
          initTableInternal();
          execInitTable();
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_initialized = true;
      m_initLock.release();
    }
  }

  protected void initTableInternal() throws ProcessingException {
    for (IColumn c : getColumnSet().getColumns()) {
      c.initColumn();
    }
    getColumnSet().initialize();
    if (getColumnFilterManager() == null) {
      setColumnFilterManager(createColumnFilterManager());
    }
  }

  public final void disposeTable() {
    try {
      disposeTableInternal();
      execDisposeTable();
    }
    catch (Throwable t) {
      LOG.warn(getClass().getName(), t);
    }
  }

  protected void disposeTableInternal() throws ProcessingException {
    for (IColumn c : getColumnSet().getColumns()) {
      c.disposeColumn();
    }
  }

  public void doHyperlinkAction(ITableRow row, IColumn col, URL url) throws ProcessingException {
    if (row != null && col != null) {
      selectRow(row);
      setContextColumn(col);
      execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals("local"));
    }
  }

  public ITableRowFilter[] getRowFilters() {
    return m_rowFilters.toArray(new ITableRowFilter[m_rowFilters.size()]);
  }

  public void addRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      //avoid duplicate add
      boolean exists = false;
      for (ITableRowFilter existingFilter : m_rowFilters) {
        if (existingFilter == filter) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        m_rowFilters.add(filter);
      }
      applyRowFilters();
    }
  }

  public void removeRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      m_rowFilters.remove(filter);
      applyRowFilters();
    }
  }

  public void applyRowFilters() {
    applyRowFiltersInternal();
    fireRowFilterChanged();
  }

  private void applyRowFiltersInternal() {
    for (ITableRow row : m_rows) {
      applyRowFiltersInternal((InternalTableRow) row);
    }
  }

  private void applyRowFiltersInternal(InternalTableRow row) {
    row.setFilterAcceptedInternal(true);
    if (m_rowFilters.size() > 0) {
      for (ITableRowFilter filter : m_rowFilters) {
        if (!filter.accept(row)) {
          row.setFilterAcceptedInternal(false);
          /*
           * ticket 95770
           */
          if (isSelectedRow(row)) {
            deselectRow(row);
          }
          break;
        }
      }
    }
  }

  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  public boolean isAutoResizeColumns() {
    return propertySupport.getPropertyBool(PROP_AUTO_RESIZE_COLUMNS);
  }

  public void setAutoResizeColumns(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_RESIZE_COLUMNS, b);
  }

  public ColumnSet getColumnSet() {
    return m_columnSet;
  }

  public int getColumnCount() {
    return getColumnSet().getColumnCount();
  }

  public IColumn[] getColumns() {
    return getColumnSet().getColumns();
  }

  public String[] getColumnNames() {
    String[] a = new String[getColumnCount()];
    for (int i = 0; i < a.length; i++) {
      a[i] = getColumnSet().getColumn(i).getHeaderCell().getText();
    }
    return a;
  }

  public int getVisibleColumnCount() {
    return getColumnSet().getVisibleColumnCount();
  }

  public IHeaderCell getVisibleHeaderCell(int visibleColumnIndex) {
    return getHeaderCell(getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  public IHeaderCell getHeaderCell(int columnIndex) {
    return getHeaderCell(getColumnSet().getColumn(columnIndex));
  }

  public IHeaderCell getHeaderCell(IColumn col) {
    return col.getHeaderCell();
  }

  public ICell getVisibleCell(int rowIndex, int visibleColumnIndex) {
    return getVisibleCell(getRow(rowIndex), visibleColumnIndex);
  }

  public ICell getVisibleCell(ITableRow row, int visibleColumnIndex) {
    return getCell(row, getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  public ICell getCell(int rowIndex, int columnIndex) {
    return getCell(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  public ICell getSummaryCell(int rowIndex) {
    return getSummaryCell(getRow(rowIndex));
  }

  public ICell getSummaryCell(ITableRow row) {
    IColumn[] a = getColumnSet().getSummaryColumns();
    if (a.length == 0) {
      IColumn col = getColumnSet().getFirstVisibleColumn();
      if (col != null) {
        a = new IColumn[]{col};
      }
    }
    if (a.length == 0) {
      return new Cell();
    }
    else if (a.length == 1) {
      Cell cell = new Cell(getCell(row, a[0]));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      return cell;
    }
    else {
      Cell cell = new Cell(getCell(row, a[0]));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      StringBuilder b = new StringBuilder();
      for (IColumn c : a) {
        if (b.length() > 0) {
          b.append(" ");
        }
        b.append(getCell(row, c).getText());
      }
      cell.setText(b.toString());
      return cell;
    }
  }

  public ICell getCell(ITableRow row, IColumn col) {
    row = resolveRow(row);
    if (row == null || col == null) return null;
    return row.getCell(col.getColumnIndex());
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return isCellEditable(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  public boolean isCellEditable(ITableRow row, int visibleColumnIndex) {
    return isCellEditable(row, getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  public boolean isCellEditable(ITableRow row, IColumn column) {
    return row != null & column != null && column.isCellEditable(row);
  }

  public boolean isCheckable() {
    return propertySupport.getPropertyBool(PROP_CHECKABLE);
  }

  public void setCheckable(boolean b) {
    propertySupport.setPropertyBool(PROP_CHECKABLE, b);
  }

  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  public void setMultilineText(boolean on) {
    propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, on);
  }

  public boolean isInitialMultilineText() {
    return m_initialMultiLineText;
  }

  public void setInitialMultilineText(boolean on) {
    m_initialMultiLineText = on;
  }

  public boolean hasKeyboardNavigation() {
    return propertySupport.getPropertyBool(PROP_KEYBOARD_NAVIGATION);
  }

  public void setKeyboardNavigation(boolean on) {
    propertySupport.setPropertyBool(PROP_KEYBOARD_NAVIGATION, on);
  }

  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  public void setMultiSelect(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, b);
  }

  public boolean isMultiCheck() {
    return propertySupport.getPropertyBool(PROP_MULTI_CHECK);
  }

  public void setMultiCheck(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_CHECK, b);
  }

  public boolean isAutoDiscardOnDelete() {
    return m_autoDiscardOnDelete;
  }

  public void setAutoDiscardOnDelete(boolean on) {
    m_autoDiscardOnDelete = on;
  }

  public boolean isTableInitialized() {
    return m_initialized;
  }

  public boolean isTableChanging() {
    return m_tableChanging > 0;
  }

  public void setTableChanging(boolean b) {
    // use a stack counter because setTableChanging might be called in nested
    // loops
    if (b) {
      m_tableChanging++;
      if (m_tableChanging == 1) {
        // 0 --> 1
        propertySupport.setPropertiesChanging(true);
      }
    }
    else {
      if (m_tableChanging > 0) {
        if (m_tableChanging == 1) {
          //will be going to zero, but process decorations here, so events are added to the event buffer
          processDecorationBuffer();
          if (!m_sortValid) {
            sort();
          }
        }
        m_tableChanging--;
        if (m_tableChanging == 0) {
          processEventBuffer();
          propertySupport.setPropertiesChanging(false);
        }
      }
    }
  }

  public IKeyStroke[] getKeyStrokes() {
    IKeyStroke[] keyStrokes = (IKeyStroke[]) propertySupport.getProperty(PROP_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new IKeyStroke[0];
    }
    return keyStrokes;
  }

  public void setKeyStrokes(IKeyStroke[] keyStrokes) {
    propertySupport.setProperty(PROP_KEY_STROKES, keyStrokes);
  }

  public void requestFocus() {
    fireRequestFocus();
  }

  public void extractTableData(AbstractTableFieldData target) throws ProcessingException {
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      int newRowIndex = target.addRow();
      for (int j = 0, nj = row.getCellCount(); j < nj; j++) {
        target.setValueAt(newRowIndex, j, row.getCellValue(j));
      }
      target.setRowState(newRowIndex, row.getStatus());
    }
    ITableRow[] deletedRows = getDeletedRows();
    for (int i = 0, ni = deletedRows.length; i < ni; i++) {
      ITableRow row = deletedRows[i];
      int newRowIndex = target.addRow();
      for (int j = 0, nj = row.getCellCount(); j < nj; j++) {
        target.setValueAt(newRowIndex, j, row.getCellValue(j));
      }
      target.setRowState(newRowIndex, AbstractTableFieldData.STATUS_DELETED);
    }
    target.setValueSet(true);
  }

  @SuppressWarnings("unchecked")
  public void updateTable(AbstractTableFieldData source) throws ProcessingException {
    if (source.isValueSet()) {
      clearDeletedRows();
      int deleteCount = 0;
      ArrayList<ITableRow> newRows = new ArrayList<ITableRow>();
      for (int i = 0, ni = source.getRowCount(); i < ni; i++) {
        int importState = source.getRowState(i);
        if (importState != AbstractTableFieldData.STATUS_DELETED) {
          ITableRow newTableRow = new TableRow(getColumnSet());
          for (int j = 0, nj = source.getColumnCount(); j < nj; j++) {
            if (j < getColumnCount()) {
              getColumnSet().getColumn(j).setValue(newTableRow, source.getValueAt(i, j));
            }
            else {
              newTableRow.setCellValue(j, source.getValueAt(i, j));
            }
          }
          newTableRow.setStatus(importState);
          newRows.add(newTableRow);
        }
        else {
          deleteCount++;
        }
      }
      replaceRows(newRows.toArray(new ITableRow[newRows.size()]));
      if (deleteCount > 0) {
        try {
          setTableChanging(true);
          //
          for (int i = 0, ni = source.getRowCount(); i < ni; i++) {
            int importState = source.getRowState(i);
            if (importState == AbstractTableFieldData.STATUS_DELETED) {
              ITableRow newTableRow = new TableRow(getColumnSet());
              for (int j = 0, nj = source.getColumnCount(); j < nj; j++) {
                if (j < getColumnCount()) {
                  getColumnSet().getColumn(j).setValue(newTableRow, source.getValueAt(i, j));
                }
                else {
                  newTableRow.setCellValue(j, source.getValueAt(i, j));
                }
              }
              newTableRow.setStatus(ITableRow.STATUS_NON_CHANGED);
              ITableRow addedRow = addRow(newTableRow);
              deleteRow(addedRow);
            }
          }
        }
        finally {
          setTableChanging(false);
        }
      }
    }
  }

  public IMenu[] getMenus() {
    return m_menus;
  }

  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  public boolean runMenu(Class<? extends IMenu> menuType) throws ProcessingException {
    for (IMenu m : getMenus()) {
      if (m.getClass() == menuType) {
        if ((!m.isInheritAccessibility()) || isEnabled()) {
          m.prepareAction();
          if (m.isVisible() && m.isEnabled()) {
            m.doAction();
            return true;
          }
          else {
            return false;
          }
        }
      }
    }
    return false;
  }

  /**
   * factory to manage table column filters
   * <p>
   * default creates a {@link DefaultTableColumnFilterManager}
   */
  protected ITableColumnFilterManager createColumnFilterManager() {
    return new DefaultTableColumnFilterManager(this);
  }

  /**
   * factory to manage custom columns
   * <p>
   * default creates null
   */
  protected ITableCustomizer createTableCustomizer() {
    return null;
  }

  /*
   * Row handling methods. Operate on a Row instance.
   */

  public ITableRow createRow() throws ProcessingException {
    return new P_TableRowBuilder().createRow();
  }

  public ITableRow createRow(Object rowValues) throws ProcessingException {
    return new P_TableRowBuilder().createRow(rowValues);
  }

  public ITableRow[] createRowsByArray(Object dataArray) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByArray(dataArray);
  }

  public ITableRow[] createRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByArray(dataArray, rowStatus);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public ITableRow[] createRowsByMatrix(Object dataMatrixOrReference) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public ITableRow[] createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference, rowStatus);
  }

  public ITableRow[] createRowsByCodes(ICode[] codes) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByCodes(codes);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public void replaceRowsByMatrix(Object dataMatrixOrReference) throws ProcessingException {
    replaceRows(createRowsByMatrix(dataMatrixOrReference));
  }

  public void replaceRowsByArray(Object dataArray) throws ProcessingException {
    replaceRows(createRowsByArray(dataArray));
  }

  public void replaceRows(ITableRow[] newRows) throws ProcessingException {
    /*
     * There are two ways to replace: (1) Completely replace all rows by
     * discarding all rows and adding new rows when - autoDiscardOnDelete=true
     * (2) Replace rows by applying insert/update/delete on existing rows by
     * primary key match when - autoDiscardOnDelete=false
     */
    if (isAutoDiscardOnDelete()) {
      replaceRowsCase1(newRows);
    }
    else {
      replaceRowsCase2(newRows);
    }
  }

  private void replaceRowsCase1(ITableRow[] newRows) throws ProcessingException {
    try {
      setTableChanging(true);
      //
      ArrayList<CompositeObject> selectedKeys = new ArrayList<CompositeObject>();
      for (ITableRow r : getSelectedRows()) {
        selectedKeys.add(new CompositeObject(getRowKeys(r)));
      }
      discardAllRows();
      addRows(newRows, false);
      // restore selection
      ArrayList<ITableRow> selectedRows = new ArrayList<ITableRow>();
      if (selectedKeys.size() > 0) {
        for (ITableRow r : m_rows) {
          if (selectedKeys.remove(new CompositeObject(getRowKeys(r)))) {
            selectedRows.add(r);
            if (selectedKeys.size() == 0) {
              break;
            }
          }
        }
      }
      selectRows(selectedRows.toArray(new ITableRow[selectedRows.size()]), false);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void replaceRowsCase2(ITableRow[] newRows) throws ProcessingException {
    try {
      setTableChanging(true);
      //
      int[] oldToNew = new int[getRowCount()];
      int[] newToOld = new int[newRows.length];
      Arrays.fill(oldToNew, -1);
      Arrays.fill(newToOld, -1);
      HashMap<CompositeObject, Integer> newRowIndexMap = new HashMap<CompositeObject, Integer>();
      for (int i = newRows.length - 1; i >= 0; i--) {
        newRowIndexMap.put(new CompositeObject(getRowKeys(newRows[i])), new Integer(i));
      }
      int mappedCount = 0;
      for (int i = 0, ni = getRowCount(); i < ni; i++) {
        ITableRow existingRow = m_rows.get(i);
        Integer newIndex = newRowIndexMap.remove(new CompositeObject(getRowKeys(existingRow)));
        if (newIndex != null) {
          oldToNew[i] = newIndex.intValue();
          newToOld[newIndex.intValue()] = i;
          mappedCount++;
        }
      }
      ITableRow[] updatedRows = new ITableRow[mappedCount];
      int index = 0;
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] >= 0) {
          ITableRow oldRow = getRow(i);
          ITableRow newRow = newRows[oldToNew[i]];
          try {
            oldRow.setRowChanging(true);
            //
            //TODO set row icon,bg,fg,tooltip on row-main-cell
            oldRow.setEnabled(newRow.isEnabled());
            oldRow.setStatus(newRow.getStatus());
            for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
              if (columnIndex < newRow.getCellCount()) {
                oldRow.getCellForUpdate(columnIndex).updateFrom(newRow.getCell(columnIndex));
              }
            }
          }
          finally {
            oldRow.setRowPropertiesChanged(false);
            oldRow.setRowChanging(false);
          }
          //
          updatedRows[index] = oldRow;
          index++;
        }
      }
      ITableRow[] deletedRows = new ITableRow[getRowCount() - mappedCount];
      index = 0;
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] < 0) {
          deletedRows[index] = m_rows.get(i);
          index++;
        }
      }
      ITableRow[] insertedRows = new ITableRow[newRows.length - mappedCount];
      int[] insertedRowIndexes = new int[newRows.length - mappedCount];
      index = 0;
      for (int i = 0; i < newToOld.length; i++) {
        if (newToOld[i] < 0) {
          insertedRows[index] = newRows[i];
          insertedRowIndexes[index] = i;
          index++;
        }
      }
      //
      updateRows(updatedRows);
      deleteRows(deletedRows);
      addRows(insertedRows, false, insertedRowIndexes);
    }
    finally {
      setTableChanging(false);
    }
  }

  public void updateRow(ITableRow row) {
    if (row != null) {
      updateRows(new ITableRow[]{row});
    }
  }

  public void updateAllRows() {
    ITableRow[] rows = getRows();
    updateRows(rows);
  }

  public void setRowState(ITableRow row, int rowState) throws ProcessingException {
    setRowState(new ITableRow[]{row}, rowState);
  }

  public void setAllRowState(int rowState) throws ProcessingException {
    setRowState(getRows(), rowState);
  }

  public void setRowState(ITableRow[] rows, int rowState) throws ProcessingException {
    try {
      setTableChanging(true);
      //
      for (int i = 0; i < rows.length; i++) {
        rows[i].setStatus(rowState);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  public void updateRows(ITableRow[] rows) {
    try {
      setTableChanging(true);
      //
      ArrayList<ITableRow> resolvedRowList = new ArrayList<ITableRow>(rows.length);
      for (int i = 0; i < rows.length; i++) {
        ITableRow resolvedRow = resolveRow(rows[i]);
        if (resolvedRow != null) {
          resolvedRowList.add(resolvedRow);
          updateRowImpl(resolvedRow);
        }
      }
      if (resolvedRowList.size() > 0) {
        fireRowsUpdated(resolvedRowList.toArray(new ITableRow[0]));
      }
      if (getColumnSet().getSortColumnCount() > 0) {
        // restore order of rows according to sort criteria
        if (isTableChanging()) {
          m_sortValid = false;
        }
        else {
          sort();
        }
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void updateRowImpl(ITableRow row) {
    if (row != null) {
      /*
       * do NOT use ITableRow#setRowChanging, this might cause a stack overflow
       */
      enqueueDecorationTasks(row);
    }
  }

  public int getRowCount() {
    return m_rows.size();
  }

  public int getDeletedRowCount() {
    return m_deletedRows.size();
  }

  public int getSelectedRowCount() {
    return m_selectedRows.size();
  }

  public ITableRow getSelectedRow() {
    if (m_selectedRows.size() > 0) {
      return m_selectedRows.first();
    }
    else {
      return null;
    }
  }

  public ITableRow[] getSelectedRows() {
    return m_selectedRows.toArray(new ITableRow[m_selectedRows.size()]);
  }

  public boolean isSelectedRow(ITableRow row) {
    row = resolveRow(row);
    if (row == null) return false;
    else return m_selectedRows.contains(row);
  }

  public void selectRow(int rowIndex) {
    selectRow(getRow(rowIndex));
  }

  public void selectRow(ITableRow row) {
    selectRow(row, false);
  }

  public void selectRow(ITableRow row, boolean append) {
    if (row != null) {
      selectRows(new ITableRow[]{row}, append);
    }
    else {
      selectRows(new ITableRow[0], append);
    }
  }

  public void selectRows(ITableRow[] rows) {
    selectRows(rows, false);
  }

  public void selectRows(ITableRow[] rows, boolean append) {
    rows = resolveRows(rows);
    TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
    if (append) {
      newSelection.addAll(m_selectedRows);
      newSelection.addAll(Arrays.asList(rows));
    }
    else {
      newSelection.addAll(Arrays.asList(rows));
    }
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITableRow first = newSelection.first();
      newSelection.clear();
      newSelection.add(first);
    }
    if (!m_selectedRows.equals(newSelection)) {
      m_selectedRows = newSelection;
      fireRowsSelected(m_selectedRows.toArray(new ITableRow[0]));
    }
  }

  public void selectFirstRow() {
    selectRow(getRow(0));
  }

  public void selectNextRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() + 1 < getRowCount()) {
      selectRow(getRow(row.getRowIndex() + 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(0);
    }
  }

  public void selectPreviousRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() - 1 >= 0) {
      selectRow(getRow(row.getRowIndex() - 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(getRowCount() - 1);
    }
  }

  public void selectLastRow() {
    selectRow(getRow(getRowCount() - 1));
  }

  public void deselectRow(ITableRow row) {
    if (row != null) {
      deselectRows(new ITableRow[]{row});
    }
    else {
      deselectRows(new ITableRow[0]);
    }
  }

  public void deselectRows(ITableRow[] rows) {
    rows = resolveRows(rows);
    if (rows != null && rows.length > 0) {
      if (m_selectedRows.removeAll(Arrays.asList(rows))) {
        fireRowsSelected(m_selectedRows.toArray(new ITableRow[0]));
      }
    }
  }

  public void selectAllRows() {
    selectRows(getRows(), false);
  }

  public void deselectAllRows() {
    selectRow(null, false);
  }

  public void selectAllEnabledRows() {
    ArrayList<ITableRow> newList = new ArrayList<ITableRow>();
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      if (row.isEnabled()) {
        newList.add(row);
      }
      else if (isSelectedRow(row)) {
        newList.add(row);
      }
    }
    selectRows(newList.toArray(new ITableRow[0]), false);
  }

  public void deselectAllEnabledRows() {
    ITableRow[] selectedRows = getSelectedRows();
    ArrayList<ITableRow> newList = new ArrayList<ITableRow>();
    for (int i = 0; i < selectedRows.length; i++) {
      if (selectedRows[i].isEnabled()) {
        newList.add(selectedRows[i]);
      }
    }
    deselectRows(newList.toArray(new ITableRow[0]));
  }

  public ITableRow[] getCheckedRows() {
    final ArrayList<ITableRow> list = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.isChecked()) {
        list.add(row);
      }
    }
    return list.toArray(new ITableRow[list.size()]);
  }

  public String getDefaultIconId() {
    String iconId = propertySupport.getPropertyString(PROP_DEFAULT_ICON);
    if (iconId != null && iconId.length() == 0) iconId = null;
    return iconId;
  }

  public void setDefaultIconId(String iconId) {
    propertySupport.setPropertyString(PROP_DEFAULT_ICON, iconId);
  }

  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  public final void setEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_ENABLED, b);
  }

  public boolean isScrollToSelection() {
    return propertySupport.getPropertyBool(PROP_SCROLL_TO_SELECTION);
  }

  public void setScrollToSelection(boolean b) {
    propertySupport.setPropertyBool(PROP_SCROLL_TO_SELECTION, b);
  }

  /**
   * @return a copy of a row<br>
   *         when the row is changed it has to be applied to the table using
   *         modifyRow(row);
   */
  public ITableRow getRow(int rowIndex) {
    ITableRow row = null;
    ITableRow[] rows = getRows();
    if (rowIndex >= 0 && rowIndex < rows.length) {
      row = rows[rowIndex];
    }
    return row;
  }

  public ITableRow[] getRows() {
    //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
    synchronized (m_cachedRowsLock) {
      if (m_cachedRows == null) {
        //this code must be thread-safe
        m_cachedRows = m_rows.toArray(new ITableRow[m_rows.size()]);
      }
      return m_cachedRows;
    }
  }

  public ITableRow[] getFilteredRows() {
    ITableRow[] rows = getRows();
    if (m_rowFilters.size() > 0) {
      //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
      synchronized (m_cachedFilteredRowsLock) {
        if (m_cachedFilteredRows == null) {
          //this code must be thread-safe
          if (m_rowFilters.size() > 0) {
            ArrayList<ITableRow> list = new ArrayList<ITableRow>(getRowCount());
            for (ITableRow row : rows) {
              if (row != null && row.isFilterAccepted()) {
                list.add(row);
              }
            }
            m_cachedFilteredRows = list.toArray(new ITableRow[list.size()]);
          }
          else {
            m_cachedFilteredRows = new ITableRow[0];
          }
        }
        return m_cachedFilteredRows;
      }
    }
    else {
      return rows;
    }
  }

  public int getFilteredRowCount() {
    if (m_rowFilters.size() > 0) {
      return getFilteredRows().length;
    }
    else {
      return getRowCount();
    }
  }

  public ITableRow getFilteredRow(int index) {
    if (m_rowFilters.size() > 0) {
      ITableRow row = null;
      ITableRow[] filteredRows = getFilteredRows();
      if (index >= 0 && index < filteredRows.length) {
        row = filteredRows[index];
      }
      return row;
    }
    else {
      return getRow(index);
    }
  }

  public int getFilteredRowIndex(ITableRow row) {
    ITableRow[] filteredRows = getFilteredRows();
    for (int i = 0; i < filteredRows.length; i++) {
      if (filteredRows[i].equals(row)) {
        return i;
      }
    }
    return -1;
  }

  public Object[][] getTableData() {
    Object[][] data = new Object[getRowCount()][getColumnCount()];
    for (int r = 0; r < getRowCount(); r++) {
      for (int c = 0; c < getColumnCount(); c++) {
        data[r][c] = getRow(r).getCellValue(c);
      }
    }
    return data;
  }

  public Object[][] exportTableRowsAsCSV(ITableRow[] rows, IColumn[] columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormats) {
    int nr = rows.length;
    Object[][] a = new Object[nr + (includeLineForColumnNames ? 1 : 0) + (includeLineForColumnTypes ? 1 : 0) + (includeLineForColumnFormats ? 1 : 0)][columns.length];
    for (int c = 0; c < columns.length; c++) {
      IColumn col = columns[c];
      Class type;
      boolean byValue;
      String format;
      if (col instanceof IDateColumn) {
        if (((IDateColumn) col).isHasTime()) {
          type = Timestamp.class;
          byValue = true;
          format = ((IDateColumn) col).getFormat();
        }
        else {
          type = Date.class;
          byValue = true;
          format = ((IDateColumn) col).getFormat();
        }
      }
      else if (col instanceof IDoubleColumn) {
        type = Double.class;
        byValue = true;
        format = ((IDoubleColumn) col).getFormat();
      }
      else if (col instanceof IIntegerColumn) {
        type = Integer.class;
        byValue = true;
        format = ((IIntegerColumn) col).getFormat();
      }
      else if (col instanceof ILongColumn) {
        type = Long.class;
        byValue = true;
        format = ((ILongColumn) col).getFormat();
      }
      else if (col instanceof IBigDecimalColumn) {
        type = Long.class;
        byValue = true;
        format = ((IBigDecimalColumn) col).getFormat();
      }
      else if (col instanceof ISmartColumn) {
        type = String.class;
        byValue = false;
        format = null;
      }
      else if (col instanceof ITimeColumn) {
        type = Date.class;
        byValue = true;
        format = ((ITimeColumn) col).getFormat();
      }
      else {
        type = String.class;
        byValue = false;
        format = null;
      }
      //
      int csvRowIndex = 0;
      if (includeLineForColumnNames) {
        a[csvRowIndex][c] = columns[c].getHeaderCell().getText();
        csvRowIndex++;
      }
      if (includeLineForColumnTypes) {
        a[csvRowIndex][c] = type;
        csvRowIndex++;
      }
      if (includeLineForColumnFormats) {
        a[csvRowIndex][c] = format;
        csvRowIndex++;
      }
      for (int r = 0; r < nr; r++) {
        if (byValue) {
          if (type == Timestamp.class) {
            a[csvRowIndex][c] = TypeCastUtility.castValue(columns[c].getValue(rows[r]), Timestamp.class);
          }
          else {
            a[csvRowIndex][c] = columns[c].getValue(rows[r]);
          }
        }
        else {
          a[csvRowIndex][c] = columns[c].getDisplayText(rows[r]);
        }
        csvRowIndex++;
      }
    }
    return a;
  }

  public ITableRow[] getRows(int[] rowIndexes) {
    if (rowIndexes == null) {
      return new ITableRow[0];
    }
    ITableRow[] rows = new ITableRow[rowIndexes.length];
    int missingCount = 0;
    for (int i = 0; i < rowIndexes.length; i++) {
      rows[i] = getRow(rowIndexes[i]);
      if (rows[i] == null) missingCount++;
    }
    if (missingCount > 0) {
      ITableRow[] newRows = new ITableRow[rowIndexes.length - missingCount];
      int index = 0;
      for (int i = 0; i < rows.length; i++) {
        if (rows[i] != null) {
          newRows[index] = rows[i];
          index++;
        }
      }
      rows = newRows;
    }
    return rows;
  }

  /**
   * @return a copy of a deleted row<br>
   *         when the row is changed it has to be applied to the table using
   *         modifyRow(row);
   */
  public ITableRow[] getDeletedRows() {
    return m_deletedRows.values().toArray(new ITableRow[0]);
  }

  public int getInsertedRowCount() {
    int count = 0;
    ITableRow[] rows = getRows();
    for (int i = 0, ni = rows.length; i < ni; i++) {
      if (rows[i].getStatus() == ITableRow.STATUS_INSERTED) {
        count++;
      }
    }
    return count;
  }

  public ITableRow[] getInsertedRows() {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    ITableRow[] rows = getRows();
    for (int i = 0, ni = rows.length; i < ni; i++) {
      ITableRow row = rows[i];
      if (row.getStatus() == ITableRow.STATUS_INSERTED) {
        rowList.add(row);
      }
    }
    return rowList.toArray(new ITableRow[rowList.size()]);
  }

  public int getUpdatedRowCount() {
    int count = 0;
    ITableRow[] rows = getRows();
    for (int i = 0, ni = rows.length; i < ni; i++) {
      if (rows[i].getStatus() == ITableRow.STATUS_UPDATED) {
        count++;
      }
    }
    return count;
  }

  public ITableRow[] getUpdatedRows() {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    ITableRow[] rows = getRows();
    for (int i = 0, ni = rows.length; i < ni; i++) {
      ITableRow row = rows[i];
      if (row.getStatus() == ITableRow.STATUS_UPDATED) {
        rowList.add(row);
      }
    }
    return rowList.toArray(new ITableRow[rowList.size()]);
  }

  /**
   * Convenience to add row by data only
   */
  public ITableRow addRowByArray(Object dataArray) throws ProcessingException {
    if (dataArray == null) return null;
    ITableRow[] a = addRowsByMatrix(new Object[]{dataArray});
    if (a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  public ITableRow[] addRowsByMatrix(Object dataMatrix) throws ProcessingException {
    return addRowsByMatrix(dataMatrix, ITableRow.STATUS_INSERTED);
  }

  public ITableRow[] addRowsByMatrix(Object dataMatrix, int rowStatus) throws ProcessingException {
    return addRows(createRowsByMatrix(dataMatrix, rowStatus));
  }

  public ITableRow[] addRowsByArray(Object dataArray) throws ProcessingException {
    return addRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  public ITableRow[] addRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    return addRows(createRowsByArray(dataArray, rowStatus));
  }

  public ITableRow addRow(ITableRow newRow) throws ProcessingException {
    return addRow(newRow, false);
  }

  public ITableRow addRow(ITableRow newRow, boolean markAsInserted) throws ProcessingException {
    ITableRow[] addedRows = addRows(new ITableRow[]{newRow}, markAsInserted);
    if (addedRows.length > 0) {
      return addedRows[0];
    }
    else {
      return null;
    }
  }

  public ITableRow[] addRows(ITableRow[] newRows) throws ProcessingException {
    return addRows(newRows, false);
  }

  public ITableRow[] addRows(ITableRow[] newRows, boolean markAsInserted) throws ProcessingException {
    return addRows(newRows, markAsInserted, null);
  }

  public ITableRow[] addRows(ITableRow[] newRows, boolean markAsInserted, int[] insertIndexes) throws ProcessingException {
    if (newRows == null || newRows.length == 0) return new ITableRow[0];
    try {
      setTableChanging(true);
      //
      int oldRowCount = m_rows.size();
      //m_rows.ensureCapacity(m_rows.size() + newRows.length);
      ITableRow[] newIRows = new ITableRow[newRows.length];
      for (int i = 0; i < newRows.length; i++) {
        newIRows[i] = addRowImpl(newRows[i], markAsInserted);
      }
      fireRowsInserted(newIRows);
      if (getColumnSet().getSortColumnCount() > 0) {
        // restore order of rows according to sort criteria
        if (isTableChanging()) {
          m_sortValid = false;
        }
        else {
          sort();
        }
      }
      else if (insertIndexes != null) {
        ITableRow[] sortArray = new ITableRow[m_rows.size()];
        // add new rows that have a given sortIndex
        for (int i = 0; i < insertIndexes.length; i++) {
          sortArray[insertIndexes[i]] = newIRows[i];
        }
        int sortArrayIndex = 0;
        // add existing rows
        for (int i = 0; i < oldRowCount; i++) {
          // find next empty slot
          while (sortArray[sortArrayIndex] != null)
            sortArrayIndex++;
          sortArray[sortArrayIndex] = m_rows.get(i);
        }
        // add new rows that have no given sortIndex
        for (int i = insertIndexes.length; i < newIRows.length; i++) {
          // find next empty slot
          while (sortArray[sortArrayIndex] != null)
            sortArrayIndex++;
          sortArray[sortArrayIndex] = newIRows[i];
        }
        sortInternal(sortArray);
      }
      return newIRows;
    }
    finally {
      setTableChanging(false);
    }
  }

  private ITableRow addRowImpl(ITableRow newRow, boolean markAsInserted) throws ProcessingException {
    if (markAsInserted) {
      newRow.setStatus(ITableRow.STATUS_INSERTED);
    }
    InternalTableRow newIRow = new InternalTableRow(this, newRow);
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
    }
    int newIndex = m_rows.size();
    newIRow.setRowIndex(newIndex);
    newIRow.setTableInternal(this);
    m_rows.add(newIRow);
    enqueueDecorationTasks(newIRow);
    return newIRow;
  }

  public void moveRow(int sourceIndex, int targetIndex) {
    moveRowImpl(sourceIndex, targetIndex);
  }

  /**
   * move the movingRow to the location just before the target row
   */
  public void moveRowBefore(ITableRow movingRow, ITableRow targetRow) {
    movingRow = resolveRow(movingRow);
    targetRow = resolveRow(targetRow);
    if (movingRow != null && targetRow != null) {
      moveRowImpl(movingRow.getRowIndex(), targetRow.getRowIndex());
    }
  }

  /**
   * move the movingRow to the location just after the target row
   */
  public void moveRowAfter(ITableRow movingRow, ITableRow targetRow) {
    movingRow = resolveRow(movingRow);
    targetRow = resolveRow(targetRow);
    if (movingRow != null && targetRow != null) {
      moveRowImpl(movingRow.getRowIndex(), targetRow.getRowIndex() + 1);
    }
  }

  private void moveRowImpl(int sourceIndex, int targetIndex) {
    if (sourceIndex < 0) sourceIndex = 0;
    if (sourceIndex >= getRowCount()) sourceIndex = getRowCount() - 1;
    if (targetIndex < 0) targetIndex = 0;
    if (targetIndex >= getRowCount()) targetIndex = getRowCount() - 1;
    if (targetIndex != sourceIndex) {
      synchronized (m_cachedRowsLock) {
        m_cachedRows = null;
      }
      ITableRow row = m_rows.remove(sourceIndex);
      m_rows.add(targetIndex, row);
      // update row indexes
      int min = Math.min(sourceIndex, targetIndex);
      int max = Math.max(sourceIndex, targetIndex);
      ITableRow[] changedRows = new ITableRow[max - min + 1];
      for (int i = min; i <= max; i++) {
        changedRows[i - min] = getRow(i);
        ((InternalTableRow) changedRows[i - min]).setRowIndex(i);
      }
      fireRowOrderChanged();
      // rebuild selection
      selectRows(getSelectedRows(), false);
    }
  }

  public void deleteRow(int rowIndex) {
    deleteRows(new int[]{rowIndex});
  }

  public void deleteRows(int[] rowIndexes) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    deleteRows(rowList.toArray(new ITableRow[0]));
  }

  public void deleteRow(ITableRow row) {
    if (row != null) {
      deleteRows(new ITableRow[]{row});
    }
  }

  public void deleteAllRows() {
    deleteRows(getRows());
  }

  public void deleteRows(ITableRow[] rows) {
    ITableRow[] existingRows = getRows();
    //peformance quick-check
    if (rows != existingRows) {
      rows = resolveRows(rows);
    }
    if (rows != null && rows.length > 0) {
      try {
        setTableChanging(true);
        //
        int rowCountBefore = getRowCount();
        int min = getRowCount();
        int max = 0;
        for (int i = 0; i < rows.length; i++) {
          ITableRow row = rows[i];
          min = Math.min(min, row.getRowIndex());
          max = Math.max(max, row.getRowIndex());
        }
        ITableRow[] deletedRows = rows;
        // remove from selection
        deselectRows(deletedRows);
        //delete impl
        //peformance quick-check
        if (rows == existingRows) {
          //remove all of them
          m_rows.clear();
          synchronized (m_cachedRowsLock) {
            m_cachedRows = null;
          }
          for (int i = deletedRows.length - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows[i];
            if (candidateRow != null) {
              deleteRowImpl(candidateRow);
            }
          }
        }
        else {
          for (int i = deletedRows.length - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows[i];
            if (candidateRow != null) {
              // delete regardless if index is right
              boolean removed = m_rows.remove(candidateRow);
              if (removed) {
                synchronized (m_cachedRowsLock) {
                  m_cachedRows = null;
                }
                deleteRowImpl(candidateRow);
              }
            }
          }
        }
        // get affected rows
        HashSet<ITableRow> selectionRows = new HashSet<ITableRow>(Arrays.asList(getSelectedRows()));
        int minAffectedIndex = Math.max(min - 1, 0);
        ITableRow[] affectedRows = new ITableRow[getRowCount() - minAffectedIndex];
        for (int i = minAffectedIndex; i < getRowCount(); i++) {
          affectedRows[i - minAffectedIndex] = getRow(i);
          ((InternalTableRow) affectedRows[i - minAffectedIndex]).setRowIndex(i);
          selectionRows.remove(getRow(i));
        }
        if (rowCountBefore == deletedRows.length) {
          fireAllRowsDeleted(deletedRows);
        }
        else {
          fireRowsDeleted(deletedRows);
        }
        selectRows(selectionRows.toArray(new ITableRow[selectionRows.size()]), false);
      }
      finally {
        setTableChanging(false);
      }
    }
  }

  private void deleteRowImpl(ITableRow row) {
    if (!(row instanceof InternalTableRow)) {
      return;
    }
    InternalTableRow internalRow = (InternalTableRow) row;
    if (isAutoDiscardOnDelete()) {
      internalRow.setTableInternal(null);
      // don't manage deleted rows any further
    }
    else if (internalRow.getStatus() == ITableRow.STATUS_INSERTED) {
      internalRow.setTableInternal(null);
      // it was new and now it is gone, no further action required
    }
    else {
      internalRow.setStatus(ITableRow.STATUS_DELETED);
      m_deletedRows.put(new CompositeObject(getRowKeys(internalRow)), internalRow);
    }
  }

  public void discardRow(int rowIndex) {
    discardRows(new int[]{rowIndex});
  }

  public void discardRows(int[] rowIndexes) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    discardRows(rowList.toArray(new ITableRow[0]));
  }

  public void discardRow(ITableRow row) {
    if (row != null) {
      discardRows(new ITableRow[]{row});
    }
  }

  public void discardAllRows() {
    discardRows(getRows());
  }

  /**
   * discard is the same as delete with the exception that discarded rows are
   * not collected in the deletedRows list
   */
  public void discardRows(ITableRow[] rows) {
    try {
      setTableChanging(true);
      //
      for (int i = 0; i < rows.length; i++) {
        ((InternalTableRow) rows[i]).setStatus(ITableRow.STATUS_INSERTED);
      }
      deleteRows(rows);
    }
    finally {
      setTableChanging(false);
    }
  }

  public void setContextColumn(IColumn col) {
    propertySupport.setProperty(PROP_CONTEXT_COLUMN, col);
  }

  public IColumn getContextColumn() {
    return (IColumn) propertySupport.getProperty(PROP_CONTEXT_COLUMN);
  }

  public void clearDeletedRows() {
    for (Iterator it = m_deletedRows.values().iterator(); it.hasNext();) {
      ((InternalTableRow) it.next()).setTableInternal(null);
    }
    m_deletedRows.clear();
  }

  public Object[] getRowKeys(int rowIndex) {
    ITableRow row = getRow(rowIndex);
    return getRowKeys(row);
  }

  public Object[] getRowKeys(ITableRow row) {
    // do not resolve
    Object[] keys = new Object[0];
    if (row != null) {
      keys = row.getKeyValues();
    }
    return keys;
  }

  public ITableRow findRowByKey(Object[] keys) {
    IColumn[] keyColumns = getColumnSet().getKeyColumns();
    if (keyColumns.length == 0) {
      keyColumns = getColumnSet().getColumns();
    }
    for (ITableRow row : m_rows) {
      boolean match = true;
      if (keys != null && keys.length > 0) {
        for (int i = 0; i < keyColumns.length && i < keys.length; i++) {
          if (!CompareUtility.equals(keyColumns[i].getValue(row), keys[i])) {
            match = false;
            break;
          }
        }
      }
      if (match) {
        return row;
      }
    }
    return null;
  }

  public ITableColumnFilterManager getColumnFilterManager() {
    return m_columnFilterManager;
  }

  public void setColumnFilterManager(ITableColumnFilterManager m) {
    m_columnFilterManager = m;
  }

  public ITableCustomizer getTableCustomizer() {
    return m_tableCustomizer;
  }

  public void setTableCustomizer(ITableCustomizer c) {
    m_tableCustomizer = c;
  }

  public boolean isSortEnabled() {
    return m_sortEnabled;
  }

  public void setSortEnabled(boolean b) {
    m_sortEnabled = b;
  }

  public void sort() {
    try {
      if (isSortEnabled()) {
        IColumn[] sortCols = getColumnSet().getSortColumns();
        if (sortCols.length > 0) {
          // first make sure decorations and lookups are up-to-date
          processDecorationBuffer();
          ITableRow[] a = getRows();
          Arrays.sort(a, new TableRowComparator(sortCols));
          sortInternal(a);
        }
      }
    }
    finally {
      m_sortValid = true;
    }
  }

  public void sort(ITableRow[] rowsInNewOrder) {
    ITableRow[] resolvedRows = resolveRows(rowsInNewOrder);
    if (resolvedRows.length == rowsInNewOrder.length) {
      sortInternal(resolvedRows);
    }
    else {
      // check which rows could not be mapped
      ArrayList<ITableRow> list = new ArrayList<ITableRow>();
      list.addAll(m_rows);
      list.removeAll(Arrays.asList(resolvedRows));
      ArrayList<ITableRow> sortedList = new ArrayList<ITableRow>();
      sortedList.addAll(Arrays.asList(resolvedRows));
      sortedList.addAll(list);
      sortInternal(sortedList.toArray(new ITableRow[sortedList.size()]));
    }
  }

  private void sortInternal(ITableRow[] resolvedRows) {
    for (int i = 0; i < resolvedRows.length; i++) {
      ((InternalTableRow) resolvedRows[i]).setRowIndex(i);
    }
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      m_rows.clear();
      m_rows.addAll(Arrays.asList(resolvedRows));
    }
    //sort selection without firing an event
    if (m_selectedRows != null && m_selectedRows.size() > 0) {
      ArrayList<ITableRow> backupSelection = new ArrayList<ITableRow>(m_selectedRows.size());
      backupSelection.addAll(m_selectedRows);
      m_selectedRows.clear();
      m_selectedRows.addAll(backupSelection);
    }
    fireRowOrderChanged();
  }

  public void resetColumnVisibilities() {
    resetColumns(true, false, false, false);
  }

  public void resetColumnOrder() {
    resetColumns(false, true, false, false);
  }

  public void resetColumnSortOrder() {
    resetColumns(false, false, true, false);
  }

  public void resetColumnWidths() {
    resetColumns(false, false, false, true);
  }

  public void resetDisplayableColumns() {
    resetColumns(true, true, true, true);
  }

  public void resetColumns(boolean visibility, boolean order, boolean sorting, boolean widths) {
    try {
      setTableChanging(true);
      //
      try {
        if (sorting) {
          m_sortValid = false;
        }
        resetColumnsInternal(visibility, order, sorting, widths);
        execResetColumns(visibility, order, sorting, widths);
      }
      catch (Throwable t) {
        LOG.error("reset columns " + visibility + "," + order + "," + sorting + "," + widths, t);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  private void resetColumnsInternal(boolean visibility, boolean order, boolean sorting, boolean widths) {
    ClientUIPreferences env = ClientUIPreferences.getInstance();
    for (IColumn col : getColumns()) {
      env.removeTableColumnPreferences(col, visibility, order, sorting, widths);
    }
    //Visibilities
    if (visibility) {
      ArrayList<IColumn> list = new ArrayList<IColumn>();
      for (IColumn col : getColumnSet().getAllColumnsInUserOrder()) {
        if (col.isDisplayable()) {
          boolean configuredVisible = ((AbstractColumn) col).isInitialVisible();
          if (configuredVisible) {
            list.add(col);
          }
        }
      }
      getColumnSet().setVisibleColumns(list.toArray(new IColumn[list.size()]));
    }
    //Order
    if (order) {
      ArrayList<IColumn> list = new ArrayList<IColumn>();
      for (IColumn col : getColumns()) {
        if (col.isDisplayable() && col.isVisible()) {
          list.add(col);
        }
      }
      getColumnSet().setVisibleColumns(list.toArray(new IColumn[list.size()]));
    }
    //Sorting
    if (sorting) {
      TreeMap<CompositeObject, IColumn> sortMap = new TreeMap<CompositeObject, IColumn>();
      int index = 0;
      for (IColumn col : getColumns()) {
        if (col.getInitialSortIndex() >= 0) {
          sortMap.put(new CompositeObject(col.getInitialSortIndex(), index), col);
        }
        index++;
      }
      //
      getColumnSet().clearSortColumns();
      getColumnSet().clearPermanentHeadSortColumns();
      getColumnSet().clearPermanentTailSortColumns();
      for (IColumn col : sortMap.values()) {
        if (col.isInitialAlwaysIncludeSortAtBegin()) {
          getColumnSet().addPermanentHeadSortColumn(col, col.isInitialSortAscending());
        }
        else if (col.isInitialAlwaysIncludeSortAtEnd()) {
          getColumnSet().addPermanentTailSortColumn(col, col.isInitialSortAscending());
        }
        else {
          getColumnSet().addSortColumn(col, col.isInitialSortAscending());
        }
      }
    }
    //Widths
    if (widths) {
      for (IColumn col : getColumns()) {
        if (col.isDisplayable()) {
          col.setWidth(col.getInitialWidth());
        }
      }
    }
  }

  /**
   * Affects columns with lookup calls or code types<br>
   * cells that have changed values fetch new texts/decorations from the lookup
   * service in one single batch call lookup (performance optimization)
   */
  private void processDecorationBuffer() {
    /*
     * 1. process lookup service calls
     */
    try {
      BatchLookupCall batchCall = new BatchLookupCall();
      ArrayList<ITableRow> tableRowList = new ArrayList<ITableRow>();
      ArrayList<Integer> columnIndexList = new ArrayList<Integer>();
      HashMap<LocalLookupCall, LookupRow[]> localLookupCache = new HashMap<LocalLookupCall, LookupRow[]>();
      for (P_CellLookup lookup : m_cellLookupBuffer) {
        ITableRow row = lookup.getRow();
        if (row.getTable() == AbstractTable.this) {
          ISmartColumn<?> col = lookup.getColumn();
          LookupCall call = col.prepareLookupCall(row);
          if (call != null) {
            //split: local vs remote
            if (call instanceof LocalLookupCall) {
              LookupRow[] result = null;
              //optimize local calls by caching the results
              result = localLookupCache.get(call);
              if (verifyLocalLookupCallBeanQuality((LocalLookupCall) call)) {
                result = localLookupCache.get(call);
                if (result == null) {
                  result = call.getDataByKey();
                  localLookupCache.put((LocalLookupCall) call, result);
                }
              }
              else {
                result = call.getDataByKey();
              }
              applyLookupResult((InternalTableRow) row, col.getColumnIndex(), result);
            }
            else {
              tableRowList.add(row);
              columnIndexList.add(new Integer(col.getColumnIndex()));
              batchCall.addLookupCall(call);
            }
          }
        }
      }
      m_cellLookupBuffer.clear();
      //
      if (!batchCall.isEmpty()) {
        ITableRow[] tableRows = tableRowList.toArray(new ITableRow[0]);
        LookupRow[][] resultArray;
        IBatchLookupService service = SERVICES.getService(IBatchLookupService.class);
        resultArray = service.getBatchDataByKey(batchCall);
        for (int i = 0; i < tableRows.length; i++) {
          applyLookupResult((InternalTableRow) tableRows[i], ((Number) columnIndexList.get(i)).intValue(), resultArray[i]);
        }
      }
    }
    catch (ProcessingException e) {
      if (e.isInterruption()) {
        // nop
      }
      else {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
    finally {
      m_cellLookupBuffer.clear();
    }
    /*
     * 2. update row decorations
     */
    HashSet<ITableRow> set = m_rowDecorationBuffer;
    m_rowDecorationBuffer = new HashSet<ITableRow>();
    for (ITableRow row : set) {
      if (row.getTable() == AbstractTable.this) {
        applyRowDecorationsImpl(row);
      }
    }
    /*
     * check row filters
     */
    if (m_rowFilters.size() > 0) {
      boolean filterChanged = false;
      for (ITableRow row : set) {
        if (row.getTable() == AbstractTable.this) {
          if (row instanceof InternalTableRow) {
            InternalTableRow irow = (InternalTableRow) row;
            boolean oldFlag = irow.isFilterAccepted();
            applyRowFiltersInternal(irow);
            boolean newFlag = irow.isFilterAccepted();
            filterChanged = (oldFlag != newFlag);
          }
        }
      }
      if (filterChanged) {
        fireRowFilterChanged();
      }
    }
  }

  private static final boolean DEV = Platform.inDevelopmentMode();

  /**
   * In order to use caching of results on local lookup calls, it is crucial that the javabean concepts are valid,
   * especially hashCode and equals.
   * <p>
   * Scout tries to help developers to find problems related to this issue and write a warning in development mode on
   * all local lookup call subclasses that do not overwrite hashCode and equals.
   */
  private boolean verifyLocalLookupCallBeanQuality(LocalLookupCall call) {
    if (call.getClass() == LocalLookupCall.class) {
      return true;
    }
    if (ConfigurationUtility.isMethodOverwrite(LocalLookupCall.class, "equals", new Class[]{Object.class}, call.getClass())) {
      return true;
    }
    if (DEV) {
      LOG.warn("" + call.getClass() + " subclasses LocalLookupCall and should override the 'boolean equals(Object obj)' method");
    }
    return false;
  }

  /**
   * Fire events in form of one batch<br>
   * fire all buffered events<br>
   * coalesce all TableEvents of same type and sort according to their type
   */
  private void processEventBuffer() {
    ArrayList<TableEvent> list = m_tableEventBuffer;
    m_tableEventBuffer = new ArrayList<TableEvent>();
    if (list.size() > 0) {
      HashMap<Integer, List<TableEvent>> coalesceMap = new HashMap<Integer, List<TableEvent>>();
      for (TableEvent e : list) {
        List<TableEvent> subList = coalesceMap.get(e.getType());
        if (subList == null) {
          subList = new ArrayList<TableEvent>();
          coalesceMap.put(e.getType(), subList);
        }
        subList.add(e);
      }
      TreeMap<Integer, TableEvent> sortedCoalescedMap = new TreeMap<Integer, TableEvent>();
      for (Map.Entry<Integer, List<TableEvent>> entry : coalesceMap.entrySet()) {
        int type = entry.getKey();
        List<TableEvent> subList = entry.getValue();
        int lastIndex = subList.size() - 1;
        switch (type) {
          case TableEvent.TYPE_ALL_ROWS_DELETED: {
            ArrayList<TableEvent> singleList = new ArrayList<TableEvent>(1);
            singleList.add(subList.get(lastIndex));// use last
            sortedCoalescedMap.put(10, coalesceTableEvents(singleList, false, true));
            break;
          }
          case TableEvent.TYPE_ROWS_DELETED: {
            sortedCoalescedMap.put(20, coalesceTableEvents(subList, false, true));// merge
            break;
          }
          case TableEvent.TYPE_ROWS_INSERTED: {
            sortedCoalescedMap.put(30, coalesceTableEvents(subList, true, false));// merge
            break;
          }
          case TableEvent.TYPE_ROWS_UPDATED: {
            sortedCoalescedMap.put(40, coalesceTableEvents(subList, true, false));// merge
            break;
          }
          case TableEvent.TYPE_COLUMN_HEADERS_UPDATED: {
            sortedCoalescedMap.put(60, coalesceTableEvents(subList, false, false));// merge
            break;
          }
          case TableEvent.TYPE_COLUMN_ORDER_CHANGED: {
            sortedCoalescedMap.put(70, coalesceTableEvents(subList, false, false));// merge
            break;
          }
          case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
            sortedCoalescedMap.put(80, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROW_ORDER_CHANGED: {
            sortedCoalescedMap.put(90, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROWS_DRAG_REQUEST: {
            sortedCoalescedMap.put(100, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROW_DROP_ACTION: {
            sortedCoalescedMap.put(110, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_HEADER_POPUP: {
            sortedCoalescedMap.put(130, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_EMPTY_SPACE_POPUP: {
            sortedCoalescedMap.put(140, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROW_POPUP: {
            sortedCoalescedMap.put(150, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROW_ACTION: {
            sortedCoalescedMap.put(160, subList.get(lastIndex));// use last
            break;
          }
          case TableEvent.TYPE_ROWS_SELECTED: {
            sortedCoalescedMap.put(170, subList.get(lastIndex));// use last
            break;
          }
          default: {
            sortedCoalescedMap.put(-type, subList.get(lastIndex));// use last
          }
        }
      }
      fireTableEventBatchInternal(sortedCoalescedMap.values().toArray(new TableEvent[0]));
    }
  }

  private TableEvent coalesceTableEvents(List<TableEvent> list, boolean includeExistingRows, boolean includeRemovedRows) {
    if (list.size() == 1) {
      return list.get(0);
    }
    else {
      TableEvent last = list.get(list.size() - 1);
      TableEvent ce = new TableEvent(last.getTable(), last.getType());
      //
      ce.setSortInMemoryAllowed(last.isSortInMemoryAllowed());
      ce.setDragObject(last.getDragObject());
      ce.setDropObject(last.getDropObject());
      ce.addPopupMenus(last.getPopupMenus());
      //columns
      HashSet<IColumn> colList = new HashSet<IColumn>();
      for (TableEvent t : list) {
        if (t.getColumns() != null) {
          colList.addAll(Arrays.asList(t.getColumns()));
        }
      }
      ce.setColumns(colList.toArray(new IColumn[0]));
      //rows
      HashSet<ITableRow> rowList = new HashSet<ITableRow>();
      for (TableEvent t : list) {
        if (t.getRowCount() > 0) {
          for (ITableRow row : t.getRows()) {
            if (row.getTable() == AbstractTable.this && includeExistingRows) {
              rowList.add(row);
            }
            else if (row.getTable() != AbstractTable.this && includeRemovedRows) {
              rowList.add(row);
            }
          }
        }
      }
      ce.setRows(rowList.toArray(new ITableRow[0]));
      //
      return ce;
    }
  }

  /**
   * do decoration and filtering later
   */
  private void enqueueDecorationTasks(ITableRow row) {
    if (row != null) {
      for (int i = 0; i < row.getCellCount(); i++) {
        IColumn column = getColumnSet().getColumn(i);
        // lookups
        if (column instanceof ISmartColumn) {
          ISmartColumn smartColumn = (ISmartColumn) column;
          if (smartColumn.getLookupCall() != null) {
            m_cellLookupBuffer.add(new P_CellLookup(row, smartColumn));
          }
        }
      }
      m_rowDecorationBuffer.add(row);
    }
  }

  /*
   * does not use setTableChanging()
   */
  private void applyRowDecorationsImpl(ITableRow tableRow) {
    // disable row changed trigger on row
    try {
      tableRow.setRowChanging(true);
      //
      // row decorator on table
      this.decorateRow(tableRow);
      // row decorator on columns
      ColumnSet cset = getColumnSet();
      for (int c = 0; c < tableRow.getCellCount(); c++) {
        // cell decorator on column
        IColumn<?> col = cset.getColumn(c);
        col.decorateCell(tableRow);
        // cell decorator on table
        this.decorateCell(tableRow, col);
      }
    }
    catch (Throwable t) {
      LOG.error("Error occured while applying row decoration", t);
    }
    finally {
      tableRow.setRowPropertiesChanged(false);
      tableRow.setRowChanging(false);
    }
  }

  private void applyLookupResult(InternalTableRow tableRow, int columnIndex, LookupRow[] result) {
    // disable row changed trigger on row
    try {
      tableRow.setRowChanging(true);
      //
      Cell cell = (Cell) tableRow.getCell(columnIndex);
      if (result.length == 1) {
        cell.setText(result[0].getText());
      }
      else if (result.length > 1) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
          if (i > 0) {
            if (isMultilineText()) buf.append("\n");
            else buf.append(", ");
          }
          buf.append(result[i].getText());
        }
        cell.setText(buf.toString());
      }
      else {
        cell.setText("");
      }
    }
    finally {
      tableRow.setRowPropertiesChanged(false);
      tableRow.setRowChanging(false);
    }
  }

  public ITableRow resolveRow(ITableRow row) {
    if (row == null) {
      return null;
    }
    if (!(row instanceof InternalTableRow)) {
      throw new IllegalArgumentException("only accept InternalTableRow, not " + (row != null ? row.getClass() : null));
    }
    // check owner
    if (row.getTable() == this) {
      return row;
    }
    else {
      return null;
    }
  }

  public ITableRow[] resolveRows(ITableRow[] rows) {
    if (rows == null) rows = new ITableRow[0];
    int mismatchCount = 0;
    for (int i = 0; i < rows.length; i++) {
      if (resolveRow(rows[i]) != rows[i]) {
        LOG.warn("could not resolve row " + rows[i]);
        mismatchCount++;
      }
    }
    if (mismatchCount > 0) {
      ITableRow[] resolvedRows = new ITableRow[rows.length - mismatchCount];
      int index = 0;
      for (int i = 0; i < rows.length; i++) {
        if (resolveRow(rows[i]) == rows[i]) {
          resolvedRows[index] = rows[i];
          index++;
        }
      }
      rows = resolvedRows;
    }
    return rows;
  }

  public boolean isHeaderVisible() {
    return propertySupport.getPropertyBool(PROP_HEADER_VISIBLE);
  }

  public void setHeaderVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_HEADER_VISIBLE, b);
  }

  public final void decorateCell(ITableRow row, IColumn col) {
    Cell cell = row.getCellForUpdate(col.getColumnIndex());
    decorateCellInternal(cell, row, col);
    try {
      execDecorateCell(cell, row, col);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  protected void decorateCellInternal(Cell view, ITableRow row, IColumn col) {
  }

  public final void decorateRow(ITableRow row) {
    decorateRowInternal(row);
    try {
      execDecorateRow(row);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
    }
  }

  protected void decorateRowInternal(ITableRow row) {
    // icon
    if (row.getIconId() == null) {
      String s = getDefaultIconId();
      if (s != null) {
        row.setIconId(s);
      }
    }
  }

  /**
   * reset/initialize all columns: visibilities, order, sorting, widths
   */
  @ConfigOperation
  @Order(90)
  protected void execResetColumns(boolean visibility, boolean order, boolean sorting, boolean widths) throws ProcessingException {
  }

  /**
   * Model Observer
   */
  public void addTableListener(TableListener listener) {
    m_listenerList.add(TableListener.class, listener);
  }

  public void removeTableListener(TableListener listener) {
    m_listenerList.remove(TableListener.class, listener);
  }

  public void addPriorityTableListener(TableListener listener) {
    m_listenerList.insert(TableListener.class, listener, 0);
  }

  private void fireRowsInserted(ITableRow[] rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_INSERTED, rows));
  }

  private void fireRowsUpdated(ITableRow[] rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_UPDATED, rows));
  }

  /**
   * Request to reload/replace table data with refreshed data
   */
  private void fireRowsDeleted(ITableRow[] rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_DELETED, rows));
  }

  private void fireAllRowsDeleted(ITableRow[] rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ALL_ROWS_DELETED, rows));
  }

  private void fireRowsSelected(ITableRow[] rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_SELECTED, rows));
  }

  private void fireRowClick(ITableRow row) {
    if (row != null) {
      try {
        //single observer for checkable tables
        if (isCheckable() && row.isEnabled() && isEnabled()) {
          row.setChecked(!row.isChecked());
        }
        //end single observer
        execRowClick(row);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
  }

  private void fireRowAction(ITableRow row) {
    if (row != null) {
      try {
        execRowAction(row);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
  }

  private void fireRowOrderChanged() {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROW_ORDER_CHANGED, getRows()));
  }

  private void fireRequestFocus() {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_REQUEST_FOCUS));
  }

  private void fireRowFilterChanged() {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROW_FILTER_CHANGED));
  }

  private TransferObject fireRowsDragRequest() {
    ITableRow[] rows = getSelectedRows();
    if (rows != null && rows.length > 0) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_DRAG_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getDragObject();
    }
    else {
      return null;
    }
  }

  private void fireRowDropAction(ITableRow row, TransferObject dropData) {
    ITableRow[] rows = null;
    if (row != null) rows = new ITableRow[]{row};
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_DROP_ACTION, rows);
    e.setDropObject(dropData);
    fireTableEventInternal(e);
  }

  private IMenu[] fireEmptySpacePopup() {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_EMPTY_SPACE_POPUP);
    fireTableEventInternal(e);
    return e.getPopupMenus();
  }

  private IMenu[] fireRowPopup() {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_POPUP, getSelectedRows());
    fireTableEventInternal(e);
    return e.getPopupMenus();
  }

  private void addLocalPopupMenus(TableEvent e) {
    boolean multiSelect = getSelectedRowCount() >= 2;
    boolean allRowsEnabled = true;
    for (ITableRow row : getSelectedRows()) {
      if (!row.isEnabled()) {
        allRowsEnabled = false;
        break;
      }
    }
    IMenu[] a = getMenus();
    for (IMenu menu : a) {
      IMenu validMenu = null;
      switch (e.getType()) {
        case TableEvent.TYPE_HEADER_POPUP:
        case TableEvent.TYPE_EMPTY_SPACE_POPUP: {
          if (menu.isEmptySpaceAction()) {
            validMenu = menu;
          }
          break;
        }
        case TableEvent.TYPE_ROW_POPUP: {
          if (multiSelect) {
            if (menu.isMultiSelectionAction()) {
              validMenu = menu;
            }
          }
          else {
            if (menu.isSingleSelectionAction()) {
              validMenu = menu;
            }
          }
          break;
        }
      }
      if (validMenu != null && validMenu.isInheritAccessibility()) {
        if (!isEnabled() || !allRowsEnabled) {
          validMenu = null;
        }
      }
      if (validMenu != null) {
        validMenu.prepareAction();
        if (validMenu.isVisible()) {
          e.addPopupMenu(validMenu);
        }
      }
    }
  }

  private IMenu[] fireHeaderPopup() {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_HEADER_POPUP);
    fireTableEventInternal(e);
    // single observer for column organization menus
    if (getTableCustomizer() != null) {
      if (e.getPopupMenuCount() > 0) {
        e.addPopupMenu(new MenuSeparator());
      }
      for (IMenu m : new IMenu[]{new AddCustomColumnMenu(this), new ModifyCustomColumnMenu(this), new RemoveCustomColumnMenu(this)}) {
        m.prepareAction();
        if (m.isVisible()) {
          e.addPopupMenu(m);
        }
      }
    }
    if (e.getPopupMenuCount() > 0) {
      e.addPopupMenu(new MenuSeparator());
    }
    for (IMenu m : new IMenu[]{new ResetColumnsMenu(this), new OrganizeColumnsMenu(this), new ColumnFilterMenu(this), new CopyWidthsOfColumnsMenu(this)}) {
      m.prepareAction();
      if (m.isVisible()) {
        e.addPopupMenu(m);
      }
    }
    return e.getPopupMenus();
  }

  // main handler
  protected void fireTableEventInternal(TableEvent e) {
    if (isTableChanging()) {
      // buffer the event for later batch firing
      m_tableEventBuffer.add(e);
    }
    else {
      EventListener[] listeners = m_listenerList.getListeners(TableListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          try {
            ((TableListener) listeners[i]).tableChanged(e);
          }
          catch (Throwable t) {
            LOG.error("fire " + e, t);
          }
        }
      }
    }
  }

  // batch handler
  private void fireTableEventBatchInternal(TableEvent[] batch) {
    if (isTableChanging()) {
      LOG.error("Illegal State: firing a event batch while table is changing");
    }
    else {
      if (batch.length > 0) {
        EventListener[] listeners = m_listenerList.getListeners(TableListener.class);
        if (listeners != null && listeners.length > 0) {
          for (int i = 0; i < listeners.length; i++) {
            ((TableListener) listeners[i]).tableChangedBatch(batch);
          }
        }
      }
    }
  }

  protected boolean handleKeyStroke(String keyName, char keyChar) {
    if (keyName == null) {
      return false;
    }
    keyName = keyName.toLowerCase();
    // check if there is no menu keystroke with that name
    for (IMenu m : getMenus()) {
      if (m.getKeyStroke() != null && m.getKeyStroke().equalsIgnoreCase(keyName)) {
        return false;
      }
    }
    // check if there is no keystroke with that name (ticket 78234)
    for (IKeyStroke k : getKeyStrokes()) {
      if (k.getKeyStroke() != null && k.getKeyStroke().equalsIgnoreCase(keyName)) {
        return false;
      }
    }
    if (keyChar > ' ' && (!keyName.contains("control")) && (!keyName.contains("ctrl")) && (!keyName.contains("alt"))) {
      // select first/next line with corresponding character
      String newText = "" + Character.toLowerCase(keyChar);
      m_keyStrokeBuffer.append(newText);
      String prefix = m_keyStrokeBuffer.getText();

      IColumn col = getContextColumn();
      if (col == null) {
        IColumn[] sortCols = getColumnSet().getSortColumns();
        if (sortCols.length > 0) {
          col = sortCols[sortCols.length - 1];
        }
        else {
          TreeMap<CompositeObject, IColumn> sortMap = new TreeMap<CompositeObject, IColumn>();
          int index = 0;
          for (IColumn c : getColumnSet().getVisibleColumns()) {
            if (c.getDataType() == String.class) {
              sortMap.put(new CompositeObject(1, index), c);
            }
            else if (c.getDataType() == Boolean.class) {
              sortMap.put(new CompositeObject(3, index), c);
            }
            else {
              sortMap.put(new CompositeObject(2, index), c);
            }
            index++;
          }
          if (sortMap.size() > 0) {
            col = sortMap.get(sortMap.firstKey());
          }
        }
      }
      if (col != null) {
        int colIndex = col.getColumnIndex();
        String pattern = StringUtility.toRegExPattern(prefix.toLowerCase());
        pattern = pattern + ".*";
        if (LOG.isInfoEnabled()) LOG.info("finding regex:" + pattern + " in column " + getColumnSet().getColumn(colIndex).getHeaderCell().getText());
        // loop over values and find matching one
        int rowCount = getRowCount();
        ITableRow selRow = getSelectedRow();
        int startIndex = 0;
        if (selRow != null) {
          if (prefix.length() <= 1) {
            startIndex = selRow.getRowIndex() + 1;
          }
          else {
            startIndex = selRow.getRowIndex();
          }
        }
        for (int i = 0; i < rowCount; i++) {
          ITableRow row = m_rows.get((startIndex + i) % rowCount);
          String text = row.getCell(colIndex).getText();
          if (text != null && text.toLowerCase().matches(pattern)) {
            // handled
            selectRow(row, false);
            return true;
          }
        }
      }
    }
    return false;
  }

  public ITableUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /*
   * UI Notifications
   */
  protected class P_TableUIFacade implements ITableUIFacade {
    private int m_uiProcessorCount = 0;

    protected void pushUIProcessor() {
      m_uiProcessorCount++;
    }

    protected void popUIProcessor() {
      m_uiProcessorCount--;
    }

    public boolean isUIProcessing() {
      return m_uiProcessorCount > 0;
    }

    public void fireRowClickFromUI(ITableRow row) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        if (row != null) {
          fireRowClick(resolveRow(row));
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public void fireRowActionFromUI(ITableRow row) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        if (row != null) {
          fireRowAction(row);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public IMenu[] fireRowPopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fireRowPopup();
      }
      finally {
        popUIProcessor();
      }
    }

    public IMenu[] fireEmptySpacePopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fireEmptySpacePopup();
      }
      finally {
        popUIProcessor();
      }
    }

    public IMenu[] fireHeaderPopupFromUI() {
      try {
        pushUIProcessor();
        //
        return fireHeaderPopup();
      }
      finally {
        popUIProcessor();
      }
    }

    public boolean fireKeyTypedFromUI(String keyStrokeText, char keyChar) {
      try {
        pushUIProcessor();
        //
        return handleKeyStroke(keyStrokeText, keyChar);
      }
      finally {
        popUIProcessor();
      }
    }

    public void fireColumnMovedFromUI(IColumn c, int toViewIndex) {
      try {
        pushUIProcessor();
        //
        c = getColumnSet().resolveColumn(c);
        if (c != null) {
          getColumnSet().moveColumnToVisibleIndex(c.getColumnIndex(), toViewIndex);
          ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public void setColumnWidthFromUI(IColumn c, int newWidth) {
      try {
        pushUIProcessor();
        //
        c = getColumnSet().resolveColumn(c);
        if (c != null) {
          c.setWidthInternal(newWidth);
          ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public void fireHeaderSortFromUI(IColumn c, boolean multiSort) {
      try {
        pushUIProcessor();
        //
        if (isSortEnabled()) {
          c = getColumnSet().resolveColumn(c);
          if (c != null) {
            getColumnSet().handleSortEvent(c, multiSort);
            ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
            sort();
          }
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public void setSelectedRowsFromUI(ITableRow[] rows) {
      try {
        pushUIProcessor();
        //
        HashSet<ITableRow> requestedRows = new HashSet<ITableRow>(Arrays.asList(resolveRows(rows)));
        ArrayList<ITableRow> validRows = new ArrayList<ITableRow>();
        // add existing selected rows that are masked by filter
        for (ITableRow row : getSelectedRows()) {
          if (!row.isFilterAccepted()) {
            validRows.add(row);
          }
        }
        // remove all filtered from requested
        requestedRows.removeAll(validRows);
        // add remainder
        for (ITableRow row : requestedRows) {
          validRows.add(row);
        }
        selectRows(validRows.toArray(new ITableRow[validRows.size()]), false);
      }
      finally {
        popUIProcessor();
      }
    }

    public TransferObject fireRowsDragRequestFromUI() {
      try {
        pushUIProcessor();
        //
        return fireRowsDragRequest();
      }
      finally {
        popUIProcessor();
      }
    }

    public void fireRowDropActionFromUI(ITableRow row, TransferObject dropData) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        fireRowDropAction(row, dropData);
      }
      finally {
        popUIProcessor();
      }
    }

    public void fireHyperlinkActionFromUI(ITableRow row, IColumn col, URL url) {
      try {
        pushUIProcessor();
        //
        doHyperlinkAction(resolveRow(row), col, url);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      finally {
        popUIProcessor();
      }
    }

    public void setContextColumnFromUI(IColumn col) {
      try {
        pushUIProcessor();
        //
        if (col != null && col.getTable() != AbstractTable.this) {
          col = null;
        }
        setContextColumn(col);
      }
      finally {
        popUIProcessor();
      }
    }

    public IFormField prepareCellEditFromUI(ITableRow row, IColumn col) {
      try {
        pushUIProcessor();
        //
        m_editContext = null;
        row = resolveRow(row);
        if (row != null && col != null) {
          try {
            IFormField f = col.prepareEdit(row);
            if (f != null) {
              m_editContext = new P_CellEditorContext(row, col, f);
            }
            return f;
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
          catch (Throwable t) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
          }
        }
      }
      finally {
        popUIProcessor();
      }
      return null;
    }

    public void completeCellEditFromUI() {
      try {
        pushUIProcessor();
        //
        if (m_editContext != null) {
          try {
            m_editContext.getColumn().completeEdit(m_editContext.getRow(), m_editContext.getFormField());
          }
          catch (ProcessingException e) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(e);
          }
          catch (Throwable t) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
          }
          finally {
            m_editContext = null;
          }
        }
      }
      finally {
        popUIProcessor();
      }
    }

    public void cancelCellEditFromUI() {
      try {
        pushUIProcessor();
        //
        m_editContext = null;
      }
      finally {
        popUIProcessor();
      }
    }

  }

  private class P_CellLookup {
    private final ITableRow m_row;
    private final ISmartColumn m_column;

    public P_CellLookup(ITableRow row, ISmartColumn col) {
      m_row = row;
      m_column = col;
    }

    public ITableRow getRow() {
      return m_row;
    }

    public ISmartColumn getColumn() {
      return m_column;
    }
  }// end private class

  private class P_TableRowBuilder extends AbstractTableRowBuilder {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getColumnSet());
    }

  }

  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_EMPTY_SPACE_POPUP: {
          // single observer for table-defined menus
          addLocalPopupMenus(e);
          break;
        }
        case TableEvent.TYPE_HEADER_POPUP: {
          // single observer for table-owned menus
          addLocalPopupMenus(e);
          break;
        }
        case TableEvent.TYPE_ROW_POPUP: {
          // single observer for table-defined menus
          addLocalPopupMenus(e);
          break;
        }
        case TableEvent.TYPE_ROWS_SELECTED: {
          // single observer exec
          try {
            execRowsSelected(e.getRows());
          }
          catch (ProcessingException ex) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
          }
          catch (Throwable t) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
          }
          break;
        }
      }
    }
  }

  private static class P_CellEditorContext {
    private final ITableRow m_row;
    private final IColumn<?> m_column;
    private final IFormField m_formField;

    public P_CellEditorContext(ITableRow row, IColumn<?> col, IFormField f) {
      m_row = row;
      m_column = col;
      m_formField = f;
    }

    public ITableRow getRow() {
      return m_row;
    }

    public IColumn<?> getColumn() {
      return m_column;
    }

    public IFormField getFormField() {
      return m_formField;
    }
  }
}
