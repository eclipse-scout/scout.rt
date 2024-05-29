/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.basic.table.ITableExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableContentChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCopyChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTableRowDataMapperChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableCreateTileChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDecorateRowChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDisposeTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDragChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableDropChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableInitTableChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableResetColumnsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowClickChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsCheckedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.TableChains.TableRowsSelectedChain;
import org.eclipse.scout.rt.client.res.AttachmentSupport;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IEventHistory;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.MouseButton;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.KeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITableContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TableContextMenu;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.controls.AbstractTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizerProvider;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.NullTableCustomizerProvider;
import org.eclipse.scout.rt.client.ui.basic.table.internal.InternalTableRow;
import org.eclipse.scout.rt.client.ui.basic.table.menus.OrganizeColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizerProvider;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.UserTableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IColumnAwareUserFilterState;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilter;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TextTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.eclipse.scout.rt.platform.util.visitor.CollectingVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeTraversals;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.ExtensionUtility;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Columns are defined as inner classes<br>
 * for every inner column class there is a generated getXYColumn method directly on the table
 */
@ClassId("e88f7f88-9747-40ea-88bd-744803aef7a7")
public abstract class AbstractTable extends AbstractWidget implements ITable, IContributionOwner, IExtensibleObject {

