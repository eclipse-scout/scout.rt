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
import java.util.Collection;
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
import org.eclipse.scout.commons.CollectionUtility;
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
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.dnd.TextTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableAddHeaderMenusChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableContentChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCopyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTableRowDataMapperChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateRowChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDisposeTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDragChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableHyperlinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableResetColumnsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.IActionVisitor;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TableContextMenu;
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
import org.eclipse.scout.rt.client.ui.basic.table.columns.IContentAssistColumn;
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
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.BatchLookupResultCache;
import org.eclipse.scout.rt.shared.services.lookup.IBatchLookupService;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.service.SERVICES;

/**
 * Columns are defined as inner classes<br>
 * for every inner column class there is a generated getXYColumn method directly
 * on the table
 */
public abstract class AbstractTable extends AbstractPropertyObserver implements ITable, IContributionOwner, IExtensibleObject {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTable.class);

  private boolean m_initialized;
  private final OptimisticLock m_initLock;
  private ColumnSet m_columnSet;
  /**
   * synchronized list
   */
  private final List<ITableRow> m_rows;
  private final Object m_cachedRowsLock;
  private List<ITableRow> m_cachedRows;
  private final Map<CompositeObject, ITableRow> m_deletedRows;
  private List<ITableRow/* ordered by rowIndex */> m_selectedRows = new ArrayList<ITableRow>();
  private Map<Class<?>, Class<? extends IMenu>> m_menuReplacementMapping;
  private ITableUIFacade m_uiFacade;
  private final List<ITableRowFilter> m_rowFilters;
  private String m_userPreferenceContext;
  // batch mutation
  private boolean m_autoDiscardOnDelete;
  private boolean m_sortEnabled;
  private boolean m_sortValid;
  private boolean m_initialMultiLineText;
  private int m_tableChanging;
  private List<TableEvent> m_tableEventBuffer = new ArrayList<TableEvent>();
  private final HashSet<P_CellLookup> m_cellLookupBuffer = new HashSet<P_CellLookup>();
  private HashSet<ITableRow> m_rowDecorationBuffer = new HashSet<ITableRow>();
  // key stroke buffer for select-as-you-type
  private final KeyStrokeBuffer m_keyStrokeBuffer;
  private final EventListenerList m_listenerList = new EventListenerList();
  //cell editing
  private P_CellEditorContext m_editContext;
  private final Set<ITableRow> m_rowValidty;
  //checkable table
  private IBooleanColumn m_checkableColumn;
  //auto filter
  private final Object m_cachedFilteredRowsLock;
  private List<ITableRow> m_cachedFilteredRows;
  private IEventHistory<TableEvent> m_eventHistory;
  private ContributionComposite m_contributionHolder;
  private final ObjectExtensions<AbstractTable, ITableExtension<? extends AbstractTable>> m_objectExtensions;
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
    m_objectExtensions = new ObjectExtensions<AbstractTable, ITableExtension<? extends AbstractTable>>(this);
    //add single observer listener
    addTableListener(new P_TableListener());
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<? extends ITableExtension<? extends AbstractTable>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITableExtension<? extends AbstractTable> createLocalExtension() {
    return new LocalTableExtension<AbstractTable>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected void callInitializer() {
    interceptInitConfig();
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass()) + ITypeWithClassId.ID_CONCAT_SYMBOL + getContainer().classId();
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
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
   * Subclasses can override this method. Default is {@code false}.
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
   *          Table rows that were dragged (unmodifiable list).
   * @return A transferable object representing the given rows.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(10)
  protected TransferObject execDrag(List<ITableRow> rows) throws ProcessingException {
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
  protected TransferObject execCopy(List<? extends ITableRow> rows) throws ProcessingException {
    if (!CollectionUtility.hasElements(rows)) {
      return null;
    }

    StringBuilder plainText = new StringBuilder();
    StringBuilder htmlText = new StringBuilder("<html>");
    // Adding the following MS-office specific style information will cause Excel
    // to put all line-break-delimited entries of a <td> cell into a single Excel cell,
    // instead of one sub-cell for each <br />
    htmlText.append("<head><style type=\"text/css\"> br {mso-data-placement:same-cell;} </style></head>");
    htmlText.append("<body><table border=\"0\">");

    List<IColumn<?>> columns = getColumnSet().getVisibleColumns();
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
   * @deprecated use {@link #interceptRowClick(ITableRow, MouseButton)} instead. Will be removed with V5.0.
   */
  @Deprecated
  protected void execRowClick(ITableRow row) throws ProcessingException {

  }

  /**
   * Called when the user clicks on a row in this table.
   * <p>
   * Subclasses can override this method. The default fires a {@link TableEvent#TYPE_ROW_CLICK} event.
   *
   * @param Row
   *          that was clicked (never null).
   * @param mouseButton
   *          the mouse button ({@link MouseButton}) which triggered this method
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(80)
  protected void execRowClick(ITableRow row, MouseButton mouseButton) throws ProcessingException {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_CLICK, CollectionUtility.arrayList(row));
    fireTableEventInternal(e);
    execRowClick(row);
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
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_ACTION, CollectionUtility.arrayList(row));
      fireTableEventInternal(e);
    }
  }

  /**
   * Called when one or more rows are selected.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param rows
   *          a unmodifiable list of selected rows.
   *          that were selected.
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(100)
  protected void execRowsSelected(List<? extends ITableRow> rows) throws ProcessingException {
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

  /**
   * This method is called during initializing the table and is thought to add header menus to the given collection of
   * menus. Menus added in this method should be of menu type {@link ITableMenu.TableMenuType#Header}.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          a live collection of the menus. Add additional header menus to this list optionally add some separators at
   *          the end.
   */
  protected void execCreateHeaderMenus(OrderedCollection<IMenu> menus) {
    // header menus
    if (getTableCustomizer() != null) {
      menus.addLast(new AddCustomColumnMenu(this));
      menus.addLast(new ModifyCustomColumnMenu(this));
      menus.addLast(new RemoveCustomColumnMenu(this));
    }
    if (menus.size() > 0) {
      menus.addLast(new MenuSeparator());
    }
    menus.addLast(new ResetColumnsMenu(this));
    menus.addLast(new OrganizeColumnsMenu(this));
    menus.addLast(new ColumnFilterMenu(this));
    menus.addLast(new CopyWidthsOfColumnsMenu(this));
    menus.addLast(new MenuSeparator());
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  private List<Class<? extends IColumn>> getConfiguredColumns() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IColumn>> foca = ConfigurationUtility.filterClasses(dca, IColumn.class);
    return ConfigurationUtility.removeReplacedClasses(foca);
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_eventHistory = createEventHistory();
    m_uiFacade = createUIFacade();
    m_contributionHolder = new ContributionComposite(this);
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

    List<Class<? extends IMenu>> ma = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    Map<Class<?>, Class<? extends IMenu>> replacements = ConfigurationUtility.getReplacementMapping(ma);
    if (!replacements.isEmpty()) {
      m_menuReplacementMapping = replacements;
    }
    for (Class<? extends IMenu> clazz : ma) {
      try {
        IMenu menu = ConfigurationUtility.newInnerInstance(this, clazz);
        menus.addOrdered(menu);
      }
      catch (Throwable t) {
        String className = "null";
        if (clazz != null) {
          className = clazz.getName();
        }
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + className + "'.", t));
      }
    }
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);
    try {
      injectMenusInternal(menus);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contributing menus.", e);
    }

    execCreateHeaderMenus(menus);
    //set container on menus
    for (IMenu menu : menus) {
      menu.setContainerInternal(this);
    }

    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    ITableContextMenu contextMenu = new TableContextMenu(this, menus.getOrderedList());
    setContextMenu(contextMenu);

    // key strokes
    List<Class<? extends IKeyStroke>> ksClasses = getConfiguredKeyStrokes();
    ArrayList<IKeyStroke> ksList = new ArrayList<IKeyStroke>(ksClasses.size());
    for (Class<? extends IKeyStroke> clazz : ksClasses) {
      try {
        IKeyStroke ks = ConfigurationUtility.newInnerInstance(this, clazz);
        ksList.add(ks);
      }
      catch (Throwable t) {
        String className = "null";
        if (clazz != null) {
          className = clazz.getName();
        }
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + className + "'.", t));
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
    // add keystroke contributions
    List<IKeyStroke> contributedKeyStrokes = m_contributionHolder.getContributionsByClass(IKeyStroke.class);
    ksList.addAll(contributedKeyStrokes);
    setKeyStrokes(ksList);

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
                e.setDragObject(interceptDrag(e.getRows()));
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
                interceptDrop(e.getFirstRow(), e.getDropObject());
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
                e.setCopyObject(interceptCopy(e.getRows()));
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
              interceptContentChanged();
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
    List<Class<? extends IColumn>> ca = getConfiguredColumns();
    OrderedCollection<IColumn<?>> columns = new OrderedCollection<IColumn<?>>();

    // configured columns
    for (Class<? extends IColumn> clazz : ca) {
      try {
        IColumn<?> column = ConfigurationUtility.newInnerInstance(this, clazz);
        columns.addOrdered(column);
      }
      catch (Exception e) {
        String className = "null";
        if (clazz != null) {
          className = clazz.getName();
        }
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + className + "'.", e));
      }
    }

    // contributed columns
    List<IColumn> contributedColumns = m_contributionHolder.getContributionsByClass(IColumn.class);
    for (IColumn c : contributedColumns) {
      columns.addOrdered(c);
    }

    // dynamically injected columns
    try {
      injectColumnsInternal(columns);
    }
    catch (Exception e) {
      LOG.error("error occured while dynamically contribute columns.", e);
    }

    // move columns
    ExtensionUtility.moveModelObjects(columns);

    m_columnSet = new ColumnSet(this, columns.getOrderedList());
    if (getConfiguredCheckableColumn() != null) {
      AbstractBooleanColumn checkableColumn = getColumnSet().getColumnByClass(getConfiguredCheckableColumn());
      setCheckableColumn(checkableColumn);
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic columns<br>
   * To change the order or specify the insert position use {@link IColumn#setOrder(double)}.
   *
   * @param columns
   *          live and mutable collection of configured columns, not yet initialized
   */
  protected void injectColumnsInternal(OrderedCollection<IColumn<?>> columns) {
    ITableCustomizer c = getTableCustomizer();
    if (c != null) {
      c.injectCustomColumns(columns);
    }
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to manage menu list and add/remove menus.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
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
          ActionUtility.initActions(getMenus());
          interceptInitTable();
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
      interceptDisposeTable();
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
        interceptHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals(LOCAL_URL_HOST));
      }
      finally {
        m_actionRunning = false;
      }
    }
  }

  @Override
  public List<ITableRowFilter> getRowFilters() {
    return CollectionUtility.arrayList(m_rowFilters);
  }

  @Override
  public void addRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      //avoid duplicate add
      if (!m_rowFilters.contains(filter)) {
        m_rowFilters.add(filter);
        applyRowFilters();
      }
    }
  }

  @Override
  public void removeRowFilter(ITableRowFilter filter) {
    if (filter != null) {
      if (m_rowFilters.remove(filter)) {
        applyRowFilters();
      }
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
  public List<IColumn<?>> getColumns() {
    return getColumnSet().getColumns();
  }

  @Override
  public List<String> getColumnNames() {
    List<String> columnNames = new ArrayList<String>(getColumnCount());
    for (IColumn col : getColumns()) {
      columnNames.add(col.getHeaderCell().getText());
    }
    return columnNames;
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
    List<IColumn<?>> a = getColumnSet().getSummaryColumns();
    if (a.size() == 0) {
      IColumn<?> col = getColumnSet().getFirstDefinedVisibileColumn();
      if (col != null) {
        a = CollectionUtility.<IColumn<?>> arrayList(col);
      }
    }
    if (a.isEmpty()) {
      return new Cell();
    }
    else if (a.size() == 1) {
      Cell cell = new Cell(getCell(row, a.get(0)));
      if (cell.getIconId() == null) {
        // use icon of row
        cell.setIconId(row.getIconId());
      }
      return cell;
    }
    else {
      Cell cell = new Cell(getCell(row, a.get(0)));
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
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(propertySupport.<IKeyStroke> getPropertyList(PROP_KEY_STROKES));
  }

  @Override
  public void setKeyStrokes(List<? extends IKeyStroke> keyStrokes0) {
    propertySupport.setPropertyList(PROP_KEY_STROKES, CollectionUtility.arrayListWithoutNullElements(keyStrokes0));
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
    return interceptCreateTableRowDataMapper(rowType);
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
    List<ITableRow> deletedRows = getDeletedRows();
    for (ITableRow delRow : deletedRows) {
      if (rowMapper.acceptExport(delRow)) {
        AbstractTableRowData rowData = target.addRow();
        rowMapper.exportTableRowData(delRow, rowData);
        rowData.setRowState(AbstractTableRowData.STATUS_DELETED);
      }
    }
  }

  @Override
  public void importFromTableBeanData(AbstractTableFieldBeanData source) throws ProcessingException {
    importFromTableRowBeanData(CollectionUtility.arrayList(source.getRows()), source.getRowType());
  }

  public void importFromTableRowBeanData(List<? extends AbstractTableRowData> rowDatas, Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
    discardAllDeletedRows();
    clearValidatedValuesOnAllColumns();
    clearAllRowsValidity();
    int deleteCount = 0;
    List<ITableRow> newRows = new ArrayList<ITableRow>(rowDatas.size());
    ITableRowDataMapper mapper = createTableRowDataMapper(rowType);
    for (int i = 0, ni = rowDatas.size(); i < ni; i++) {
      AbstractTableRowData rowData = rowDatas.get(i);
      if (rowData.getRowState() != AbstractTableFieldData.STATUS_DELETED && mapper.acceptImport(rowData)) {
        ITableRow newTableRow = new TableRow(getColumnSet());
        mapper.importTableRowData(newTableRow, rowData);
        newRows.add(newTableRow);
      }
      else {
        deleteCount++;
      }
    }
    replaceRows(newRows);
    if (deleteCount > 0) {
      try {
        setTableChanging(true);
        //
        for (int i = 0, ni = rowDatas.size(); i < ni; i++) {
          AbstractTableRowData rowData = rowDatas.get(i);
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
    for (ITableRow delRow : getDeletedRows()) {
      int newRowIndex = target.addRow();
      for (int j = 0, nj = delRow.getCellCount(); j < nj; j++) {
        target.setValueAt(newRowIndex, j, delRow.getCellValue(j));
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
      List<ITableRow> newRows = new ArrayList<ITableRow>();
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
      replaceRows(newRows);
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
  public void setMenus(List<? extends IMenu> menus) {
    getContextMenu().setChildActions(menus);
  }

  @Override
  public void addMenu(IMenu menu) {
    List<IMenu> menus = getMenus();
    menus.add(menu);
    setMenus(menus);
  }

  protected void setContextMenu(ITableContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public ITableContextMenu getContextMenu() {
    return (ITableContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <T extends IMenu> T getMenu(final Class<T> menuType) {
    IContextMenu contextMenu = getContextMenu();
    if (contextMenu != null) {
      final Holder<T> resultHolder = new Holder<T>();
      contextMenu.acceptVisitor(new IActionVisitor() {

        @SuppressWarnings("unchecked")
        @Override
        public int visit(IAction action) {
          if (menuType.isAssignableFrom(action.getClass())) {
            resultHolder.setValue((T) action);
            return CANCEL;
          }
          return CONTINUE;
        }
      });
      return resultHolder.getValue();
    }
    return null;
  }

  @SuppressWarnings("deprecation")
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
          m.aboutToShow();
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

  public List<ITableRow> createRowsByArray(Object dataArray) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByArray(dataArray);
  }

  public List<ITableRow> createRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByArray(dataArray, rowStatus);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new
   * AtomicReference<Object>(Object[][])
   * so that the further processing can set the content of the holder to null while processing.
   */
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) throws ProcessingException {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference, rowStatus);
  }

  public List<ITableRow> createRowsByCodes(Collection<? extends ICode<?>> codes) throws ProcessingException {
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
  public void replaceRows(List<? extends ITableRow> newRows) throws ProcessingException {
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

  private void replaceRowsCase1(List<? extends ITableRow> newRows) throws ProcessingException {
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
      selectRows(selectedRows, false);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void replaceRowsCase2(List<? extends ITableRow> newRows) throws ProcessingException {
    try {
      setTableChanging(true);
      //
      int[] oldToNew = new int[getRowCount()];
      int[] newToOld = new int[newRows.size()];
      Arrays.fill(oldToNew, -1);
      Arrays.fill(newToOld, -1);
      HashMap<CompositeObject, Integer> newRowIndexMap = new HashMap<CompositeObject, Integer>();
      for (int i = newRows.size() - 1; i >= 0; i--) {
        newRowIndexMap.put(new CompositeObject(getRowKeys(newRows.get(i))), Integer.valueOf(i));
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
      List<ITableRow> updatedRows = new ArrayList<ITableRow>(mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] >= 0) {
          ITableRow oldRow = getRow(i);
          ITableRow newRow = newRows.get(oldToNew[i]);
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

          updatedRows.add(oldRow);
        }
      }
      List<ITableRow> deletedRows = new ArrayList<ITableRow>(getRowCount() - mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] < 0) {
          deletedRows.add(m_rows.get(i));
        }
      }
      List<ITableRow> insertedRows = new ArrayList<ITableRow>(newRows.size() - mappedCount);
      int[] insertedRowIndexes = new int[newRows.size() - mappedCount];
      int index = 0;
      for (int i = 0; i < newToOld.length; i++) {
        if (newToOld[i] < 0) {
          insertedRows.add(newRows.get(i));
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
      updateRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void updateAllRows() {
    updateRows(getRows());
  }

  @Override
  public void setRowState(ITableRow row, int rowState) throws ProcessingException {
    setRowState(CollectionUtility.arrayList(row), rowState);
  }

  @Override
  public void setAllRowState(int rowState) throws ProcessingException {
    setRowState(getRows(), rowState);
  }

  @Override
  public void setRowState(Collection<? extends ITableRow> rows, int rowState) throws ProcessingException {
    try {
      setTableChanging(true);
      //
      for (ITableRow row : rows) {
        row.setStatus(rowState);
      }
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void updateRows(Collection<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      //
      List<ITableRow> resolvedRowList = new ArrayList<ITableRow>(rows.size());
      for (ITableRow row : rows) {
        ITableRow resolvedRow = resolveRow(row);
        if (resolvedRow != null) {
          resolvedRowList.add(resolvedRow);
          updateRowImpl(resolvedRow);
        }
      }
      if (resolvedRowList.size() > 0) {
        fireRowsUpdated(resolvedRowList);
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
    return CollectionUtility.firstElement(m_selectedRows);
  }

  @Override
  public List<ITableRow> getSelectedRows() {
    return CollectionUtility.arrayList(m_selectedRows);
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
    selectRows(CollectionUtility.arrayList(row), append);
  }

  @Override
  public void selectRows(List<? extends ITableRow> rows) {
    selectRows(rows, false);
  }

  @Override
  public void selectRows(List<? extends ITableRow> rows, boolean append) {
    rows = resolveRows(rows);
    TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
    if (append) {
      newSelection.addAll(m_selectedRows);
      newSelection.addAll(rows);
    }
    else {
      newSelection.addAll(rows);
    }
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITableRow first = newSelection.first();
      newSelection.clear();
      newSelection.add(first);
    }
    if (!CollectionUtility.equalsCollection(m_selectedRows, newSelection, true)) {
      m_selectedRows = new ArrayList<ITableRow>(newSelection);
      // notify menus
      List<ITableRow> notificationCopy = CollectionUtility.arrayList(m_selectedRows);

      fireRowsSelected(notificationCopy);
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
      deselectRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void deselectRows(List<? extends ITableRow> rows) {
    rows = resolveRows(rows);
    if (CollectionUtility.hasElements(rows)) {
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      if (newSelection.removeAll(rows)) {
        m_selectedRows = new ArrayList<ITableRow>(newSelection);
        fireRowsSelected(m_selectedRows);
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
    List<ITableRow> newList = new ArrayList<ITableRow>();
    for (int i = 0, ni = getRowCount(); i < ni; i++) {
      ITableRow row = getRow(i);
      if (row.isEnabled()) {
        newList.add(row);
      }
      else if (isSelectedRow(row)) {
        newList.add(row);
      }
    }
    selectRows(newList, false);
  }

  @Override
  public void deselectAllEnabledRows() {
    List<ITableRow> selectedRows = getSelectedRows();
    ArrayList<ITableRow> newList = new ArrayList<ITableRow>();
    for (ITableRow selectedRow : selectedRows) {
      if (selectedRow.isEnabled()) {
        newList.add(selectedRow);
      }
    }
    deselectRows(newList);
  }

  @Override
  public Collection<ITableRow> getCheckedRows() {
    final List<ITableRow> checkedRows = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.isChecked()) {
        checkedRows.add(row);
      }
    }
    return checkedRows;
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
    if (!isMultiCheck() && value && getCheckedRows().size() > 0) {
      uncheckAllRows();
    }
    row.setChecked(value);
    if (getCheckableColumn() != null) {
      getCheckableColumn().setValue(row, value);
    }
  }

  @Override
  public void checkRows(Collection<? extends ITableRow> rows, boolean value) throws ProcessingException {
    rows = resolveRows(rows);
    // check checked count with multicheck
    if (rows.size() > 1 && !isMultiCheck()) {
      ITableRow first = CollectionUtility.firstElement(rows);
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
        for (ITableRow row : getRows()) {
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
    return CollectionUtility.getElement(getRows(), rowIndex);
  }

  @Override
  public List<ITableRow> getRows() {
    //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
    synchronized (m_cachedRowsLock) {
      if (m_cachedRows == null) {
        //this code must be thread-safe
        m_cachedRows = CollectionUtility.arrayList(m_rows);
      }
      return m_cachedRows;
    }
  }

  @Override
  public List<ITableRow> getFilteredRows() {
    List<ITableRow> rows = getRows();
    if (m_rowFilters.size() > 0) {
      //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
      synchronized (m_cachedFilteredRowsLock) {
        if (m_cachedFilteredRows == null) {
          //this code must be thread-safe
          if (m_rowFilters.size() > 0) {
            List<ITableRow> filteredRows = new ArrayList<ITableRow>(getRowCount());
            for (ITableRow row : rows) {
              if (row != null && row.isFilterAccepted()) {
                filteredRows.add(row);
              }
            }
            m_cachedFilteredRows = filteredRows;
          }
          else {
            m_cachedFilteredRows = CollectionUtility.emptyArrayList();
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
      return getFilteredRows().size();
    }
    else {
      return getRowCount();
    }
  }

  @Override
  public ITableRow getFilteredRow(int index) {
    if (m_rowFilters.size() > 0) {
      ITableRow row = null;
      List<ITableRow> filteredRows = getFilteredRows();
      if (index >= 0 && index < filteredRows.size()) {
        row = filteredRows.get(index);
      }
      return row;
    }
    else {
      return getRow(index);
    }
  }

  @Override
  public int getFilteredRowIndex(ITableRow row) {
    return getFilteredRows().indexOf(row);
  }

  @Override
  public List<ITableRow> getNotDeletedRows() {
    List<ITableRow> notDeletedRows = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() != ITableRow.STATUS_DELETED) {
        notDeletedRows.add(row);
      }
    }
    return notDeletedRows;
  }

  @Override
  public int getNotDeletedRowCount() {
    return getNotDeletedRows().size();
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
  public Object[][] exportTableRowsAsCSV(List<? extends ITableRow> rows, List<? extends IColumn> columns, boolean includeLineForColumnNames, boolean includeLineForColumnTypes, boolean includeLineForColumnFormat) {
    return TableUtility.exportRowsAsCSV(rows, columns, includeLineForColumnNames, includeLineForColumnTypes, includeLineForColumnFormat);
  }

  @Override
  public List<ITableRow> getRows(int[] rowIndexes) {
    if (rowIndexes == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<ITableRow> result = new ArrayList<ITableRow>(rowIndexes.length);
    for (int rowIndex : rowIndexes) {
      ITableRow row = getRow(rowIndex);
      if (row != null) {
        result.add(row);
      }
    }
    return result;
  }

  /**
   * @return a copy of a deleted row<br>
   *         when the row is changed it has to be applied to the table using
   *         modifyRow(row);
   */
  @Override
  public List<ITableRow> getDeletedRows() {
    return CollectionUtility.arrayList(m_deletedRows.values());
  }

  @Override
  public int getInsertedRowCount() {
    int count = 0;
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_INSERTED) {
        count++;
      }
    }
    return count;
  }

  @Override
  public List<ITableRow> getInsertedRows() {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_INSERTED) {
        rowList.add(row);
      }
    }
    return rowList;
  }

  @Override
  public int getUpdatedRowCount() {
    int count = 0;
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_UPDATED) {
        count++;
      }
    }
    return count;
  }

  @Override
  public List<ITableRow> getUpdatedRows() {
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (ITableRow row : getRows()) {
      if (row.getStatus() == ITableRow.STATUS_UPDATED) {
        rowList.add(row);
      }
    }
    return rowList;
  }

  /**
   * Convenience to add row by data only
   */
  @Override
  public ITableRow addRowByArray(Object dataArray) throws ProcessingException {
    if (dataArray == null) {
      return null;
    }
    List<ITableRow> result = addRowsByMatrix(new Object[]{dataArray});
    return CollectionUtility.firstElement(result);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix) throws ProcessingException {
    return addRowsByMatrix(dataMatrix, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix, int rowStatus) throws ProcessingException {
    return addRows(createRowsByMatrix(dataMatrix, rowStatus));
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray) throws ProcessingException {
    return addRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray, int rowStatus) throws ProcessingException {
    return addRows(createRowsByArray(dataArray, rowStatus));
  }

  @Override
  public ITableRow addRow(ITableRow newRow) throws ProcessingException {
    return addRow(newRow, false);
  }

  @Override
  public ITableRow addRow(ITableRow newRow, boolean markAsInserted) throws ProcessingException {
    List<ITableRow> addedRows = addRows(CollectionUtility.arrayList(newRow), markAsInserted);
    return CollectionUtility.firstElement(addedRows);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows) throws ProcessingException {
    return addRows(newRows, false);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted) throws ProcessingException {
    return addRows(newRows, markAsInserted, null);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted, int[] insertIndexes) throws ProcessingException {
    if (newRows == null) {
      return CollectionUtility.emptyArrayList();
    }
    try {
      setTableChanging(true);
      //
      int oldRowCount = m_rows.size();
      List<ITableRow> newIRows = new ArrayList<ITableRow>(newRows.size());
      for (ITableRow newRow : newRows) {
        newIRows.add(addRowImpl(newRow, markAsInserted));
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
          sortArray[insertIndexes[i]] = newIRows.get(i);
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
        for (int i = insertIndexes.length; i < newIRows.size(); i++) {
          // find next empty slot
          while (sortArray[sortArrayIndex] != null) {
            sortArrayIndex++;
          }
          sortArray[sortArrayIndex] = newIRows.get(i);
        }
        sortInternal(Arrays.asList(sortArray));
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
      int sourceIndex = movingRow.getRowIndex();
      int targetIndex = targetRow.getRowIndex();
      if (sourceIndex < targetIndex) {
        moveRowImpl(sourceIndex, targetIndex - 1);
      }
      else {
        moveRowImpl(sourceIndex, targetIndex);
      }
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
      int sourceIndex = movingRow.getRowIndex();
      int targetIndex = targetRow.getRowIndex();
      if (sourceIndex > targetIndex) {
        moveRowImpl(sourceIndex, targetIndex + 1);
      }
      else {
        moveRowImpl(sourceIndex, targetIndex);
      }
    }
  }

  /**
   * @see {@link List#add(int, Object)}
   * @param sourceIndex
   * @param targetIndex
   */
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
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0; i < rowIndexes.length; i++) {
      ITableRow row = getRow(rowIndexes[i]);
      if (row != null) {
        rowList.add(row);
      }
    }
    deleteRows(rowList);
  }

  @Override
  public void deleteRow(ITableRow row) {
    if (row != null) {
      deleteRows(CollectionUtility.arrayList(row));
    }
  }

  @Override
  public void deleteAllRows() {
    deleteRows(getRows());
  }

  @Override
  public void deleteRows(Collection<? extends ITableRow> rows) {

    List<ITableRow> existingRows = getRows();
    //peformance quick-check
    if (rows != existingRows) {
      rows = resolveRows(rows);
    }
    if (CollectionUtility.hasElements(rows)) {
      try {
        setTableChanging(true);
        //
        int rowCountBefore = getRowCount();
        int min = getRowCount();
        int max = 0;
        for (ITableRow row : rows) {
          min = Math.min(min, row.getRowIndex());
          max = Math.max(max, row.getRowIndex());
        }
        List<ITableRow> deletedRows = new ArrayList<ITableRow>(rows);
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
          for (int i = deletedRows.size() - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows.get(i);
            if (candidateRow != null) {
              deleteRowImpl(candidateRow);
            }
          }
        }
        else {
          for (int i = deletedRows.size() - 1; i >= 0; i--) {
            ITableRow candidateRow = deletedRows.get(i);
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
        List<ITableRow> selectionRows = new ArrayList<ITableRow>(getSelectedRows());
        int minAffectedIndex = Math.max(min - 1, 0);
        ITableRow[] affectedRows = new ITableRow[getRowCount() - minAffectedIndex];
        for (int i = minAffectedIndex; i < getRowCount(); i++) {
          affectedRows[i - minAffectedIndex] = getRow(i);
          ((InternalTableRow) affectedRows[i - minAffectedIndex]).setRowIndex(i);
          selectionRows.remove(getRow(i));
        }
        if (rowCountBefore == deletedRows.size()) {
          fireAllRowsDeleted(deletedRows);
        }
        else {
          fireRowsDeleted(deletedRows);
        }
        selectRows(selectionRows, false);
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
    List<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int rIndex : rowIndexes) {
      ITableRow row = getRow(rIndex);
      if (row != null) {
        rowList.add(row);
      }
    }
    discardRows(rowList);
  }

  @Override
  public void discardRow(ITableRow row) {
    if (row != null) {
      discardRows(CollectionUtility.arrayList(row));
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
  public void discardRows(Collection<? extends ITableRow> rows) {
    try {
      setTableChanging(true);
      //
      for (ITableRow row : rows) {
        ((InternalTableRow) row).setStatus(ITableRow.STATUS_INSERTED);
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
      discardDeletedRows(CollectionUtility.arrayList(deletedRow));
    }
  }

  @Override
  public void discardDeletedRows(Collection<? extends ITableRow> deletedRows) {
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
  public List<Object> getRowKeys(int rowIndex) {
    ITableRow row = getRow(rowIndex);
    return getRowKeys(row);
  }

  @Override
  public List<Object> getRowKeys(ITableRow row) {
    if (row != null) {
      return row.getKeyValues();
    }
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public ITableRow findRowByKey(List<?> keys) {
    List<IColumn<?>> keyColumns = getColumnSet().getKeyColumns();
    if (keyColumns.size() == 0) {
      keyColumns = getColumnSet().getColumns();
    }
    for (ITableRow row : m_rows) {
      boolean match = true;
      if (CollectionUtility.hasElements(keys)) {
        for (int i = 0; i < keyColumns.size() && i < keys.size(); i++) {
          if (!CompareUtility.equals(keyColumns.get(i).getValue(row), keys.get(i))) {
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
    return (ITableColumnFilterManager) propertySupport.getProperty(PROP_COLUMN_FILTER_MANAGER);
  }

  @Override
  public void setColumnFilterManager(ITableColumnFilterManager m) {
    propertySupport.setProperty(PROP_COLUMN_FILTER_MANAGER, m);
  }

  @Override
  public ITableCustomizer getTableCustomizer() {
    return (ITableCustomizer) propertySupport.getProperty(PROP_TABLE_CUSTOMIZER);
  }

  @Override
  public void setTableCustomizer(ITableCustomizer c) {
    propertySupport.setProperty(PROP_TABLE_CUSTOMIZER, c);
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
        List<IColumn<?>> sortCols = getColumnSet().getSortColumns();
        if (sortCols.size() > 0) {
          // first make sure decorations and lookups are up-to-date
          processDecorationBuffer();
          List<ITableRow> a = new ArrayList<ITableRow>(getRows());
          Collections.sort(a, new TableRowComparator(sortCols));
          sortInternal(a);
        }
      }
    }
    finally {
      m_sortValid = true;
    }
  }

  @Override
  public void sort(List<? extends ITableRow> rowsInNewOrder) {
    List<ITableRow> resolvedRows = resolveRows(rowsInNewOrder);
    if (resolvedRows.size() == rowsInNewOrder.size()) {
      sortInternal(resolvedRows);
    }
    else {
      // check which rows could not be mapped
      ArrayList<ITableRow> list = new ArrayList<ITableRow>();
      list.addAll(m_rows);
      list.removeAll(resolvedRows);
      ArrayList<ITableRow> sortedList = new ArrayList<ITableRow>();
      sortedList.addAll(resolvedRows);
      sortedList.addAll(list);
      sortInternal(sortedList);
    }
  }

  private void sortInternal(List<? extends ITableRow> resolvedRows) {
    int i = 0;
    for (ITableRow row : resolvedRows) {
      ((InternalTableRow) row).setRowIndex(i);
      i++;
    }
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      m_rows.clear();
      m_rows.addAll(resolvedRows);
    }
    //sort selection without firing an event
    if (m_selectedRows != null && m_selectedRows.size() > 0) {
      TreeSet<ITableRow> newSelection = new TreeSet<ITableRow>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      m_selectedRows = new ArrayList<ITableRow>(newSelection);
    }
    fireRowOrderChanged();
  }

  @SuppressWarnings("unchecked")
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
      m_objectExtensions.runInExtensionContext(new Runnable() {
        @Override
        public void run() {
          // enforce recreation of the contributed columns so column indexes etc. will be reset
          m_contributionHolder.resetContributionsByClass(AbstractTable.this, IColumn.class);
          // runs within extension context, so that extensions and contributions can be created
          createColumnsInternal();
        }
      });
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
          for (ITableColumnFilter filter : filterManager.getFilters()) {
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
        interceptResetColumns(visibility, order, sorting, widths);
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
      getColumnSet().setVisibleColumns(list);
    }
    //Order
    if (order) {
      TreeMap<CompositeObject, IColumn<?>> orderMap = new TreeMap<CompositeObject, IColumn<?>>();
      int index = 0;
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable() && col.isVisible()) {
          orderMap.put(new CompositeObject(col.getOrder(), index), col);
          index++;
        }
      }
      getColumnSet().setVisibleColumns(orderMap.values());
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
            IContentAssistColumn<?, ?> col = lookup.getColumn();
            ILookupCall<?> call = col.prepareLookupCall(row);
            if (call != null && call.getKey() != null) {
              //split: local vs remote
              if (call instanceof LocalLookupCall) {
                List<ILookupRow<?>> result = lookupResultCache.getDataByKey(call);
                applyLookupResult((InternalTableRow) row, col.getColumnIndex(), result);
              }
              else {
                tableRowList.add(row);
                columnIndexList.add(Integer.valueOf(col.getColumnIndex()));
                batchCall.addLookupCall(call);
              }
            }
            else {
              applyLookupResult((InternalTableRow) row, col.getColumnIndex(), new ArrayList<ILookupRow<?>>(0));
            }
          }
        }
      }
      m_cellLookupBuffer.clear();
      //
      if (batchCall != null && tableRowList != null && columnIndexList != null && !batchCall.isEmpty()) {
        ITableRow[] tableRows = tableRowList.toArray(new ITableRow[tableRowList.size()]);
        List<List<ILookupRow<?>>> resultArray;
        IBatchLookupService service = SERVICES.getService(IBatchLookupService.class);
        resultArray = service.getBatchDataByKey(batchCall);
        for (int i = 0; i < tableRows.length; i++) {
          applyLookupResult((InternalTableRow) tableRows[i], ((Number) columnIndexList.get(i)).intValue(), resultArray.get(i));
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
      List<TableEvent> list = m_tableEventBuffer;
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
        Map<Integer, TableEvent> sortedCoalescedMap = new TreeMap<Integer, TableEvent>();
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
          fireTableEventBatchInternal(new ArrayList<TableEvent>(sortedCoalescedMap.values()));
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
      Set<IColumn<?>> colList = new LinkedHashSet<IColumn<?>>();
      for (TableEvent t : list) {
        if (t.getColumns() != null) {
          colList.addAll(t.getColumns());
        }
      }
      ce.setColumns(colList);
      //rows
      List<ITableRow> rowList = new ArrayList<ITableRow>();
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
      ce.setRows(rowList);
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
        if (column instanceof IContentAssistColumn) {
          IContentAssistColumn<?, ?> assistColumn = (IContentAssistColumn<?, ?>) column;
          if (assistColumn.getLookupCall() != null) {
            m_cellLookupBuffer.add(new P_CellLookup(row, assistColumn));
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

  private void applyLookupResult(InternalTableRow tableRow, int columnIndex, List<ILookupRow<?>> result) {
    // disable row changed trigger on row
    try {
      tableRow.setRowChanging(true);
      //
      Cell cell = (Cell) tableRow.getCell(columnIndex);
      if (result.size() == 1) {
        cell.setText(result.get(0).getText());
        cell.setTooltipText(result.get(0).getTooltipText());
      }
      else if (result.size() > 1) {
        StringBuffer buf = new StringBuffer();
        StringBuffer bufTooltip = new StringBuffer();

        for (int i = 0; i < result.size(); i++) {
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
          ILookupRow<?> row = result.get(i);
          buf.append(row.getText());
          bufTooltip.append(row.getTooltipText());
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
  public List<ITableRow> resolveRows(Collection<? extends ITableRow> rows) {
    if (rows == null) {
      rows = CollectionUtility.emptyArrayList();
    }
    List<ITableRow> resolvedRows = new ArrayList<ITableRow>(rows.size());
    for (ITableRow row : rows) {
      if (resolveRow(row) == row) {
        resolvedRows.add(row);
      }
      else {
        LOG.warn("could not resolve row " + row);
      }
    }
    return resolvedRows;
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
      interceptDecorateCell(cell, row, col);
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
      interceptDecorateRow(row);
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

  private void fireRowsInserted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_INSERTED, rows));
  }

  private void fireRowsUpdated(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_UPDATED, rows));
  }

  /**
   * Request to reload/replace table data with refreshed data
   */
  private void fireRowsDeleted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_DELETED, rows));
  }

  private void fireAllRowsDeleted(List<? extends ITableRow> rows) {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ALL_ROWS_DELETED, rows));
  }

  private void fireRowsSelected(List<? extends ITableRow> rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_SELECTED, rows));
  }

  private void fireRowClick(ITableRow row, MouseButton mouseButton) {
    if (row != null) {
      try {
        interceptRowClickSingleObserver(row, mouseButton);
        interceptRowClick(row, mouseButton);
      }
      catch (ProcessingException ex) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(ex);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(createNewUnexpectedProcessingException(t));
      }
    }
  }

  protected void interceptRowClickSingleObserver(ITableRow row, MouseButton mouseButton) throws ProcessingException {
    // Only toggle checked state if the table and row are enabled.
    if (!row.isEnabled() || !isEnabled()) {
      return;
    }

    // Only toggle checked state if being fired by the left mousebutton (https://bugs.eclipse.org/bugs/show_bug.cgi?id=453543).
    if (mouseButton != MouseButton.Left) {
      return;
    }

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
    else if (isCheckable()) {
      //row-level checkbox
      row.setChecked(!row.isChecked());
    }
  }

  private void fireRowAction(ITableRow row) {
    if (!m_actionRunning) {
      try {
        m_actionRunning = true;
        if (row != null) {
          try {
            interceptRowAction(row);
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
    e.setColumns(CollectionUtility.hashSet(column));
    e.setRows(CollectionUtility.arrayList(row));
    fireTableEventInternal(e);
  }

  private void fireRowFilterChanged() {
    synchronized (m_cachedFilteredRowsLock) {
      m_cachedFilteredRows = null;
    }
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROW_FILTER_CHANGED));
  }

  private TransferObject fireRowsDragRequest() {
    List<ITableRow> rows = getSelectedRows();
    if (CollectionUtility.hasElements(rows)) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_DRAG_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getDragObject();
    }
    else {
      return null;
    }
  }

  private void fireRowDropAction(ITableRow row, TransferObject dropData) {
    List<ITableRow> rows = null;
    if (row != null) {
      rows = CollectionUtility.arrayList(row);
    }
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_DROP_ACTION, rows);
    e.setDropObject(dropData);
    fireTableEventInternal(e);
  }

  private TransferObject fireRowsCopyRequest() {
    List<ITableRow> rows = getSelectedRows();
    if (CollectionUtility.hasElements(rows)) {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_COPY_REQUEST, rows);
      fireTableEventInternal(e);
      return e.getCopyObject();
    }
    else {
      return null;
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
   * @Deprecated use {@link #execCreateHeaderMenus(OrderedCollection)} instead.
   */
  @SuppressWarnings("deprecation")
  @ConfigOperation
  @Order(100)
  @Deprecated
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
  private void fireTableEventBatchInternal(List<? extends TableEvent> batch) {
    if (CollectionUtility.hasElements(batch)) {
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
        List<IColumn<?>> sortCols = getColumnSet().getSortColumns();
        if (sortCols.size() > 0) {
          col = CollectionUtility.lastElement(sortCols);
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
    public void fireRowClickFromUI(ITableRow row, MouseButton mouseButton) {
      try {
        pushUIProcessor();
        //
        row = resolveRow(row);
        if (row != null) {
          fireRowClick(resolveRow(row), mouseButton);
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
    public void fireVisibleColumnsChangedFromUI(Collection<IColumn<?>> visibleColumns) {
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
    public void setSelectedRowsFromUI(List<? extends ITableRow> rows) {
      try {
        pushUIProcessor();
        //
        Set<ITableRow> requestedRows = new HashSet<ITableRow>(resolveRows(rows));
        List<ITableRow> validRows = new ArrayList<ITableRow>();
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
        selectRows(validRows, false);
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

  private static class P_CellLookup {
    private final ITableRow m_row;
    private final IContentAssistColumn<?, ?> m_column;

    public P_CellLookup(ITableRow row, IContentAssistColumn<?, ?> col) {
      m_row = row;
      m_column = col;
    }

    public ITableRow getRow() {
      return m_row;
    }

    public IContentAssistColumn<?, ?> getColumn() {
      return m_column;
    }
  }// end private class

  private class P_TableRowBuilder extends AbstractTableRowBuilder<Object> {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getColumnSet());
    }

  }

  private class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      switch (e.getType()) {
        case TableEvent.TYPE_ROWS_SELECTED: {
          // single observer exec
          try {
            interceptRowsSelected(e.getRows());
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

  protected static class LocalTableExtension<TABLE extends AbstractTable> extends AbstractExtension<TABLE> implements ITableExtension<TABLE> {

    public LocalTableExtension(TABLE owner) {
      super(owner);
    }

    @Override
    public void execHyperlinkAction(TableHyperlinkActionChain chain, URL url, String path, boolean local) throws ProcessingException {
      getOwner().execHyperlinkAction(url, path, local);
    }

    @Override
    public void execRowAction(TableRowActionChain chain, ITableRow row) throws ProcessingException {
      getOwner().execRowAction(row);
    }

    @Override
    public void execContentChanged(TableContentChangedChain chain) throws ProcessingException {
      getOwner().execContentChanged();
    }

    @Override
    public ITableRowDataMapper execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain chain, Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
      return getOwner().execCreateTableRowDataMapper(rowType);
    }

    @Override
    public void execInitTable(TableInitTableChain chain) throws ProcessingException {
      getOwner().execInitTable();
    }

    @Override
    public void execResetColumns(TableResetColumnsChain chain, boolean visibility, boolean order, boolean sorting, boolean widths) throws ProcessingException {
      getOwner().execResetColumns(visibility, order, sorting, widths);
    }

    @Override
    public void execDecorateCell(TableDecorateCellChain chain, Cell view, ITableRow row, IColumn<?> col) throws ProcessingException {
      getOwner().execDecorateCell(view, row, col);
    }

    @Override
    public void execDrop(TableDropChain chain, ITableRow row, TransferObject t) throws ProcessingException {
      getOwner().execDrop(row, t);
    }

    @Override
    public void execDisposeTable(TableDisposeTableChain chain) throws ProcessingException {
      getOwner().execDisposeTable();
    }

    @Override
    public void execAddHeaderMenus(TableAddHeaderMenusChain chain, TableEvent e) throws ProcessingException {
      getOwner().execAddHeaderMenus(e);
    }

    @Override
    public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) throws ProcessingException {
      getOwner().execRowClick(row, mouseButton);
    }

    @Override
    public void execDecorateRow(TableDecorateRowChain chain, ITableRow row) throws ProcessingException {
      getOwner().execDecorateRow(row);
    }

    @Override
    public TransferObject execCopy(TableCopyChain chain, List<? extends ITableRow> rows) throws ProcessingException {
      return getOwner().execCopy(rows);
    }

    @Override
    public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) throws ProcessingException {
      getOwner().execRowsSelected(rows);
    }

    @Override
    public TransferObject execDrag(TableDragChain chain, List<ITableRow> rows) throws ProcessingException {
      return getOwner().execDrag(rows);
    }
  }

  protected final void interceptHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableHyperlinkActionChain chain = new TableHyperlinkActionChain(extensions);
    chain.execHyperlinkAction(url, path, local);
  }

  protected final void interceptRowAction(ITableRow row) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowActionChain chain = new TableRowActionChain(extensions);
    chain.execRowAction(row);
  }

  protected final void interceptContentChanged() throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableContentChangedChain chain = new TableContentChangedChain(extensions);
    chain.execContentChanged();
  }

  protected final ITableRowDataMapper interceptCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCreateTableRowDataMapperChain chain = new TableCreateTableRowDataMapperChain(extensions);
    return chain.execCreateTableRowDataMapper(rowType);
  }

  protected final void interceptInitTable() throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableInitTableChain chain = new TableInitTableChain(extensions);
    chain.execInitTable();
  }

  protected final void interceptResetColumns(boolean visibility, boolean order, boolean sorting, boolean widths) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableResetColumnsChain chain = new TableResetColumnsChain(extensions);
    chain.execResetColumns(visibility, order, sorting, widths);
  }

  protected final void interceptDecorateCell(Cell view, ITableRow row, IColumn<?> col) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateCellChain chain = new TableDecorateCellChain(extensions);
    chain.execDecorateCell(view, row, col);
  }

  protected final void interceptDrop(ITableRow row, TransferObject t) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDropChain chain = new TableDropChain(extensions);
    chain.execDrop(row, t);
  }

  protected final void interceptDisposeTable() throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDisposeTableChain chain = new TableDisposeTableChain(extensions);
    chain.execDisposeTable();
  }

  protected final void interceptAddHeaderMenus(TableEvent e) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableAddHeaderMenusChain chain = new TableAddHeaderMenusChain(extensions);
    chain.execAddHeaderMenus(e);
  }

  protected final void interceptRowClick(ITableRow row, MouseButton mouseButton) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowClickChain chain = new TableRowClickChain(extensions);
    chain.execRowClick(row, mouseButton);
  }

  protected final void interceptDecorateRow(ITableRow row) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateRowChain chain = new TableDecorateRowChain(extensions);
    chain.execDecorateRow(row);
  }

  protected final TransferObject interceptCopy(List<? extends ITableRow> rows) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCopyChain chain = new TableCopyChain(extensions);
    return chain.execCopy(rows);
  }

  protected final void interceptRowsSelected(List<? extends ITableRow> rows) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowsSelectedChain chain = new TableRowsSelectedChain(extensions);
    chain.execRowsSelected(rows);
  }

  protected final TransferObject interceptDrag(List<ITableRow> rows) throws ProcessingException {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDragChain chain = new TableDragChain(extensions);
    return chain.execDrag(rows);
  }
}
