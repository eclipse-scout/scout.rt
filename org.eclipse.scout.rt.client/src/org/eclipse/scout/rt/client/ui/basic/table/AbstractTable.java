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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.action.ActionFinder;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ColumnFilterMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.DefaultTableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.AddCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.RemoveCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.internal.InternalTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.menus.CopyWidthsOfColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
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
  private ColumnSet m_columnSet;
  /**
   * synchronized list
   */
  private final List<ITableRow> m_rows;
  private final Object m_cachedRowsLock;
  private ITableRow[] m_cachedRows;
  private final HashMap<CompositeObject, ITableRow> m_deletedRows;
  private TreeSet<ITableRow/* ordered by rowIndex */> m_selectedRows = new TreeSet<ITableRow>(new RowIndexComparator());
  private IMenu[] m_menus;
  private Map<Class<?>, Class<? extends IMenu>> m_menuReplacementMapping;
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
  private Set<ITableRow> m_rowValidty;
  //checkable table
  private IBooleanColumn m_checkableColumn;
  //auto filter
  private final Object m_cachedFilteredRowsLock;
  private ITableRow[] m_cachedFilteredRows;
  private ITableColumnFilterManager m_columnFilterManager;
  private ITableCustomizer m_tableCustomizer;
  private IEventHistory<TableEvent> m_eventHistory;
  // only do one action at a time
  private boolean m_actionRunning;

  public AbstractTable() {
    this(true);
  }

  public AbstractTable(boolean callInitializer) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerTable(this);
    }
    m_rowValidty = new HashSet<ITableRow>();
    m_cachedRowsLock = new Object();
    m_cachedFilteredRowsLock = new Object();
    m_rows = Collections.synchronizedList(new ArrayList<ITableRow>(1));
    m_deletedRows = new HashMap<CompositeObject, ITableRow>();
    m_keyStrokeBuffer = new KeyStrokeBuffer(500L);
    m_rowFilters = new ArrayList<ITableRowFilter>(1);
    m_initLock = new OptimisticLock();
    m_actionRunning = false;
    //add single observer listener
    addTableListener(new P_TableListener());
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    initConfig();
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass()) + ITypeWithClassId.ID_CONCAT_SYMBOL + getContainer().classId();
  }

  /*
   * Configuration
   */

  /**
   * Configures the title of this table. The title of the table is rarely used because a table is usually surrounded by
   * an {@link AbstractTableField} having its own title / label.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return Title of this table.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredTitle() {
    return null;
  }

  /**
   * Configures the default icon for this table. The default icon is used for each row in the table.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(20)
  protected String getConfiguredDefaultIconId() {
    return null;
  }

  /**
   * Configures whether only one row can be selected at once in this table.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if more then one row in this table can be selected at once, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  protected boolean getConfiguredMultiSelect() {
    return true;
  }

  /**
   * Configures whether only one row can be checked in this table. This configuration is only useful if
   * {@link #getConfiguredCheckable()} is {@code true} .
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if more then one row in this table can be checked, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(32)
  protected boolean getConfiguredMultiCheck() {
    return true;
  }

  /**
   * Configures the default menu that is used on the ENTER (action key) or the double click on a table row.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return The default menu to use.
   */
  @ConfigProperty(ConfigProperty.MENU_CLASS)
  @Order(35)
  protected Class<? extends IMenu> getConfiguredDefaultMenu() {
    return null;
  }

  /**
   * Interception method used for customizing the default menu. Should be used by the framework only.
   * 
   * @since 3.8.1
   */
  protected Class<? extends IMenu> getDefaultMenuInternal() {
    return getConfiguredDefaultMenu();
  }

  /**
   * Configures whether deleted rows are automatically erased or cached for later processing (service deletion).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if deleted rows are automatically erased, {@code false} if deleted nodes are cached for later
   *         processing.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(50)
  protected boolean getConfiguredAutoDiscardOnDelete() {
    return false;
  }

  /**
   * Configures whether sort is enabled for this table. If sort is enabled, the table rows are sorted based on their
   * sort index (see {@link AbstractColumn#getConfiguredSortIndex()}) and the user might change the sorting at run time.
   * If sort is disabled, the table rows are not sorted and the user cannot change the sorting.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if sort is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredSortEnabled() {
    return true;
  }

  /**
   * Configures whether the header row is visible. The header row contains the titles of each column.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if the header row is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredHeaderVisible() {
    return true;
  }

  /**
   * Configures whether the columns are auto resized. If true, all columns are resized so that the table never needs
   * horizontal scrolling. This is especially useful for tables inside a form.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if the columns are auto resized, {@code false} otherwise.
   * @see {@link AbstractColumn#getConfiguredWidth()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredAutoResizeColumns() {
    return false;
  }

  /**
   * Configures whether the table supports multiline text. If multiline text is supported and a string column has set
   * the {@link AbstractStringColumn#getConfiguredTextWrap()} property to true, the text is wrapped and uses two or more
   * lines.
   * <p>
   * Subclasses can override this method. Default is {@code false}. If the method is not overridden and at least one
   * string column has set the {@link AbstractStringColumn#getConfiguredTextWrap()} to true, the multiline property is
   * set automatically to true.
   * 
   * @return {@code true} if the table supports multiline text, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredMultilineText() {
    return false;
  }

  /**
   * Configures the row height hint. This is a hint for the UI if and only if it is not capable of having variable table
   * row height based on cell contents (such as rap/rwt or swt).
   * <p>
   * This property is interpreted in different manner for each GUI port:
   * <ul>
   * <li>Swing: The property is ignored.
   * <li>SWT: Used as the maximal row height.
   * <li>rap/rwt: Used as the fixed row height in multiline tables.
   * </ul>
   * This hint defines the table row height in pixels being used as the fixed row height for all table rows of this
   * table.
   * </p>
   * Subclasses can override this method. Default is {@code -1}.
   * 
   * @return Table row height hint in pixels.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(92)
  protected int getConfiguredRowHeightHint() {
    return -1;
  }

  /**
   * Configures whether the table is checkable.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if the table is checkable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredCheckable() {
    return false;
  }

  /**
   * Configures the checkable column. The checkable column represents the check state of the row, i.e. if it is checked
   * or not. If no checkable column is configured, only the row itself represents if the row was checked or not.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   * 
   * @return A column class extending {@link AbstractBooleanColumn} that represents the row check state.
   */
  @ConfigProperty(ConfigProperty.TABLE_COLUMN)
  @Order(102)
  protected Class<? extends AbstractBooleanColumn> getConfiguredCheckableColumn() {
    return null;
  }

  /**
   * Configures the drop support of this table.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   * 
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the drag support of this table.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   * 
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  protected int getConfiguredDragType() {
    return 0;
  }

  /**
   * Configures whether the keyboard can be used for navigation in table. When activated, the user can click on a column
   * in the table. Now starting to type some letters, the row matching the typed letters in the column will be selected.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   * 
   * @return {@code true} if the keyboard navigation is supported, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredKeyboardNavigation() {
    return true;
  }

  /**
   * Configures whether the table always scrolls to the selection. When activated and the selection in a table changes,
   * the table is scrolled to the selection so that the selected row is visible.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   * 
   * @return {@code true} if the table scrolls to the selection, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  protected boolean getConfiguredScrollToSelection() {
    return false;
  }

  /**
   * Called after a drag operation was executed on one or several table rows.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param rows
   *          Table rows that were dragged.
   * @return A transferable object representing the given rows.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(10)
  protected TransferObject execDrag(ITableRow[] rows) throws ProcessingException {
    return null;
  }

  /**
   * Called after a drop operation was executed on the table.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param row
   *          Table row on which the transferable object was dropped (row may be null for empty space drop).
   * @param t
   *          Transferable object that was dropped on the row.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(20)
  protected void execDrop(ITableRow row, TransferObject t) throws ProcessingException {
  }

  /**
   * Called by a <code>CTRL-C</code> event on the table to copy the given rows into the clipboard.
   * <p>
   * Subclasses can override this method. The default creates a {@link TextTransferObject} of the table content (HTML
   * table).
   * 
   * @param rows
   *          The selected table rows to copy.
   * @return A transferable object representing the given rows or null to not populate the clipboard.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(30)
  protected TransferObject execCopy(ITableRow[] rows) throws ProcessingException {
    if (rows.length == 0) {
      return null;
    }

    StringBuilder plainText = new StringBuilder();
    StringBuilder htmlText = new StringBuilder("<html>");
    // Adding the following MS-office specific style information will cause Excel
    // to put all line-break-delimited entries of a <td> cell into a single Excel cell,
    // instead of one sub-cell for each <br />
    htmlText.append("<head><style type=\"text/css\"> br {mso-data-placement:same-cell;} </style></head>");
    htmlText.append("<body><table border=\"0\">");

    IColumn<?>[] columns = getColumnSet().getVisibleColumns();
    Pattern patternHtmlCheck = Pattern.compile(".*?<\\s*html.*?>.*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    Pattern patternBodyContent = Pattern.compile("<\\s*body.*?>(.*?)<\\s*/\\s*body\\s*>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    boolean firstRow = true;
    for (ITableRow row : rows) {
      // text/html
      htmlText.append("<tr>");
      // text/plain
      if (!firstRow) {
        plainText.append(System.getProperty("line.separator"));
      }

      boolean firstColumn = true;
      for (IColumn<?> column : columns) {
        String text;
        if (column instanceof IBooleanColumn) {
          boolean value = BooleanUtility.nvl(((IBooleanColumn) column).getValue(row), false);
          text = value ? "X" : "";
        }
        else {
          text = StringUtility.emptyIfNull(row.getCell(column).getText());
        }

        // text/plain
        if (!firstColumn) {
          plainText.append("\t");
        }
        plainText.append(StringUtility.emptyIfNull(StringUtility.unwrapText(text)));

        // text/html
        String html = null;
        if (patternHtmlCheck.matcher(text).matches()) {
          // ensure proper HTML and extract body content
          Matcher matcher = patternBodyContent.matcher(HTMLUtility.cleanupHtml(text, false, false, null));
          if (matcher.find()) {
            html = matcher.group(1);
          }
        }
        if (html == null) {
          html = StringUtility.htmlEncode(text);
        }
        htmlText.append("<td>");
        htmlText.append(html);
        htmlText.append("</td>");
        firstColumn = false;
      }
      htmlText.append("</tr>");
      firstRow = false;
    }
    htmlText.append("</table></body></html>");

    TextTransferObject transferObject = new TextTransferObject(plainText.toString(), htmlText.toString());
    return transferObject;
  }

  /**
   * Called after the table content changed, rows were added, removed or changed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(40)
  protected void execContentChanged() throws ProcessingException {
  }

  /**
   * Called after {@link AbstractColumn#execDecorateCell(Cell,ITableRow)} on the column to decorate the cell.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateCell(Cell view, ITableRow row, IColumn<?> col) throws ProcessingException {
  }

  /**
   * Called during initialization of this table, after the columns were initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(60)
  protected void execInitTable() throws ProcessingException {
  }

  /**
   * Called when this table is disposed, after the columns were disposed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(70)
  protected void execDisposeTable() throws ProcessingException {
  }

  /**
   * Called when the user clicks on a row in this table.
   * <p>
   * Subclasses can override this method. The default fires a {@link TableEvent#TYPE_ROW_CLICK} event.
   * 
   * @param Row
   *          that was clicked (never null).
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(80)
  protected void execRowClick(ITableRow row) throws ProcessingException {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_CLICK, new ITableRow[]{row});
    fireTableEventInternal(e);
  }

  /**
   * Called when the row has been activated.
   * <p>
   * Subclasses can override this method. The default opens the configured default menu or if no default menu is
   * configured, fires a {@link TableEvent#TYPE_ROW_ACTION} event.
   * 
   * @param Row
   *          that was activated (never null).
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(90)
  protected void execRowAction(ITableRow row) throws ProcessingException {
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null) {
      try {
        runMenu(defaultMenuType);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
      }
    }
    else {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_ACTION, new ITableRow[]{row});
      fireTableEventInternal(e);
    }
  }

  /**
   * Called when one or more rows are selected.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param rows
   *          that were selected.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(100)
  protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
  }

  /**
   * Called when the row is going to be decorated.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param row
   *          that is going to be decorated.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(110)
  protected void execDecorateRow(ITableRow row) throws ProcessingException {
  }

  /**
   * Called when a hyperlink is used within the table. The hyperlink's table row is the selected row and the column is
   * the context column ({@link #getContextColumn()}).
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param url
   *          Hyperlink to process.
   * @param path
   *          Path of URL ({@link URL#getPath()}).
   * @param local
   *          {@code true} if the url is not a valid external url but a local model url (http://local/...)
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(120)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
  }

  private Class<? extends IMenu>[] getConfiguredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class[] filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    Class<IMenu>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  private Class<? extends IColumn>[] getConfiguredColumns() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IColumn>[] foca = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, IColumn.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  private Class<? extends IKeyStroke>[] getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    Class<IKeyStroke>[] fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected void initConfig() {
    m_eventHistory = createEventHistory();
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
    setRowHeightHint(getConfiguredRowHeightHint());
    setKeyboardNavigation(getConfiguredKeyboardNavigation());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setScrollToSelection(getConfiguredScrollToSelection());
    if (getTableCustomizer() == null) {
      setTableCustomizer(createTableCustomizer());
    }
    // columns
    createColumnsInternal();
    // menus
    ArrayList<IMenu> menuList = new ArrayList<IMenu>();
    Class<? extends IMenu>[] ma = getConfiguredMenus();
    Map<Class<?>, Class<? extends IMenu>> replacements = ConfigurationUtility.getReplacementMapping(ma);
    if (!replacements.isEmpty()) {
      m_menuReplacementMapping = replacements;
    }
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
      LOG.error("error occured while dynamically contributing menus.", e);
    }
    //set container on menus
    for (IMenu menu : menuList) {
      menu.setContainerInternal(this);
    }

    m_menus = menuList.toArray(new IMenu[menuList.size()]);
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
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null || ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowAction", new Class[]{ITableRow.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() throws ProcessingException {
          fireRowAction(getSelectedRow());
        }
      });
    }
    setKeyStrokes(ksList.toArray(new IKeyStroke[ksList.size()]));
    // add Convenience observer for drag & drop callbacks and event history
    addTableListener(new TableAdapter() {
      @Override
      public void tableChanged(TableEvent e) {
        //event history
        IEventHistory<TableEvent> h = getEventHistory();
        if (h != null) {
          h.notifyEvent(e);
        }
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
          case TableEvent.TYPE_ROWS_COPY_REQUEST: {
            if (e.getCopyObject() == null) {
              try {
                e.setCopyObject(execCopy(e.getRows()));
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

  private void initColumnsInternal() {
    for (IColumn<?> c : getColumnSet().getColumns()) {
      try {
        c.initColumn();
      }
      catch (Throwable t) {
        LOG.error("column " + c, t);
      }
    }
    getColumnSet().initialize();
  }

  private void disposeColumnsInternal() {
    for (IColumn<?> c : getColumnSet().getColumns()) {
      try {
        c.disposeColumn();
      }
      catch (Throwable t) {
        LOG.error("column " + c, t);
      }
    }
  }

  private void createColumnsInternal() {
    Class<? extends IColumn>[] ca = getConfiguredColumns();
    ArrayList<IColumn<?>> colList = new ArrayList<IColumn<?>>();
    for (int i = 0; i < ca.length; i++) {
      try {
        IColumn<?> column = ConfigurationUtility.newInnerInstance(this, ca[i]);
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
    if (getConfiguredCheckableColumn() != null) {
      AbstractBooleanColumn checkableColumn = getColumnSet().getColumnByClass(getConfiguredCheckableColumn());
      setCheckableColumn(checkableColumn);
    }
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

  @Override
  public String getUserPreferenceContext() {
    return m_userPreferenceContext;
  }

  @Override
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
  @Override
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
    initColumnsInternal();
    if (getColumnFilterManager() == null) {
      setColumnFilterManager(createColumnFilterManager());
    }
  }

  @Override
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
    disposeColumnsInternal();
  }

  @Override
  public void doHyperlinkAction(ITableRow row, IColumn<?> col, URL url) throws ProcessingException {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (row != null) {
          selectRow(row);
        }
        if (col != null) {
          setContextColumn(col);
        }
        execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals(LOCAL_URL_HOST));
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  @Override
  public ITableRowFilter[] getRowFilters() {
    return m_rowFilters.toArray(new ITableRowFilter[m_rowFilters.size()]);
  }

  @Override
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

  @Override
  public void removeRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      m_rowFilters.remove(filter);
      applyRowFilters();
    }
  }

  @Override
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

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  @Override
  public boolean isAutoResizeColumns() {
    return propertySupport.getPropertyBool(PROP_AUTO_RESIZE_COLUMNS);
  }

  @Override
  public void setAutoResizeColumns(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_RESIZE_COLUMNS, b);
  }

  @Override
  public ColumnSet getColumnSet() {
    return m_columnSet;
  }

  @Override
  public int getColumnCount() {
    return getColumnSet().getColumnCount();
  }

  @Override
  public IColumn<?>[] getColumns() {
    return getColumnSet().getColumns();
  }

  @Override
  public String[] getColumnNames() {
    String[] a = new String[getColumnCount()];
    for (int i = 0; i < a.length; i++) {
      a[i] = getColumnSet().getColumn(i).getHeaderCell().getText();
    }
    return a;
  }

  @Override
  public int getVisibleColumnCount() {
    return getColumnSet().getVisibleColumnCount();
  }

  @Override
  public IHeaderCell getVisibleHeaderCell(int visibleColumnIndex) {
    return getHeaderCell(getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  @Override
  public IHeaderCell getHeaderCell(int columnIndex) {
    return getHeaderCell(getColumnSet().getColumn(columnIndex));
  }

  @Override
  public IHeaderCell getHeaderCell(IColumn<?> col) {
    return col.getHeaderCell();
  }

  @Override
  public ICell getVisibleCell(int rowIndex, int visibleColumnIndex) {
    return getVisibleCell(getRow(rowIndex), visibleColumnIndex);
  }

  @Override
  public ICell getVisibleCell(ITableRow row, int visibleColumnIndex) {
    return getCell(row, getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  @Override
  public ICell getCell(int rowIndex, int columnIndex) {
    return getCell(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  @Override
  public ICell getSummaryCell(int rowIndex) {
    return getSummaryCell(getRow(rowIndex));
  }

  @Override
  public ICell getSummaryCell(ITableRow row) {
    IColumn<?>[] a = getColumnSet().getSummaryColumns();
    if (a.length == 0) {
      IColumn<?> col = getColumnSet().getFirstDefinedVisibileColumn();
      if (col != null) {
        a = new IColumn<?>[]{col};
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
      for (IColumn<?> c : a) {
        if (b.length() > 0) {
          b.append(" ");
        }
        b.append(getCell(row, c).getText());
      }
      cell.setText(b.toString());
      return cell;
    }
  }

  @Override
  public ICell getCell(ITableRow row, IColumn<?> col) {
    row = resolveRow(row);
    if (row == null || col == null) {
      return null;
    }
    return row.getCell(col.getColumnIndex());
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return isCellEditable(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  @Override
  public boolean isCellEditable(ITableRow row, int visibleColumnIndex) {
    return isCellEditable(row, getColumnSet().getVisibleColumn(visibleColumnIndex));
  }

  /**
   * Note that this is not a java bean method and thus not thread-safe
   */
  @Override
  public boolean isCellEditable(ITableRow row, IColumn<?> column) {
    return row != null && column != null && column.isVisible() && column.isCellEditable(row);
  }

  @Override
  public Object getProperty(String name) {
    return propertySupport.getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) {
    propertySupport.setProperty(name, value);
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public boolean isCheckable() {
    return propertySupport.getPropertyBool(PROP_CHECKABLE);
  }

  @Override
  public void setCheckable(boolean b) {
    propertySupport.setPropertyBool(PROP_CHECKABLE, b);
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  @Override
  public boolean isMultilineText() {
    return propertySupport.getPropertyBool(PROP_MULTILINE_TEXT);
  }

  @Override
  public void setMultilineText(boolean on) {
    propertySupport.setPropertyBool(PROP_MULTILINE_TEXT, on);
  }

  @Override
  public int getRowHeightHint() {
    return propertySupport.getPropertyInt(PROP_ROW_HEIGHT_HINT);
  }

  @Override
  public void setRowHeightHint(int h) {
    propertySupport.setPropertyInt(PROP_ROW_HEIGHT_HINT, h);
  }

  @Override
  public boolean isInitialMultilineText() {
    return m_initialMultiLineText;
  }

  @Override
  public void setInitialMultilineText(boolean on) {
    m_initialMultiLineText = on;
  }

  @Override
  public boolean hasKeyboardNavigation() {
    return propertySupport.getPropertyBool(PROP_KEYBOARD_NAVIGATION);
  }

  @Override
  public void setKeyboardNavigation(boolean on) {
    propertySupport.setPropertyBool(PROP_KEYBOARD_NAVIGATION, on);
  }

  @Override
  public boolean isMultiSelect() {
    return propertySupport.getPropertyBool(PROP_MULTI_SELECT);
  }

  @Override
  public void setMultiSelect(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_SELECT, b);
  }

  @Override
  public boolean isMultiCheck() {
    return propertySupport.getPropertyBool(PROP_MULTI_CHECK);
  }

  @Override
  public void setMultiCheck(boolean b) {
    propertySupport.setPropertyBool(PROP_MULTI_CHECK, b);
  }

  @Override
  public IBooleanColumn getCheckableColumn() {
    return m_checkableColumn;
  }

  @Override
  public void setCheckableColumn(IBooleanColumn checkableColumn) {
    m_checkableColumn = checkableColumn;
  }

  @Override
  public boolean isAutoDiscardOnDelete() {
    return m_autoDiscardOnDelete;
  }

  @Override
  public void setAutoDiscardOnDelete(boolean on) {
    m_autoDiscardOnDelete = on;
  }

  @Override
  public boolean isTableInitialized() {
    return m_initialized;
  }

  @Override
  public boolean isTableChanging() {
    return m_tableChanging > 0;
  }

  @Override
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
      // all calls to further methods are wrapped into a try-catch block so that the change counters are adjusted correctly
      if (m_tableChanging > 0) {
        Throwable saveEx = null;
        if (m_tableChanging == 1) {
          try {
            //will be going to zero, but process decorations here, so events are added to the event buffer
            processDecorationBuffer();
            if (!m_sortValid) {
              sort();
            }
          }
          catch (Throwable t) {
            saveEx = t;
          }
        }
        m_tableChanging--;
        if (m_tableChanging == 0) {
          try {
            processEventBuffer();
          }
          catch (Throwable t) {
            if (saveEx == null) {
              saveEx = t;
            }
          }
          try {
            propertySupport.setPropertiesChanging(false);
          }
          catch (Throwable t) {
            if (saveEx == null) {
              saveEx = t;
            }
          }
        }
        if (saveEx == null) {
          return;
        }
        else if (saveEx instanceof RuntimeException) {
          throw (RuntimeException) saveEx;
        }
        else if (saveEx instanceof Error) {
          throw (Error) saveEx;
        }
      }
    }
  }

  @Override
  public IKeyStroke[] getKeyStrokes() {
    IKeyStroke[] keyStrokes = (IKeyStroke[]) propertySupport.getProperty(PROP_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new IKeyStroke[0];
    }
    return keyStrokes;
  }

  @Override
  public void setKeyStrokes(IKeyStroke[] keyStrokes) {
    propertySupport.setProperty(PROP_KEY_STROKES, keyStrokes);
  }

  @Override
  public void requestFocus() {
    fireRequestFocus();
  }

  @Override
  public void requestFocusInCell(IColumn<?> column, ITableRow row) {
    if (isCellEditable(row, column)) {
      fireRequestFocusInCell(column, row);
    }
  }

  @Override
  public ITableRowDataMapper createTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
    return execCreateTableRowDataMapper(rowType);
  }

  /**
   * Creates a {@link TableRowDataMapper} that is used for reading and writing data from the given
   * {@link AbstractTableRowData} type.
   * 
   * @param rowType
   * @return
   * @throws ProcessingException
   * @since 3.8.2
   */
  @ConfigOperation
  @Order(130)
  protected ITableRowDataMapper execCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
    return new TableRowDataMapper(rowType, getColumnSet());
  }

  @Override
  public void exportToTableBeanData(AbstractTableFieldBeanData target) throws ProcessingException {
    ITableRowDataMapper rowMapper = createTableRowDataMapper(target.getRowType());
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      if (rowMapper.acceptExport(row)) {
        AbstractTableRowData rowData = target.addRow();
        rowMapper.exportTableRowData(row, rowData);
      }
    }
    ITableRow[] deletedRows = getDeletedRows();
    for (int i = 0, ni = deletedRows.length; i < ni; i++) {
      ITableRow row = deletedRows[i];
      if (rowMapper.acceptExport(row)) {
        AbstractTableRowData rowData = target.addRow();
        rowMapper.exportTableRowData(row, rowData);
        rowData.setRowState(AbstractTableRowData.STATUS_DELETED);
      }
    }
  }

  @Override
  public void importFromTableBeanData(AbstractTableFieldBeanData source) throws ProcessingException {
    discardAllDeletedRows();
    clearValidatedValuesOnAllColumns();
    clearAllRowsValidity();
    int deleteCount = 0;
    ArrayList<ITableRow> newRows = new ArrayList<ITableRow>();
    ITableRowDataMapper mapper = createTableRowDataMapper(source.getRowType());
    for (int i = 0, ni = source.getRowCount(); i < ni; i++) {
      AbstractTableRowData rowData = source.rowAt(i);
      if (rowData.getRowState() != AbstractTableFieldData.STATUS_DELETED && mapper.acceptImport(rowData)) {
        ITableRow newTableRow = new TableRow(getColumnSet());
        mapper.importTableRowData(newTableRow, rowData);
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
          AbstractTableRowData rowData = source.rowAt(i);
          if (rowData.getRowState() == AbstractTableFieldData.STATUS_DELETED && mapper.acceptImport(rowData)) {
            ITableRow newTableRow = new TableRow(getColumnSet());
            mapper.importTableRowData(newTableRow, rowData);
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

  @Override
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

  @Override
  @SuppressWarnings("unchecked")
  public void updateTable(AbstractTableFieldData source) throws ProcessingException {
    if (source.isValueSet()) {
      clearValidatedValuesOnAllColumns();
      clearAllRowsValidity();
      discardAllDeletedRows();
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

  @Override
  public IMenu[] getMenus() {
    return m_menus;
  }

  @Override
  public <T extends IMenu> T getMenu(Class<T> menuType) throws ProcessingException {
    // ActionFinder performs instance-of checks. Hence the menu replacement mapping is not required
    return new ActionFinder().findAction(getMenus(), menuType);
  }

  @Override
  public boolean runMenu(Class<? extends IMenu> menuType) throws ProcessingException {
    Class<? extends IMenu> c = getReplacingMenuClass(menuType);
    for (IMenu m : getMenus()) {
      if (m.getClass() == c) {
        if (!m.isEnabledProcessingAction()) {
          return false;
        }
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
   * Checks whether the menu with the given class has been replaced by another menu. If so, the replacing
   * menu's class is returned. Otherwise the given class itself.
   * 
   * @param c
   * @return Returns the possibly available replacing menu class for the given class.
   * @see Replace
   * @since 3.8.2
   */
  private <T extends IMenu> Class<? extends T> getReplacingMenuClass(Class<T> c) {
    if (m_menuReplacementMapping != null) {
      @SuppressWarnings("unchecked")
      Class<? extends T> replacingMenuClass = (Class<? extends T>) m_menuReplacementMapping.get(c);
      if (replacingMenuClass != null) {
        return replacingMenuClass;
      }
    }
    return c;
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
  @Override
  public void replaceRowsByMatrix(Object dataMatrixOrReference) throws ProcessingException {
    replaceRows(createRowsByMatrix(dataMatrixOrReference));
  }

  @Override
  public void replaceRowsByArray(Object dataArray) throws ProcessingException {
    replaceRows(createRowsByArray(dataArray));
  }

  @Override
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
        newRowIndexMap.put(new CompositeObject(getRowKeys(newRows[i])), Integer.valueOf(i));
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
            oldRow.setEnabled(newRow.isEnabled());
            oldRow.setStatus(newRow.getStatus());
            for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
              if (columnIndex < newRow.getCellCount()) {
                oldRow.getCellForUpdate(columnIndex).updateFrom(newRow.getCell(columnIndex));
              }
              else {
                // reset the visible values
                oldRow.getCellForUpdate(columnIndex).setText(null);
                oldRow.getCellForUpdate(columnIndex).setValue(null);
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

  @Override
  public void updateRow(ITableRow row) {
    if (row != null) {
      updateRows(new ITableRow[]{row});
    }
  }

  @Override
  public void updateAllRows() {
    ITableRow[] rows = getRows();
    updateRows(rows);
  }

  @Override
  public void setRowState(ITableRow row, int rowState) throws ProcessingException {
    setRowState(new ITableRow[]{row}, rowState);
  }

  @Override
  public void setAllRowState(int rowState) throws ProcessingException {
    setRowState(getRows(), rowState);
  }

  @Override
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

  @Override
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
        fireRowsUpdated(resolvedRowList.toArray(new ITableRow[resolvedRowList.size()]));
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
      for (IColumn<?> col : getColumns()) {
        if (col instanceof AbstractColumn<?>) {
          ((AbstractColumn<?>) col).validateColumnValue(row);
        }
      }
      enqueueDecorationTasks(row);
    }
  }

  @Override
  public int getRowCount() {
    return m_rows.size();
  }

  @Override
  public int getDeletedRowCount() {
    return m_deletedRows.size();
  }

  @Override
  public int getSelectedRowCount() {
    return m_selectedRows.size();
  }

  @Override
  public ITableRow getSelectedRow() {
    if (m_selectedRows.size() > 0) {
      return m_selectedRows.first();
    }
    else {
      return null;
    }
  }

  @Override
  public ITableRow[] getSelectedRows() {
    return m_selectedRows.toArray(new ITableRow[m_selectedRows.size()]);
  }

  @Override
  public boolean isSelectedRow(ITableRow row) {
    row = resolveRow(row);
    if (row == null) {
      return false;
    }
    else {
      return m_selectedRows.contains(row);
    }
  }

  @Override
  public void selectRow(int rowIndex) {
    selectRow(getRow(rowIndex));
  }

  @Override
  public void selectRow(ITableRow row) {
    selectRow(row, false);
  }

  @Override
  public void selectRow(ITableRow row, boolean append) {
    if (row != null) {
      selectRows(new ITableRow[]{row}, append);
    }
    else {
      selectRows(new ITableRow[0], append);
    }
  }

  @Override
  public void selectRows(ITableRow[] rows) {
    selectRows(rows, false);
  }

  @Override
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
      fireRowsSelected(m_selectedRows.toArray(new ITableRow[m_selectedRows.size()]));
    }
  }

  @Override
  public void selectFirstRow() {
    selectRow(getRow(0));
  }

  @Override
  public void selectNextRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() + 1 < getRowCount()) {
      selectRow(getRow(row.getRowIndex() + 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(0);
    }
  }

  @Override
  public void selectPreviousRow() {
    ITableRow row = getSelectedRow();
    if (row != null && row.getRowIndex() - 1 >= 0) {
      selectRow(getRow(row.getRowIndex() - 1));
    }
    else if (row == null && getRowCount() > 0) {
      selectRow(getRowCount() - 1);
    }
  }

  @Override
  public void selectLastRow() {
    selectRow(getRow(getRowCount() - 1));
  }

  @Override
  public void deselectRow(ITableRow row) {
    if (row != null) {
      deselectRows(new ITableRow[]{row});
    }
    else {
      deselectRows(new ITableRow[0]);
    }
  }

  @Override
  public void deselectRows(ITableRow[] rows) {
    rows = resolveRows(rows);
    if (rows != null && rows.length > 0) {
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      if (newSelection.removeAll(Arrays.asList(rows))) {
        m_selectedRows = newSelection;
        fireRowsSelected(m_selectedRows.toArray(new ITableRow[m_selectedRows.size()]));
      }
    }
  }

  @Override
  public void selectAllRows() {
    selectRows(getRows(), false);
  }

  @Override
  public void deselectAllRows() {
    selectRow(null, false);
  }

  @Override
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
    selectRows(newList.toArray(new ITableRow[newList.size()]), false);
  }

  @Override
  public void deselectAllEnabledRows() {
    ITableRow[] selectedRows = getSelectedRows();
    ArrayList<ITableRow> newList = new ArrayList<ITableRow>();
    for (int i = 0; i < selectedRows.length; i++) {
      if (selectedRows[i].isEnabled()) {
        newList.add(selectedRows[i]);
      }
    }
    deselectRows(newList.toArray(new ITableRow[newList.size()]));
  }

  @Override
  public ITableRow[] getCheckedRows() {
    final ArrayList<ITableRow> list = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.isChecked()) {
        list.add(row);
      }
    }
    return list.toArray(new ITableRow[list.size()]);
  }

  @Override
  public void checkRow(int row, boolean value) throws ProcessingException {
    checkRow(getRow(row), value);
  }

  @Override
  public void checkRow(ITableRow row, boolean value) throws ProcessingException {
    if (!row.isEnabled()) {
      return;
    }
    if (!isMultiCheck() && value && getCheckedRows().length > 0) {
      uncheckAllRows();
    }
    row.setChecked(value);
    if (getCheckableColumn() != null) {
      getCheckableColumn().setValue(row, value);
    }
  }

  @Override
  public void checkRows(ITableRow[] rows, boolean value) throws ProcessingException {
    rows = resolveRows(rows);
    // check checked count with multicheck
    if (rows.length > 1 && !isMultiCheck()) {
      ITableRow first = rows[0];
      first.setChecked(value);
    }
    else {
      for (ITableRow row : rows) {
        checkRow(row, value);
      }
    }
  }

  @Override
  public void checkAllRows() throws ProcessingException {
    try {
      setTableChanging(true);
      for (int i = 0; i < getRowCount(); i++) {
        checkRow(i, true);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckAllRows() throws ProcessingException {
    try {
      setTableChanging(true);
      for (int i = 0; i < getRowCount(); i++) {
        checkRow(i, false);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public String getDefaultIconId() {
    String iconId = propertySupport.getPropertyString(PROP_DEFAULT_ICON);
    if (iconId != null && iconId.length() == 0) {
      iconId = null;
    }
    return iconId;
  }

  @Override
  public void setDefaultIconId(String iconId) {
    propertySupport.setPropertyString(PROP_DEFAULT_ICON, iconId);
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public final void setEnabled(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_ENABLED, b);
    if (changed) {
      //update the state of all current cell beans that are out there
      try {
        setTableChanging(true);
        //
        ITableRow[] rows = getRows();
        for (ITableRow row : rows) {
          enqueueDecorationTasks(row);
        }
      }
      finally {
        setTableChanging(false);
      }
    }
  }

  @Override
  public boolean isScrollToSelection() {
    return propertySupport.getPropertyBool(PROP_SCROLL_TO_SELECTION);
  }

  @Override
  public void setScrollToSelection(boolean b) {
    propertySupport.setPropertyBool(PROP_SCROLL_TO_SELECTION, b);
  }

  @Override
  public void scrollToSelection() {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_SCROLL_TO_SELECTION));
  }

  /**
   * @return a copy of a row<br>
   *         when the row is changed it has to be applied to the table using
   *         modifyRow(row);
   */
  @Override
  public ITableRow getRow(int rowIndex) {
    ITableRow row = null;
    ITableRow[] rows = getRows();
    if (rowIndex >= 0 && rowIndex < rows.length) {
      row = rows[rowIndex];
    }
    return row;
  }

  @Override
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

  @Override
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

  @Override
  public int getFilteredRowCount() {
    if (m_rowFilters.size() > 0) {
      return getFilteredRows().length;
    }
    else {
      return getRowCount();
    }
  }

  @Override
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

  @Override
  public int getFilteredRowIndex(ITableRow row) {
    ITableRow[] filteredRows = getFilteredRows();
    for (int i = 0; i < filteredRows.length; i++) {
      if (filteredRows[i].equals(row)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public Object[][] getTableData() {
    Object[][] data = new Object[getRowCount()][getColumnCount()];
    for (int r = 0; r < getRowCount(); r++) {
      for (int c = 0; c < getColumnCount(); c++) {
        data[r][c] = getRow(r).getCellValue(c);
      }
    }
    return data;
  }

  @Override
  public Object[][] exportTableRowsAsCSV(ITableRow[] rows, IColumn<?>[] columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormats) {
    return TableUtility.exportRowsAsCSV(rows, columns, includeLineForColumnNames, includeLineForColumnTypes, includeLineForColumnFormats);
  }

  @Override
  public ITableRow[] getRows(int[] rowIndexes) {
    if (rowIndexes == null) {
      return new ITableRow[0];
    }
    ITableRow[] rows = new ITableRow[rowIndexes.length];
    int missingCount = 0;
    for (int i = 0; i < rowIndexes.length; i++) {
      rows[i] = getRow(rowIndexes[i]);
      if (rows[i] == null) {
        missingCount++;
      }
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
  @Override
  public ITableRow[] getDeletedRows() {
    return m_deletedRows.values().toArray(new ITableRow[m_deletedRows.size()]);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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
  @Override
  public ITableRow addRowByArray(Object dataArray) throws ProcessingException {
    if (dataArray == null) {
      return null;
    }
    ITableRow[] a = addRowsByMatrix(new Object[]{dataArray});
    if (a.length > 0) {
      return a[0];
    }
    else {
      return null;
    }
  }

  @Override
  public ITableRow[] addRowsByMatrix(Object dataMatrix) throws ProcessingException {
    return addRowsByMatrix(dataMatrix, ITableRow.STATUS_INSERTED);
  }

  @Override
  public ITableRow[] addRowsByMatrix(Object dataMatrix, int rowStatus) throws ProcessingException {
    return addRows(createRowsByMatrix(dataMatrix, rowStatus));
  }

  @Override
  public ITableRow[] addRowsByArray(Object dataArray) throws ProcessingException {
    return addRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  @Override
  public ITableRow[] addRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    return addRows(createRowsByArray(dataArray, rowStatus));
  }

  @Override
  public ITableRow addRow(ITableRow newRow) throws ProcessingException {
    return addRow(newRow, false);
  }

  @Override
  public ITableRow addRow(ITableRow newRow, boolean markAsInserted) throws ProcessingException {
    ITableRow[] addedRows = addRows(new ITableRow[]{newRow}, markAsInserted);
    if (addedRows.length > 0) {
      return addedRows[0];
    }
    else {
      return null;
    }
  }

  @Override
  public ITableRow[] addRows(ITableRow[] newRows) throws ProcessingException {
    return addRows(newRows, false);
  }

  @Override
  public ITableRow[] addRows(ITableRow[] newRows, boolean markAsInserted) throws ProcessingException {
    return addRows(newRows, markAsInserted, null);
  }

  @Override
  public ITableRow[] addRows(ITableRow[] newRows, boolean markAsInserted, int[] insertIndexes) throws ProcessingException {
    if (newRows == null || newRows.length == 0) {
      return new ITableRow[0];
    }
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
          while (sortArray[sortArrayIndex] != null) {
            sortArrayIndex++;
          }
          sortArray[sortArrayIndex] = m_rows.get(i);
        }
        // add new rows that have no given sortIndex
        for (int i = insertIndexes.length; i < newIRows.length; i++) {
          // find next empty slot
          while (sortArray[sortArrayIndex] != null) {
            sortArrayIndex++;
          }
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
    for (IColumn<?> col : getColumns()) {
      if (col instanceof AbstractColumn<?>) {
        ((AbstractColumn<?>) col).validateColumnValue(newIRow);
      }
    }
    wasEverValid(newIRow);
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      int newIndex = m_rows.size();
      newIRow.setRowIndex(newIndex);
      newIRow.setTableInternal(this);
      m_rows.add(newIRow);
    }
    enqueueDecorationTasks(newIRow);
    return newIRow;
  }

  @Override
  public void moveRow(int sourceIndex, int targetIndex) {
    moveRowImpl(sourceIndex, targetIndex);
  }

  /**
   * move the movingRow to the location just before the target row
   */
  @Override
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
  @Override
  public void moveRowAfter(ITableRow movingRow, ITableRow targetRow) {
    movingRow = resolveRow(movingRow);
    targetRow = resolveRow(targetRow);
    if (movingRow != null && targetRow != null) {
      moveRowImpl(movingRow.getRowIndex(), targetRow.getRowIndex() + 1);
    }
  }

  private void moveRowImpl(int sourceIndex, int targetIndex) {
    if (sourceIndex < 0) {
      sourceIndex = 0;
    }
    if (sourceIndex >= getRowCount()) {
      sourceIndex = getRowCount() - 1;
    }
    if (targetIndex < 0) {
      targetIndex = 0;
    }
    if (targetIndex >= getRowCount()) {
      targetIndex = getRowCount() - 1;
    }
    if (targetIndex != sourceIndex) {
      synchronized (m_cachedRowsLock) {
        m_cachedRows = null;
        ITableRow row = m_rows.remove(sourceIndex);
        m_rows.add(targetIndex, row);
      }
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

  @Override
  public void deleteRow(int rowIndex) {
    deleteRows(new int[]{rowIndex});
  }

  @Override
  public void deleteRows(int[] rowIndexes) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    deleteRows(rowList.toArray(new ITableRow[rowList.size()]));
  }

  @Override
  public void deleteRow(ITableRow row) {
    if (row != null) {
      deleteRows(new ITableRow[]{row});
    }
  }

  @Override
  public void deleteAllRows() {
    deleteRows(getRows());
  }

  @Override
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
          synchronized (m_cachedRowsLock) {
            m_rows.clear();
            m_cachedRows = null;
          }
          clearValidatedValuesOnAllColumns();
          clearAllRowsValidity();
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
              boolean removed = false;
              synchronized (m_cachedRowsLock) {
                removed = m_rows.remove(candidateRow);
                if (removed) {
                  m_cachedRows = null;
                }
              }
              if (removed) {
                clearValidatedValueOnColumns(candidateRow);
                clearRowValidity(candidateRow);
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

  private void clearValidatedValuesOnAllColumns() {
    for (IColumn column : getColumnSet().getColumns()) {
      if (column instanceof AbstractColumn<?>) {
        ((AbstractColumn) column).clearValidatedValues();
      }
    }
  }

  private void clearValidatedValueOnColumns(ITableRow row) {
    for (IColumn column : getColumnSet().getColumns()) {
      if (column instanceof AbstractColumn<?>) {
        ((AbstractColumn) column).clearValidatedValue(row);
      }
    }
  }

  @Override
  public void discardRow(int rowIndex) {
    discardRows(new int[]{rowIndex});
  }

  @Override
  public void discardRows(int[] rowIndexes) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    discardRows(rowList.toArray(new ITableRow[rowList.size()]));
  }

  @Override
  public void discardRow(ITableRow row) {
    if (row != null) {
      discardRows(new ITableRow[]{row});
    }
  }

  @Override
  public void discardAllRows() {
    discardRows(getRows());
  }

  /**
   * discard is the same as delete with the exception that discarded rows are
   * not collected in the deletedRows list
   */
  @Override
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

  @Override
  public void discardAllDeletedRows() {
    for (Iterator<ITableRow> it = m_deletedRows.values().iterator(); it.hasNext();) {
      ((InternalTableRow) it.next()).setTableInternal(null);
    }
    m_deletedRows.clear();
  }

  @Override
  public void discardDeletedRow(ITableRow deletedRow) {
    if (deletedRow != null) {
      discardDeletedRows(new ITableRow[]{deletedRow});
    }
  }

  @Override
  public void discardDeletedRows(ITableRow[] deletedRows) {
    if (deletedRows != null) {
      for (ITableRow row : deletedRows) {
        m_deletedRows.remove(new CompositeObject(getRowKeys(row)));
        ((InternalTableRow) row).setTableInternal(null);
      }
    }
  }

  @Override
  public void setContextColumn(IColumn<?> col) {
    propertySupport.setProperty(PROP_CONTEXT_COLUMN, col);
  }

  @Override
  public IColumn<?> getContextColumn() {
    return (IColumn<?>) propertySupport.getProperty(PROP_CONTEXT_COLUMN);
  }

  @Override
  public Object[] getRowKeys(int rowIndex) {
    ITableRow row = getRow(rowIndex);
    return getRowKeys(row);
  }

  @Override
  public Object[] getRowKeys(ITableRow row) {
    // do not resolve
    Object[] keys = new Object[0];
    if (row != null) {
      keys = row.getKeyValues();
    }
    return keys;
  }

  @Override
  public ITableRow findRowByKey(Object[] keys) {
    IColumn<?>[] keyColumns = getColumnSet().getKeyColumns();
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

  @Override
  public ITableColumnFilterManager getColumnFilterManager() {
    return m_columnFilterManager;
  }

  @Override
  public void setColumnFilterManager(ITableColumnFilterManager m) {
    m_columnFilterManager = m;
  }

  @Override
  public ITableCustomizer getTableCustomizer() {
    return m_tableCustomizer;
  }

  @Override
  public void setTableCustomizer(ITableCustomizer c) {
    m_tableCustomizer = c;
  }

  @Override
  public ITypeWithClassId getContainer() {
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method unless you are implementing a container that holds and controls an {@link ITable}
   */
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public boolean isSortEnabled() {
    return m_sortEnabled;
  }

  @Override
  public void setSortEnabled(boolean b) {
    m_sortEnabled = b;
  }

  @Override
  public void sort() {
    try {
      if (isSortEnabled()) {
        // Consider any active sort-column, not only explicit ones.
        // This is to support reverse (implicit) sorting of columns, meaning that multiple column sort is done
        // without CTRL-key held. In contrast to explicit multiple column sort, the first clicked column
        // is the least significant sort column.
        IColumn<?>[] sortCols = getColumnSet().getSortColumns();
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

  @Override
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
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      m_selectedRows = newSelection;
    }
    fireRowOrderChanged();
  }

  @Override
  public void resetColumnConfiguration() {
    discardAllRows();
    //
    try {
      setTableChanging(true);
      // save displayable state
      HashMap<String, Boolean> displayableState = new HashMap<String, Boolean>();
      for (IColumn<?> col : getColumns()) {
        displayableState.put(col.getColumnId(), col.isDisplayable());
      }
      // reset columns
      disposeColumnsInternal();
      createColumnsInternal();
      initColumnsInternal();
      // re-apply displayable
      for (IColumn<?> col : getColumns()) {
        if (displayableState.get(col.getColumnId()) != null) {
          col.setDisplayable(displayableState.get(col.getColumnId()));
        }
      }
      // re-apply existing filters to new columns
      ITableColumnFilterManager filterManager = getColumnFilterManager();
      if (filterManager != null && filterManager.getFilters() != null) {
        for (IColumn<?> col : getColumns()) {
          for (ITableColumnFilter<?> filter : filterManager.getFilters()) {
            if (filter.getColumn().getColumnId().equals(col.getColumnId())) {
              filter.setColumn(col);
            }
          }
          filterManager.refresh();
        }
      }
      fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED));
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void resetColumnVisibilities() {
    resetColumns(true, false, false, false);
  }

  @Override
  public void resetColumnOrder() {
    resetColumns(false, true, false, false);
  }

  @Override
  public void resetColumnSortOrder() {
    resetColumns(false, false, true, false);
  }

  @Override
  public void resetColumnWidths() {
    resetColumns(false, false, false, true);
  }

  @Override
  public void resetDisplayableColumns() {
    resetColumns(true, true, true, true);
  }

  @Override
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
    env.removeAllTableColumnPreferences(this, visibility, order, sorting, widths);
    //Order
    if (order) {
      TreeMap<CompositeObject, IColumn<?>> orderMap = new TreeMap<CompositeObject, IColumn<?>>();
      int index = 0;
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable()) {
          orderMap.put(new CompositeObject(col.getViewOrder(), index), col);
          index++;
        }
      }
      getColumnSet().setVisibleColumns(orderMap.values().toArray(new IColumn[orderMap.size()]));
    }

    //Visibilities
    if (visibility) {
      ArrayList<IColumn<?>> list = new ArrayList<IColumn<?>>();
      for (IColumn<?> col : getColumnSet().getAllColumnsInUserOrder()) {
        if (col.isDisplayable()) {
          boolean configuredVisible = ((AbstractColumn<?>) col).isInitialVisible();
          if (configuredVisible) {
            list.add(col);
          }
        }
      }
      getColumnSet().setVisibleColumns(list.toArray(new IColumn<?>[list.size()]));
    }

    //Sorting
    if (sorting) {
      TreeMap<CompositeObject, IColumn<?>> sortMap = new TreeMap<CompositeObject, IColumn<?>>();
      int index = 0;
      for (IColumn<?> col : getColumns()) {
        if (col.getInitialSortIndex() >= 0) {
          sortMap.put(new CompositeObject(col.getInitialSortIndex(), index), col);
        }
        index++;
      }
      //
      getColumnSet().clearSortColumns();
      getColumnSet().clearPermanentHeadSortColumns();
      getColumnSet().clearPermanentTailSortColumns();
      for (IColumn<?> col : sortMap.values()) {
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
      for (IColumn<?> col : getColumns()) {
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
      BatchLookupCall batchCall = null;
      ArrayList<ITableRow> tableRowList = null;
      ArrayList<Integer> columnIndexList = null;
      if (m_cellLookupBuffer.size() > 0) {
        batchCall = new BatchLookupCall();
        tableRowList = new ArrayList<ITableRow>();
        columnIndexList = new ArrayList<Integer>();
        BatchLookupResultCache lookupResultCache = new BatchLookupResultCache();
        for (P_CellLookup lookup : m_cellLookupBuffer) {
          ITableRow row = lookup.getRow();
          if (row.getTable() == AbstractTable.this) {
            ISmartColumn<?> col = lookup.getColumn();
            LookupCall call = col.prepareLookupCall(row);
            if (call != null && call.getKey() != null) {
              //split: local vs remote
              if (call instanceof LocalLookupCall) {
                LookupRow[] result = lookupResultCache.getDataByKey(call);
                applyLookupResult((InternalTableRow) row, col.getColumnIndex(), result);
              }
              else {
                tableRowList.add(row);
                columnIndexList.add(Integer.valueOf(col.getColumnIndex()));
                batchCall.addLookupCall(call);
              }
            }
            else {
              applyLookupResult((InternalTableRow) row, col.getColumnIndex(), LookupRow.EMPTY_ARRAY);
            }
          }
        }
      }
      m_cellLookupBuffer.clear();
      //
      if (batchCall != null && tableRowList != null && columnIndexList != null && !batchCall.isEmpty()) {
        ITableRow[] tableRows = tableRowList.toArray(new ITableRow[tableRowList.size()]);
        LookupRow[][] resultArray;
        IBatchLookupService service = SERVICES.getService(IBatchLookupService.class);
        resultArray = service.getBatchDataByKey(batchCall);
        for (int i = 0; i < tableRows.length; i++) {
          applyLookupResult((InternalTableRow) tableRows[i], ((Number) columnIndexList.get(i)).intValue(), resultArray[i]);
        }
      }
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
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
            filterChanged = filterChanged || (oldFlag != newFlag);
          }
        }
      }
      if (filterChanged) {
        fireRowFilterChanged();
      }
    }
  }

  private int m_processEventBufferLoopDetection;

  /**
   * Fire events in form of one batch<br>
   * fire all buffered events<br>
   * coalesce all TableEvents of same type and sort according to their type
   */
  private void processEventBuffer() {
    //loop detection
    try {
      m_processEventBufferLoopDetection++;
      if (m_processEventBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in " + getClass() + ". see stack trace for more details.", new Exception("LOOP DETECTION"));
        return;
      }
      //
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
            case TableEvent.TYPE_SCROLL_TO_SELECTION: {
              sortedCoalescedMap.put(180, subList.get(lastIndex));// use last
              break;
            }
            default: {
              sortedCoalescedMap.put(-type, subList.get(lastIndex));// use last
            }
          }
        }
        // fire the batch and set tree to changing, otherwise a listener might trigger another events that then are processed before all other listeners received that batch
        try {
          setTableChanging(true);
          //
          fireTableEventBatchInternal(sortedCoalescedMap.values().toArray(new TableEvent[sortedCoalescedMap.size()]));
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_processEventBufferLoopDetection--;
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
      ce.setCopyObject(last.getCopyObject());
      ce.addPopupMenus(last.getPopupMenus());
      //columns
      Set<IColumn> colList = new LinkedHashSet<IColumn>();
      for (TableEvent t : list) {
        if (t.getColumns() != null) {
          colList.addAll(Arrays.asList(t.getColumns()));
        }
      }
      ce.setColumns(colList.toArray(new IColumn<?>[colList.size()]));
      //rows
      Set<ITableRow> rowList = new LinkedHashSet<ITableRow>();
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
      ce.setRows(rowList.toArray(new ITableRow[rowList.size()]));
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
        IColumn<?> column = getColumnSet().getColumn(i);
        // lookups
        if (column instanceof ISmartColumn) {
          ISmartColumn<?> smartColumn = (ISmartColumn<?>) column;
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
        cell.setTooltipText(result[0].getTooltipText());
      }
      else if (result.length > 1) {
        StringBuffer buf = new StringBuffer();
        StringBuffer bufTooltip = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
          if (i > 0) {
            if (isMultilineText()) {
              buf.append("\n");
              bufTooltip.append("\n");
            }
            else {
              buf.append(", ");
              bufTooltip.append(", ");
            }
          }
          buf.append(result[i].getText());
          bufTooltip.append(result[i].getTooltipText());
        }
        cell.setText(buf.toString());
        cell.setTooltipText(bufTooltip.toString());
      }
      else {
        cell.setText("");
        cell.setTooltipText("");
      }
    }
    finally {
      tableRow.setRowPropertiesChanged(false);
      tableRow.setRowChanging(false);
    }
  }

  @Override
  public void tablePopulated() {
    if (m_tableEventBuffer.isEmpty()) {
      synchronized (m_cachedFilteredRowsLock) {
        m_cachedFilteredRows = null;
      }
      fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_TABLE_POPULATED, null));
    }
  }

  @Override
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

  @Override
  public ITableRow[] resolveRows(ITableRow[] rows) {
    if (rows == null) {
      rows = new ITableRow[0];
    }
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

  @Override
  public boolean isHeaderVisible() {
    return propertySupport.getPropertyBool(PROP_HEADER_VISIBLE);
  }

  @Override
  public void setHeaderVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_HEADER_VISIBLE, b);
  }

  @Override
  public final void decorateCell(ITableRow row, IColumn<?> col) {
    Cell cell = row.getCellForUpdate(col.getColumnIndex());
    decorateCellInternal(cell, row, col);
    try {
      execDecorateCell(cell, row, col);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
    }
  }

  public boolean wasEverValid(ITableRow row) {
    if (!m_rowValidty.contains(row)) {
      for (IColumn<?> col : getColumns()) {
        if (row.getCell(col).getErrorStatus() != null) {
          return false;
        }
      }
      m_rowValidty.add(row);
    }
    return true;
  }

  private void clearRowValidity(ITableRow row) {
    m_rowValidty.remove(row);
  }

  private void clearAllRowsValidity() {
    m_rowValidty.clear();
  }

  protected void decorateCellInternal(Cell view, ITableRow row, IColumn<?> col) {
  }

  @Override
  public final void decorateRow(ITableRow row) {
    decorateRowInternal(row);
    try {
      execDecorateRow(row);
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
    catch (Throwable t) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
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
   * Called when the columns are reset.
   * <p>
   * Subclasses can override this method. The default does nothing.
   * 
   * @param visiblity
   *          {@code true} if the visibility is reset.
   * @param order
   *          {@code true} if the order is reset.
   * @param sorting
   *          {@code true} if the sorting is reset.
   * @param widths
   *          {@code true} if the column widths are reset.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(90)
  protected void execResetColumns(boolean visibility, boolean order, boolean sorting, boolean widths) throws ProcessingException {
  }

  /**
   * Model Observer
   */
  @Override
  public void addTableListener(TableListener listener) {
    m_listenerList.add(TableListener.class, listener);
  }

  @Override
  public void removeTableListener(TableListener listener) {
    m_listenerList.remove(TableListener.class, listener);
  }

  @Override
  public void addUITableListener(TableListener listener) {
    m_listenerList.insertAtFront(TableListener.class, listener);
  }

  protected IEventHistory<TableEvent> createEventHistory() {
    return new DefaultTableEventHistory(5000L);
  }

  @Override
  public IEventHistory<TableEvent> getEventHistory() {
    return m_eventHistory;
  }

  @Override
  public IMenu[] fetchMenusForRowsInternal(ITableRow[] rows) {
    TableEvent e;
    if (rows.length == 0) {
      e = new TableEvent(this, TableEvent.TYPE_EMPTY_SPACE_POPUP);
    }
    else {
      e = new TableEvent(this, TableEvent.TYPE_ROW_POPUP, rows);
    }
    fireTableEventInternal(e);
    return e.getPopupMenus();
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
        interceptRowClickSingleObserver(row);
        execRowClick(row);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
      }
    }
  }

  protected void interceptRowClickSingleObserver(ITableRow row) throws ProcessingException {
    // single observer for checkable tables
    // if row click is targetted to cell editor, do not interpret click as check/uncheck event
    if (row.isEnabled() && isEnabled()) {
      IColumn<?> ctxCol = getContextColumn();
      if (isCellEditable(row, ctxCol)) {
        //cell-level checkbox
        if (ctxCol instanceof IBooleanColumn) {
          //editable boolean columns consume this click
          IFormField field = ctxCol.prepareEdit(row);
          if (field instanceof IBooleanField) {
            IBooleanField bfield = (IBooleanField) field;
            bfield.setChecked(!bfield.isChecked());
            ctxCol.completeEdit(row, field);
          }
        }
        else {
          //other editable columns have no effect HERE, the ui will open an editor
        }
      }
      else {
        //row-level checkbox
        if (isCheckable()) {
          row.setChecked(!row.isChecked());
        }
      }
    }
  }

  private void fireRowAction(ITableRow row) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (row != null) {
          try {
            execRowAction(row);
          }
          catch (ProcessingException ex) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
          }
          catch (Throwable t) {
            SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
          }
        }
      }
      finally {
        m_actionRunning = false;
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

  private void fireRequestFocusInCell(IColumn<?> column, ITableRow row) {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_REQUEST_FOCUS_IN_CELL);
    e.setColumns(new IColumn<?>[]{column});
    e.setRows(new ITableRow[]{row});
    fireTableEventInternal(e);
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
    if (row != null) {
      rows = new ITableRow[]{row};
    }
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_DROP_ACTION, rows);
    e.setDropObject(dropData);
    fireTableEventInternal(e);
  }

  private TransferObject fireRowsCopyRequest() {
    ITableRow[] rows = getSelectedRows();
    if (rows != null && rows.length > 0) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_COPY_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getCopyObject();
    }
    else {
      return null;
    }
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
    boolean singleSelect = getSelectedRowCount() == 1;
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
            if ((!menu.isInheritAccessibility()) || (isEnabled())) {
              validMenu = menu;
            }
          }
          break;
        }
        case TableEvent.TYPE_ROW_POPUP: {
          if (multiSelect) {
            if (menu.isMultiSelectionAction()) {
              if ((!menu.isInheritAccessibility()) || (isEnabled() && allRowsEnabled)) {
                validMenu = menu;
              }
            }
          }
          else if (singleSelect) {
            if (menu.isSingleSelectionAction()) {
              if ((!menu.isInheritAccessibility()) || (isEnabled() && allRowsEnabled)) {
                validMenu = menu;
              }
            }
          }
          break;
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

  /**
   * Called before the header menus are displayed.
   * <p>
   * Subclasses can override this method. The default add menus for add, modifying and removing custom column and menus
   * for reseting, organizing and filtering the columns.
   * 
   * @param e
   *          Table event of type {@link TableEvent#TYPE_HEADER_POPUP}.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(100)
  protected void execAddHeaderMenus(TableEvent e) throws ProcessingException {
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
  }

  private IMenu[] fireHeaderPopup() {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_HEADER_POPUP);
    fireTableEventInternal(e);
    try {
      execAddHeaderMenus(e);
    }
    catch (ProcessingException ex) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
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
      //Ensure all editor values have been applied.
//      getUIFacade().completeCellEditFromUI();

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
    if (batch.length == 0) {
      return;
    }
    if (batch.length > 0) {
      EventListener[] listeners = m_listenerList.getListeners(TableListener.class);
      if (listeners != null && listeners.length > 0) {
        for (int i = 0; i < listeners.length; i++) {
          ((TableListener) listeners[i]).tableChangedBatch(batch);
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

      IColumn<?> col = getContextColumn();
      if (col == null) {
        IColumn<?>[] sortCols = getColumnSet().getSortColumns();
        if (sortCols.length > 0) {
          col = sortCols[sortCols.length - 1];
        }
        else {
          TreeMap<CompositeObject, IColumn<?>> sortMap = new TreeMap<CompositeObject, IColumn<?>>();
          int index = 0;
          for (IColumn<?> c : getColumnSet().getVisibleColumns()) {
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
        if (LOG.isInfoEnabled()) {
          LOG.info("finding regex:" + pattern + " in column " + getColumnSet().getColumn(colIndex).getHeaderCell().getText());
        }
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

  @Override
  public ITableUIFacade getUIFacade() {
    return m_uiFacade;
  }

  private ProcessingException createNewUnexpectedProcessingException(Throwable t) {
    return new ProcessingException("Unexpected", t);
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

    @Override
    public boolean isUIProcessing() {
      return m_uiProcessorCount > 0;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void fireVisibleColumnsChangedFromUI(IColumn<?>[] visibleColumns) {
      try {
        pushUIProcessor();
        //
        getColumnSet().setVisibleColumns(visibleColumns);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireColumnMovedFromUI(IColumn<?> c, int toViewIndex) {
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

    @Override
    public void setColumnWidthFromUI(IColumn<?> c, int newWidth) {
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

    @Override
    public void fireHeaderSortFromUI(IColumn<?> c, boolean multiSort) {
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

    @Override
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

    @Override
    public TransferObject fireRowsDragRequestFromUI() {
      try {
        pushUIProcessor();
        return fireRowsDragRequest();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireRowDropActionFromUI(ITableRow row, TransferObject dropData) {
      try {
        pushUIProcessor();
        row = resolveRow(row);
        fireRowDropAction(row, dropData);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public TransferObject fireRowsCopyRequestFromUI() {
      try {
        pushUIProcessor();
        return fireRowsCopyRequest();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireHyperlinkActionFromUI(ITableRow row, IColumn<?> col, URL url) {
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

    @Override
    public void setContextColumnFromUI(IColumn<?> col) {
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

    @Override
    public IFormField prepareCellEditFromUI(ITableRow row, IColumn<?> col) {
      try {
        pushUIProcessor();
        //
        m_editContext = null;
        row = resolveRow(row);
        if (row != null && col != null) {
          try {
            // ensure the editable row to be selected.
            // This is crucial if the cell's value is changed right away in @{link IColumn#prepareEdit(ITableRow)}, e.g. in @{link AbstractBooleanColumn}
            row.getTable().selectRow(row);
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

    @Override
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

    @Override
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
    private final ISmartColumn<?> m_column;

    public P_CellLookup(ITableRow row, ISmartColumn<?> col) {
      m_row = row;
      m_column = col;
    }

    public ITableRow getRow() {
      return m_row;
    }

    public ISmartColumn<?> getColumn() {
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