  private static final String AUTO_DISCARD_ON_DELETE = "AUTO_DISCARD_ON_DELETE";
  private static final String SORT_VALID = "SORT_VALID";
  private static final String INITIAL_MULTI_LINE_TEXT = "INITIAL_MULTI_LINE_TEXT";
  private static final String ACTION_RUNNING = "ACTION_RUNNING";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractTable.class);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(AUTO_DISCARD_ON_DELETE, SORT_VALID, INITIAL_MULTI_LINE_TEXT, ACTION_RUNNING);

  public interface IResetColumnsOption {
    String VISIBILITY = "visibility";
    String ORDER = "order";
    String SORTING = "sorting";
    String WIDTHS = "widths";
    String BACKGROUND_EFFECTS = "backgroundEffects";
    String FILTERS = "filters";
  }

  private final OptimisticLock m_initLock;
  private List<ITableRow> m_rows; // synchronized list
  private List<ITableRow> m_rootRows; // synchronized list
  private final Object m_cachedRowsLock;
  private final Map<CompositeObject, ITableRow> m_rowsByKey;
  private final Map<CompositeObject, ITableRow> m_deletedRows;
  private final List<ITableRowFilter> m_rowFilters;
  private final AttachmentSupport m_attachmentSupport;
  private final TableListeners m_listeners;
  private final Object m_cachedFilteredRowsLock;
  private final ObjectExtensions<AbstractTable, ITableExtension<? extends AbstractTable>> m_objectExtensions;

  /**
   * Provides 4 boolean flags.<br>
   * Used: {@link #AUTO_DISCARD_ON_DELETE}, {@link #SORT_VALID}, {@link #INITIAL_MULTI_LINE_TEXT},
   * {@link #ACTION_RUNNING}
   */
  private byte m_flags;
  private ColumnSet m_columnSet;
  private List<ITableRow> m_cachedRows;
  private List<ITableRow/* ordered by rowIndex */> m_selectedRows;
  private Set<ITableRow/* ordered by rowIndex */> m_checkedRows;
  private Map<Class<?>, Class<? extends IMenu>> m_menuReplacementMapping;
  private ITableUIFacade m_uiFacade;
  private String m_userPreferenceContext;
  private int m_tableChanging;
  private AbstractEventBuffer<TableEvent> m_eventBuffer;
  private int m_eventBufferLoopDetection;
  private Set<ITableRow> m_rowDecorationBuffer;
  private Map<Integer, Set<ITableRow>> m_rowValueChangeBuffer;
  private P_CellEditorContext m_editContext;
  private IBooleanColumn m_checkableColumn;
  private List<ITableRow> m_cachedFilteredRows;
  private IEventHistory<TableEvent> m_eventHistory;
  private ContributionComposite m_contributionHolder;
  private List<ITableControl> m_tableControls;
  private IReloadHandler m_reloadHandler;
  private ITableCompactHandler m_compactHandler;
  private ISummaryCellBuilder m_summaryCellBuilder;
  private int m_valueChangeTriggerEnabled = 1;// >=1 is true
  private boolean m_treeStructureDirty;

  public AbstractTable() {
    this(true);
  }

  public AbstractTable(boolean callInitializer) {
    super(false);
    m_selectedRows = new ArrayList<>();
    m_checkedRows = new LinkedHashSet<>();
    m_rowDecorationBuffer = new HashSet<>();
    m_rowValueChangeBuffer = new HashMap<>();
    m_listeners = new TableListeners();
    m_cachedRowsLock = new Object();
    m_cachedFilteredRowsLock = new Object();
    m_rows = Collections.synchronizedList(new ArrayList<>(1));
    m_rootRows = Collections.synchronizedList(new ArrayList<>(1));
    m_rowsByKey = Collections.synchronizedMap(new HashMap<>());
    m_deletedRows = new HashMap<>();
    m_rowFilters = new ArrayList<>(1);
    m_attachmentSupport = BEANS.get(AttachmentSupport.class);
    m_initLock = new OptimisticLock();
    m_objectExtensions = new ObjectExtensions<>(this, false);
    //add single observer listener
    addTableListener(e -> {
      try {
        interceptRowsSelected(e.getRows());
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }, TableEvent.TYPE_ROWS_SELECTED);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<? extends ITableExtension<? extends AbstractTable>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected ITableExtension<? extends AbstractTable> createLocalExtension() {
    return new LocalTableExtension<>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  /**
   * Override because the container is used for classId (which may also be a {@link IPage}) instead of the parent which
   * is used by the default implementation of AbstractWidget
   */
  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
    ITypeWithClassId container = getContainer();
    if (container != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + container.classId();
    }
    return simpleClassId;
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
   * This has only an effect, if {@link #getConfiguredRowIconVisible()} is set to true.
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
   * Configures whether the row icon is visible.
   * <p>
   * If set to true the gui creates a column which contains the row icons. The column has a fixed width (see
   * {@link AbstractTable#getConfiguredRowIconColumnWidth()}), is not movable and always the first column (resp. the
   * second if the table is checkable). The column is not available in the model.
   * <p>
   * If you need other settings or if you need the icon at another column position, you cannot use the row icons.
   * Instead, you have to create a column and use {@link Cell#setIconId(String)} to set the icons on its cells.
   * <p>
   * Subclasses can override this method. Default is false.
   *
   * @return {@code true} if the row icon is visible, {@code false} otherwise.
   * @see ITableRow#getIconId()
   * @see #getConfiguredDefaultIconId()
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(25)
  protected boolean getConfiguredRowIconVisible() {
    return false;
  }

  /**
   * Configures the row icon column width.
   * <p>
   * Has no effect if the row icon is not visible.
   *
   * @see #getConfiguredDefaultIconId()
   * @see #getConfiguredRowIconVisible()
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(27)
  protected int getConfiguredRowIconColumnWidth() {
    return IColumn.NARROW_MIN_WIDTH;
  }

  /**
   * Configures whether only one row can be selected at once in this table.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if more than one row in this table can be selected at once, {@code false} otherwise.
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
   * @return {@code true} if more than one row in this table can be checked, {@code false} otherwise.
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
   * Configures whether the header row is enabled. In a disabled header, it is not possible to move or resize the
   * columns and the table header menu cannot be opened.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the header row is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(70)
  protected boolean getConfiguredHeaderEnabled() {
    return true;
  }

  /**
   * Configures whether the header menus are enabled. When header menus are disabled, a click on the header will toggle
   * between ascending and descending sorting instead of opening the header popup.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if header menus are enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(75)
  protected boolean getConfiguredHeaderMenusEnabled() {
    return true;
  }

  /**
   * Configures whether the client UI preferences for this table are (re)-stored.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if client UI preferences are enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(77)
  protected boolean getConfiguredClientUiPreferencesEnabled() {
    return true;
  }

  /**
   * Configures whether the columns are auto resized. If true, all columns are resized so that the table never needs
   * horizontal scrolling. This is especially useful for tables inside a form.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the columns are auto resized, {@code false} otherwise.
   * @see AbstractColumn#getConfiguredWidth()
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
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(190)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
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
   * <p>
   * Configures the drag support of this table.
   * </p>
   * <p>
   * Method marked as final as currently only drop is implemented for this field.
   * </p>
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(190)
  protected final int getConfiguredDragType() {
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
   * the table is scrolled to the selection so that the selected row is visible. The selection is also revealed on row
   * order changes (e.g. when the table is sorted or rows are inserted above the selected row).
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

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(240)
  protected GroupingStyle getConfiguredGroupingStyle() {
    return GroupingStyle.TOP;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(250)
  protected HierarchicalStyle getConfiguredHierarchicalStyle() {
    return HierarchicalStyle.DEFAULT;
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(260)
  protected CheckableStyle getConfiguredCheckableStyle() {
    return CheckableStyle.CHECKBOX;
  }

  /**
   * Configures whether the table shows tooltips if the cell content is truncated.
   * <p>
   * Subclasses can override this method. Default is {@link TriState#UNDEFINED}
   *
   * @return
   *         <ul>
   *         <li>{@link TriState#TRUE} if the tooltip should always be shown if the cell content is truncated</li>
   *         <li>{@link TriState#FALSE} if the tooltip should never be shown</li>
   *         <li>{@link TriState#UNDEFINED} cell tooltip is only shown if it is not possible to resize the column</li>
   *         </ul>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected TriState getConfiguredTruncatedCellTooltipEnabled() {
    return TriState.UNDEFINED;
  }

  /**
   * Configures whether the table tile mode is enabled by default.
   * <p>
   * Subclasses can override this method. Default is <code>false</code>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredTileMode() {
    return false;
  }

  /**
   * Configures whether the table should be in compact mode. Default is false.
   *
   * @see ITableCompactHandler
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(290)
  protected boolean getConfiguredCompact() {
    return false;
  }

  /**
   * <p>
   * Called after a drag operation was executed on one or several table rows.
   * </p>
   * <p>
   * Method marked as final as currently only drop is implemented for this field.
   * </p>
   *
   * @param rows
   *          Table rows that were dragged (unmodifiable list).
   * @return A transferable object representing the given rows.
   */
  @ConfigOperation
  @Order(10)
  protected final TransferObject execDrag(List<ITableRow> rows) {
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
   */
  @ConfigOperation
  @Order(20)
  protected void execDrop(ITableRow row, TransferObject t) {
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
   */
  @ConfigOperation
  @Order(30)
  protected TransferObject execCopy(List<? extends ITableRow> rows) {
    if (!CollectionUtility.hasElements(rows)) {
      return null;
    }

    StringBuilder plainText = new StringBuilder();

    List<IColumn<?>> columns = getColumnSet().getVisibleColumns();

    boolean firstRow = true;
    for (ITableRow row : rows) {
      appendCopyTextForRow(plainText, row, firstRow, columns);
      firstRow = false;
    }

    TextTransferObject transferObject = new TextTransferObject(plainText.toString());
    return transferObject;
  }

  /**
   * Called by {@link #execCopy(List)} for each row in case of a <code>CTRL-C</code> event on the table to copy the
   * given rows into the clipboard.
   */
  protected void appendCopyTextForRow(StringBuilder clipboardPlainText, ITableRow row, boolean firstRow, List<IColumn<?>> columns) {
    if (!firstRow) {
      clipboardPlainText.append(System.getProperty("line.separator"));
    }

    boolean firstColumn = true;
    for (IColumn<?> column : columns) {
      appendCopyTextForColumn(clipboardPlainText, row, column, firstColumn);
      firstColumn = false;
    }
  }

  /**
   * Called by {@link #execCopy(List)} for each row and each visible column in case of a <code>CTRL-C</code> event on
   * the table to copy the given rows into the clipboard.
   */
  protected void appendCopyTextForColumn(StringBuilder clipboardPlainText, ITableRow row, IColumn<?> column, boolean firstColumn) {
    String text;
    if (column instanceof IBooleanColumn) {
      boolean value = BooleanUtility.nvl(((IBooleanColumn) column).getValue(row), false);
      text = value ? "X" : "";
    }
    else {
      text = StringUtility.emptyIfNull(row.getCell(column).getText());
    }

    // special intercept for html
    if (text != null && row.getCell(column).isHtmlEnabled()) {
      text = HTML.raw(text).toPlainText();
    }

    // text/plain
    if (!firstColumn) {
      clipboardPlainText.append("\t");
    }

    clipboardPlainText.append(unwrapText(text));
  }

  /**
   * Transform text for copy: remove tabs and newlines because they would destroy column consistency when pasting into
   * Excel.
   * <p>
   * Trim and concatenate non-empty lines, but preserve multiple whitespace within original line.
   */
  protected String unwrapText(String s) {
    if (s == null || s.isEmpty()) {
      return "";
    }

    return Arrays.stream(s.split("[\n\r]"))
        .map(line -> line.replaceAll("\t", " "))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .collect(Collectors.joining(" "));
  }

  /**
   * Called after the table content changed, rows were added, removed or changed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(40)
  protected void execContentChanged() {
  }

  /**
   * Called after {@link AbstractColumn#execDecorateCell(Cell,ITableRow)} on the column to decorate the cell.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateCell(Cell view, ITableRow row, IColumn<?> col) {
  }

  /**
   * Called during initialization of this table, after the columns were initialized.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(60)
  protected void execInitTable() {
  }

  /**
   * Called when this table is disposed, after the columns were disposed.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(70)
  protected void execDisposeTable() {
  }

  /**
   * Called when the user clicks on a row in this table.
   * <p>
   * Subclasses can override this method. The default fires a {@link TableEvent#TYPE_ROW_CLICK} event.
   *
   * @param row
   *          that was clicked (never null).
   * @param mouseButton
   *          the mouse button ({@link MouseButton}) which triggered this method
   */
  @ConfigOperation
  @Order(80)
  protected void execRowClick(ITableRow row, MouseButton mouseButton) {
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_CLICK, CollectionUtility.arrayList(row));
    fireTableEventInternal(e);
  }

  /**
   * Called when the row has been activated.
   * <p>
   * Subclasses can override this method. The default opens the configured default menu or if no default menu is
   * configured, fires a {@link TableEvent#TYPE_ROW_ACTION} event.
   *
   * @param row
   *          that was activated (never null).
   */
  @ConfigOperation
  @Order(90)
  protected void execRowAction(ITableRow row) {
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null) {
      try {
        runMenu(defaultMenuType);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
    else {
      TableEvent e = new TableEvent(this, TableEvent.TYPE_ROW_ACTION, CollectionUtility.arrayList(row));
      fireTableEventInternal(e);
    }
  }

  /**
   * Called whenever the selection changes.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param rows
   *          an unmodifiable list of the selected rows, may be empty but not null.
   */
  @ConfigOperation
  @Order(100)
  protected void execRowsSelected(List<? extends ITableRow> rows) {
  }

  /**
   * Called when the row is going to be decorated.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param row
   *          that is going to be decorated.
   */
  @ConfigOperation
  @Order(110)
  protected void execDecorateRow(ITableRow row) {
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(120)
  protected void execAppLinkAction(String ref) {
  }

  /**
   * Called when rows get checked or unchecked.
   * <p>
   * Subclasses can override this method.
   *
   * @param rows
   *          list of rows which have been checked or unchecked (never null).
   */
  @ConfigOperation
  @Order(130)
  protected void execRowsChecked(Collection<? extends ITableRow> rows) {
  }

  /**
   * Called when this table requests tiles to be displayed, called by {@link #createTiles(List)}.
   * <p>
   * Subclasses can override this method. The default returns <code>null</code> (= no tile for this row).
   */
  @ConfigOperation
  @Order(140)
  protected ITile execCreateTile(ITableRow row) {
    return null;
  }

  /**
   * This method is called during initializing the table and is thought to add header menus to the given collection of
   * menus. Menus added in this method should be of menu type {@link TableMenuType#Header}.<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          a live collection of the menus. Add additional header menus to this list optionally add some separators at
   *          the end.
   */
  protected void addHeaderMenus(OrderedCollection<IMenu> menus) {
    menus.addLast(new OrganizeColumnsMenu(this));
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  protected List<Class<? extends ITableControl>> getConfiguredTableControls() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITableControl>> filtered = ConfigurationUtility.filterClasses(dca, ITableControl.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  private List<Class<? extends IColumn>> getConfiguredColumns() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IColumn>> columns = ConfigurationUtility.filterClasses(dca, IColumn.class);
    return ConfigurationUtility.removeReplacedClasses(columns);
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  private Class<? extends ITileTableHeader> getConfiguredTileTableHeader() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITileTableHeader>> fca = ConfigurationUtility.filterClasses(dca, ITileTableHeader.class);
    return CollectionUtility.firstElement(ConfigurationUtility.removeReplacedClasses(fca));
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_eventHistory = createEventHistory();
    m_eventBuffer = createEventBuffer();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    m_contributionHolder = new ContributionComposite(this);
    setLoading(false);
    setGroupingStyle(getConfiguredGroupingStyle());
    setHierarchicalStyle(getConfiguredHierarchicalStyle());
    setCheckableStyle(getConfiguredCheckableStyle());
    setTitle(getConfiguredTitle());
    setAutoDiscardOnDelete(getConfiguredAutoDiscardOnDelete());
    setSortEnabled(getConfiguredSortEnabled());
    setDefaultIconId(getConfiguredDefaultIconId());
    setCssClass((getConfiguredCssClass()));
    setRowIconVisible(getConfiguredRowIconVisible());
    setRowIconColumnWidth(getConfiguredRowIconColumnWidth());
    setHeaderVisible(getConfiguredHeaderVisible());
    setHeaderEnabled(getConfiguredHeaderEnabled());
    setHeaderMenusEnabled(getConfiguredHeaderMenusEnabled());
    setAutoResizeColumns(getConfiguredAutoResizeColumns());
    setCheckable(getConfiguredCheckable());
    setMultiCheck(getConfiguredMultiCheck());
    setMultiSelect(getConfiguredMultiSelect());
    setInitialMultilineText(getConfiguredMultilineText());
    setMultilineText(getConfiguredMultilineText());
    setKeyboardNavigation(getConfiguredKeyboardNavigation());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setScrollToSelection(getConfiguredScrollToSelection());
    setTableStatusVisible(getConfiguredTableStatusVisible());
    setTextFilterEnabled(getConfiguredTextFilterEnabled());
    setTruncatedCellTooltipEnabled(getConfiguredTruncatedCellTooltipEnabled());
    setClientUiPreferencesEnabled(getConfiguredClientUiPreferencesEnabled());
    if (getTableCustomizer() == null) {
      setTableCustomizer(createTableCustomizer());
    }
    // columns
    createColumnsInternal();
    // table controls
    createTableControlsInternal();
    // menus
    initMenus();
    // key strokes
    List<Class<? extends IKeyStroke>> ksClasses = getConfiguredKeyStrokes();
    List<IKeyStroke> ksList = new ArrayList<>(ksClasses.size());
    for (Class<? extends IKeyStroke> clazz : ksClasses) {
      IKeyStroke ks = ConfigurationUtility.newInnerInstance(this, clazz);
      ks.init();
      if (ks.getKeyStroke() != null) {
        ksList.add(ks);
      }
    }
    // add ENTER keystroke when default menu is used or execRowAction has an override
    Class<? extends IMenu> defaultMenuType = getDefaultMenuInternal();
    if (defaultMenuType != null || ConfigurationUtility.isMethodOverwrite(AbstractTable.class, "execRowAction", new Class[]{ITableRow.class}, this.getClass())) {
      ksList.add(new KeyStroke("ENTER") {
        @Override
        protected void execAction() {
          fireRowAction(getSelectedRow());
        }
      });
    }
    // add keystroke contributions
    List<IKeyStroke> contributedKeyStrokes = m_contributionHolder.getContributionsByClass(IKeyStroke.class);
    ksList.addAll(contributedKeyStrokes);
    setKeyStrokes(ksList);

    setTableOrganizer(createTableOrganizer());

    // setTileMode() creates the mediator and tileTableHeader lazy if set to true. Do this here to
    // already have a mostly initialized table (AbstractTileTableHeader requires an initialized columnSet).
    setTileMode(getConfiguredTileMode());

    setSummaryCellBuilder(createSummaryCellBuilder());
    setCompactHandler(createCompactHandler());
    setCompact(getConfiguredCompact());

    // add Convenience observer for drag & drop callbacks, event history and ui sort possible check
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
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROW_DROP_ACTION: {
            if (e.getDropObject() != null && isEnabled()) {
              try {
                interceptDrop(e.getFirstRow(), e.getDropObject());
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROWS_COPY_REQUEST: {
            if (e.getCopyObject() == null) {
              try {
                e.setCopyObject(interceptCopy(e.getRows()));
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ALL_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_DELETED:
          case TableEvent.TYPE_ROWS_INSERTED:
          case TableEvent.TYPE_ROWS_UPDATED: {
            if (isValueChangeTriggerEnabled()) {
              try {
                interceptContentChanged();
              }
              catch (RuntimeException ex) {
                BEANS.get(ExceptionHandler.class).handle(ex);
              }
            }
            break;
          }
          case TableEvent.TYPE_ROWS_CHECKED:
            try {
              interceptRowsChecked(e.getRows());
            }
            catch (RuntimeException ex) {
              BEANS.get(ExceptionHandler.class).handle(ex);
            }
            break;
          case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
          case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
            checkIfColumnPreventsUiSortForTable();
            break;
        }
      }

    }, TableEvent.TYPE_ROWS_DRAG_REQUEST, TableEvent.TYPE_ROW_DROP_ACTION, TableEvent.TYPE_ROWS_COPY_REQUEST, TableEvent.TYPE_ALL_ROWS_DELETED, TableEvent.TYPE_ROWS_DELETED, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED,
        TableEvent.TYPE_ROWS_CHECKED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
  }

  protected void initMenus() {
    List<Class<? extends IMenu>> ma = getDeclaredMenus();
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    Map<Class<?>, Class<? extends IMenu>> replacements = ConfigurationUtility.getReplacementMapping(ma);
    if (!replacements.isEmpty()) {
      m_menuReplacementMapping = replacements;
    }
    for (Class<? extends IMenu> clazz : ma) {
      IMenu menu = ConfigurationUtility.newInnerInstance(this, clazz);
      menus.addOrdered(menu);
    }
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);

    addHeaderMenus(menus);
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    setContextMenu(new TableContextMenu(this, menus.getOrderedList()));
  }

  @Override
  public AbstractEventBuffer<TableEvent> createEventBuffer() {
    return BEANS.get(TableEventBuffer.class);
  }

  protected AbstractEventBuffer<TableEvent> getEventBuffer() {
    return m_eventBuffer;
  }

  private void initColumnsInternal() {
    getColumnSet().initColumns();
  }

  private void disposeColumnsInternal() {
    getColumnSet().disposeColumns();
  }

  private void createTableControlsInternal() {
    List<Class<? extends ITableControl>> tcs = getConfiguredTableControls();
    OrderedCollection<ITableControl> tableControls = new OrderedCollection<>();
    for (Class<? extends ITableControl> clazz : tcs) {
      ITableControl tableControl = ConfigurationUtility.newInnerInstance(this, clazz);
      ((AbstractTableControl) tableControl).setTable(this);
      tableControls.addOrdered(tableControl);
    }
    m_tableControls = tableControls.getOrderedList();
  }

  protected ITileTableHeader createTileTableHeader() {
    ITileTableHeader tileTableHeader = null;
    Class<? extends ITileTableHeader> tth = getConfiguredTileTableHeader();
    if (tth != null) {
      tileTableHeader = ConfigurationUtility.newInnerInstance(this, tth);
      tileTableHeader.setParentInternal(this);
      // since we create the tileTableHeader lazy, the table is already initialized and must init the header manually.
      tileTableHeader.init();
      if (tileTableHeader instanceof IGroupBox) {
        ((IGroupBox) tileTableHeader).rebuildFieldGrid();
      }
    }
    return tileTableHeader;
  }

  @Override
  public ITileTableHeader getTileTableHeader() {
    return (ITileTableHeader) propertySupport.getProperty(PROP_TILE_TABLE_HEADER);
  }

  @Override
  public void setTileTableHeader(ITileTableHeader tileTableHeader) {
    propertySupport.setProperty(PROP_TILE_TABLE_HEADER, tileTableHeader);
  }

  protected ITableTileGridMediator createTableTileGridMediator() {
    return BEANS.get(ITableTileGridMediatorProvider.class).createTableTileGridMediator(this);
  }

  @Override
  public ITableTileGridMediator getTableTileGridMediator() {
    return (ITableTileGridMediator) propertySupport.getProperty(PROP_TABLE_TILE_GRID_MEDIATOR);
  }

  @Override
  public void setTableTileGridMediator(ITableTileGridMediator mediator) {
    propertySupport.setProperty(PROP_TABLE_TILE_GRID_MEDIATOR, mediator);
  }

  private void createColumnsInternal() {
    List<Class<? extends IColumn>> ca = getConfiguredColumns();
    OrderedCollection<IColumn<?>> columns = new OrderedCollection<>();

    // configured columns
    for (Class<? extends IColumn> clazz : ca) {
      IColumn<?> column = ConfigurationUtility.newInnerInstance(this, clazz);
      columns.addOrdered(column);
    }

    // contributed columns
    List<IColumn> contributedColumns = m_contributionHolder.getContributionsByClass(IColumn.class);
    for (IColumn c : contributedColumns) {
      columns.addOrdered(c);
    }

    // dynamically injected columns
    injectColumnsInternal(columns);

    // move columns
    ExtensionUtility.moveModelObjects(columns);

    m_columnSet = new ColumnSet(this, columns.getOrderedList());
    if (getConfiguredCheckableColumn() != null) {
      AbstractBooleanColumn checkableColumn = getColumnSet().getColumnByClass(getConfiguredCheckableColumn());
      setCheckableColumn(checkableColumn);
    }

    PropertyChangeListener columnVisibleListener = evt -> {
      // disable ui sort possible property if needed
      checkIfColumnPreventsUiSortForTable();
      // prevent invisible context column (because the UI does not know of invisible columns)
      checkIfContextColumnIsVisible();
    };
    for (IColumn column : m_columnSet.getColumns()) {
      column.addPropertyChangeListener(IColumn.PROP_VISIBLE, columnVisibleListener);
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
    if (isInitDone()) {
      //re-initialize
      try {
        reinit();
      }
      catch (RuntimeException e) {
        LOG.error("Failed re-initializing table {}", getClass().getName(), e);
      }
    }
  }

  @Override
  protected final void initInternal() {
    super.initInternal();
    try {
      if (m_initLock.acquire()) {
        try {
          setTableChanging(true);
          initTableInternal();
          interceptInitTable();
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_initLock.release();
    }
  }

  protected void initTableInternal() {
    initColumnsInternal();
    if (getUserFilterManager() == null) {
      setUserFilterManager(createUserFilterManager());
    }
    setTileMode(ClientUIPreferences.getInstance().getTableTileMode(this, isTileMode()));
  }

  @Override
  protected final void disposeInternal() {
    try {
      disposeTableInternal();
      interceptDisposeTable();
    }
    catch (Exception e) {
      LOG.error("Could not dispose table [{}]", getClass().getName(), e);
    }
    super.disposeInternal();
  }

  protected void disposeTableInternal() {
    disposeColumnsInternal();
  }

  @Override
  public void doAppLinkAction(String ref) {
    if (isActionRunning()) {
      return;
    }
    try {
      setActionRunning(true);
      interceptAppLinkAction(ref);
    }
    finally {
      setActionRunning(false);
    }
  }

  @Override
  public void addAttachment(BinaryResource attachment) {
    m_attachmentSupport.addAttachment(attachment);
  }

  @Override
  public Set<BinaryResource> getAttachments() {
    return m_attachmentSupport.getAttachments();
  }

  @Override
  public BinaryResource getAttachment(String filename) {
    return m_attachmentSupport.getAttachment(filename);
  }

  @Override
  public void removeAttachment(BinaryResource attachment) {
    m_attachmentSupport.removeAttachment(attachment);
  }

  @Override
  public List<ITableRowFilter> getRowFilters() {
    return CollectionUtility.arrayList(m_rowFilters);
  }

  @Override
  public void addRowFilter(ITableRowFilter filter) {
    if (filter != null && !m_rowFilters.contains(filter)) {
      m_rowFilters.add(filter);
      applyRowFilters();
    }
  }

  @Override
  public void removeRowFilter(ITableRowFilter filter) {
    if (filter != null && m_rowFilters.remove(filter)) {
      // #253699 By removing the row filter additional rows may be accepted by the filters.
      // The rows currently accepted by the user row filters do not contain these additional rows. So we will remove the user row filters. They will be reapplied by the UI.
      removeUserRowFilters();
    }
  }

  public void removeUserRowFilters() {
    removeUserRowFilters(true);
  }

  public void removeUserRowFilters(boolean applyRowFilters) {
    for (ITableRowFilter filter : getRowFilters()) {
      if (filter instanceof UserTableRowFilter) {
        m_rowFilters.remove(filter);
      }
    }
    if (applyRowFilters) {
      applyRowFilters();
    }
  }

  @Override
  public void applyRowFilters() {
    boolean filterChanged = applyRowFiltersInternal();
    if (filterChanged) {
      fireRowFilterChanged();
    }
  }

  private boolean applyRowFiltersInternal() {
    boolean filterChanged = false;
    for (ITableRow row : m_rows) {
      boolean wasFilterAccepted = row.isFilterAccepted();
      applyRowFiltersInternal((InternalTableRow) row);
      if (row.isFilterAccepted() != wasFilterAccepted) {
        filterChanged = true;
      }
    }
    return filterChanged;
  }

  private void applyRowFiltersInternal(InternalTableRow row) {
    List<ITableRowFilter> rejectingFilters = new ArrayList<>();
    row.setFilterAcceptedInternal(true);
    row.setRejectedByUser(false);
    if (!m_rowFilters.isEmpty()) {
      for (ITableRowFilter filter : m_rowFilters) {
        if (!filter.accept(row)) {
          row.setFilterAcceptedInternal(false);
          /*
           * ticket 95770
           */
          if (isSelectedRow(row)) {
            deselectRow(row);
          }
          rejectingFilters.add(filter);
        }
      }
    }

    // Prefer row.isRejectedByUser to allow a filter to set this flag
    row.setRejectedByUser(row.isRejectedByUser() || rejectingFilters.size() == 1 && rejectingFilters.get(0) instanceof IUserFilter);
  }

  @Override
  public String getTitle() {
    return propertySupport.getPropertyString(PROP_TITLE);
  }

  @Override
  public void setTitle(String s) {
    propertySupport.setPropertyString(PROP_TITLE, s);
  }

  private boolean isSortValid() {
    return FLAGS_BIT_HELPER.isBitSet(SORT_VALID, m_flags);
  }

  private void setSortValid(boolean valid) {
    m_flags = FLAGS_BIT_HELPER.changeBit(SORT_VALID, valid, m_flags);
  }

  private boolean isActionRunning() {
    return FLAGS_BIT_HELPER.isBitSet(ACTION_RUNNING, m_flags);
  }

  private void setActionRunning(boolean running) {
    m_flags = FLAGS_BIT_HELPER.changeBit(ACTION_RUNNING, running, m_flags);
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
  public void expandAll(ITableRow startRow) {
    expandAllInternal(startRow, true);
  }

  @Override
  public void collapseAll(ITableRow startRow) {
    expandAllInternal(startRow, false);
  }

  public void expandRows(List<ITableRow> rows) {
    expandRowsInternal(rows, true);
  }

  public void collapseRows(List<ITableRow> rows) {
    expandRowsInternal(rows, false);
  }

  private void expandAllInternal(ITableRow startRow, boolean expanded) {
    final List<ITableRow> rows;
    if (startRow != null) {
      rows = CollectionUtility.arrayList(startRow);
    }
    else {
      rows = m_rootRows;
    }
    CollectingVisitor<ITableRow> collector = new CollectingVisitor<>() {
      @Override
      protected boolean accept(ITableRow element) {
        return element.isExpanded() != expanded;
      }
    };
    rows.forEach(root -> TreeTraversals.create(collector, ITableRow::getChildRows).traverse(root));
    expandRowsInternal(collector.getCollection(), expanded);
  }

  protected void expandRowsInternal(List<? extends ITableRow> rows, boolean expanded) {
    try {
      setTableChanging(true);
      List<? extends ITableRow> changedRows = rows.stream().filter(row -> row.setExpanded(expanded)).collect(Collectors.toList());
      fireRowsExpanded(changedRows);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public boolean isExpanded(ITableRow row) {
    return false;
  }

  @Override
  public void setRowExpanded(ITableRow row, boolean expanded) {
    if (row == null || row.isExpanded() == expanded) {
      return;
    }
    expandRowsInternal(CollectionUtility.arrayList(row), expanded);
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
    List<String> columnNames = new ArrayList<>(getColumnCount());
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
    return getSummaryCellBuilder().build(row);
  }

  @Override
  public ICell getCell(ITableRow row, IColumn<?> col) {
    row = resolveRow(row);
    if (row == null || col == null) {
      return null;
    }
    return row.getCell(col.getColumnIndex());
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return isCellEditable(getRow(rowIndex), getColumnSet().getColumn(columnIndex));
  }

  @Override
  public boolean isCellEditable(ITableRow row, IColumn<?> column) {
    return row != null && column != null && column.isCellEditable(row);
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
  public void setCompact(boolean compact) {
    if (propertySupport.setPropertyBool(PROP_COMPACT, compact)) {
      getCompactHandler().handle(isCompact());
    }
  }

  @Override
  public boolean isCompact() {
    return propertySupport.getPropertyBool(PROP_COMPACT);
  }

  @Override
  public ITableCompactHandler getCompactHandler() {
    return m_compactHandler;
  }

  protected ITableCompactHandler createCompactHandler() {
    return BEANS.get(ITableCompactHandlerProvider.class).createCompactHandler(this);
  }

  @Override
  public void setCompactHandler(ITableCompactHandler compactHandler) {
    Assertions.assertNotNull(compactHandler);
    if (m_compactHandler != null && isCompact()) {
      // Reset compact state
      m_compactHandler.handle(false);
    }
    m_compactHandler = compactHandler;
    if (isInitConfigDone()) {
      m_compactHandler.handle(isCompact());
    }
  }

  /**
   * Creates a compact handler used by {@link MobileSummaryCellBuilder} to create a summary cell. The created handler is
   * not the same instance as used by {@link #isCompact()} and returned by {@link #getCompactHandler()} because it uses
   * a different configuration (e.g. links are disabled). But it uses the same factory method to create the handler
   * which is {@link #createCompactHandler()}.
   *
   * @see #createSummaryCellBuilder()
   */
  protected ITableCompactHandler createSummaryCompactHandler() {
    return createCompactHandler()
        .withMoreLinkAvailable(false)
        .withLineCustomizer(line -> line.getTextBlock().setHtmlToPlainTextEnabled(true));
  }

  protected ISummaryCellBuilder createSummaryCellBuilder() {
    if (UserAgentUtility.isMobileDevice()) {
      return new MobileSummaryCellBuilder(createSummaryCompactHandler());
    }
    return new SummaryCellBuilder(this);
  }

  @Override
  public ISummaryCellBuilder getSummaryCellBuilder() {
    return m_summaryCellBuilder;
  }

  @Override
  public void setSummaryCellBuilder(ISummaryCellBuilder summaryCellBuilder) {
    m_summaryCellBuilder = summaryCellBuilder;
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
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(PROP_DROP_MAXIMUM_SIZE);
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
  public boolean isInitialMultilineText() {
    return FLAGS_BIT_HELPER.isBitSet(INITIAL_MULTI_LINE_TEXT, m_flags);
  }

  @Override
  public void setInitialMultilineText(boolean on) {
    m_flags = FLAGS_BIT_HELPER.changeBit(INITIAL_MULTI_LINE_TEXT, on, m_flags);
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
    return FLAGS_BIT_HELPER.isBitSet(AUTO_DISCARD_ON_DELETE, m_flags);
  }

  @Override
  public void setAutoDiscardOnDelete(boolean on) {
    m_flags = FLAGS_BIT_HELPER.changeBit(AUTO_DISCARD_ON_DELETE, on, m_flags);
  }

  @Override
  public boolean isTableChanging() {
    return m_tableChanging > 0;
  }

  @Override
  @SuppressWarnings("squid:S1143")
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
        try {
          if (m_tableChanging == 1) {
            if (m_treeStructureDirty) {
              rebuildTreeStructureInternal();
            }
            //will be going to zero, but process decorations here, so events are added to the event buffer
            processDecorationBuffer();
            if (!isSortValid()) {
              sort();
            }
          }
        }
        catch (RuntimeException | PlatformError t) {
          // covers all unchecked exceptions
          saveEx = t;
          throw t;
        }
        finally {
          // exceptions in above try-block will not be thrown if the finally-block throws, thus we suppress them.
          m_tableChanging--;
          if (m_tableChanging == 0) {
            try {
              processEventBuffer();
            }
            catch (RuntimeException | PlatformError t) {
              if (saveEx != null) {
                saveEx.addSuppressed(t);
              }
              else {
                saveEx = t;
                throw t;
              }
            }
            finally {
              try {
                propertySupport.setPropertiesChanging(false);
              }
              catch (RuntimeException | PlatformError t) {
                if (saveEx != null) {
                  saveEx.addSuppressed(t);
                }
                else {
                  throw t;
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void firePendingEvents() {
    int tableChanging = m_tableChanging;
    for (int i = 0; i < tableChanging; i++) {
      setTableChanging(false);
    }
    // Restore previous changing count so that upcoming events will be buffered again
    for (int i = 0; i < tableChanging; i++) {
      setTableChanging(true);
    }
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(getKeyStrokesInternal());
  }

  /**
   * Returns a modifiable live list
   */
  protected List<IKeyStroke> getKeyStrokesInternal() {
    return propertySupport.getPropertyList(PROP_KEY_STROKES);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getMenus(), getKeyStrokesInternal(), m_tableControls, CollectionUtility.arrayList(getTileTableHeader()));
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

  private void startCellEdit() {
    if (m_editContext == null) {
      return;
    }
    m_editContext.getFormField().init();
    TableEvent tableEvent = new TableEvent(this, TableEvent.TYPE_START_CELL_EDIT);
    tableEvent.setCellEditor(m_editContext.getFormField());
    tableEvent.setRows(Arrays.asList(m_editContext.getRow()));
    tableEvent.setColumns(Arrays.asList(m_editContext.getColumn()));
    fireTableEventInternal(tableEvent);
  }

  private void endCellEdit() {
    if (m_editContext == null) {
      return;
    }
    TableEvent tableEvent = new TableEvent(this, TableEvent.TYPE_END_CELL_EDIT);
    tableEvent.setCellEditor(m_editContext.getFormField());
    fireTableEventInternal(tableEvent);
  }

  protected void disposeCellEditor() {
    if (m_editContext == null) {
      return;
    }
    m_editContext.getFormField().dispose();
    m_editContext = null;
  }

  @Override
  public void completeCellEdit() {
    if (m_editContext == null) {
      return;
    }
    endCellEdit();
    m_editContext.getColumn().completeEdit(m_editContext.getRow(), m_editContext.getFormField());
    disposeCellEditor();
  }

  @Override
  public void cancelCellEdit() {
    if (m_editContext == null) {
      return;
    }
    endCellEdit();
    disposeCellEditor();
  }

  @Override
  public ITableRowDataMapper createTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    return interceptCreateTableRowDataMapper(rowType);
  }

  /**
   * Creates a {@link TableRowDataMapper} that is used for reading and writing data from the given
   * {@link AbstractTableRowData} type.
   *
   * @since 3.8.2
   */
  @ConfigOperation
  @Order(130)
  protected ITableRowDataMapper execCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    return new TableRowDataMapper(rowType, getColumnSet());
  }

  @Override
  public void exportToTableBeanData(AbstractTableFieldBeanData target) {
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
  public void importFromTableBeanData(AbstractTableFieldBeanData source) {
    importFromTableRowBeanData(CollectionUtility.arrayList(source.getRows()), source.getRowType());
  }

  public void importFromTableRowBeanData(List<? extends AbstractTableRowData> rowDatas, Class<? extends AbstractTableRowData> rowType) {
    discardAllDeletedRows();
    int deleteCount = 0;
    List<ITableRow> newRows = new ArrayList<>(rowDatas.size());
    ITableRowDataMapper mapper = createTableRowDataMapper(rowType);
    for (AbstractTableRowData rowData : rowDatas) {
      if (rowData.getRowState() != AbstractTableRowData.STATUS_DELETED && mapper.acceptImport(rowData)) {
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
        for (AbstractTableRowData rowData : rowDatas) {
          if (rowData.getRowState() == AbstractTableRowData.STATUS_DELETED && mapper.acceptImport(rowData)) {
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
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  @Override
  public boolean runMenu(Class<? extends IMenu> menuType) {
    Class<? extends IMenu> c = getReplacingMenuClass(menuType);
    for (IMenu m : getMenus()) {
      if (m.getClass() == c) {
        m.doAction();
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the menu with the given class has been replaced by another menu. If so, the replacing menu's class
   * is returned. Otherwise, the given class itself.
   *
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
   * Factory method to create a user filter manager instance.
   * <p>
   * The default implementation creates a {@link TableUserFilterManager}.
   */
  protected TableUserFilterManager createUserFilterManager() {
    return new TableUserFilterManager(this);
  }

  /**
   * Factory method to return a table customizer instance.
   * <p>
   * The default implementation uses <code>BEANS.get()</code> to retrieve an instance of
   * {@link ITableCustomizerProvider} which returns {@link NullTableCustomizerProvider} if no other provider is
   * registered. You may register your own provider to create a custom table customizer which is used by all tables in a
   * Scout application without sub-classing <code>AbstractTable</code>.
   */
  protected ITableCustomizer createTableCustomizer() {
    return BEANS.get(ITableCustomizerProvider.class).createTableCustomizer(this);
  }

  /**
   * Factory method to return a table organizer instance.
   * <p>
   * The default implementation uses <code>BEANS.get()</code> to retrieve an instance of {@link ITableOrganizerProvider}
   * which returns {@link TableOrganizer} if no other provider is registered. You may register your own provider to
   * create a custom table organizer which is used by all tables in a Scout application without sub-classing
   * <code>AbstractTable</code>.
   */
  protected ITableOrganizer createTableOrganizer() {
    return BEANS.get(ITableOrganizerProvider.class).createTableOrganizer(this);
  }

  /*
   * Row handling methods. Operate on a Row instance.
   */

  @Override
  public ITableRow createRow() {
    return new P_TableRowBuilder().createRow();
  }

  @Override
  public ITableRow createRow(Object rowValues) {
    return new P_TableRowBuilder().createRow(rowValues);
  }

  @Override
  public List<ITableRow> createRowsByArray(Object dataArray) {
    return new P_TableRowBuilder().createRowsByArray(dataArray);
  }

  @Override
  public List<ITableRow> createRowsByArray(Object dataArray, int rowStatus) {
    return new P_TableRowBuilder().createRowsByArray(dataArray, rowStatus);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference) {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public List<ITableRow> createRowsByMatrix(Object dataMatrixOrReference, int rowStatus) {
    return new P_TableRowBuilder().createRowsByMatrix(dataMatrixOrReference, rowStatus);
  }

  @Override
  public List<ITableRow> createRowsByCodes(Collection<? extends ICode<?>> codes) {
    return new P_TableRowBuilder().createRowsByCodes(codes);
  }

  /**
   * Performance note:<br>
   * Since the matrix may contain large amount of data, the Object[][] can be passed as new AtomicReference
   * <Object>(Object[][]) so that the further processing can set the content of the holder to null while processing.
   */
  @Override
  public void replaceRowsByMatrix(Object dataMatrixOrReference) {
    replaceRows(createRowsByMatrix(dataMatrixOrReference));
  }

  @Override
  public void replaceRowsByArray(Object dataArray) {
    replaceRows(createRowsByArray(dataArray));
  }

  @Override
  public void replaceRows(List<? extends ITableRow> newRows) {
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

  /**
   * Replace rows discarding deleted rows
   */
  private void replaceRowsCase1(List<? extends ITableRow> newRows) {
    try {
      setTableChanging(true);
      //
      List<CompositeObject> selectedKeys = getSelectedKeys();
      discardAllRows();
      addRows(newRows, false);
      restoreSelection(selectedKeys);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public List<CompositeObject> getSelectedKeys() {
    List<CompositeObject> selectedKeys = new ArrayList<>();
    for (ITableRow r : getSelectedRows()) {
      selectedKeys.add(new CompositeObject(getRowKeys(r)));
    }
    return selectedKeys;
  }

  @Override
  public void restoreSelection(List<CompositeObject> selectedKeys) {
    List<ITableRow> selectedRows = new ArrayList<>();
    if (!selectedKeys.isEmpty()) {
      for (ITableRow r : m_rows) {
        if (selectedKeys.remove(new CompositeObject(getRowKeys(r)))) {
          selectedRows.add(r);
          if (selectedKeys.isEmpty()) {
            break;
          }
        }
      }
    }
    selectRows(selectedRows, false);
  }

  /**
   * Replace rows by applying insert/update/delete on existing rows by primary key match
   */
  private void replaceRowsCase2(List<? extends ITableRow> newRows) {
    try {
      setTableChanging(true);
      //
      int[] oldToNew = new int[getRowCount()];
      int[] newToOld = new int[newRows.size()];
      Arrays.fill(oldToNew, -1);
      Arrays.fill(newToOld, -1);
      Map<CompositeObject, Integer> newRowIndexMap = new HashMap<>();
      for (int i = newRows.size() - 1; i >= 0; i--) {
        newRowIndexMap.put(new CompositeObject(getRowKeys(newRows.get(i))), i);
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
      List<ITableRow> updatedRows = new ArrayList<>(mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] >= 0) {
          ITableRow existingRow = getRow(i);
          ITableRow newRow = newRows.get(oldToNew[i]);

          replaceRowValues(existingRow, newRow);
          updatedRows.add(existingRow);
        }
      }

      List<ITableRow> deletedRows = new ArrayList<>(getRowCount() - mappedCount);
      for (int i = 0; i < oldToNew.length; i++) {
        if (oldToNew[i] < 0) {
          deletedRows.add(m_rows.get(i));
        }
      }
      List<ITableRow> insertedRows = new ArrayList<>(newRows.size() - mappedCount);
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

  /**
   * Update existing row with values from new row
   */
  private void replaceRowValues(ITableRow existingRow, ITableRow newRow) {
    try {
      existingRow.setRowChanging(true);
      //
      existingRow.setEnabled(newRow.isEnabled());
      existingRow.setStatus(newRow.getStatus());

      //map values
      for (IColumn<?> col : getColumns()) {

        int columnIndex = col.getColumnIndex();
        Object newValue = null;
        if (columnIndex < newRow.getCellCount()) {
          newValue = newRow.getCellValue(columnIndex);
        }

        col.parseValueAndSet(existingRow, newValue);
      }
    }
    finally {
      existingRow.setRowPropertiesChanged(false);
      existingRow.setRowChanging(false);
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
  public void setRowState(ITableRow row, int rowState) {
    setRowState(CollectionUtility.arrayList(row), rowState);
  }

  @Override
  public void setAllRowState(int rowState) {
    setRowState(getRows(), rowState);
  }

  @Override
  public void setRowState(Collection<? extends ITableRow> rows, int rowState) {
    try {
      setTableChanging(true);
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
      List<ITableRow> resolvedRowList = new ArrayList<>(rows.size());
      for (ITableRow row : rows) {
        ITableRow resolvedRow = resolveRow(row);
        if (resolvedRow != null) {
          resolvedRowList.add(resolvedRow);
          updateRowImpl(resolvedRow);
        }
      }
      if (!resolvedRowList.isEmpty()) {
        fireRowsUpdated(resolvedRowList);
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
      ensureInvalidColumnsVisible(row);
      Set<Integer> changedColumnValues = row.getUpdatedColumnIndexes(ICell.VALUE_BIT);
      if (CollectionUtility.containsAny(changedColumnValues, IntStream.of(getColumnSet().getKeyColumnIndexes()).boxed().toArray(Integer[]::new))) {
        // update primary key
        m_rowsByKey.values().remove(row);
        m_rowsByKey.put(new CompositeObject(row.getKeyValues()), row);
      }
      if (CollectionUtility.containsAny(changedColumnValues, getColumnSet().getSortColumns().stream().map(IColumn::getColumnIndex).collect(Collectors.toSet()))) {
        // sort has to be updated
        // restore order of rows according to sort criteria
        if (isTableChanging()) {
          setSortValid(false);
        }
        else {
          sort();
        }
      }
      if (!changedColumnValues.isEmpty()) {
        enqueueValueChangeTasks(row, changedColumnValues);
      }
      enqueueDecorationTasks(row);
    }
  }

  @Override
  public void ensureInvalidColumnsVisible() {
    List<ITableRow> rows = getRows();
    for (ITableRow row : rows) {
      ensureInvalidColumnsVisible(row);
    }
  }

  private void ensureInvalidColumnsVisible(ITableRow row) {
    for (IColumn<?> col : getColumns()) {
      col.ensureVisibleIfInvalid(row);
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
  public boolean isCheckedRow(ITableRow row) {
    row = resolveRow(row);
    if (row == null) {
      return false;
    }
    else {
      return m_checkedRows.contains(row);
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
    TreeSet<ITableRow> newSelection = new TreeSet<>(new RowIndexComparator());
    if (append) {
      newSelection.addAll(m_selectedRows);
    }
    newSelection.addAll(rows);
    // check selection count with multiselect
    if (newSelection.size() > 1 && !isMultiSelect()) {
      ITableRow first = newSelection.first();
      newSelection.clear();
      newSelection.add(first);
    }
    if (!CollectionUtility.equalsCollection(m_selectedRows, newSelection, true)) {
      m_selectedRows = new ArrayList<>(newSelection);
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
    Set<ITableRow> rowsToDeselect = new HashSet<>(resolveRows(rows));
    if (!CollectionUtility.hasElements(rowsToDeselect)) {
      return; // nothing to deselect
    }

    TreeSet<ITableRow> newSelection = new TreeSet<>(new RowIndexComparator());
    newSelection.addAll(m_selectedRows);
    if (newSelection.removeAll(rowsToDeselect)) {
      m_selectedRows = new ArrayList<>(newSelection);
      fireRowsSelected(m_selectedRows);
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
    List<ITableRow> newList = new ArrayList<>();
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
    List<ITableRow> newList = new ArrayList<>();
    for (ITableRow selectedRow : selectedRows) {
      if (selectedRow.isEnabled()) {
        newList.add(selectedRow);
      }
    }
    deselectRows(newList);
  }

  @Override
  public List<ITableRow> getCheckedRows() {
    return CollectionUtility.arrayList(m_checkedRows);
  }

  @Override
  public void checkRow(int row, boolean value) {
    checkRow(getRow(row), value);
  }

  @Override
  public void checkRow(ITableRow row, boolean value) {
    checkRows(CollectionUtility.arrayList(row), value);
  }

  @Override
  public void checkRows(Collection<? extends ITableRow> rows, boolean value) {
    checkRows(rows, value, false);
  }

  public void checkRows(Collection<? extends ITableRow> rows, boolean value, boolean enabledRowsOnly) {
    try {
      rows = resolveRows(rows);
      // check checked-count with multi-check
      if (!isMultiCheck() && value) {
        ITableRow rowToCheck = null;
        for (ITableRow row : rows) {
          if (row.isChecked() != value && (!enabledRowsOnly || row.isEnabled())) {
            rowToCheck = row;
            break;
          }
        }
        if (rowToCheck != null) {
          if (!enabledRowsOnly) {
            uncheckAllRows();
          }
          else {
            uncheckAllEnabledRows();
          }
          checkRowImpl(rowToCheck, value);
          fireRowsChecked(CollectionUtility.arrayList(rowToCheck));
        }
      }
      else {
        List<ITableRow> rowsUpdated = new ArrayList<>();
        for (ITableRow row : rows) {
          if (row.isChecked() != value && (!enabledRowsOnly || row.isEnabled())) {
            checkRowImpl(row, value);
            rowsUpdated.add(row);
          }
        }
        if (!rowsUpdated.isEmpty()) {
          if (value) {
            // sort checked rows if new checked rows have been added (not necessary if checked rows have been removed)
            sortCheckedRows();
          }
          fireRowsChecked(CollectionUtility.arrayList(rowsUpdated));
        }
      }
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  private void checkRowImpl(ITableRow row, boolean value) {
    if (!(row instanceof InternalTableRow)) {
      return;
    }
    InternalTableRow internalRow = (InternalTableRow) row;
    if (value) {
      m_checkedRows.add(internalRow);
    }
    else {
      m_checkedRows.remove(internalRow);
    }
    if (getCheckableColumn() != null) {
      getCheckableColumn().setValue(internalRow, value);
    }
    else {
      // Do not use setStatus() or setStatusUpdated(), because this would trigger unnecessary UPDATED events
      internalRow.setStatusInternal(ITableRow.STATUS_UPDATED);
    }
  }

  @Override
  public void checkAllRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), true);
    }
    finally {
      setTableChanging(false);
    }
  }

  public void checkAllEnabledRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), true, true);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckRow(ITableRow row) {
    checkRow(row, false);
  }

  @Override
  public void uncheckRows(Collection<? extends ITableRow> rows) {
    checkRows(rows, false);
  }

  @Override
  public void uncheckAllEnabledRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), false, true);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public void uncheckAllRows() {
    try {
      setTableChanging(true);
      checkRows(getRows(), false);
    }
    finally {
      setTableChanging(false);
    }
  }

  @Override
  public String getDefaultIconId() {
    String iconId = propertySupport.getPropertyString(PROP_DEFAULT_ICON);
    if (iconId != null && iconId.isEmpty()) {
      iconId = null;
    }
    return iconId;
  }

  @Override
  public void setDefaultIconId(String iconId) {
    propertySupport.setPropertyString(PROP_DEFAULT_ICON, iconId);
  }

  @Override
  public boolean isRowIconVisible() {
    return propertySupport.getPropertyBool(PROP_ROW_ICON_VISIBLE);
  }

  @Override
  public void setRowIconVisible(boolean rowIconVisible) {
    propertySupport.setPropertyBool(PROP_ROW_ICON_VISIBLE, rowIconVisible);
  }

  @Override
  public int getRowIconColumnWidth() {
    return propertySupport.getPropertyInt(PROP_ROW_ICON_COLUMN_WIDTH);
  }

  @Override
  public void setRowIconColumnWidth(int width) {
    propertySupport.setPropertyInt(PROP_ROW_ICON_COLUMN_WIDTH, width);
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
   *         when the row is changed it has to be applied to the table using modifyRow(row);
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
    if (!m_rowFilters.isEmpty()) {
      //lazy create list in getter, make sure to be thread-safe since getters may be called from "wild" threads
      synchronized (m_cachedFilteredRowsLock) {
        if (m_cachedFilteredRows == null) {
          //this code must be thread-safe
          if (!m_rowFilters.isEmpty()) {
            List<ITableRow> filteredRows = new ArrayList<>(getRowCount());
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
    if (!m_rowFilters.isEmpty()) {
      return getFilteredRows().size();
    }
    else {
      return getRowCount();
    }
  }

  @Override
  public ITableRow getFilteredRow(int index) {
    if (!m_rowFilters.isEmpty()) {
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
    List<ITableRow> notDeletedRows = new ArrayList<>();
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
    List<ITableRow> result = new ArrayList<>(rowIndexes.length);
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
   *         when the row is changed it has to be applied to the table using modifyRow(row);
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
    List<ITableRow> rowList = new ArrayList<>();
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
    List<ITableRow> rowList = new ArrayList<>();
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
  public ITableRow addRowByArray(Object dataArray) {
    if (dataArray == null) {
      return null;
    }
    List<ITableRow> result = addRowsByMatrix(new Object[]{dataArray});
    return CollectionUtility.firstElement(result);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix) {
    return addRowsByMatrix(dataMatrix, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByMatrix(Object dataMatrix, int rowStatus) {
    return addRows(createRowsByMatrix(dataMatrix, rowStatus));
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray) {
    return addRowsByArray(dataArray, ITableRow.STATUS_INSERTED);
  }

  @Override
  public List<ITableRow> addRowsByArray(Object dataArray, int rowStatus) {
    return addRows(createRowsByArray(dataArray, rowStatus));
  }

  @Override
  public ITableRow addRow() {
    return addRow(true);
  }

  @Override
  public ITableRow addRow(boolean markAsInserted) {
    return addRow(createRow(), markAsInserted);
  }

  @Override
  public ITableRow addRow(ITableRow newRow) {
    return addRow(newRow, false);
  }

  @Override
  public ITableRow addRow(ITableRow newRow, boolean markAsInserted) {
    List<ITableRow> addedRows = addRows(CollectionUtility.arrayList(newRow), markAsInserted);
    return CollectionUtility.firstElement(addedRows);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows) {
    return addRows(newRows, false);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted) {
    return addRows(newRows, markAsInserted, null);
  }

  @Override
  public List<ITableRow> addRows(List<? extends ITableRow> newRows, boolean markAsInserted, int[] insertIndexes) {
    if (newRows == null) {
      return CollectionUtility.emptyArrayList();
    }
    List<InternalTableRow> newIRows = null;
    try {
      setTableChanging(true);
      //
      int oldRowCount = m_rows.size();
      initCells(newRows);
      if (markAsInserted) {
        updateStatus(newRows, ITableRow.STATUS_INSERTED);
      }
      newIRows = createInternalRows(newRows);

      addCellObserver(newIRows);
      // Fire ROWS_INSERTED event before really adding the internal rows to the table, because adding might trigger ROWS_UPDATED events (due to validation)
      fireRowsInserted(newIRows);
      for (int i = 0; i < newIRows.size(); i++) {
        InternalTableRow newIRow = newIRows.get(i);
        addInternalRow(newIRow);
        // copy check status of rows after adding them to the table since InternalTableRow maintains this on the table, not on the row
        ITableRow newRow = newRows.get(i);
        boolean checked = newRow.isChecked() || (getCheckableColumn() != null && BooleanUtility.nvl(getCheckableColumn().getValue(newRow)));
        checkRow(newIRow, checked);
      }

      if (getColumnSet().getSortColumnCount() > 0) {
        // restore order of rows according to sort criteria
        setSortValid(false);
      }
      else if (insertIndexes != null) {
        ITableRow[] sortArray = createSortArray(newIRows, insertIndexes, oldRowCount);
        sortInternal(Arrays.asList(sortArray));
      }
    }
    finally {
      setTableChanging(false);
    }

    return new ArrayList<>(newIRows);
  }

  private ITableRow[] createSortArray(List<InternalTableRow> newIRows, int[] insertIndexes, int oldRowCount) {
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
    return sortArray;
  }

  /**
   * Add InternalTableRow as an observer to the cell in order to update the row status on changes
   */
  private void addCellObserver(List<InternalTableRow> rows) {
    for (InternalTableRow row : rows) {
      for (int i = 0; i < row.getCellCount(); i++) {
        Cell cell = row.getCellForUpdate(i);
        cell.setObserver(row);
      }
    }
  }

  /**
   * initialize cells with column default values
   */
  private void initCells(List<? extends ITableRow> rows) {
    for (int i = 0; i < getColumnCount(); i++) {
      for (ITableRow row : rows) {
        IColumn<?> col = getColumnSet().getColumn(i);
        col.initCell(row);
      }
    }
  }

  private void updateStatus(List<? extends ITableRow> rows, int status) {
    for (ITableRow newRow : rows) {
      newRow.setStatus(status);
    }
  }

  private List<InternalTableRow> createInternalRows(List<? extends ITableRow> newRows) {
    // make sure rows InternalTableRows are in the same order as the given ITableRows, addRows(...) relies on this
    List<InternalTableRow> newIRows = new ArrayList<>(newRows.size());
    for (ITableRow newRow : newRows) {
      newIRows.add(new InternalTableRow(this, newRow));
    }
    return newIRows;
  }

  private ITableRow addInternalRow(InternalTableRow newIRow) {
    synchronized (m_cachedRowsLock) {
      m_cachedRows = null;
      int newIndex = m_rows.size();
      newIRow.setRowIndex(newIndex);
      newIRow.setTableInternal(this);
      m_rows.add(newIRow);
      m_rowsByKey.put(new CompositeObject(newIRow.getKeyValues()), newIRow);
    }
    rebuildTreeStructure();

    Set<Integer> indexes = new HashSet<>();
    for (int idx : getColumnSet().getAllColumnIndexes()) {
      indexes.add(idx);
    }

    enqueueValueChangeTasks(newIRow, indexes);
    enqueueDecorationTasks(newIRow);
    return newIRow;
  }

  private void rebuildTreeStructure() {
    if (isTableChanging()) {
      m_treeStructureDirty = true;
    }
    else {
      rebuildTreeStructureInternal();
    }
  }

  private void rebuildTreeStructureInternal() {
    List<ITableRow> rootNodes = new ArrayList<>();
    Map<ITableRow/*parent*/, List<ITableRow> /*child rows*/> parentToChildren = new HashMap<>();
    m_rows.forEach(row -> {
      List<Object> parentRowKeys = getParentRowKeys(row);
      if (parentRowKeys.stream().filter(Objects::nonNull).findAny().orElse(null) != null) {
        ITableRow parentRow = getRowByKey(parentRowKeys);
        if (parentRow == null) {
          throw new IllegalArgumentException("Could not find the parent row of '" + row + "'. parent keys are defined.");
        }
        parentToChildren.computeIfAbsent(parentRow, children -> new ArrayList<>())
            .add(row);
      }
      else {
        row.setParentRowInternal(null);
        rootNodes.add(row);
      }
    });

    m_rootRows = Collections.synchronizedList(rootNodes);
    boolean hierarchical = !parentToChildren.isEmpty();
    setHierarchicalInternal(hierarchical);
    if (hierarchical) {
      CollectingVisitor<ITableRow> collector = new CollectingVisitor<>();
      rootNodes.forEach(root -> TreeTraversals.create(collector, node -> {
        List<ITableRow> childRows = parentToChildren.getOrDefault(node, Collections.emptyList());
        node.setChildRowsInternal(childRows);
        childRows.forEach(childRow -> childRow.setParentRowInternal(node));
        return childRows;
      }).traverse(root));
      m_rows = Collections.synchronizedList(collector.getCollection());
    }
    m_treeStructureDirty = false;
  }

  @Override
  public boolean isHierarchical() {
    return propertySupport.getPropertyBool(PROP_HIERARCHICAL_ROWS);
  }

  protected void setHierarchicalInternal(boolean hierarchical) {
    propertySupport.setPropertyBool(PROP_HIERARCHICAL_ROWS, hierarchical);
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
   * @see List#add(int, Object)
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
      // rebuild selection and checked rows
      selectRows(getSelectedRows(), false);
      sortCheckedRows();
    }
  }

  @Override
  public void deleteRow(int rowIndex) {
    deleteRows(new int[]{rowIndex});
  }

  @Override
  public void deleteRows(int[] rowIndexes) {
    List<ITableRow> rowList = new ArrayList<>();
    for (int rowIndexe : rowIndexes) {
      ITableRow row = getRow(rowIndexe);
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
    // performance quick-check
    if (rows != existingRows) {
      rows = resolveRows(rows);
      CollectingVisitor<ITableRow> collector = new CollectingVisitor<>();
      rows.forEach(parent -> TreeTraversals.create(collector, ITableRow::getChildRows).traverse(parent));
      rows = collector.getCollection();
    }
    if (!CollectionUtility.hasElements(rows)) {
      return;
    }
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
      List<ITableRow> deletedRows = new ArrayList<>(rows);
      // remove from selection
      deselectRows(deletedRows);
      uncheckRows(deletedRows);
      // delete impl
      // performance quick-check
      if (rows == existingRows) {
        //remove all of them
        synchronized (m_cachedRowsLock) {
          m_rows.clear();
          m_rootRows.clear();
          m_rowsByKey.clear();
          m_cachedRows = null;
        }
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
              m_rowsByKey.remove(new CompositeObject(candidateRow.getKeyValues()));
              if (removed) {
                m_cachedRows = null;
              }
            }
            if (removed) {
              deleteRowImpl(candidateRow);
              rebuildTreeStructure();
            }
          }
        }
      }
      // update index of rows at the bottom of deleted rows
      int minAffectedIndex = Math.max(min - 1, 0);
      ITableRow[] affectedRows = new ITableRow[getRowCount() - minAffectedIndex];
      for (int i = minAffectedIndex; i < getRowCount(); i++) {
        affectedRows[i - minAffectedIndex] = getRow(i);
        ((InternalTableRow) affectedRows[i - minAffectedIndex]).setRowIndex(i);
      }
      if (rowCountBefore == deletedRows.size()) {
        removeUserRowFilters(false);
        fireAllRowsDeleted(deletedRows);
      }
      else {
        fireRowsDeleted(deletedRows);
      }
    }
    finally {
      setTableChanging(false);
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
      // it was new, now it is gone, no further action required
    }
    else {
      internalRow.setStatus(ITableRow.STATUS_DELETED);
      m_deletedRows.put(new CompositeObject(getRowKeys(internalRow)), internalRow);
    }
  }

  @Override
  public void discardRow(int rowIndex) {
    discardRows(new int[]{rowIndex});
  }

  @Override
  public void discardRows(int[] rowIndexes) {
    List<ITableRow> rowList = new ArrayList<>();
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
   * discard is the same as delete with the exception that discarded rows are not collected in the deletedRows list
   */
  @Override
  public void discardRows(Collection<? extends ITableRow> rows) {
    boolean oldAutoDiscardOnDelete = isAutoDiscardOnDelete();
    try {
      setTableChanging(true);
      setAutoDiscardOnDelete(true);
      //
      deleteRows(rows);
    }
    finally {
      setAutoDiscardOnDelete(oldAutoDiscardOnDelete);
      setTableChanging(false);
    }
  }

  @Override
  public void discardAllDeletedRows() {
    for (ITableRow iTableRow : m_deletedRows.values()) {
      ((InternalTableRow) iTableRow).setTableInternal(null);
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
  public ITableRow getRowByKey(List<?> keys) {
    if (!CollectionUtility.hasElements(keys)) {
      return null;
    }
    return m_rowsByKey.get(new CompositeObject(keys));
  }

  @Override
  public List<Object> getParentRowKeys(int rowIndex) {
    ITableRow row = getRow(rowIndex);
    return getParentRowKeys(row);
  }

  @Override
  public List<Object> getParentRowKeys(ITableRow row) {
    if (row != null) {
      return row.getParentKeyValues();
    }
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public ITableRow findParentRow(ITableRow row) {
    return getRowByKey(getParentRowKeys(row));
  }

  /**
   * Gets if the given cell values are equal to the given search values
   *
   * @param searchValues
   *          The values to search in the given cells. Must not be <code>null</code>.
   * @param keyColumns
   *          The columns describing the cells to be searched. Must not be <code>null</code>.
   * @param row
   *          The row holding the cells to be searched. Must not be <code>null</code>.
   * @return <code>true</code> if the cells described by the given columns and row have the same content as the given
   *         searchValues. <code>false</code> otherwise. If the number of columns is different from the number of search
   *         values only the columns are searched for which a search value exists (
   *         <code>min(searchValues.size(), keyColumns.size()</code>).
   */
  protected boolean areCellsEqual(List<?> searchValues, List<IColumn<?>> keyColumns, ITableRow row) {
    int keyIndex = 0;
    int numKeyColumns = keyColumns.size();
    for (Object key : searchValues) {
      if (keyIndex >= numKeyColumns) {
        break;
      }

      Object cellValue = keyColumns.get(keyIndex).getValue(row);
      if (ObjectUtility.notEquals(key, cellValue)) {
        return false;
      }

      keyIndex++;
    }
    return true;
  }

  @Override
  public TableUserFilterManager getUserFilterManager() {
    return (TableUserFilterManager) propertySupport.getProperty(PROP_USER_FILTER_MANAGER);
  }

  @Override
  public void setUserFilterManager(TableUserFilterManager m) {
    propertySupport.setProperty(PROP_USER_FILTER_MANAGER, m);
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
    IWidget parentWidget = getParent();
    if (parentWidget != null) {
      return parentWidget;
    }
    return (ITypeWithClassId) propertySupport.getProperty(PROP_CONTAINER);
  }

  /**
   * do not use this internal method
   */
  public void setContainerInternal(ITypeWithClassId container) {
    propertySupport.setProperty(PROP_CONTAINER, container);
  }

  @Override
  public boolean isSortEnabled() {
    return propertySupport.getPropertyBool(PROP_SORT_ENABLED);
  }

  @Override
  public void setSortEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_SORT_ENABLED, b);
  }

  @Override
  public boolean isUiSortPossible() {
    return propertySupport.getPropertyBool(PROP_UI_SORT_POSSIBLE);
  }

  @Override
  public void setUiSortPossible(boolean b) {
    propertySupport.setPropertyBool(PROP_UI_SORT_POSSIBLE, b);
  }

  public void onGroupedColumnInvisible(IColumn<?> col) {
    if (isTableChanging()) {
      setSortValid(false);
    }
    else {
      sort();
    }
  }

  @Override
  public void sort() {
    try {
      if (isSortEnabled()) {
        // Consider any active sort-column, not only explicit ones.
        // This is to support reverse (implicit) sorting of columns, meaning that multiple column sort is done
        // without CTRL-key held. In contrast to explicit multiple column sort, the first clicked column
        // is the least significant sort column.
        if (!getRows().isEmpty()) {//NOSONAR
          Comparator<ITableRow> comparator = null;
          LinkedHashSet<IColumn<?>> sortCols = new LinkedHashSet<>(getColumnSet().getSortColumns());
          if (!sortCols.isEmpty()) {
            // add all visible columns (not already added, thus LinkedHashSet)
            // as fallback sorting to guarantee same sorting as in JS.
            sortCols.addAll(getColumnSet().getVisibleColumns());
            comparator = new TableRowComparator(sortCols);
          }
          // first make sure decorations and lookups are up-to-date
          processDecorationBuffer();
          sortInternal(sortRows(getRows(), comparator));
        }
      }
    }
    finally {
      setSortValid(true);
    }
  }

  protected List<ITableRow> sortRows(List<? extends ITableRow> rows, Comparator<ITableRow> comparator) {
    List<ITableRow> rootNodes = new ArrayList<>();
    Map<ITableRow/*parent*/, List<ITableRow> /*child rows*/> parentToChildren = new HashMap<>();
    rows.forEach(row -> {
      ITableRow parentRow = findParentRow(row);
      if (parentRow == null) {
        rootNodes.add(row);
      }
      else {
        parentToChildren.computeIfAbsent(parentRow, children -> new ArrayList<>())
            .add(row);
      }
    });

    CollectingVisitor<ITableRow> collector = new CollectingVisitor<>();
    if (comparator != null) {
      rootNodes.sort(comparator);
    }
    rootNodes.forEach(root -> TreeTraversals.create(collector, node -> {
      List<ITableRow> childRows = parentToChildren.get(node);
      if (comparator != null && CollectionUtility.hasElements(childRows)) {
        childRows.sort(comparator);
      }
      return childRows;
    }).traverse(root));
    return collector.getCollection();
  }

  @Override
  public void sort(List<? extends ITableRow> rowsInNewOrder) {
    List<ITableRow> resolvedRows = resolveRows(rowsInNewOrder);
    if (resolvedRows.size() == rowsInNewOrder.size()) {
      sortInternal(resolvedRows);
    }
    else {
      // check which rows could not be mapped
      List<ITableRow> list = new ArrayList<>(m_rows);
      list.removeAll(resolvedRows);
      List<ITableRow> sortedList = new ArrayList<>();
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
    //sort selection and checked rows without firing an event
    if (m_selectedRows != null && !m_selectedRows.isEmpty()) {
      Set<ITableRow> newSelection = new TreeSet<>(new RowIndexComparator());
      newSelection.addAll(m_selectedRows);
      m_selectedRows = new ArrayList<>(newSelection);
    }
    sortCheckedRows();
    fireRowOrderChanged();
  }

  private void sortCheckedRows() {
    if (m_checkedRows == null || m_checkedRows.isEmpty()) {
      return;
    }
    TreeSet<ITableRow> newCheckedRows = new TreeSet<>(new RowIndexComparator());
    newCheckedRows.addAll(m_checkedRows);
    m_checkedRows = new LinkedHashSet<>(newCheckedRows);
  }

  @Override
  public void resetColumnConfiguration() {
    discardAllRows();
    //
    try {
      setTableChanging(true);
      // save displayable state
      HashMap<String, Boolean> displayableState = new HashMap<>();
      for (IColumn<?> col : getColumns()) {
        displayableState.put(col.getColumnId(), col.isDisplayable());
      }
      // reset columns
      disposeColumnsInternal();
      m_objectExtensions.runInExtensionContext(() -> {
        // enforce recreation of the contributed columns so column indexes etc. will be reset
        m_contributionHolder.resetContributionsByClass(AbstractTable.this, IColumn.class);
        // runs within extension context, so that extensions and contributions can be created
        createColumnsInternal();
      });
      initColumnsInternal();
      // re-apply displayable
      for (IColumn<?> col : getColumns()) {
        if (displayableState.get(col.getColumnId()) != null) {
          col.setDisplayable(displayableState.get(col.getColumnId()));
        }
      }
      // relink existing filters to new columns
      linkColumnFilters();
      // reapply compact state
      if (isCompact()) {
        getCompactHandler().handle(isCompact());
      }
      // reset context column (disposed column must not be used anymore)
      setContextColumn(null);
      fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED));
    }
    finally {
      setTableChanging(false);
    }
  }

  private void linkColumnFilters() {
    TableUserFilterManager filterManager = getUserFilterManager();
    if (filterManager == null) {
      return;
    }
    for (IColumn<?> col : getColumns()) {
      getUserFilterManager().getFilters().stream()
          .filter(IColumnAwareUserFilterState.class::isInstance)
          .map(IColumnAwareUserFilterState.class::cast)
          .forEach(filter -> filter.replaceColumn(col));
    }
  }

  @Override
  public void resetColumnVisibilities() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.VISIBILITY));
  }

  @Override
  public void resetColumnOrder() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.ORDER));
  }

  @Override
  public void resetColumnSortOrder() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.SORTING));
  }

  @Override
  public void resetColumnWidths() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.WIDTHS));
  }

  @Override
  public void resetColumnBackgroundEffects() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.BACKGROUND_EFFECTS));
  }

  @Override
  public void resetColumnFilters() {
    resetColumns(CollectionUtility.hashSet(IResetColumnsOption.FILTERS));
  }

  @Override
  public void resetColumns() {
    resetColumns(CollectionUtility.hashSet(
        IResetColumnsOption.VISIBILITY,
        IResetColumnsOption.ORDER,
        IResetColumnsOption.SORTING,
        IResetColumnsOption.WIDTHS,
        IResetColumnsOption.BACKGROUND_EFFECTS,
        IResetColumnsOption.FILTERS));
  }

  @Override
  public void reset() {
    reset(true);
  }

  @Override
  public void reset(boolean store) {
    try {
      setTableChanging(true);
      TableUserFilterManager m = getUserFilterManager();
      if (m != null) {
        m.reset();
      }
      ITableCustomizer cst = getTableCustomizer();
      if (cst != null) {
        cst.removeAllColumns();
      }
      resetColumns();
    }
    finally {
      setTableChanging(false);
    }
    if (store) {
      ClientUIPreferences.getInstance().setAllTableColumnPreferences(this);
    }
  }

  protected void resetColumns(Set<String> options) {
    try {
      setTableChanging(true);
      resetColumnsInternal(options);
      interceptResetColumns(options);
    }
    finally {
      setTableChanging(false);
    }
  }

  private void resetColumnsInternal(Set<String> options) {
    if (options.contains(IResetColumnsOption.SORTING)) {
      setSortValid(false);
    }
    if (options.contains(IResetColumnsOption.ORDER)) {
      SortedMap<CompositeObject, IColumn<?>> orderMap = new TreeMap<>();
      int index = 0;
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable()) {
          orderMap.put(new CompositeObject(col.getOrder(), index), col);
          index++;
        }
      }
      getColumnSet().setVisibleColumns(orderMap.values());
    }

    if (options.contains(IResetColumnsOption.VISIBILITY)) {
      List<IColumn<?>> list = new ArrayList<>();
      for (IColumn<?> col : getColumnSet().getAllColumnsInUserOrder()) {
        if (col.isDisplayable()) {
          boolean configuredVisible = col.isInitialVisible();
          if (configuredVisible) {
            list.add(col);
          }
        }
      }
      getColumnSet().setVisibleColumns(list);
    }

    if (options.contains(IResetColumnsOption.SORTING)) {
      getColumnSet().resetSortingAndGrouping();
    }

    if (options.contains(IResetColumnsOption.WIDTHS)) {
      for (IColumn<?> col : getColumns()) {
        if (col.isDisplayable()) {
          col.setWidth(col.getInitialWidth());
        }
      }
    }

    if (options.contains(IResetColumnsOption.BACKGROUND_EFFECTS)) {
      for (IColumn<?> col : getColumns()) {
        if (col instanceof INumberColumn) {
          ((INumberColumn) col).setBackgroundEffect(((INumberColumn) col).getInitialBackgroundEffect());
        }
      }
    }

    if (options.contains(IResetColumnsOption.FILTERS)) {
      removeUserRowFilters();
    }
  }

  /**
   * Affects columns with lookup calls or code types<br>
   * cells that have changed values fetch new texts/decorations from the lookup service in one single batch call lookup
   * (performance optimization)
   */
  private void processDecorationBuffer() {
    /*
     * update row decorations
     */
    Map<Integer, Set<ITableRow>> changes = m_rowValueChangeBuffer;
    m_rowValueChangeBuffer = new HashMap<>();
    applyRowValueChanges(changes);

    Set<ITableRow> set = m_rowDecorationBuffer;
    m_rowDecorationBuffer = new HashSet<>();
    applyRowDecorations(set);
    /*
     * check row filters
     */
    if (!m_rowFilters.isEmpty()) {
      boolean filterChanged = false;
      for (ITableRow row : set) {
        if (row.getTable() == AbstractTable.this && row instanceof InternalTableRow) {
          InternalTableRow internalRow = (InternalTableRow) row;
          boolean oldFlag = internalRow.isFilterAccepted();
          applyRowFiltersInternal(internalRow);
          boolean newFlag = internalRow.isFilterAccepted();
          filterChanged = filterChanged || (oldFlag != newFlag);
        }
      }
      if (filterChanged) {
        fireRowFilterChanged();
      }
    }
  }

  private void applyRowValueChanges(Map<Integer, Set<ITableRow>> changes) {
    // performance quick-check
    if (changes.isEmpty()) {
      return;
    }
    try {
      for (ITableRow tableRow : getRows()) {
        tableRow.setRowChanging(true);
      }

      Set<Entry<Integer, Set<ITableRow>>> entrySet = changes.entrySet();

      for (Entry<Integer, Set<ITableRow>> e : entrySet) {
        IColumn<?> col = getColumnSet().getColumn(e.getKey());
        col.updateDisplayTexts(CollectionUtility.arrayList(e.getValue()));
      }
    }
    finally {
      for (ITableRow tableRow : getRows()) {
        tableRow.setRowPropertiesChanged(false);
        tableRow.setRowChanging(false);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void applyRowDecorations(Set<ITableRow> rows) {
    // performance quick-check
    if (rows.isEmpty()) {
      return;
    }
    try {
      for (ITableRow tableRow : rows) {
        tableRow.setRowChanging(true);
        this.decorateRow(tableRow);
      }

      for (IColumn col : getColumns()) {
        col.decorateCells(CollectionUtility.arrayList(rows));

        // cell decorator on table
        for (ITableRow row : rows) {
          this.decorateCell(row, col);
        }
      }
    }
    catch (Exception ex) {
      LOG.error("Error occurred while applying row decoration", ex);
    }
    finally {
      for (ITableRow tableRow : rows) {
        tableRow.setRowPropertiesChanged(false);
        tableRow.setRowChanging(false);
      }
    }
  }

  /**
   * Fires events in form in of one batch <br>
   * Unnecessary events are removed or merged.
   */
  private void processEventBuffer() {
    //loop detection
    try {
      m_eventBufferLoopDetection++;
      if (m_eventBufferLoopDetection > 100) {
        LOG.error("LOOP DETECTION in {}. see stack trace for more details.", getClass(), new Exception("LOOP DETECTION"));
        return;
      }
      //
      if (!getEventBuffer().isEmpty()) {
        List<TableEvent> list = getEventBuffer().consumeAndCoalesceEvents();
        // fire the batch and set tree to changing, otherwise a listener might trigger another events that
        // then are processed before all other listeners received that batch
        try {
          setTableChanging(true);
          //
          m_listeners.fireEvents(list);
        }
        finally {
          setTableChanging(false);
        }
      }
    }
    finally {
      m_eventBufferLoopDetection--;
    }
  }

  /**
   * do decoration and filtering later
   */
  private void enqueueDecorationTasks(ITableRow row) {
    if (row != null) {
      m_rowDecorationBuffer.add(row);
    }
  }

  private void enqueueValueChangeTasks(ITableRow row, Set<Integer> valueChangedColumns) {
    for (Integer colIndex : valueChangedColumns) {
      Set<ITableRow> rows = m_rowValueChangeBuffer.get(colIndex);
      if (rows == null) {
        rows = new HashSet<>();
      }
      rows.add(row);
      m_rowValueChangeBuffer.put(colIndex, rows);
    }
  }

  @Override
  public ITableRow resolveRow(ITableRow row) {
    if (row == null) {
      return null;
    }
    if (!(row instanceof InternalTableRow)) {
      throw new IllegalArgumentException("only accept InternalTableRow, not " + row.getClass());
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
    List<ITableRow> resolvedRows = new ArrayList<>(rows.size());
    for (ITableRow row : rows) {
      if (resolveRow(row) == row) {
        resolvedRows.add(row);
      }
      else {
        //noinspection PlaceholderCountMatchesArgumentCount
        LOG.info("Could not resolve row with keys {} in table {}",
            row.getKeyValues(), this.getClass().getName(),
            LOG.isDebugEnabled() ? new Exception("stacktrace") : null);
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
  public boolean isHeaderEnabled() {
    return propertySupport.getPropertyBool(PROP_HEADER_ENABLED);
  }

  @Override
  public void setHeaderEnabled(boolean headerEnabled) {
    propertySupport.setPropertyBool(PROP_HEADER_ENABLED, headerEnabled);
  }

  @Override
  public boolean isHeaderMenusEnabled() {
    return propertySupport.getPropertyBool(PROP_HEADER_MENUS_ENABLED);
  }

  @Override
  public void setHeaderMenusEnabled(boolean headerMenusEnabled) {
    propertySupport.setPropertyBool(PROP_HEADER_MENUS_ENABLED, headerMenusEnabled);
  }

  @Override
  public boolean isClientUiPreferencesEnabled() {
    return propertySupport.getPropertyBool(PROP_CLIENT_UI_PREFERENCES_ENABLED);
  }

  @Override
  public void setClientUiPreferencesEnabled(boolean clientUiPreferencesEnabled) {
    propertySupport.setPropertyBool(PROP_CLIENT_UI_PREFERENCES_ENABLED, clientUiPreferencesEnabled);
  }

  @Override
  public boolean isTextFilterEnabled() {
    return propertySupport.getPropertyBool(PROP_TEXT_FILTER_ENABLED);
  }

  @Override
  public void setTextFilterEnabled(boolean textFilterEnabled) {
    propertySupport.setPropertyBool(PROP_TEXT_FILTER_ENABLED, textFilterEnabled);
  }

  @Override
  public final void decorateCell(ITableRow row, IColumn<?> col) {
    Cell cell = row.getCellForUpdate(col.getColumnIndex());
    decorateCellInternal(cell, row, col);
    try {
      interceptDecorateCell(cell, row, col);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  protected void decorateCellInternal(Cell view, ITableRow row, IColumn<?> col) {
  }

  @Override
  public final void decorateRow(ITableRow row) {
    decorateRowInternal(row);
    try {
      interceptDecorateRow(row);
    }
    catch (Exception e) {
      BEANS.get(ExceptionHandler.class).handle(e);
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
   * @param options
   *          Set of constants of {@link IResetColumnsOption}
   */
  @ConfigOperation
  @Order(90)
  protected void execResetColumns(Set<String> options) {
  }

  @Override
  public TableListeners tableListeners() {
    return m_listeners;
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
    TableEvent e = new TableEvent(this, TableEvent.TYPE_ROWS_UPDATED, rows);
    // For each row, add information about updated columns to the event. (A row may also be updated if
    // not specific column was changed, e.g. when a row's enabled state changes.)
    for (ITableRow row : rows) {
      // Convert column indexes to IColumns
      Set<Integer> columnIndexes = row.getUpdatedColumnIndexes();
      if (!columnIndexes.isEmpty()) {
        Set<IColumn<?>> columns = new HashSet<>();
        for (Integer columnIndex : columnIndexes) {
          IColumn<?> column = getColumns().get(columnIndex);
          if (column != null) {
            columns.add(column);
          }
        }
        // Put updated columns into event
        e.setUpdatedColumns(row, columns);
      }
    }
    fireTableEventInternal(e);
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

  private void fireRowsChecked(List<? extends ITableRow> rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_CHECKED, rows));
  }

  private void fireRowsExpanded(List<? extends ITableRow> rows) {
    fireTableEventInternal(new TableEvent(this, TableEvent.TYPE_ROWS_EXPANDED, rows));
  }

  private void fireRowClick(ITableRow row, MouseButton mouseButton) {
    if (row != null) {
      try {
        interceptRowClickSingleObserver(row, mouseButton);
        interceptRowClick(row, mouseButton);
      }
      catch (Exception ex) {
        BEANS.get(ExceptionHandler.class).handle(ex);
      }
    }
  }

  protected void interceptRowClickSingleObserver(ITableRow row, MouseButton mouseButton) {
    // Only toggle checked state if the table and row are enabled.
    if (!row.isEnabled() || !isEnabledIncludingParents()) {
      return;
    }

    // Only toggle checked state if being fired by the left mouse button (https://bugs.eclipse.org/bugs/show_bug.cgi?id=453543).
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
          IBooleanField booleanField = (IBooleanField) field;
          booleanField.toggleValue();
          ctxCol.completeEdit(row, field);
        }
      }
      else {
        //other editable columns have no effect HERE, the ui will open an editor
      }
    }
  }

  private void fireRowAction(ITableRow row) {
    if (isActionRunning() || row == null) {
      return;
    }

    try {
      setActionRunning(true);
      interceptRowAction(row);
    }
    catch (Exception ex) {
      BEANS.get(ExceptionHandler.class).handle(ex);
    }
    finally {
      setActionRunning(false);
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

  // main handler
  public void fireTableEventInternal(TableEvent e) {
    if (isTableChanging()) {
      // buffer the event for later batch firing
      getEventBuffer().add(e);
    }
    else {
      doFireTableEvent(e);
    }
  }

  protected void doFireTableEvent(TableEvent e) {
    m_listeners.fireEvent(e);
  }

  @Override
  public ITableUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setReloadHandler(IReloadHandler reloadHandler) {
    m_reloadHandler = reloadHandler;
  }

  @Override
  public IReloadHandler getReloadHandler() {
    return m_reloadHandler;
  }

  @Override
  public List<ITableControl> getTableControls() {
    return CollectionUtility.arrayList(m_tableControls);
  }

  @Override
  public void addTableControl(ITableControl control) {
    m_tableControls.add(control);
    addTableControlInternal(control);
  }

  private void addTableControlInternal(ITableControl control) {
    ((AbstractTableControl) control).setTable(this);
    m_tableControls.sort(new OrderedComparator());
    propertySupport.firePropertyChange(PROP_TABLE_CONTROLS, null, getTableControls());
  }

  @Override
  public void removeTableControl(ITableControl control) {
    m_tableControls.remove(control);
    ((AbstractTableControl) control).setTable(null);
    propertySupport.firePropertyChange(PROP_TABLE_CONTROLS, null, getTableControls());
  }

  @Override
  public <T extends ITableControl> T getTableControl(Class<T> controlClass) {
    for (ITableControl control : m_tableControls) {
      if (controlClass.isAssignableFrom(control.getClass())) {
        return controlClass.cast(control);
      }
    }
    return null;
  }

  /**
   * Configures the visibility of the table status.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the table status is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredTableStatusVisible() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(210)
  protected boolean getConfiguredTextFilterEnabled() {
    return true;
  }

  @Override
  public boolean isTableStatusVisible() {
    return propertySupport.getPropertyBool(PROP_TABLE_STATUS_VISIBLE);
  }

  @Override
  public void setTableStatusVisible(boolean visible) {
    propertySupport.setPropertyBool(PROP_TABLE_STATUS_VISIBLE, visible);
  }

  @Override
  public IStatus getTableStatus() {
    return (IStatus) propertySupport.getProperty(PROP_TABLE_STATUS);
  }

  @Override
  public void setTableStatus(IStatus status) {
    propertySupport.setProperty(PROP_TABLE_STATUS, status);
  }

  /**
   * Check if this column would prevent an ui sort for table. If it prevents an ui sort,
   * {@link ITable#setUiSortPossible(boolean)} is set to <code>false</code> for all columns of the table.
   */
  protected void checkIfColumnPreventsUiSortForTable() {
    for (IColumn<?> column : m_columnSet.getColumns()) {
      if (!column.isVisible() && column.getSortIndex() != -1) {
        setUiSortPossible(false);
        return;
      }
    }
    setUiSortPossible(true);
  }

  /**
   * Checks if the context column is visible. If not, the context column is set to <code>null</code>.
   */
  protected void checkIfContextColumnIsVisible() {
    IColumn<?> contextColumn = getContextColumn();
    if (contextColumn != null && !contextColumn.isVisible()) {
      setContextColumn(null);
    }
  }

  @Override
  public List<ITableRowTileMapping> createTiles(List<? extends ITableRow> rows) {
    return rows.stream()
        .map(row -> {
          ITile tile = interceptCreateTile(row);
          if (tile != null && tile.getParent() == null) {
            tile.setParentInternal(this);
          }
          return BEANS.get(TableRowTileMapping.class)
              .withTableRow(row)
              .withTile(tile);
        })
        .filter(m -> m.getTile() != null)
        .collect(Collectors.toList());
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
    public void fireHeaderSortFromUI(IColumn<?> c, boolean multiSort, boolean ascending) {
      try {
        pushUIProcessor();
        //
        if (isSortEnabled()) {
          c = getColumnSet().resolveColumn(c);
          if (c != null) {
            getColumnSet().handleSortEvent(c, multiSort, ascending);
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
    public void fireHeaderGroupFromUI(IColumn<?> c, boolean multiGroup, boolean ascending) {
      try {
        pushUIProcessor();
        //
        if (isSortEnabled()) {
          c = getColumnSet().resolveColumn(c);
          if (c != null) {
            getColumnSet().handleGroupingEvent(c, multiGroup, ascending);
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
    public void fireAggregationFunctionChanged(INumberColumn<?> c, String function) {

      try {
        pushUIProcessor();
        getColumnSet().setAggregationFunction(c, function);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setColumnBackgroundEffect(INumberColumn<?> column, String effect) {
      try {
        pushUIProcessor();
        column.setBackgroundEffect(effect);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setCheckedRowsFromUI(List<? extends ITableRow> rows, boolean checked) {
      if (!isEnabled()) {
        return;
      }
      try {
        pushUIProcessor();
        checkRows(rows, checked, true);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setExpandedRowsFromUI(List<? extends ITableRow> rows, boolean expanded) {
      if (CollectionUtility.isEmpty(rows)) {
        return;
      }
      try {
        pushUIProcessor();
        expandRowsInternal(rows, expanded);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setSelectedRowsFromUI(List<? extends ITableRow> rows) {
      try {
        pushUIProcessor();
        selectRows(rows, false);
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
    public void fireAppLinkActionFromUI(String ref) {
      try {
        pushUIProcessor();
        //
        doAppLinkAction(ref);
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
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
      if (!isEnabled()) {
        return null;
      }
      try {
        pushUIProcessor();
        //
        disposeCellEditor();
        row = resolveRow(row);
        if (row != null && col != null) {
          // ensure the editable row to be selected.
          // This is crucial if the cell's value is changed right away in IColumn#prepareEdit(ITableRow), e.g. in AbstractBooleanColumn
          row.getTable().selectRow(row);
          IFormField f = col.prepareEdit(row);
          if (f != null) {
            m_editContext = new P_CellEditorContext(row, col, f);
          }
          startCellEdit();
          return f;
        }
      }
      finally {
        popUIProcessor();
      }
      return null;
    }

    @Override
    public void completeCellEditFromUI() {
      if (!isEnabled()) {
        return;
      }
      try {
        pushUIProcessor();
        completeCellEdit();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void cancelCellEditFromUI() {
      try {
        pushUIProcessor();
        cancelCellEdit();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireTableReloadFromUI(String reloadReason) {
      if (m_reloadHandler != null) {
        try {
          pushUIProcessor();
          //
          m_reloadHandler.reload(reloadReason);
        }
        finally {
          popUIProcessor();
        }
      }
    }

    @Override
    public void fireTableResetFromUI() {
      reset();
    }

    @Override
    public void fireSortColumnRemovedFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getColumnSet().removeSortColumn(column);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        sort();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireGroupColumnRemovedFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getColumnSet().removeGroupColumn(column);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
        sort();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireOrganizeColumnAddFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getTableOrganizer().addColumn(column);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireOrganizeColumnRemoveFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getTableOrganizer().removeColumn(column);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireOrganizeColumnModifyFromUI(IColumn<?> column) {
      try {
        pushUIProcessor();
        //
        getTableOrganizer().modifyColumn(column);
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(AbstractTable.this);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireFilterAddedFromUI(IUserFilterState filter) {
      try {
        pushUIProcessor();
        //
        getUserFilterManager().addFilter(filter);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void fireFilterRemovedFromUI(IUserFilterState filter) {
      try {
        pushUIProcessor();
        //
        getUserFilterManager().removeFilter(filter);
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void setFilteredRowsFromUI(List<? extends ITableRow> rows) {
      try {
        pushUIProcessor();
        // Remove existing filter first, so that only one UserTableRowFilter is active
        removeUserRowFilters(false);

        // Create and add a new filter
        UserTableRowFilter filter = new UserTableRowFilter(rows);

        // Do not use addRowFilter to prevent applyRowFilters
        m_rowFilters.add(filter);
        applyRowFilters();
      }
      finally {
        popUIProcessor();
      }
    }

    @Override
    public void removeFilteredRowsFromUI() {
      try {
        pushUIProcessor();
        removeUserRowFilters();
      }
      finally {
        popUIProcessor();
      }
    }
  }

  private class P_TableRowBuilder extends AbstractTableRowBuilder<Object> {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getColumnSet());
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
    public void execAppLinkAction(TableAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }

    @Override
    public void execRowAction(TableRowActionChain chain, ITableRow row) {
      getOwner().execRowAction(row);
    }

    @Override
    public void execContentChanged(TableContentChangedChain chain) {
      getOwner().execContentChanged();
    }

    @Override
    public ITableRowDataMapper execCreateTableRowDataMapper(TableCreateTableRowDataMapperChain chain, Class<? extends AbstractTableRowData> rowType) {
      return getOwner().execCreateTableRowDataMapper(rowType);
    }

    @Override
    public void execInitTable(TableInitTableChain chain) {
      getOwner().execInitTable();
    }

    @Override
    public void execResetColumns(TableResetColumnsChain chain, Set<String> options) {
      getOwner().execResetColumns(options);
    }

    @Override
    public void execDecorateCell(TableDecorateCellChain chain, Cell view, ITableRow row, IColumn<?> col) {
      getOwner().execDecorateCell(view, row, col);
    }

    @Override
    public void execDrop(TableDropChain chain, ITableRow row, TransferObject t) {
      getOwner().execDrop(row, t);
    }

    @Override
    public void execDisposeTable(TableDisposeTableChain chain) {
      getOwner().execDisposeTable();
    }

    @Override
    public void execRowClick(TableRowClickChain chain, ITableRow row, MouseButton mouseButton) {
      getOwner().execRowClick(row, mouseButton);
    }

    @Override
    public void execDecorateRow(TableDecorateRowChain chain, ITableRow row) {
      getOwner().execDecorateRow(row);
    }

    @Override
    public TransferObject execCopy(TableCopyChain chain, List<? extends ITableRow> rows) {
      return getOwner().execCopy(rows);
    }

    @Override
    public void execRowsSelected(TableRowsSelectedChain chain, List<? extends ITableRow> rows) {
      getOwner().execRowsSelected(rows);
    }

    @Override
    public TransferObject execDrag(TableDragChain chain, List<ITableRow> rows) {
      return getOwner().execDrag(rows);
    }

    @Override
    public void execRowsChecked(TableRowsCheckedChain chain, List<? extends ITableRow> row) {
      getOwner().execRowsChecked(row);
    }

    @Override
    public ITile execCreateTile(TableCreateTileChain chain, ITableRow row) {
      return getOwner().execCreateTile(row);
    }
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableAppLinkActionChain chain = new TableAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected final void interceptRowAction(ITableRow row) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowActionChain chain = new TableRowActionChain(extensions);
    chain.execRowAction(row);
  }

  protected final void interceptContentChanged() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableContentChangedChain chain = new TableContentChangedChain(extensions);
    chain.execContentChanged();
  }

  protected final ITableRowDataMapper interceptCreateTableRowDataMapper(Class<? extends AbstractTableRowData> rowType) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCreateTableRowDataMapperChain chain = new TableCreateTableRowDataMapperChain(extensions);
    return chain.execCreateTableRowDataMapper(rowType);
  }

  protected final void interceptInitTable() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableInitTableChain chain = new TableInitTableChain(extensions);
    chain.execInitTable();
  }

  protected final void interceptResetColumns(Set<String> options) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableResetColumnsChain chain = new TableResetColumnsChain(extensions);
    chain.execResetColumns(options);
  }

  protected final void interceptDecorateCell(Cell view, ITableRow row, IColumn<?> col) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateCellChain chain = new TableDecorateCellChain(extensions);
    chain.execDecorateCell(view, row, col);
  }

  protected final void interceptDrop(ITableRow row, TransferObject t) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDropChain chain = new TableDropChain(extensions);
    chain.execDrop(row, t);
  }

  protected final void interceptDisposeTable() {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDisposeTableChain chain = new TableDisposeTableChain(extensions);
    chain.execDisposeTable();
  }

  protected final void interceptRowClick(ITableRow row, MouseButton mouseButton) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowClickChain chain = new TableRowClickChain(extensions);
    chain.execRowClick(row, mouseButton);
  }

  protected final void interceptDecorateRow(ITableRow row) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDecorateRowChain chain = new TableDecorateRowChain(extensions);
    chain.execDecorateRow(row);
  }

  protected final TransferObject interceptCopy(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCopyChain chain = new TableCopyChain(extensions);
    return chain.execCopy(rows);
  }

  protected final void interceptRowsSelected(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowsSelectedChain chain = new TableRowsSelectedChain(extensions);
    chain.execRowsSelected(rows);
  }

  protected final void interceptRowsChecked(List<? extends ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableRowsCheckedChain chain = new TableRowsCheckedChain(extensions);
    chain.execRowsChecked(rows);
  }

  protected final TransferObject interceptDrag(List<ITableRow> rows) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableDragChain chain = new TableDragChain(extensions);
    return chain.execDrag(rows);
  }

  protected final ITile interceptCreateTile(ITableRow row) {
    List<? extends ITableExtension<? extends AbstractTable>> extensions = getAllExtensions();
    TableCreateTileChain chain = new TableCreateTileChain(extensions);
    return chain.execCreateTile(row);
  }

  @Override
  public boolean isValueChangeTriggerEnabled() {
    return m_valueChangeTriggerEnabled >= 1;
  }

  @Override
  public void setValueChangeTriggerEnabled(boolean b) {
    if (b) {
      m_valueChangeTriggerEnabled++;
    }
    else {
      m_valueChangeTriggerEnabled--;
    }
  }

  protected void setTableOrganizer(ITableOrganizer tableOrganizer) {
    propertySupport.setProperty(PROP_TABLE_ORGANIZER, tableOrganizer);
  }

  @Override
  public ITableOrganizer getTableOrganizer() {
    return (ITableOrganizer) propertySupport.getProperty(PROP_TABLE_ORGANIZER);
  }

  @Override
  public boolean isCustomizable() {
    return getTableCustomizer() != null;
  }

  @Override
  public GroupingStyle getGroupingStyle() {
    return (GroupingStyle) propertySupport.getProperty(PROP_GROUPING_STYLE);
  }

  @Override
  public void setGroupingStyle(GroupingStyle groupingStyle) {
    propertySupport.setProperty(PROP_GROUPING_STYLE, groupingStyle);
  }

  @Override
  public HierarchicalStyle getHierarchicalStyle() {
    return (HierarchicalStyle) propertySupport.getProperty(PROP_HIERARCHICAL_STYLE);
  }

  @Override
  public void setHierarchicalStyle(HierarchicalStyle hierarchicalStyle) {
    propertySupport.setProperty(PROP_HIERARCHICAL_STYLE, hierarchicalStyle);
  }

  @Override
  public CheckableStyle getCheckableStyle() {
    return (CheckableStyle) propertySupport.getProperty(PROP_CHECKABLE_STYLE);
  }

  @Override
  public void setCheckableStyle(CheckableStyle checkableStyle) {
    propertySupport.setProperty(PROP_CHECKABLE_STYLE, checkableStyle);
  }

  @Override
  public long getEstimatedRowCount() {
    return propertySupport.getPropertyLong(PROP_ESTIMATED_ROW_COUNT);
  }

  @Override
  public void setEstimatedRowCount(long estimatedRowCount) {
    propertySupport.setPropertyLong(PROP_ESTIMATED_ROW_COUNT, estimatedRowCount);
  }

  @Override
  public int getMaxRowCount() {
    return propertySupport.getPropertyInt(PROP_MAX_ROW_COUNT);
  }

  @Override
  public void setMaxRowCount(int maxRowCount) {
    propertySupport.setPropertyInt(PROP_MAX_ROW_COUNT, maxRowCount);
  }

  @Override
  public TriState isTruncatedCellTooltipEnabled() {
    return (TriState) propertySupport.getProperty(PROP_TRUNCATED_CELL_TOOLTIP_ENABLED);
  }

  @Override
  public void setTruncatedCellTooltipEnabled(TriState truncatedCellTooltipEnabled) {
    if (truncatedCellTooltipEnabled == null) {
      truncatedCellTooltipEnabled = TriState.UNDEFINED;
    }
    propertySupport.setProperty(PROP_TRUNCATED_CELL_TOOLTIP_ENABLED, truncatedCellTooltipEnabled);
  }

  @Override
  public boolean isTileMode() {
    return BooleanUtility.nvl((Boolean) propertySupport.getProperty(PROP_TILE_MODE));
  }

  @Override
  public void setTileMode(boolean tileMode) {
    if (tileMode) {
      // create mediator and header lazy
      if (getTableTileGridMediator() == null) {
        setTableTileGridMediator(createTableTileGridMediator());
      }
      if (getTileTableHeader() == null) {
        setTileTableHeader(createTileTableHeader());
      }
    }
    propertySupport.setProperty(PROP_TILE_MODE, tileMode);
  }
}
