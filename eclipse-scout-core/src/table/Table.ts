/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractTableAccessibilityRenderer, Action, AggregateTableControl, Alignment, AppLinkKeyStroke, aria, arrays, BookmarkTableRowIdentifierBooleanComponentDo, BookmarkTableRowIdentifierDo, BookmarkTableRowIdentifierLongComponentDo,
  BookmarkTableRowIdentifierStringComponentDo, BooleanColumn, Cell, CellEditorPopup, clipboard, Column, ColumnModel, CompactColumn, Comparator, ContextMenuKeyStroke, ContextMenuPopup, DefaultTableAccessibilityRenderer, Desktop,
  DesktopPopupOpenEvent, Device, DisplayViewId, DoubleClickSupport, dragAndDrop, DragAndDropHandler, DropType, EnumObject, EventHandler, Filter, Filterable, FilterOrFunction, FilterResult, FilterSupport, FullModelOf, graphics,
  HierarchicalTableAccessibilityRenderer, HtmlComponent, IconColumn, InitModelOf, Insets, KeyStrokeContext, LimitedResultTableStatus, LoadingSupport, Menu, MenuBar, MenuDestinations, MenuItemsOrder, menus, NumberColumn,
  NumberColumnAggregationFunction, NumberColumnBackgroundEffect, ObjectOrChildModel, ObjectOrModel, objects, Predicate, PropertyChangeEvent, Range, scout, scrollbars, ScrollToOptions, Status, StatusOrModel, strings, styles,
  TableCompactHandler, TableControl, TableCopyKeyStroke, TableEventMap, TableFooter, TableHeader, TableLayout, TableModel, TableNavigationCollapseKeyStroke, TableNavigationDownKeyStroke, TableNavigationEndKeyStroke,
  TableNavigationExpandKeyStroke, TableNavigationHomeKeyStroke, TableNavigationPageDownKeyStroke, TableNavigationPageUpKeyStroke, TableNavigationUpKeyStroke, TableOrganizer, TableRefreshKeyStroke, TableRow, TableRowModel,
  TableSelectAllKeyStroke, TableSelectionHandler, TableStartCellEditKeyStroke, TableTextUserFilter, TableTileGridMediator, TableToggleRowKeyStroke, TableTooltip, TableUpdateBuffer, TableUserFilter, TableUserFilterModel, Tile,
  TileTableHeaderBox, tooltips, TooltipSupport, UpdateFilteredElementsOptions, ValueField, Widget
} from '../index';
import $ from 'jquery';

export class Table extends Widget implements TableModel, Filterable<TableRow> {
  declare model: TableModel;
  declare eventMap: TableEventMap;
  declare self: Table;
  declare columnMap: ColumnMap;

  autoResizeColumns: boolean;
  columnAddable: boolean;
  columnLayoutDirty: boolean;
  columns: Column<any>[];
  contextColumn: Column<any>;
  checkable: boolean;
  displayViewId: DisplayViewId; // set by DesktopBench
  checkableStyle: TableCheckableStyle;
  cellEditorPopup: CellEditorPopup<any>;
  compact: boolean;
  openFieldPopupOnCellEdit: boolean;
  compactHandler: TableCompactHandler;
  compactColumn: CompactColumn;
  dropType: DropType;
  dropMaximumSize: number;
  dragAndDropHandler: DragAndDropHandler;
  groupingStyle: TableGroupingStyle;
  header: TableHeader;
  tableStatus: Status;
  rowBorders: Insets;
  headerEnabled: boolean;
  headerVisible: boolean;
  headerMenusEnabled: boolean;
  hasReloadHandler: boolean;
  /**
   * Defines whether hierarchical mode is active, meaning the table is grouping rows with the same {@link TableRow.parentRow} and allowing the user to expand and collapse these groups.
   *
   * The property returns true if there is at least one row with a parent row, false otherwise.
   */
  hierarchical: boolean;
  hierarchicalStyle: TableHierarchicalStyle;
  keyStrokes: Action[];
  menus: Menu[];
  menuBar: MenuBar;
  menuBarVisible: boolean;
  contextMenu: ContextMenuPopup;
  multiCheck: boolean;
  multiSelect: boolean;
  multilineText: boolean;
  scrollToSelection: boolean;
  selectedRows: TableRow[];
  sortEnabled: boolean;
  tableControls: TableControl[];
  tableStatusVisible: boolean;
  tableTileGridMediator: TableTileGridMediator;
  tileMode: boolean;
  tileTableHeader: TileTableHeaderBox;
  tileProducer: (row: TableRow) => Tile;
  footer: TableFooter;
  footerVisible: boolean;
  filters: Filter<TableRow>[];
  /**
   * Contains all rows of the table.
   */
  rows: TableRow[];
  /**
   * Contains only the root rows of the table.
   * If the table is not {@link hierarchical}, it is the same as {@link rows}.
   */
  rootRows: TableRow[];
  /**
   * Contains only the rows that are expanded and accepted by all filters.
   */
  visibleRows: TableRow[];
  estimatedRowCount: number;
  maxRowCount: number;
  aggregateRowHeight: number;
  truncatedCellTooltipEnabled: boolean;
  checkableColumn: BooleanColumn;
  rowIconColumn: IconColumn;
  uiCssClass: string;
  /** visible rows by id */
  visibleRowsMap: Record<string, TableRow>;
  rowLevelPadding: number;
  /** rows by id */
  rowsMap: Record<string, TableRow>;
  rowHeight: number;
  rowWidth: number;
  /** read-only, set by _calculateRowInsets(), also used in TableHeader.js */
  rowInsets: Insets;
  /** read-only, set by _calculateRowInsets(), also used in TableLayout.js */
  rowMargins: Insets;
  rowIconVisible: boolean;
  rowIconColumnWidth: number;
  staticMenus: Menu[];
  selectionHandler: TableSelectionHandler;
  tooltips: TableTooltip[];
  tableNodeColumn: Column<any>;
  updateBuffer: TableUpdateBuffer;
  /**
   * Initial value must be > 0 to make prefSize work (if it is 0, no filler will be generated).
   * If rows have a variable height, prefSize is only correct for 10 rows.
   * Layout will adjust this value depending on the view port size.
   */
  viewRangeSize: number;
  viewRangeDirty: boolean;
  viewRangeRendered: Range;
  virtual: boolean;
  textFilterEnabled: boolean;
  filterSupport: FilterSupport<TableRow>;
  filteredElementsDirty: boolean;
  defaultMenuTypes: string[];
  accessibilityRenderer: AbstractTableAccessibilityRenderer;
  organizer: TableOrganizer;

  $data: JQuery;
  $emptyData: JQuery;
  $fillBefore: JQuery;
  $fillAfter: JQuery;

  /** @internal */
  _renderViewportBlocked: boolean;
  /** @internal */
  _filterMenusHandler: (menuItems: Menu[], destination: MenuDestinations) => Menu[];
  /** @internal */
  _aggregateRows: AggregateTableRow[];

  protected _filteredRows: TableRow[];
  protected _maxLevel: number;
  protected _animationRowLimit: number;
  protected _blockLoadThreshold: number;
  protected _doubleClickSupport: DoubleClickSupport;
  protected _permanentHeadSortColumns: Column<any>[];
  protected _permanentTailSortColumns: Column<any>[];
  protected _popupOpenHandler: EventHandler<DesktopPopupOpenEvent>;
  protected _rerenderViewPortAfterAttach: boolean;
  protected _renderViewPortAfterAttach: boolean;
  protected _triggerRowsSelectedPending: boolean;
  protected _animateAggregateRows: boolean;
  protected _postAttachActions: (() => void)[];
  protected _desktopPropertyChangeHandler: EventHandler<PropertyChangeEvent<any, Desktop>>;
  protected _menuInheritAccessibilityChangeHandler: EventHandler<PropertyChangeEvent<boolean, Menu>>;
  protected _imageLoadListener: (event: ErrorEvent) => void;
  protected _insertedRows: TableRow[];
  protected _$mouseDownRow: JQuery;
  protected _mouseDownRowId: string;
  protected _mouseDownColumn: Column<any>;

  constructor() {
    super();

    this.autoResizeColumns = false;
    this.columnAddable = true;
    this.columnLayoutDirty = false;
    this.columns = [];
    this.contextColumn = null;
    this.checkable = false;
    this.checkableStyle = Table.CheckableStyle.CHECKBOX;
    this.cellEditorPopup = null;
    this.compact = false;
    this.compactHandler = scout.create(TableCompactHandler, {table: this});
    this.compactColumn = null;
    this.dropType = DropType.NONE;
    this.dropMaximumSize = dragAndDrop.DEFAULT_DROP_MAXIMUM_SIZE;
    this.groupingStyle = Table.GroupingStyle.TOP;
    this.header = null;
    this.headerEnabled = true;
    this.headerVisible = true;
    this.headerMenusEnabled = true;
    this.hasReloadHandler = false;
    this.hierarchical = false;
    this.hierarchicalStyle = Table.HierarchicalStyle.DEFAULT;
    this.keyStrokes = [];
    this.menus = [];
    this.menuBar = null;
    this.menuBarVisible = true;
    this.contextMenu = null;
    this.multiCheck = true;
    this.multiSelect = true;
    this.multilineText = false;
    this.scrollToSelection = false;
    this.scrollTop = 0;
    this.selectedRows = [];
    this.sortEnabled = true;
    this.tableControls = [];
    this.tableStatusVisible = false;
    this.tableTileGridMediator = null;
    this.tileMode = false;
    this.tileTableHeader = null;
    this.tileProducer = null;
    this.footer = null;
    this.footerVisible = false;
    this.filters = [];
    this.rows = [];
    this.rootRows = [];
    this.visibleRows = [];
    this.estimatedRowCount = 0;
    this.maxRowCount = 0;
    this.truncatedCellTooltipEnabled = null;
    this.visibleRowsMap = {};
    this.rowLevelPadding = 0;
    this.rowsMap = {};
    this.rowHeight = 0;
    this.rowWidth = 0;
    this.rowInsets = new Insets();
    this.rowMargins = new Insets();
    this.rowIconVisible = false;
    this.rowIconColumnWidth = Column.NARROW_MIN_WIDTH;
    this.staticMenus = [];
    this.selectionHandler = new TableSelectionHandler(this);
    this.tooltips = [];
    this._filteredRows = [];
    this.tableNodeColumn = null;
    this._maxLevel = 0;
    this._aggregateRows = [];
    this._animationRowLimit = 25;
    this._blockLoadThreshold = 25;
    this.updateBuffer = new TableUpdateBuffer(this);
    this.viewRangeSize = 10;
    this.viewRangeDirty = false;
    this.viewRangeRendered = new Range(0, 0);
    this.virtual = true;
    this.textFilterEnabled = true;
    this.filterSupport = this._createFilterSupport();
    this.filteredElementsDirty = false;
    this.defaultMenuTypes = [Table.MenuType.EmptySpace];
    this.accessibilityRenderer = new DefaultTableAccessibilityRenderer();
    this.organizer = null;

    this._doubleClickSupport = new DoubleClickSupport();
    this._permanentHeadSortColumns = [];
    this._permanentTailSortColumns = [];
    this._filterMenusHandler = this._filterMenus.bind(this);
    this._popupOpenHandler = this._onDesktopPopupOpen.bind(this);
    this._rerenderViewPortAfterAttach = false;
    this._renderViewPortAfterAttach = false;
    this._postAttachActions = [];
    this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
    this._menuInheritAccessibilityChangeHandler = this._updateMenusEnabled.bind(this);
    this._addWidgetProperties(['tableControls', 'menus', 'keyStrokes', 'staticMenus', 'tileTableHeader', 'tableTileGridMediator']);

    this.$data = null;
    this.$emptyData = null;
    this.$fillBefore = null;
    this.$fillAfter = null;
  }

  // TODO [7.0] cgu create StringColumn.js incl. defaultValues from defaultValues.json

  static MenuType = {
    /**
     * The menu is always visible and displayed first in the {@link MenuBar}.
     * The menu won't be displayed in the context menu.
     */
    EmptySpace: 'Table.EmptySpace',
    /**
     * The menu is only visible if exactly one row has been selected.
     * The menu is also displayed in the context menu.
     */
    SingleSelection: 'Table.SingleSelection',
    /**
     * The menu is only visible if at least two row have been selected.
     * The menu is also displayed in the context menu.
     */
    MultiSelection: 'Table.MultiSelection',
    /**
     * The menu is displayed in the {@link TableHeader} on the right side.
     */
    Header: 'Table.Header'
  } as const;

  static HierarchicalStyle = {
    DEFAULT: 'default',
    STRUCTURED: 'structured'
  } as const;

  static GroupingStyle = {
    /**
     * Aggregate row is rendered on top of the row-group.
     */
    TOP: 'top',
    /**
     * Aggregate row is rendered on the bottom of the row-group (default).
     */
    BOTTOM: 'bottom'
  } as const;

  static CheckableStyle = {
    /**
     * When row is checked a boolean column with a checkbox is inserted into the table.
     */
    CHECKBOX: 'checkbox',
    /**
     * When a row is checked the table-row is marked as checked. By default, a background
     * color is set on the table-row when the row is checked.
     */
    TABLE_ROW: 'tableRow',
    /**
     * Like the CHECKBOX Style but a click anywhere on the row triggers the check.
     */
    CHECKBOX_TABLE_ROW: 'checkbox_table_row'
  } as const;

  /**
   * This enum defines the reload-reasons for a table reload operation
   */
  static ReloadReason = {
    /**
     * No specific reason, just reload data using the current search settings, the current row limits and the current
     * filter (Default)
     */
    UNSPECIFIED: 'unspecified',

    /**
     * Some search parameters changed or the search was reset and the search was triggered
     */
    SEARCH: 'search',

    /**
     * The user requested loading more data than his soft limit, up to the application specific hard limit
     */
    OVERRIDE_ROW_LIMIT: 'overrideRowLimit',

    /**
     * The user requested loading no more data than his soft limit;
     */
    RESET_ROW_LIMIT: 'resetRowLimit',

    /**
     * The column structure of the table was changed
     */
    ORGANIZE_COLUMNS: 'organizeColumns',

    /**
     * Any call to IPage#dataChanged
     */
    DATA_CHANGED_TRIGGER: 'dataChangedTrigger'
  } as const;

  static SELECTION_CLASSES = 'select-middle select-top select-bottom select-single selected';

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.resolveConsts([{
      property: 'hierarchicalStyle',
      constType: Table.HierarchicalStyle
    }, {
      property: 'checkableStyle',
      constType: Table.CheckableStyle
    }, {
      property: 'groupingStyle',
      constType: Table.GroupingStyle
    }]);

    this._initOrganizer(model.organizer === undefined); // auto-create unless explicitly defined in the model
    this._initColumns();

    this.rows.forEach((row, i) => {
      this.rows[i] = this._initRow(row);
    });

    this.setFilters(this.filters);

    this._updateRowStructure({
      updateTree: true
    });

    this.menuBar = this._createMenuBar();
    this.menuBar.on('propertyChange:visible', () => this._refreshMenuBarClasses());
    this._setSelectedRows(this.selectedRows);
    this._setKeyStrokes(this.keyStrokes);
    this._setMenus(this.menus);
    this._setTableControls(this.tableControls);
    this._setTableStatus(this.tableStatus);
    this._calculateValuesForBackgroundEffect();
    this._setTileMode(this.tileMode);
    this._setTileTableHeader(this.tileTableHeader);
    this._sortWhileInit(); // required in case the rows are already provided in the initial model
    this._updateMenusEnabled();
  }

  protected _initRow(row: ObjectOrModel<TableRow>): TableRow {
    let tableRow: TableRow;
    if (row instanceof TableRow) {
      tableRow = row;
    } else {
      tableRow = this._createRow(row);
    }
    this.rowsMap[tableRow.id] = tableRow;
    this._initBookmarkIdentifier(tableRow);
    this.trigger('rowInit', {
      row: tableRow
    });
    return tableRow;
  }

  protected _createRow(rowModel: TableRowModel): TableRow {
    let model = (rowModel || {}) as FullModelOf<TableRow>;
    model.objectType = scout.nvl(model.objectType, TableRow);
    model.parent = this;
    return scout.create(model);
  }

  protected _initBookmarkIdentifier(tableRow: TableRow) {
    if (tableRow.bookmarkIdentifier) {
      return; // already set
    }
    // FIXME bsh [js-bookmark] Somehow create a JS version of BookmarkTableRowIdentifierDoFactory
    let keyComponents = tableRow.getKeyValues().map(key => {
      // FIXME bsh [js-bookmark] How to check for entity key types?
      if (typeof key === 'string') {
        return scout.create(BookmarkTableRowIdentifierStringComponentDo, {key});
      }
      if (typeof key === 'number') {
        return scout.create(BookmarkTableRowIdentifierLongComponentDo, {key});
      }
      if (typeof key === 'boolean') {
        return scout.create(BookmarkTableRowIdentifierBooleanComponentDo, {key});
      }
      return null; // FIXME bsh [js-bookmark] Handle this case
    });
    tableRow.bookmarkIdentifier = scout.create(BookmarkTableRowIdentifierDo, {keyComponents});
  }

  protected _initColumns() {
    let cols = this.columns as ObjectOrChildModel<Column<any>>[];
    this.columns = cols.map((colModel, index) => {
      let column: Column<any>;
      let columnOrModel = colModel as FullModelOf<Column<any>>;
      columnOrModel.session = this.session;
      if (columnOrModel instanceof Column) {
        column = columnOrModel;
        column._setTable(this);
      } else {
        columnOrModel.table = this;
        column = scout.create(columnOrModel);
      }

      if (column.index < 0) {
        column.index = index;
      }
      if (column.checkable) {
        // set checkable column if this column is the checkable one
        this.checkableColumn = column as BooleanColumn;
      }
      return column;
    });

    // Add gui only checkbox column at the beginning
    this._setCheckable(this.checkable);

    // Add gui only row icon column at the beginning
    if (this.rowIconVisible) {
      this._insertRowIconColumn();
    }

    this._setCompact(this.compact);
    this._calculateTableNodeColumn();

    // Sync head and tail sort columns
    this._setHeadAndTailSortColumns();
    this.columnLayoutDirty = true;
  }

  protected override _destroy() {
    this._destroyOrganizer();
    this._destroyColumns();
    super._destroy();
  }

  protected _destroyColumns() {
    this.columns.forEach(column => column.destroy());
    this.checkableColumn = null;
    this.compactColumn = null;
    this.rowIconColumn = null;
    this.columns = [];
  }

  protected _calculateTableNodeColumn() {
    let candidateColumns = this.visibleColumns().filter(column => column.nodeColumnCandidate);

    let tableNodeColumn = arrays.first(candidateColumns);
    if (this.tableNodeColumn && this.tableNodeColumn !== tableNodeColumn) {
      // restore
      this.tableNodeColumn.minWidth = this.tableNodeColumn['__initialMinWidth'];
    }
    this.tableNodeColumn = tableNodeColumn;
    if (this.tableNodeColumn) {
      this.tableNodeColumn['__initialMinWidth'] = this.tableNodeColumn.minWidth;
      this.tableNodeColumn.minWidth = this.rowLevelPadding * this._maxLevel + this.tableNodeColumn.tableNodeLevel0CellPadding + 8;

      if (this.tableNodeColumn.minWidth > this.tableNodeColumn.width) {
        if (this._isDataRendered()) {
          this.resizeColumn(this.tableNodeColumn, this.tableNodeColumn.minWidth);
        } else {
          this.tableNodeColumn.width = this.tableNodeColumn.minWidth;
        }
      }
    }
  }

  protected override _createLoadingSupport(): LoadingSupport {
    return new LoadingSupport({
      widget: this,
      $container: () => {
        if (this.$container.hasClass('knight-rider-loading')) {
          return this.$container;
        }
        return this.$data;
      }
    });
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();
    this._initTableKeyStrokeContext();
  }

  protected _initTableKeyStrokeContext() {
    this.keyStrokeContext.registerKeyStrokes([
      new TableNavigationUpKeyStroke(this),
      new TableNavigationDownKeyStroke(this),
      new TableNavigationPageUpKeyStroke(this),
      new TableNavigationPageDownKeyStroke(this),
      new TableNavigationHomeKeyStroke(this),
      new TableNavigationEndKeyStroke(this),
      new TableNavigationCollapseKeyStroke(this),
      new TableNavigationExpandKeyStroke(this),
      new TableStartCellEditKeyStroke(this),
      new TableSelectAllKeyStroke(this),
      new TableRefreshKeyStroke(this),
      new TableToggleRowKeyStroke(this),
      new TableCopyKeyStroke(this),
      new ContextMenuKeyStroke(this, this.showContextMenu, this),
      new AppLinkKeyStroke(this, this.handleAppLinkAction)
    ]);
  }

  protected _insertBooleanColumn() {
    // don't add checkbox column when we're in checkableStyle mode
    if (this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      return;
    }
    let column = scout.create(BooleanColumn, {
      session: this.session,
      fixedWidth: true,
      fixedPosition: true,
      guiOnly: true,
      nodeColumnCandidate: false,
      headerMenuEnabled: false,
      showSeparator: false,
      width: Column.NARROW_MIN_WIDTH,
      table: this
    });

    arrays.insert(this.columns, column, 0);
    this.checkableColumn = column;
  }

  protected _insertRowIconColumn() {
    let position = 0,
      column = scout.create(IconColumn, {
        session: this.session,
        fixedWidth: true,
        fixedPosition: true,
        guiOnly: true,
        nodeColumnCandidate: false,
        headerMenuEnabled: false,
        showSeparator: false,
        width: this.rowIconColumnWidth,
        table: this
      });
    if (this.columns[0] === this.checkableColumn) {
      position = 1;
    }
    arrays.insert(this.columns, column, position);
    this.rowIconColumn = column;
  }

  handleAppLinkAction(event: JQuery.KeyboardEventBase) {
    let $appLink = $(event.target);
    let column = this._columnAtX($appLink.offset().left);
    let row = $appLink.findUp($elem => $elem.hasClass('table-row'), this.$container).data('row') as TableRow;
    this._triggerAppLinkAction(column, row, $appLink.data('ref'), $appLink);
  }

  /** @internal */
  _isDataRendered(): boolean {
    return this.rendered && this.$data !== null;
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('table')
      .addDeviceClass();
    this.accessibilityRenderer.renderTable(this.$container);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TableLayout(this));

    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }

    if (this.tileMode) {
      this._renderTileMode();
    } else {
      this._renderData();
    }

    this.session.desktop.on('popupOpen', this._popupOpenHandler);
    this.session.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  }

  /** @internal */
  _renderData() {
    this.$data = this.$container.appendDiv('table-data');
    this.accessibilityRenderer.renderRowGroup(this.$data);
    this.$data.on('mousedown', '.table-row', this._onRowMouseDown.bind(this))
      .on('mouseup', '.table-row', this._onRowMouseUp.bind(this))
      .on('dblclick', '.table-row', this._onRowDoubleClick.bind(this))
      .on('contextmenu', event => event.preventDefault());
    this._installScrollbars({
      axis: 'both'
    });
    this._installImageListeners();
    this._installCellTooltipSupport();
    this._calculateRowInsets();
    this._updateRowWidth();
    this._updateRowHeight();
    this._renderViewport();
    if (this.scrollToSelection) {
      this.revealSelection();
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderTableHeader();
    this._renderMenuBarVisible();
    this._renderFooterVisible();
    this._renderCheckableStyle();
    this._renderHierarchicalStyle();
    this._renderTextFilterEnabled();
    this._renderMultiSelect();
    this._renderMultiCheck();
  }

  protected override _setCssClass(cssClass: string) {
    super._setCssClass(cssClass);
    // calculate row level padding
    let paddingClasses = ['table-row-level-padding'];
    if (this.cssClass) {
      paddingClasses.push(this.cssClass);
    }
    let classes = paddingClasses.reduce((acc, cssClass) => acc + ' ' + cssClass, '');
    this.setRowLevelPadding(styles.getSize(classes, 'width', 'width', 15));
  }

  /** @internal */
  _removeData() {
    this._removeAggregateRows();
    this._uninstallImageListeners();
    this._uninstallCellTooltipSupport();
    this._uninstallScrollbars();
    this._removeRows();
    this.$fillBefore = null;
    this.$fillAfter = null;
    this.$data.remove();
    this.$data = null;
    this.$emptyData = null;
  }

  protected override _remove() {
    this.session.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
    this.session.desktop.off('popupOpen', this._popupOpenHandler);
    dragAndDrop.uninstallDragAndDropHandler(this);
    // TODO [7.0] cgu do not delete header, implement according to footer
    this.header = null;
    if (this.$data) {
      this._removeData();
    }
    this.filterSupport.remove();
    super._remove();
  }

  setRowLevelPadding(rowLevelPadding: number) {
    this.setProperty('rowLevelPadding', rowLevelPadding);
  }

  protected _renderRowLevelPadding() {
    this._rerenderViewport();
  }

  setTableControls(controls: ObjectOrChildModel<TableControl>[]) {
    this.setProperty('tableControls', controls);
  }

  protected _renderTableControls() {
    if (this.footer) {
      this.footer._renderControls();
    }
  }

  protected _setTableControls(controls: TableControl[]) {
    let i;
    for (i = 0; i < this.tableControls.length; i++) {
      this.keyStrokeContext.unregisterKeyStroke(this.tableControls[i]);
    }
    this._setProperty('tableControls', controls);
    for (i = 0; i < this.tableControls.length; i++) {
      this.keyStrokeContext.registerKeyStroke(this.tableControls[i]);
    }
    this._updateFooterVisibility();
    this.tableControls.forEach(control => {
      control.tableFooter = this.footer;
    });
  }

  /**
   * When an IMG has been loaded we must update the stored height in the model-row.
   * Note: we don't change the width of the row or table.
   */
  protected _onImageLoadOrError(event: ErrorEvent) {
    let $target = $(event.target) as JQuery;
    if ($target.data('measure') === 'in-progress') {
      // Ignore events created by autoOptimizeWidth measurement (see ColumnOptimalWidthMeasurer)
      // Using event.stopPropagation() is not possible because the image load event does not bubble
      return;
    }

    $target.toggleClass('broken', event.type === 'error');

    let $row = $target.closest('.table-row');
    let row = $row.data('row') as TableRow;
    if (!row) {
      return; // row was removed while loading the image
    }
    let oldRowHeight = row.height;
    row.height = this._measureRowHeight($row);
    if (oldRowHeight !== row.height) {
      this.invalidateLayoutTree();
    }
  }

  protected _onRowMouseDown(event: JQuery.MouseDownEvent) {
    this._doubleClickSupport.mousedown(event);
    this._$mouseDownRow = $(event.currentTarget);
    this._mouseDownRowId = this._$mouseDownRow.data('row').id;
    this._mouseDownColumn = this._columnAtX(event.pageX);
    this._$mouseDownRow.window().one('mouseup', () => {
      this._$mouseDownRow = null;
      this._mouseDownRowId = null;
      this._mouseDownColumn = null;
    });
    this.setContextColumn(this._columnAtX(event.pageX));
    // The row referenced by this._$mouseDownRow might become invalid in the onMouseDown event (e.g. if the event removes all rows).
    // Hence, we put the row aside before the event.
    let row = this._$mouseDownRow.data('row') as TableRow;
    this.selectionHandler.onMouseDown(event);
    this._$mouseDownRow = row.$row;
    let isRightClick = event.which === 3;

    let $target = $(event.target);
    // handle expansion
    if (this._isRowControl($target)) {
      if (row.expanded) {
        this.collapseRow(row);
      } else {
        this.expandRow(row);
      }
    }

    // For checkableStyle TABLE_ROW & CHECKBOX_TABLE_ROW only: check row if left click OR clicked row was not checked yet
    if (scout.isOneOf(this.checkableStyle, Table.CheckableStyle.TABLE_ROW, Table.CheckableStyle.CHECKBOX_TABLE_ROW) &&
      (!isRightClick || !row.checked) &&
      !$(event.target).is('.table-row-control') &&
      // Click on BooleanColumns should not trigger a row check. The only exception is if the BooleanColumn is the checkableColumn of this table (handled in BooleanColumn.js)
      !($target.hasClass('checkable') || $target.parent().hasClass('checkable'))) {
      this.checkRow(row, !row.checked);
    }
    if (isRightClick) {
      event.preventDefault();
      this.showContextMenu({
        pageX: event.pageX,
        pageY: event.pageY
      });
    }

    // set active descendant to the clicked row, so it is announced by screen readers.
    // This should be done last so selection state/focus/etc. is all set correctly before
    // the change of active descendant triggers the screen readers announcement.
    aria.linkElementWithActiveDescendant(this.$container, row.$row);
  }

  protected _isRowControl($target: JQuery): boolean {
    return $target.hasClass('table-row-control') || $target.parent().hasClass('table-row-control');
  }

  protected _onRowMouseUp(event: JQuery.MouseUpEvent) {
    let $appLink: JQuery, mouseButton = event.which;

    if (this._doubleClickSupport.doubleClicked()) {
      // Don't execute on double click events
      return;
    }

    let $mouseUpRow = $(event.currentTarget);
    this.selectionHandler.onMouseUp(event);

    if (!this._$mouseDownRow || this._mouseDownRowId !== $mouseUpRow.data('row').id) {
      // Don't accept if mouse up happens on another row than mouse down, or mousedown didn't happen on a row at all
      return;
    }

    let $row = $mouseUpRow;
    let column = this._columnAtX(event.pageX);
    if (column !== this._mouseDownColumn) {
      // Don't execute click / appLinks when the mouse gets pressed and moved outside a cell
      return;
    }
    let $target = $(event.target);
    if (this._isRowControl($target)) {
      // Don't start cell editor or trigger click if row control was clicked (expansion itself is handled by the mouse down handler)
      return;
    }
    let row = $row.data('row') as TableRow; // read row before the $row potentially could be replaced by the column specific logic on mouse up
    if (mouseButton === 1) {
      column.onMouseUp(event, $row);
      $appLink = this._find$AppLink(event);
    }
    if ($appLink) {
      this._triggerAppLinkAction(column, row, $appLink.data('ref'), $appLink);
    } else {
      this._triggerRowClick(event, row, mouseButton, column);
    }
  }

  protected _onRowDoubleClick(event: JQuery.DoubleClickEvent) {
    let $row = $(event.currentTarget),
      column = this._columnAtX(event.pageX);

    this.doRowAction($row.data('row'), column);
  }

  showContextMenu(options: { pageX?: number; pageY?: number }) {
    this.session.onRequestsDone(this._showContextMenu.bind(this, options));
  }

  protected _showContextMenu(options: { pageX?: number; pageY?: number }) {
    options = options || {};
    if (!this._isDataRendered() || !this.attached) { // check needed because function is called asynchronously
      return;
    }
    if (this.selectedRows.length === 0) {
      return;
    }
    let menuItems = this._filterMenusForContextMenu();
    if (menuItems.length === 0) {
      return;
    }
    let pageX: number = scout.nvl(options.pageX, null);
    let pageY: number = scout.nvl(options.pageY, null);
    if (pageX === null || pageY === null) {
      let rowToDisplay = this.isRowSelectedAndVisible(this.selectionHandler.lastActionRow) ? this.selectionHandler.lastActionRow : this.getLastSelectedAndVisibleRow();
      if (rowToDisplay !== null) {
        let $rowToDisplay = rowToDisplay.$row;
        let offset = $rowToDisplay.offset();
        let dataOffsetBounds = graphics.offsetBounds(this.$data);
        offset.left += this.$data.scrollLeft();
        pageX = offset.left + 10;
        pageY = offset.top + $rowToDisplay.outerHeight() / 2;
        pageY = Math.min(Math.max(pageY, dataOffsetBounds.y + 1), dataOffsetBounds.bottom() - 1);
      } else {
        pageX = this.$data.offset().left + 10;
        pageY = this.$data.offset().top + 10;
      }
    }
    // Prevent firing of 'onClose'-handler during contextMenu.open()
    // (Can lead to null-access when adding a new handler to this.contextMenu)
    if (this.contextMenu) {
      this.contextMenu.close();
    }
    this.contextMenu = scout.create(ContextMenuPopup, {
      parent: this,
      menuItems: menuItems,
      location: {
        x: pageX,
        y: pageY
      },
      $anchor: this.$data,
      menuFilter: this._filterMenusHandler
    });
    this.contextMenu.open();
  }

  isRowSelectedAndVisible(row: TableRow): boolean {
    if (!this.isRowSelected(row) || !row.$row) {
      return false;
    }
    return graphics.offsetBounds(row.$row).intersects(graphics.offsetBounds(this.$data));
  }

  getLastSelectedAndVisibleRow(): TableRow {
    for (let i = this.viewRangeRendered.to; i >= this.viewRangeRendered.from; i--) {
      if (this.isRowSelectedAndVisible(this.rows[i])) {
        return this.rows[i];
      }
    }
    return null;
  }

  onColumnVisibilityChanged() {
    this.columnLayoutDirty = true;
    this._calculateTableNodeColumn();
    this.trigger('columnStructureChanged');

    // Rebuild aggregate rows. This computes missing aggregate values for previously hidden columns. It is also a convenient
    // way to fix the column indices. The aggregate table control was already updated via 'columnStructureChanged' event.
    this._group(false);

    if (this._isDataRendered()) {
      this._updateRowWidth();
      this.redraw();
    }
  }

  protected override _onScroll(event: JQuery.ScrollEvent) {
    let scrollTop = this.$data[0].scrollTop;
    let scrollLeft = this.$data[0].scrollLeft;
    if (this.scrollTop !== scrollTop) {
      this._renderViewport();
    }
    this.scrollTop = scrollTop;
    this.scrollLeft = scrollLeft;
  }

  protected _renderTableStatus() {
    this.trigger('statusChanged');
  }

  setContextColumn(contextColumn: Column<any>) {
    this.setProperty('contextColumn', contextColumn);
  }

  protected _hasVisibleTableControls(): boolean {
    return this.tableControls.some(control => control.visible);
  }

  hasAggregateTableControl(): boolean {
    return this.tableControls.some(control => control instanceof AggregateTableControl);
  }

  protected _createHeader(): TableHeader {
    return scout.create(TableHeader, {
      parent: this,
      table: this,
      enabled: this.headerEnabled,
      headerMenusEnabled: this.headerMenusEnabled
    });
  }

  protected _createFooter(): TableFooter {
    return scout.create(TableFooter, {
      parent: this,
      table: this
    });
  }

  protected _initOrganizer(autoCreate = true) {
    let organizer = this.organizer || (autoCreate ? this._createOrganizer() : null);
    this._setOrganizer(organizer);
  }

  protected _createOrganizer(): TableOrganizer {
    return scout.create(TableOrganizer);
  }

  protected _destroyOrganizer() {
    this.setOrganizer(null);
  }

  setOrganizer(organizer: TableOrganizer) {
    this.setProperty('organizer', organizer);
  }

  protected _setOrganizer(organizer: TableOrganizer) {
    if (this.organizer) {
      this.organizer.uninstall();
    }
    this._setProperty('organizer', organizer);
    if (this.organizer) {
      this.organizer.install(this);
    }
  }

  protected _installCellTooltipSupport() {
    tooltips.install(this.$data, {
      parent: this,
      selector: '.table-cell',
      text: this._cellTooltipText.bind(this),
      htmlEnabled: this._isCellTooltipHtmlEnabled.bind(this),
      arrowPosition: 50,
      arrowPositionUnit: '%',
      clipOrigin: true,
      nativeTooltip: !Device.get().isCustomEllipsisTooltipPossible()
    });
  }

  protected _uninstallCellTooltipSupport() {
    tooltips.uninstall(this.$data);
  }

  /** @internal */
  _cellTooltipText($cell: JQuery): string {
    let tooltipText: string,
      $row = $cell.parent(),
      column = this.columnFor$Cell($cell, $row),
      row = $row.data('row') as TableRow;

    if (row) {
      let cell = this.cell(column, row);
      tooltipText = cell.tooltipText;
    }

    if (tooltipText) {
      return tooltipText;
    }
    if (this._isAggregatedTooltip($cell) && $cell.text().trim()) {
      let $textSpan = $cell.children('.text');
      let $iconSpan = $cell.children('.table-cell-icon');
      let iconAvailableButHidden = $iconSpan.length && !$iconSpan.isVisible();
      if ($textSpan.length && $textSpan.isContentTruncated() || iconAvailableButHidden) {
        let $clone = $cell.clone();
        $clone.children('.table-cell-icon').setVisible(true);
        if ($cell.css('direction') === 'rtl') {
          let childrenHtml = $clone.children().get().map(c => c.outerHTML).reverse();
          return strings.join('', ...childrenHtml);
        }
        return $clone.html();
      }
    }
    if (this._isTruncatedCellTooltipEnabled(column) && $cell.isContentTruncated()) {
      return strings.plainText($cell.html(), {
        trim: true,
        removeFontIcons: true
      });
    }
  }

  /** @see TableModel.truncatedCellTooltipEnabled */
  setTruncatedCellTooltipEnabled(truncatedCellTooltipEnabled: boolean) {
    this.setProperty('truncatedCellTooltipEnabled', truncatedCellTooltipEnabled);
  }

  /**
   * Decides if a cell tooltip should be shown for a truncated cell.
   */
  protected _isTruncatedCellTooltipEnabled(column: Column<any>): boolean {
    if (this.truncatedCellTooltipEnabled === null) {
      // Show cell tooltip only if it is not possible to resize the column.
      return !this.headerVisible || !this.headerEnabled || column.fixedWidth;
    }
    return this.truncatedCellTooltipEnabled;
  }

  protected _isAggregatedTooltip($cell: JQuery): boolean {
    let $row = $cell.parent();
    return $row.data('aggregateRow') /* row in the table */
      || $row.hasClass('table-aggregate'); /* aggregate table control */
  }

  protected _isCellTooltipHtmlEnabled($cell: JQuery): boolean {
    return this._isAggregatedTooltip($cell);
  }

  reload(reloadReason?: TableReloadReason) {
    if (!this.hasReloadHandler) {
      return;
    }
    this._removeRows();
    if (this._isDataRendered()) {
      this._removeAggregateRows();
      this._renderFiller();
    }
    this._triggerReload(reloadReason);
  }

  override setLoading(loading: boolean) {
    if (!loading && this.updateBuffer.isBuffering()) {
      // Don't abort loading while buffering, the buffer will do it at the end
      return;
    }
    super.setLoading(loading);
  }

  exportToClipboard() {
    this._triggerClipboardExport();
  }

  /**
   * JS implementation of AbstractTable.execCopy(rows)
   */
  protected _exportToClipboard() {
    clipboard.copyText({
      parent: this,
      text: this._selectedRowsToText()
    });
  }

  protected _selectedRowsToText(): string {
    let columns = this.visibleColumns();
    return this.selectedRows.map(row => {
      return columns.map(column => {
        let cell = column.cell(row);
        let text;
        if (column instanceof BooleanColumn) {
          text = cell.value ? 'X' : '';
        } else if (cell.htmlEnabled) {
          text = strings.plainText(cell.text);
        } else {
          text = cell.text;
        }
        // unwrap
        return this._unwrapText(text);
      }).join('\t');
    }).join('\n');
  }

  protected _unwrapText(text?: string): string {
    // Same implementation as in AbstractTable#unwrapText(String)
    return strings.nvl(text)
      .split(/[\n\r]/)
      .map(line => line.replace(/\t/g, ' '))
      .map(line => line.trim())
      .filter(line => !!line.length)
      .join(' ');
  }

  /** @see TableModel.multiSelect */
  setMultiSelect(multiSelect: boolean) {
    this.setProperty('multiSelect', multiSelect);
  }

  toggleSelection() {
    if (this.selectedRows.length === this.visibleRows.length) {
      this.deselectAll();
    } else {
      this.selectAll();
    }
  }

  selectAll() {
    this.selectRows(this.visibleRows);
  }

  deselectAll() {
    this.selectRows([]);
  }

  checkAll(checked?: boolean, options?: TableRowCheckOptions) {
    let opts: TableRowCheckOptions = $.extend(options, {
      checked: checked
    });
    this.checkRows(this.visibleRows, opts);
  }

  uncheckAll(options?: TableRowCheckOptions) {
    this.checkAll(false, options);
  }

  updateScrollbars() {
    scrollbars.update(this.$data);
  }

  protected _sort(animateAggregateRows?: boolean): boolean {
    let sortColumns = this._sortColumns();

    // Initialize comparators
    if (!this._isSortingPossible(sortColumns)) {
      return false;
    }
    this.clearAggregateRows(animateAggregateRows);
    if (!sortColumns.length) {
      // no sort column defined.
      return true;
    }

    // add all visible columns as fallback sorting to guarantee same sorting as in Java.
    sortColumns = arrays.union(sortColumns, this.columns);

    this._sortImpl(sortColumns);
    this._triggerRowOrderChanged();
    if (this._isDataRendered()) {
      this._renderRowOrderChanges();
    }

    // Do it after row order has been rendered, because renderRowOrderChanges re-renders the whole viewport which would destroy the animation
    this._group(animateAggregateRows);

    // Sort was possible -> return true
    return true;
  }

  /**
   * @returns whether or not sorting is possible. Asks each column to answer this question by calling Column#isSortingPossible.
   */
  protected _isSortingPossible(sortColumns: Column<any>[]): boolean {
    return sortColumns.every(column => column.isSortingPossible());
  }

  protected _sortColumns(): Column<any>[] {
    let sortColumns = [];
    for (let c = 0; c < this.columns.length; c++) {
      let column = this.columns[c];
      let sortIndex = column.sortIndex;
      if (sortIndex >= 0) {
        sortColumns[sortIndex] = column;
      }
    }
    return sortColumns;
  }

  protected _sortImpl(sortColumns: Column<any>[]) {
    let sortFunction: Comparator<TableRow> = (row1, row2) => {
      for (let s = 0; s < sortColumns.length; s++) {
        let column = sortColumns[s];
        let result = column.compare(row1, row2);
        if (column.sortActive && !column.sortAscending) {
          // only consider sortAscending flag when sort is active
          // columns with !sortActive are always sorted ascending (sortAscending represents last state for those, thus not considered)
          result = -result;
        }
        if (result !== 0) {
          return result;
        }
      }
      return 0;
    };

    if (this.hierarchical) {
      // sort tree and set flat row array afterward.
      this._sortHierarchical(sortFunction);
      let sortedFlatRows: TableRow[] = [];
      this.visitRows(row => sortedFlatRows.push(row));
      this.rows = sortedFlatRows;
    } else {
      // sort the flat rows and set the rootRows afterward.
      this.rows.sort(sortFunction);
      this.rootRows = this.rows;
    }

    this._updateRowStructure({
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
  }

  /**
   * Pre-order (top-down) traversal of all rows in this table (if hierarchical).
   */
  visitRows(visitFunc: (row: TableRow, level: number) => void, rows?: TableRow[], level?: number) {
    level = scout.nvl(level, 0);
    rows = rows || this.rootRows;
    rows.forEach(row => {
      visitFunc(row, level);
      this.visitRows(visitFunc, row.childRows, level + 1);
    });
  }

  protected _sortHierarchical(sortFunc: Comparator<TableRow>, rows?: TableRow[]) {
    rows = rows || this.rootRows;
    rows.sort(sortFunc);
    rows.forEach(row => this._sortHierarchical(sortFunc, row.childRows));
  }

  protected _renderRowOrderChanges() {
    let animate: boolean,
      $rows = this.$rows(),
      oldRowPositions: Record<string, number> = {};

    // store old position
    // animate only if every row is rendered, otherwise some rows would be animated and some not
    if ($rows.length === this.visibleRows.length) {
      $rows.each((index, elem) => {
        let rowWasInserted = false,
          $row = $(elem),
          row = $row.data('row') as TableRow;

        // Prevent the order animation for newly inserted rows (to not confuse the user)
        if (this._insertedRows) {
          for (let i = 0; i < this._insertedRows.length; i++) {
            if (this._insertedRows[i].id === row.id) {
              rowWasInserted = true;
              break;
            }
          }
        }

        if (!rowWasInserted) {
          animate = true;
          oldRowPositions[row.id] = $row.offset().top;
        }
      });
    }

    this._rerenderViewport();
    // If aggregate rows are being removed by animation, rerenderViewport does not delete them -> reorder
    // This may happen if grouping gets deactivated and another column will get the new first sort column
    this._order$AggregateRows();

    // Ensure selected row is visible after ordering
    if (this.scrollToSelection) {
      this.revealSelection();
    }

    // for less than animationRowLimit rows: move to old position and then animate
    if (animate) {
      $rows = this.$rows();
      $rows.each((index, elem) => {
        let $row = $(elem),
          row = $row.data('row') as TableRow,
          oldTop = oldRowPositions[row.id];

        if (oldTop !== undefined) {
          $row.css('top', oldTop - $row.offset().top).animate({
            top: 0
          }, {
            progress: function() {
              this._triggerRowOrderChanged(row, true);
              this.updateScrollbars();
            }.bind(this)
          });
        }
      });
    }
  }

  /** @see TableModel.sortEnabled */
  setSortEnabled(sortEnabled: boolean) {
    this.setProperty('sortEnabled', sortEnabled);
  }

  /**
   * @param column the column to sort by.
   * @param direction the sorting direction. Either 'asc' or 'desc'. If not specified the direction specified by the column is used {@link Column.sortAscending}.
   * @param multiSort true to add the column to the list of sorted columns. False to use this column exclusively as sort column (reset other columns). Default is false.
   * @param remove true to remove the column from the sort columns. Default is false.
   */
  sort(column: Column<any>, direction?: 'asc' | 'desc', multiSort?: boolean, remove?: boolean) {
    multiSort = scout.nvl(multiSort, false);
    remove = scout.nvl(remove, false);
    // Animate if sort removes aggregate rows
    let animateAggregateRows = !multiSort;
    if (remove) {
      this._removeSortColumn(column);
    } else {
      this._addSortColumn(column, direction, multiSort);
    }
    if (this.header) {
      this.header.onSortingChanged();
    }
    let sorted = this._sort(animateAggregateRows);
    let data: any = {
      column: column,
      sortAscending: column.sortAscending
    };
    if (remove) {
      data.sortingRemoved = true;
    }
    if (multiSort) {
      data.multiSort = true;
    }
    if (!sorted) {
      // Delegate sorting to server when it is not possible on client side
      data.sortingRequested = true;
      // hint to animate the aggregate after the row order changed event
      this._animateAggregateRows = animateAggregateRows;
    }
    this.trigger('sort', data);
  }

  protected _addSortColumn(column: Column<any>, direction?: 'asc' | 'desc', multiSort?: boolean) {
    direction = scout.nvl(direction, column.sortAscending ? 'asc' : 'desc');
    multiSort = scout.nvl(multiSort, true);

    this._updateSortIndexForColumn(column, multiSort);

    // Reset grouped flag if column should be sorted exclusively
    if (!multiSort) {
      let groupColCount = this._groupedColumns().length;
      let sortColCount = this._sortColumns().length;
      if (sortColCount === 1 && groupColCount === 1) {
        // special case: if it is the only sort column and also grouped, do not remove grouped property.
      } else {
        column.grouped = false;
      }
    }

    column.sortAscending = direction === 'asc';
    column.sortActive = true;
  }

  /**
   * Intended to be called for new sort columns.
   * Sets the sortIndex of the given column and its siblings.
   */
  protected _updateSortIndexForColumn(column: Column<any>, multiSort: boolean) {
    let sortIndex = -1;
    if (multiSort) {
      // if not already sorted set the appropriate sort index (check for sortIndex necessary if called by _onColumnHeadersUpdated)
      if (!column.sortActive || column.sortIndex === -1) {
        sortIndex = Math.max(-1, arrays.max(this.columns.map(c => c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd ? -1 : c.sortIndex)));
        column.sortIndex = sortIndex + 1;

        // increase sortIndex for all permanent tail columns (a column has been added in front of them)
        this._permanentTailSortColumns.forEach(c => {
          c.sortIndex++;
        });
      }
    } else {
      // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
      if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
        column.sortIndex = this._permanentHeadSortColumns.length;
      }

      // remove sort index for siblings (ignore permanent head/tail columns, only if not multi sort)
      arrays.eachSibling(this.columns, column, siblingColumn => {
        if (siblingColumn.sortActive) {
          this._removeSortColumnInternal(siblingColumn);
        }
      });

      // set correct sort index for all permanent tail sort columns
      let deviation = column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd ? 0 : 1;
      this._permanentTailSortColumns.forEach((c, index) => {
        c.sortIndex = this._permanentHeadSortColumns.length + deviation + index;
      });
    }
  }

  protected _removeSortColumn(column: Column<any>) {
    if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
      return;
    }
    // Adjust sibling columns with higher index
    arrays.eachSibling(this.columns, column, siblingColumn => {
      if (siblingColumn.sortIndex > column.sortIndex) {
        siblingColumn.sortIndex = siblingColumn.sortIndex - 1;
      }
    });
    this._removeSortColumnInternal(column);
  }

  protected _removeSortColumnInternal(column: Column<any>) {
    if (column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd) {
      return;
    }
    column.sortActive = false;
    column.grouped = false;
    column.sortIndex = -1;
  }

  isGroupingPossible(column: Column<any>): boolean {
    let possible = true;

    if (this.hierarchical) {
      return false;
    }

    if (!this.sortEnabled) {
      // grouping without sorting is not possible
      return false;
    }

    if (this._permanentHeadSortColumns && this._permanentHeadSortColumns.length === 0) {
      // no permanent head sort columns. grouping ok.
      return true;
    }

    if (column.initialAlwaysIncludeSortAtBegin) {
      possible = true;
      arrays.eachSibling(this._permanentHeadSortColumns, column, c => {
        if (c.sortIndex < column.sortIndex) {
          possible = possible && c.grouped;
        }
      });
      return possible;
    }

    if (column.initialAlwaysIncludeSortAtEnd) {
      // it is a tail sort column. Grouping does not make sense.
      return false;
    }

    // column itself is not a head or tail sort column. Therefore, all head sort columns must be grouped.
    this._permanentHeadSortColumns.forEach(c => {
      possible = possible && c.grouped;
    });
    return possible;
  }

  isAggregationPossible(column: Column<any>): boolean {
    if (!(column instanceof NumberColumn)) {
      return false;
    }

    if (column.grouped) {
      // Aggregation is not possible if column is grouped
      return false;
    }

    if (!column.allowedAggregationFunctions || column.allowedAggregationFunctions.length <= 1) {
      // Aggregation is not possible if no aggregation functions are allowed or only exactly one aggregation is pre-defined.
      return false;
    }

    // Aggregation is possible if it is grouped by another column or aggregation control is available
    return this.isGrouped() || this.hasAggregateTableControl();
  }

  changeAggregation(column: NumberColumn, func: NumberColumnAggregationFunction) {
    this.changeAggregations([column], [func]);
  }

  changeAggregations(columns: NumberColumn[], functions: NumberColumnAggregationFunction[]) {
    columns.forEach((column, i) => {
      let func = functions[i];
      if (column.aggregationFunction !== func) {
        column.setAggregationFunction(func);
        this._triggerAggregationFunctionChanged(column);
      }
    });

    this._group();
  }

  protected _addGroupColumn(column: Column<any>, direction?: 'asc' | 'desc', multiGroup?: boolean) {
    let sortIndex = -1;

    if (!this.isGroupingPossible(column)) {
      return;
    }

    direction = scout.nvl(direction, column.sortAscending ? 'asc' : 'desc');
    multiGroup = scout.nvl(multiGroup, true);
    if (!(column.initialAlwaysIncludeSortAtBegin || column.initialAlwaysIncludeSortAtEnd)) {
      // do not update sort index for permanent head/tail sort columns, their order is fixed (see ColumnSet.java)
      if (multiGroup) {
        sortIndex = Math.max(-1, arrays.max(this.columns.map(c => c.sortIndex === undefined || c.initialAlwaysIncludeSortAtEnd || !c.grouped ? -1 : c.sortIndex)));

        if (!column.sortActive) {
          // column was not yet present: insert at determined position
          // and move all subsequent nodes by one.
          // add just after all other grouping columns in column set.
          column.sortIndex = sortIndex + 1;
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex > sortIndex) {
              siblingColumn.sortIndex++;
            }
          });

          // increase sortIndex for all permanent tail columns (a column has been added in front of them)
          this._permanentTailSortColumns.forEach(c => {
            c.sortIndex++;
          });
        } else {
          // column already sorted, update position:
          // move all sort columns between the newly determined sort-index and the old sort-index by one.
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
              siblingColumn.sortIndex > sortIndex &&
              siblingColumn.sortIndex < column.sortIndex) {
              siblingColumn.sortIndex++;
            }
          });
          column.sortIndex = sortIndex + 1;
        }
      } else {
        // no multi-group:
        sortIndex = this._permanentHeadSortColumns.length;

        if (column.sortActive) {
          // column already sorted, update position:
          // move all sort columns between the newly determined sort-index and the old sort-index by one.
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) &&
              siblingColumn.sortIndex >= sortIndex &&
              siblingColumn.sortIndex < column.sortIndex) {
              siblingColumn.sortIndex++;
            }
          });
          column.sortIndex = sortIndex;
        } else { // not sorted yet
          arrays.eachSibling(this.columns, column, siblingColumn => {
            if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
              siblingColumn.sortIndex++;
            }
          });

          column.sortIndex = sortIndex;

          // increase sortIndex for all permanent tail columns (a column has been added in front of them)
          this._permanentTailSortColumns.forEach(c => {
            c.sortIndex++;
          });
        }

        // remove all other grouped properties:
        arrays.eachSibling(this.columns, column, siblingColumn => {
          if (siblingColumn.sortActive && !(siblingColumn.initialAlwaysIncludeSortAtBegin || siblingColumn.initialAlwaysIncludeSortAtEnd) && siblingColumn.sortIndex >= sortIndex) {
            siblingColumn.grouped = false;
          }
        });
      }

      column.sortAscending = direction === 'asc';
      column.sortActive = true;
    } else if (column.initialAlwaysIncludeSortAtBegin) {
      // do not change order or direction. just set grouped to true.
      column.grouped = true;
    }

    column.grouped = true;
  }

  protected _removeGroupColumn(column: Column<any>) {
    column.grouped = false;

    if (column.initialAlwaysIncludeSortAtBegin) {
      // head sort case: remove all groupings after this column.
      this.columns.forEach(c => {
        if (c.sortIndex >= column.sortIndex) {
          c.grouped = false;
        }
      });
    }

    this._removeSortColumn(column);
  }

  protected _buildRowDiv(row: TableRow): string {
    let rowWidth = this.rowWidth;
    let rowClass = 'table-row';
    if (row.cssClass) {
      rowClass += ' ' + row.cssClass;
    }
    if (!row.enabled) {
      rowClass += ' disabled';
    }
    if (row.checked && this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      rowClass += ' checked';
    }
    // if a row is not filterAccepted it must be visible since any of its child rows are filter accepted.
    if (!row.filterAccepted) {
      rowClass += ' filter-not-accepted';
    }
    if (this.hierarchical && arrays.empty(row.childRows)) {
      rowClass += ' leaf';
    }

    let ariaAttributes = '';
    if (strings.hasText(this.accessibilityRenderer.rowRole)) {
      let selected = this.isRowSelected(row) === true;
      let checked = String(row.checked === true);
      let disabled = !row.enabled;
      ariaAttributes = ' role="' + this.accessibilityRenderer.rowRole + '"';
      if (selected) {
        ariaAttributes += ' aria-selected="true"';
      }
      if (disabled) {
        ariaAttributes += ' aria-disabled = "true"';
      }
      if (this.checkable) {
        ariaAttributes += ' aria-checked="' + checked + '"';
      }
      if (this.hierarchical && row.expandable) {
        let expanded = row.expanded;
        ariaAttributes += ' aria-expanded="' + expanded + '"';
      }
    }

    let rowDiv = '<div' + ariaAttributes + ' class="' + rowClass + '" style="width: ' + rowWidth + 'px">';
    for (let i = 0; i < this.columns.length; i++) {
      let column = this.columns[i];
      if (column.visible) {
        rowDiv += column.buildCellForRow(row);
      }
    }
    rowDiv += '</div>';

    return rowDiv;
  }

  protected _calculateRowInsets() {
    let $tableRowDummy = this.$data.appendDiv('table-row');
    this.rowMargins = graphics.margins($tableRowDummy);
    this.rowBorders = graphics.borders($tableRowDummy);
    $tableRowDummy.remove();
  }

  /** @internal */
  _updateRowWidth() {
    this.rowWidth = this.visibleColumns().reduce((sum, column) => {
      if (this.autoResizeColumns) {
        return sum + column.width;
      }
      // Ensure the row is as long as all cells. Only necessary to use the _realWidth if the device.hasTableCellZoomBug().
      // If autoResizeColumns is enabled, it is not possible to do a proper calculation with this bug
      // -> Use regular width and live with the consequence that the last cell of a table with many columns is not fully visible
      return sum + column.realWidthIfAvailable();
    }, this.rowBorders.horizontal());
  }

  /**
   * A html element with display: table-cell gets the wrong width in Chrome when zoom is enabled, see
   * https://bugs.chromium.org/p/chromium/issues/detail?id=740502.
   * Because the table header items don't use display: table-cell, theirs width is correct.
   * -> Header items and table cells are not in sync which is normally not a big deal but gets visible very well with a lot of columns.
   * This method reads the real width and stores it on the column so that the header can use it when setting the header item's size.
   * It is also necessary to update the row width accordingly otherwise it would be cut at the very right.
   * @internal
   */
  _updateRealColumnWidths($row?: JQuery): boolean {
    if (!Device.get().hasTableCellZoomBug()) {
      return false;
    }
    let changed = false;
    $row = $row || this.$rows().eq(0);
    this.visibleColumns().forEach((column, colIndex) => {
      if (this._updateRealColumnWidth(column, colIndex, $row)) {
        changed = true;
      }
    });
    return changed;
  }

  protected _updateRealColumnWidth(column: Column<any>, colIndex?: number, $row?: JQuery): boolean {
    if (!Device.get().hasTableCellZoomBug()) {
      return false;
    }
    $row = $row || this.$rows().eq(0);
    let $cell = this.$cell(scout.nvl(colIndex, column), $row);
    if ($cell.length === 0 && column._realWidth !== null) {
      column._realWidth = null;
      return true;
    }
    let realWidth = graphics.size($cell, {exact: true}).width;
    if (realWidth !== column._realWidth) {
      column._realWidth = realWidth;
      return true;
    }
    return false;
  }

  protected _updateRowHeight() {
    let $emptyRow = this.$data.appendDiv('table-row');
    let $emptyAggrRow = this._build$AggregateRow().appendTo(this.$data);

    $emptyRow.appendDiv('table-cell').html('&nbsp;');
    $emptyAggrRow.appendDiv('table-cell table-aggregate-cell').appendSpan('text').html('&nbsp;');
    this.rowHeight = this._measureRowHeight($emptyRow);
    this.aggregateRowHeight = this._measureRowHeight($emptyAggrRow);
    $emptyRow.remove();
    $emptyAggrRow.remove();
  }

  /**
   * Updates the row heights for every visible row and aggregate row and clears the height of the others
   */
  protected _updateRowHeights() {
    this.rows.forEach(row => {
      if (!row.$row) {
        row.height = null;
      } else {
        row.height = this._measureRowHeight(row.$row);
      }
    });
    this._aggregateRows.forEach(aggregateRow => {
      if (!aggregateRow.$row) {
        aggregateRow.height = null;
      } else {
        aggregateRow.height = this._measureRowHeight(aggregateRow.$row);
      }
    });
  }

  protected _renderRowsInRange(range: Range) {
    let rowString = '',
      numRowsRendered = 0,
      prepend = false;

    let rows = this.visibleRows;
    if (rows.length === 0) {
      return;
    }

    let maxRange = new Range(0, this.rows.length);
    range = maxRange.intersect(range);
    if (this.viewRangeRendered.size() > 0 && !range.intersect(this.viewRangeRendered).equals(new Range(0, 0))) {
      throw new Error('New range must not intersect with existing.');
    }
    if (range.to <= this.viewRangeRendered.from) {
      prepend = true;
    }
    let newRange = this.viewRangeRendered.union(range);
    if (newRange.length === 2) {
      throw new Error('Can only prepend or append rows to the existing range. Existing: ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];
    this._removeEmptyData();

    // Build $rows (as string instead of jQuery objects due to efficiency reasons)
    for (let r = range.from; r < range.to; r++) {
      let row = rows[r];
      rowString += this._buildRowDiv(row);
      numRowsRendered++;
    }

    // append block of rows
    let $rows = this.$data.makeElement(rowString);
    if (prepend) {
      if (this.$fillBefore) {
        $rows = $rows.insertAfter(this.$fillBefore);
      } else {
        $rows = $rows.prependTo(this.$data);
      }
    } else if (this.$fillAfter) {
      $rows = $rows.insertBefore(this.$fillAfter);
    } else {
      $rows = $rows.appendTo(this.$data);
    }

    $rows.each((index, rowObject) => {
      let $row = $(rowObject);
      // Workaround for Chrome bug, see _updateRealColumnWidths
      // Can be removed when Chrome bug is resolved.
      // This is only necessary once (when the first row is rendered)
      if (this.viewRangeRendered.size() === numRowsRendered && this._updateRealColumnWidths($row)) {
        this._updateRowWidth();
        if (this.header && this.header.rendered) {
          this.header.resizeHeaderItems();
        }
      }
      $row.cssWidth(this.rowWidth);
      // End workaround
      let row = rows[range.from + index];
      Table.linkRowToDiv(row, $row);
      this._installRow(row);
    });

    if ($.log.isTraceEnabled()) {
      $.log.trace(numRowsRendered + ' new rows rendered from ' + range);
      $.log.trace(this._rowsRenderedInfo());
    }
  }

  protected _rowsRenderedInfo(): string {
    let numRenderedRows = this.$rows().length,
      renderedRowsRange = '(' + this.viewRangeRendered + ')';
    return numRenderedRows + ' rows rendered ' + renderedRowsRange;
  }

  /**
   * Moves the row to the top.
   */
  moveRowToTop(row: TableRow) {
    let rowIndex = this.rows.indexOf(row);
    this.moveRow(rowIndex, 0);
  }

  /**
   * Moves the row to the bottom.
   */
  moveRowToBottom(row: TableRow) {
    let rowIndex = this.rows.indexOf(row);
    this.moveRow(rowIndex, this.rows.length - 1);
  }

  /**
   * Moves the row one up, disregarding filtered rows.
   */
  moveRowUp(row: TableRow) {
    let rowIndex = this.rows.indexOf(row),
      targetIndex = rowIndex - 1;
    if (this.hierarchical) {
      // find index with same parent
      let siblings = this.rows.filter(candidate => row.parentRow === candidate.parentRow),
        rowIndexSiblings = siblings.indexOf(row),
        sibling = siblings[rowIndexSiblings - 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        targetIndex = 0;
      }
    }

    this.moveRow(rowIndex, targetIndex);
  }

  /**
   * Moves the row one down, disregarding filtered rows.
   */
  moveRowDown(row: TableRow) {
    let rowIndex = this.rows.indexOf(row),
      targetIndex = rowIndex + 1;
    if (this.hierarchical) {
      // find index with same parent
      let siblings = this.rows.filter(candidate => row.parentRow === candidate.parentRow),
        rowIndexSiblings = siblings.indexOf(row),
        sibling = siblings[rowIndexSiblings + 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        targetIndex = this.rows.length;
      }
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveVisibleRowUp(row: TableRow) {
    let rowIndex = this.rows.indexOf(row),
      visibleIndex = this.visibleRows.indexOf(row),
      sibling: TableRow,
      targetIndex: number;

    if (this.hierarchical) {
      let siblings = this.visibleRows.filter(candidate => row.parentRow === candidate.parentRow);
      sibling = siblings[siblings.indexOf(row) - 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        // no previous sibling
        return;
      }
    } else {
      sibling = this.visibleRows[visibleIndex - 1];
      if (!sibling) {
        // no previous sibling
        return;
      }
      targetIndex = this.rows.indexOf(sibling);
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveVisibleRowDown(row: TableRow) {
    let rowIndex = this.rows.indexOf(row),
      visibleIndex = this.visibleRows.indexOf(row),
      sibling: TableRow,
      targetIndex: number;

    if (this.hierarchical) {
      let siblings = this.visibleRows.filter(candidate => row.parentRow === candidate.parentRow);
      sibling = siblings[siblings.indexOf(row) + 1];
      if (sibling) {
        targetIndex = this.rows.indexOf(sibling);
      } else {
        // no following sibling
        return;
      }
    } else {
      sibling = this.visibleRows[visibleIndex + 1];
      if (!sibling) {
        // no following sibling
        return;
      }
      targetIndex = this.rows.indexOf(sibling);
    }
    this.moveRow(rowIndex, targetIndex);
  }

  moveRow(sourceIndex: number, targetIndex: number) {
    let rowCount = this.rows.length;
    sourceIndex = Math.max(sourceIndex, 0);
    sourceIndex = Math.min(sourceIndex, rowCount - 1);
    targetIndex = Math.max(targetIndex, 0);
    targetIndex = Math.min(targetIndex, rowCount - 1);

    if (sourceIndex === targetIndex) {
      return;
    }

    arrays.move(this.rows, sourceIndex, targetIndex);
    this.updateRowOrder(this.rows);
  }

  protected _removeRowsInRange(range: Range) {
    let numRowsRemoved = 0,
      rows = this.visibleRows;

    let maxRange = new Range(0, rows.length);
    range = maxRange.intersect(range);

    let newRange = this.viewRangeRendered.subtract(range);
    if (newRange.length === 2) {
      throw new Error('Can only remove rows at the beginning or end of the existing range. ' + this.viewRangeRendered + '. New: ' + newRange);
    }
    this.viewRangeRendered = newRange[0];

    for (let i = range.from; i < range.to; i++) {
      let row = rows[i];
      this._removeRow(row);
      numRowsRemoved++;
    }

    if ($.log.isTraceEnabled()) {
      $.log.trace(numRowsRemoved + ' rows removed from ' + range + '.');
      $.log.trace(this._rowsRenderedInfo());
    }
  }

  /**
   * @deprecated use {@link _removeAllRows} to only remove DOM elements, otherwise use {@link deleteAllRows}
   */
  removeAllRows() {
    this._removeAllRows();
  }

  protected _removeAllRows() {
    if (this._isDataRendered()) {
      this.$rows().each((i, elem) => {
        let $row = $(elem);
        if ($row.hasClass('hiding')) {
          // Do not remove rows which are removed using an animation
          // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
          return;
        }
        let row = $row.data('row') as TableRow;
        this._removeRow(row);
      });
    }
    this.viewRangeRendered = new Range(0, 0);
  }

  /**
   *
   * @param rows if undefined, all rows are removed
   */
  protected _removeRows(rows?: TableRow | TableRow[]) {
    if (!rows) {
      this._removeAllRows();
      return;
    }

    let tableAttached = this.isAttachedAndRendered();
    rows = arrays.ensure(rows);
    rows.forEach(row => {
      let rowIndex = this.visibleRows.indexOf(row);
      if (rowIndex === -1) {
        // row is not visible
        return;
      }
      let rowRendered = Boolean(row.$row);
      let rowInViewRange = this.viewRangeRendered.contains(rowIndex);

      // Note: these checks can only be done, when table is rendered _and_ attached. When the table is detached it can
      // still add rows, but these new rows are not rendered while the table is detached. Thus, this check would fail,
      // when a row that has been added in detached state is removed again while table is still detached.
      if (tableAttached) {
        // if row is not rendered but its row-index is inside the view range -> inconsistency
        if (!rowRendered && rowInViewRange) {
          throw new Error('Inconsistency found while removing row. Row is undefined but inside rendered view range. RowIndex: ' + rowIndex);
        }
        // if row is rendered but its row-index is not inside the view range -> inconsistency
        if (rowRendered && !rowInViewRange) {
          throw new Error('Inconsistency found while removing row. Row is rendered but not inside rendered view range. RowIndex: ' + rowIndex);
        }
      }
      this._removeRow(row);

      // Adjust view range if row is inside or before range
      if (rowInViewRange || rowIndex < this.viewRangeRendered.from) {
        if (rowIndex < this.viewRangeRendered.from) {
          this.viewRangeRendered.from--;
          this.viewRangeRendered.to--;
        } else if (rowIndex <= this.viewRangeRendered.to) {
          this.viewRangeRendered.to--;
        }
      }
    });
  }

  /**
   * Just removes the row, does NOT adjust this.viewRangeRendered
   */
  protected _removeRow(row: TableRow | AggregateTableRow) {
    let $row = row.$row;
    if (!$row) {
      return;
    }

    let tooltipSupport = this.$data.data('tooltipSupport') as TooltipSupport;
    if ($row.isOrHas(tooltipSupport?.tooltip?.$anchor)) {
      tooltipSupport.close();
    }
    this._destroyTooltipsForRow(row);
    this._destroyCellEditorForRow(row);

    // Do not remove rows which are removed using an animation
    if (!$row.hasClass('hiding')) {
      $row.remove();
      row.$row = null;
    }
  }

  /**
   * Animates the rendering of a row by setting it to invisible before doing a slideDown animation. The row needs to already be rendered.
   */
  protected _showRow(row: TableRow | AggregateTableRow) {
    let $row = row.$row;
    if (!$row) {
      return;
    }
    if ($row.is('.showing')) {
      return;
    }

    $row.hide(); // intentionally don't use setVisible(false) here
    $row.addClass('showing');
    $row.removeClass('hiding');
    $row.stop().slideDown({
      duration: 250,
      complete: () => {
        $row.removeClass('showing');
        this.updateScrollbars();
      }
    });
  }

  /**
   * Animates the removal of a row by doing a slideUp animation. The row will be removed after the animation finishes.
   */
  protected _hideRow(row: TableRow | AggregateTableRow) {
    let $row = row.$row;
    if (!$row) {
      return;
    }
    if ($row.is('.hiding')) {
      return;
    }

    $row.addClass('hiding');
    $row.removeClass('showing');
    $row.stop().slideUp({
      duration: 250,
      complete: () => {
        if (!row.$row) {
          // ignore already removed rows
          return;
        }
        $row.remove();
        if ($row[0] === row.$row[0]) {
          // Only set to null if row still is linked to the original $row
          // If row got rendered again while the animation is still running, row.$row points to the new $row
          row.$row = null;
        }
        this.updateScrollbars();
      }
    });
  }

  /**
   * This method should be used after a row is added to the DOM (new rows, updated rows). The 'row'
   * is expected to be linked with the corresponding '$row' (row.$row and $row.data('row')).
   */
  protected _installRow(row: TableRow) {
    row.height = this._measureRowHeight(row.$row);

    if (row.hasError) {
      this._showCellErrorForRow(row);
    }
    // Reopen editor popup (closed when row was removed)
    if (this.cellEditorPopup && !this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
      let editorField = this.cellEditorPopup.cell.field;
      this.startCellEdit(this.cellEditorPopup.column, row, editorField);
    }
  }

  /** @internal */
  _calcRowLevelPadding(row: { parentRow?: TableRow }): number {
    if (!row) {
      return -this.rowLevelPadding;
    }
    return this._calcRowLevelPadding(row.parentRow) + this.rowLevelPadding;
  }

  protected _showCellErrorForRow(row: TableRow) {
    for (let column of this.visibleColumns()) {
      let cell = this.cell(column, row);
      if (cell.errorStatus) {
        this._showCellError(row, this.$cell(column, row.$row), cell.errorStatus);
      }
    }
  }

  protected _showCellError(row: TableRow, $cell: JQuery, errorStatus: Status) {
    let text = errorStatus.message;

    let tooltip = scout.create(TableTooltip, {
      parent: this,
      text: text,
      autoRemove: false,
      $anchor: $cell,
      table: this
    });
    tooltip.render();
    // link to be able to remove it when row gets deleted
    tooltip.row = row;
    this.tooltips.push(tooltip);
  }

  /**
   * @returns the column at position x (e.g. from event.pageX)
   */
  protected _columnAtX(x: number): Column<any> {
    let columnOffsetRight = 0,
      columnOffsetLeft = this.$data.offset().left + this.rowBorders.left + this.rowMargins.left,
      scrollLeft = this.$data.scrollLeft();

    if (x < columnOffsetLeft) {
      // Clicked left of first column (on selection border) --> return first column
      return this.columns[0];
    }

    columnOffsetLeft -= scrollLeft;
    let visibleColumns = this.visibleColumns();
    let column = arrays.find(visibleColumns, column => {
      columnOffsetRight = columnOffsetLeft + column.width;
      if (x >= columnOffsetLeft && x < columnOffsetRight) {
        return true;
      }
      columnOffsetLeft = columnOffsetRight;
    });
    if (!column) {
      // No column found (clicked right of last column, on selection border) --> return last column
      column = visibleColumns[visibleColumns.length - 1];
    }
    return column;
  }

  protected _find$AppLink(event: JQuery.MouseUpEvent): JQuery {
    let $start = $(event.target);
    let $stop = $(event.delegateTarget);
    let $appLink = $start.findUp($elem => $elem.hasClass('app-link'), $stop);
    if ($appLink.length > 0) {
      return $appLink;
    }
    return null;
  }

  /** @internal */
  _filterMenus(menuItems: Menu[], destination: MenuDestinations, onlyVisible?: boolean, enableDisableKeyStrokes?: boolean, notAllowedTypes?: string | string[]): Menu[] {
    return menus.filterAccordingToSelection('Table', this.selectedRows.length, menuItems, destination, {onlyVisible, enableDisableKeyStrokes, notAllowedTypes, defaultMenuTypes: this.defaultMenuTypes});
  }

  /** @internal */
  _filterMenusForContextMenu(): Menu[] {
    return this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
  }

  setStaticMenus(staticMenus: ObjectOrChildModel<Menu>[]) {
    this.setProperty('staticMenus', staticMenus);
    this._updateMenuBar();
  }

  protected _removeMenus() {
    // menubar takes care of removal
  }

  notifyRowSelectionFinished() {
    if (this._triggerRowsSelectedPending) {
      this._triggerRowsSelected();
      this._triggerRowsSelectedPending = false;
    }
    this.session.onRequestsDone(() => {
      this._updateMenuBar();
      this._updateMenusEnabled();
    });
  }

  /** @internal */
  _triggerRowClick(originalEvent: JQuery.MouseEventBase, row: TableRow, mouseButton: number, column?: Column<any>) {
    this.trigger('rowClick', {
      originalEvent: originalEvent,
      row: row,
      mouseButton: mouseButton,
      column: column
    });
  }

  protected _triggerRowAction(row: TableRow, column: Column<any>) {
    this.trigger('rowAction', {
      row: row,
      column: column
    });
  }

  /**
   * Starts cell editing for the cell at the given column and row, but only if editing is allowed.
   * @see prepareCellEdit
   */
  focusCell(column: Column<any>, row: TableRow, openFieldPopupOnCellEdit = false) {
    let cell = this.cell(column, row);
    if (this.enabledComputed && row.enabled && cell.editable) {
      this.prepareCellEdit(column, row, openFieldPopupOnCellEdit);
    }
  }

  /**
   * Creates a cell editor for the cell at the given column and row, ensures the row is selected and passes the editor
   * to {@link #startCellEdit} which starts the editing by rendering the editor in a {@link CellEditorPopup}.<br>
   * If the completion of a previous cell edit is still in progress, the preparation is delayed until the completion is finished.
   *
   * @param openFieldPopupOnCellEdit true to instruct the editor to open its control popup when the editor is rendered.
   *    This only has an effect if the editor has a popup (e.g. SmartField or DateField).
   * @returns The promise will be resolved when the preparation has been finished.
   */
  prepareCellEdit(column: Column<any>, row: TableRow, openFieldPopupOnCellEdit?: boolean): JQuery.Promise<void> {
    let promise: JQuery.Promise<void> = $.resolvedPromise();
    if (this.cellEditorPopup) {
      promise = this.cellEditorPopup.waitForCompleteCellEdit();
    }
    return promise.then(this.prepareCellEditInternal.bind(this, column, row, openFieldPopupOnCellEdit));
  }

  /**
   * @param openFieldPopupOnCellEdit when this parameter is set to true, the field instance may use this property (passed to onCellEditorRendered of the field)
   * to decide whether it should open a popup immediately after it is rendered. This is used for Smart- and DateFields. Default is false.
   */
  prepareCellEditInternal(column: Column<any>, row: TableRow, openFieldPopupOnCellEdit?: boolean) {
    this.openFieldPopupOnCellEdit = scout.nvl(openFieldPopupOnCellEdit, false);
    let event = this.trigger('prepareCellEdit', {
      column: column,
      row: row
    });

    if (!event.defaultPrevented) {
      this.selectRow(row);
      let field = column.createEditor(row);
      this.startCellEdit(column, row, field);
    }
  }

  /**
   * @returns a cell for the given column and row. Row Icon column and cell icon column don't have cells --> generate one.
   */
  cell<TValue>(column: Column<TValue>, row: TableRow): Cell<TValue> {
    // @ts-expect-error
    if (column === this.rowIconColumn) {
      return scout.create(Cell, {
        iconId: row.iconId,
        cssClass: strings.join(' ', 'row-icon-cell', row.cssClass)
      }) as Cell<TValue>;
    }

    // @ts-expect-error
    if (column === this.checkableColumn) {
      return scout.create(Cell, {
        value: row.checked,
        editable: true,
        cssClass: row.cssClass
      }) as Cell<TValue>;
    }

    if (column === this.compactColumn) {
      return scout.create(Cell, {
        text: row.compactValue,
        htmlEnabled: true
      }) as Cell<TValue>;
    }

    return row.cells[column.index];
  }

  cellByCellIndex(cellIndex: number, row: TableRow): Cell {
    return this.cell(this.columns[cellIndex], row);
  }

  cellValue<TValue>(column: Column<TValue>, row: TableRow): TValue {
    let cell = this.cell(column, row);
    if (!cell) {
      return cell as TValue;
    }
    if (cell.value !== undefined) {
      return cell.value;
    }
    return null;
  }

  cellText(column: Column<any>, row: TableRow): string {
    let cell = this.cell(column, row);
    if (!cell) {
      return '';
    }
    return cell.text || '';
  }

  /**
   *
   * @returns the next editable position in the table, starting from the cell at (currentColumn / currentRow).
   * A position is an object containing row and column (cell has no reference to a row or column due to memory reasons).
   */
  nextEditableCellPos(currentColumn: Column<any>, currentRow: TableRow, reverse: boolean): TableCellPosition {
    let colIndex = this.columns.indexOf(currentColumn);
    let startColumnIndex = colIndex + 1;
    if (reverse) {
      startColumnIndex = colIndex - 1;
    }
    let pos = this.nextEditableCellPosForRow(startColumnIndex, currentRow, reverse);
    if (pos) {
      return pos;
    }

    let predicate: Predicate<TableRow> = row => {
      if (!row.$row) {
        return false;
      }

      startColumnIndex = 0;
      if (reverse) {
        startColumnIndex = this.columns.length - 1;
      }
      pos = this.nextEditableCellPosForRow(startColumnIndex, row, reverse);
      if (pos) {
        return true;
      }
    };

    let rowIndex = this.rows.indexOf(currentRow);
    let startRowIndex = rowIndex + 1;
    if (reverse) {
      startRowIndex = rowIndex - 1;
    }
    arrays.findFrom(this.rows, startRowIndex, predicate, reverse);

    return pos;
  }

  nextEditableCellPosForRow(startColumnIndex: number, row: TableRow, reverse?: boolean): TableCellPosition {
    let predicate: Predicate<Column<any>> = column => {
      if (!column.visible || column.guiOnly) {
        // does not support tabbing
        return false;
      }
      let cell = this.cell(column, row);
      return this.enabledComputed && row.enabled && cell.editable;
    };

    let column = arrays.findFrom(this.columns, startColumnIndex, predicate, reverse);
    if (column) {
      return {
        column: column,
        row: row
      };
    }
  }

  clearAggregateRows(animate?: boolean) {
    // Remove "hasAggregateRow" markers from real rows
    this._aggregateRows.forEach(aggregateRow => {
      if (aggregateRow.prevRow) {
        aggregateRow.prevRow.aggregateRowAfter = null;
      }
      if (aggregateRow.nextRow) {
        aggregateRow.nextRow.aggregateRowBefore = null;
      }
    });

    if (this._isDataRendered()) {
      this._removeAggregateRows(animate);
      this._renderSelection(); // fix selection borders
    }
    this._aggregateRows = [];
  }

  /**
   * @internal
   * Executes the aggregate function with the given funcName for each visible column, but only if the Column
   * has that function, which is currently only the case for NumberColumns.
   *
   * @param states is a reference to an Array containing the results for each visible column
   * @param row (optional) if set, an additional cell-value parameter is passed to the aggregate function
   */
  _forEachVisibleColumn(funcName: string, states: object[], row?: TableRow) {
    let value;
    this.visibleColumns().forEach((column, i) => {
      if (column[funcName]) {
        if (row) {
          value = column.cellValueOrTextForCalculation(row);
        }
        states[i] = column[funcName](states[i], value);
      } else {
        states[i] = undefined;
      }
    });
  }

  /** @internal */
  _group(animate?: boolean) {
    let firstRow: TableRow, lastRow: TableRow,
      groupColumns = this._groupedColumns(),
      onTop = this.groupingStyle === Table.GroupingStyle.TOP,
      states = [];

    this.clearAggregateRows();
    if (!groupColumns.length) {
      return;
    }

    let rows = this.visibleRows;
    this._forEachVisibleColumn('aggrStart', states);

    rows.forEach((row, r) => {
      if (!firstRow) {
        firstRow = row;
      }
      this._forEachVisibleColumn('aggrStep', states, row);
      // test if sum should be shown, if yes: reset sum-array
      let nextRow = rows[r + 1];
      // test if group is finished
      let newGroup = r === rows.length - 1 || this._isNewGroup(groupColumns, row, nextRow);
      // if group is finished: add group row
      if (newGroup) {
        // finish aggregation
        this._forEachVisibleColumn('aggrFinish', states);
        // append sum row
        this._addAggregateRow(states,
          onTop ? lastRow : row,
          onTop ? firstRow : nextRow);
        // reset after group
        this._forEachVisibleColumn('aggrStart', states);
        firstRow = null;
        lastRow = row;
      }
    });

    if (this._isDataRendered()) {
      this._renderAggregateRows(animate);
      this._renderSelection(); // fix selection borders
    }
  }

  protected _isNewGroup(groupedColumns: Column<any>[], row: TableRow, nextRow: TableRow): boolean {
    let newRow = false;
    if (!nextRow) {
      return true; // row is last row
    }

    for (let i = 0; i < groupedColumns.length; i++) {
      let col = groupedColumns[i];
      let hasCellTextForGroupingFunction = col && col.cellTextForGrouping && typeof col.cellTextForGrouping === 'function';
      newRow = newRow || hasCellTextForGroupingFunction && col.cellTextForGrouping(row) !== col.cellTextForGrouping(nextRow); // NOSONAR
      newRow = newRow || !hasCellTextForGroupingFunction && this.cellText(col, row) !== this.cellText(col, nextRow);
      if (newRow) {
        return true;
      }
    }
    return false;
  }

  protected _groupedColumns(): Column<any>[] {
    return this.columns.filter(col => col.grouped);
  }

  /**
   * Inserts a new aggregation row between 'prevRow' and 'nextRow'.
   *
   * @param contents cells of the new aggregate row
   * @param prevRow row _before_ the new aggregate row
   * @param nextRow row _after_ the new aggregate row
   */
  protected _addAggregateRow(contents: any[], prevRow: TableRow, nextRow: TableRow) {
    let aggregateRow: AggregateTableRow = {
      contents: contents.slice(),
      prevRow: prevRow,
      nextRow: nextRow
    };
    this._aggregateRows.push(aggregateRow);
    if (prevRow) {
      prevRow.aggregateRowAfter = aggregateRow;
    }
    if (nextRow) {
      nextRow.aggregateRowBefore = aggregateRow;
    }
  }

  protected _removeAggregateRows(animate?: boolean) {
    if (this._aggregateRows.length === 0) {
      return;
    }
    animate = scout.nvl(animate, false);
    if (!animate) {
      this._aggregateRows.forEach(aggregateRow => this._removeRow(aggregateRow));
      this.updateScrollbars();
    } else {
      this._aggregateRows.forEach(aggregateRow => this._hideRow(aggregateRow));
    }
  }

  protected _renderAggregateRows(animate?: boolean) {
    let onTop = this.groupingStyle === Table.GroupingStyle.TOP,
      insertFunc = onTop ? 'insertBefore' : 'insertAfter';
    animate = scout.nvl(animate, false);

    this._aggregateRows.forEach(aggregateRow => {
      let refRow = onTop ? aggregateRow.nextRow : aggregateRow.prevRow;
      if (!refRow || !refRow.$row) {
        if (aggregateRow.$row) {
          // corresponding refRow is no longer rendered (e.g. outside view range) -> remove aggregateRow
          this._removeRow(aggregateRow);
        }
        return;
      }

      if (aggregateRow.$row) {
        // already rendered, no need to update again (necessary for subsequent renderAggregateRows calls (e.g. in insertRows -> renderRows)
        return;
      }

      let $aggregateRow = this._build$AggregateRow(aggregateRow);
      $aggregateRow[insertFunc](refRow.$row).width(this.rowWidth);

      this.visibleColumns()
        .map(column => $(column.buildCellForAggregateRow(aggregateRow)).appendTo($aggregateRow))
        .forEach($c => this._resizeAggregateCell($c));

      aggregateRow.height = this._measureRowHeight($aggregateRow);
      aggregateRow.$row = $aggregateRow;
      if (animate) {
        this._showRow(aggregateRow);
      }
    });
  }

  /** @internal */
  _build$AggregateRow(aggregateRow?: AggregateTableRow): JQuery {
    let onTop = this.groupingStyle === Table.GroupingStyle.TOP;
    let $aggregateRow = this.$container
      .makeDiv('table-aggregate-row')
      .data('aggregateRow', aggregateRow);
    this.accessibilityRenderer.renderRow($aggregateRow);
    aria.description($aggregateRow, this.session.text('ui.Aggregation'));
    $aggregateRow.toggleClass('grouping-style-top', onTop);
    $aggregateRow.toggleClass('grouping-style-bottom', !onTop);
    return $aggregateRow;
  }

  groupColumn(column: Column<any>, multiGroup?: boolean, direction?: 'asc' | 'desc', remove?: boolean) {
    multiGroup = scout.nvl(multiGroup, false);
    remove = scout.nvl(remove, false);
    if (remove) {
      this._removeGroupColumn(column);
    }
    if (!this.isGroupingPossible(column)) {
      return;
    }
    if (!remove) {
      this._addGroupColumn(column, direction, multiGroup);
    }

    if (this.header) {
      this.header.onSortingChanged();
    }
    let sorted = this._sort(true);

    let data: any = {
      column: column,
      groupAscending: column.sortAscending
    };
    if (remove) {
      data.groupingRemoved = true;
    }
    if (multiGroup) {
      data.multiGroup = true;
    }
    if (!sorted) {
      // Delegate sorting to server when it is not possible on client side
      data.groupingRequested = true;

      // hint to animate the aggregate after the row order changed event
      this._animateAggregateRows = true;
    }
    this.trigger('group', data);
  }

  removeColumnGrouping(column: Column<any>) {
    if (column) {
      this.groupColumn(column, false, 'asc', true);
    }
  }

  removeAllColumnGroupings() {
    this.columns
      .filter(column => column.grouped)
      .forEach(this.removeColumnGrouping.bind(this));
  }

  /**
   * @returns true if at least one column has grouped=true
   */
  isGrouped(): boolean {
    return this.columns.some(column => column.grouped);
  }

  setColumnBackgroundEffect(column: NumberColumn, effect: NumberColumnBackgroundEffect) {
    column.setBackgroundEffect(effect);
  }

  /**
   * Updates the background effect of every column, if column.backgroundEffect is set.
   * Meaning: Recalculates the min / max values and renders the background effect again.
   */
  protected _updateBackgroundEffect() {
    this.columns.forEach((column: NumberColumn) => {
      if (!column.backgroundEffect) {
        return;
      }
      column.updateBackgroundEffect();
    });
  }

  /**
   * Recalculates the values necessary for the background effect of every column, if column.backgroundEffect is set
   */
  protected _calculateValuesForBackgroundEffect() {
    this.columns.forEach((column: NumberColumn) => {
      if (!column.backgroundEffect) {
        return;
      }
      column.calculateMinMaxValues();
    });
  }

  protected _markAutoOptimizeWidthColumnsAsDirty() {
    this.columns.forEach(column => {
      column.autoOptimizeWidthRequired = true;
    });
  }

  protected _markAutoOptimizeWidthColumnsAsDirtyIfNeeded(autoOptimizeWidthColumns: Column<any>[], oldRow: TableRow, newRow: TableRow): boolean {
    let marked = false;
    for (let i = autoOptimizeWidthColumns.length - 1; i >= 0; i--) {
      let column = autoOptimizeWidthColumns[i];
      if (this.cellValue(column, oldRow) !== this.cellValue(column, newRow)) {
        column.autoOptimizeWidthRequired = true;
        // Remove column from list since it is now marked and does not have to be processed next time
        autoOptimizeWidthColumns.splice(i, 1);
        marked = true;
      }
    }
    return marked;
  }

  /** @see TableModel.multiCheck */
  setMultiCheck(multiCheck: boolean) {
    this.setProperty('multiCheck', multiCheck);
  }

  checkedRows(): TableRow[] {
    return this.rows.filter(row => row.checked);
  }

  checkRow(row: TableRow, checked?: boolean, options?: TableRowCheckOptions) {
    let opts = $.extend(options, {
      checked: checked
    });
    this.checkRows([row], opts);
  }

  checkRows(rows: TableRow | TableRow[], options?: TableRowCheckOptions) {
    let opts: TableRowCheckOptions = $.extend({
      checked: true,
      checkOnlyEnabled: true
    }, options);
    let checkedRows: TableRow[] = [];
    // use enabled computed because when the parent of the table is disabled, it should not be allowed to check rows
    if (!this.checkable || !this.enabledComputed && opts.checkOnlyEnabled) {
      return;
    }
    rows = arrays.ensure(rows);
    rows.forEach(row => {
      if (!row.enabled && opts.checkOnlyEnabled || row.checked === opts.checked) {
        return;
      }
      if (!this.multiCheck && opts.checked) {
        for (let i = 0; i < this.rows.length; i++) {
          if (this.rows[i].checked) {
            this.rows[i].checked = false;
            checkedRows.push(this.rows[i]);
          }
        }
      }
      row.checked = opts.checked;
      checkedRows.push(row);
    });

    if (this._isDataRendered()) {
      checkedRows.forEach(row => this._renderRowChecked(row));
    }
    this._triggerRowsChecked(checkedRows);
  }

  uncheckRow(row: TableRow, options?: TableRowCheckOptions) {
    this.uncheckRows([row], options);
  }

  uncheckRows(rows: TableRow | TableRow[], options?: TableRowCheckOptions) {
    let opts = $.extend({
      checked: false
    }, options);
    this.checkRows(rows, opts);
  }

  isTableNodeColumn(column: Column<any>): boolean {
    return this.hierarchical && this.tableNodeColumn === column;
  }

  collapseRow(row: TableRow) {
    this.collapseRows(arrays.ensure(row));
  }

  collapseAll() {
    this.expandRowsInternal(this.rootRows, false, true);
  }

  expandAll() {
    this.expandRowsInternal(this.rootRows, true, true);
  }

  collapseRows(rows: TableRow[], recursive?: boolean) {
    this.expandRowsInternal(rows, false, recursive);
  }

  expandRow(row: TableRow, recursive?: boolean) {
    this.expandRows(arrays.ensure(row), recursive);
  }

  expandRows(rows: TableRow[], recursive?: boolean) {
    this.expandRowsInternal(rows, true, recursive);
  }

  /**
   * @param rows {@link rootRows} are used if not specified.
   * @param expanded Default is true.
   * @param recursive Default is false.
   */
  expandRowsInternal(rows?: TableRow[], expanded?: boolean, recursive?: boolean) {
    let changedRows: TableRow[] = [], rowsForAnimation: TableRow[] = [];
    rows = rows || this.rootRows;
    expanded = scout.nvl(expanded, true);
    recursive = scout.nvl(recursive, false);
    if (recursive) {
      // collect rows
      this.visitRows(row => {
        let changed = row.expanded !== expanded;
        if (changed) {
          row.expanded = expanded;
          changedRows.push(row);
          if (row.$row) {
            rowsForAnimation.push(row);
          }
        }
      }, rows);
    } else {
      changedRows = rows.filter(row => {
        let changed = row.expanded !== expanded;
        if (changed && row.$row) {
          rowsForAnimation.push(row);
        }
        row.expanded = expanded;
        return changed;
      });
    }
    if (changedRows.length === 0) {
      return;
    }
    this._updateRowStructure({
      visibleRows: true
    });
    this._triggerRowsExpanded(changedRows);

    if (this._isDataRendered()) {
      this._renderRowDelta();
      rowsForAnimation.forEach(row => row.animateExpansion());
      if (rows[0].$row) {
        scrollbars.ensureExpansionVisible({
          element: rows[0],
          $element: rows[0].$row,
          $scrollable: this.get$Scrollable(),
          isExpanded: element => element.expanded,
          getChildren: parent => parent.childRows.filter(row => row.filterAccepted),
          defaultChildHeight: this.rowHeight
        });
      }
    }
  }

  doRowAction(row: TableRow, column?: Column<any>) {
    if (this.selectedRows.length !== 1 || this.selectedRows[0] !== row) {
      // Only allow row action if the selected row was double-clicked because the handler of the event expects a selected row.
      // This may happen if the user modifies the selection using ctrl or shift while double-clicking.
      return;
    }

    column = column || this.columns[0];
    if (!row || !column) {
      return;
    }
    this._triggerRowAction(row, column);
  }

  insertRow(row: ObjectOrModel<TableRow>) {
    this.insertRows([row]);
  }

  insertRows(rows: ObjectOrModel<TableRow> | ObjectOrModel<TableRow>[]) {
    let rowsArr = arrays.ensure(rows);
    if (rowsArr.length === 0) {
      return;
    }
    let wasEmpty = this.rows.length === 0;

    // Update model
    rowsArr.forEach((rowData, i) => {
      let row = this._initRow(rowData);
      row.status = TableRow.Status.INSERTED;
      rowsArr[i] = row;
      // Always insert new rows at the end, if the order is wrong a rowOrderChanged event will follow
      this.rows.push(row);
    });
    let newRows = rowsArr as TableRow[];

    this.filterSupport.applyFilters(newRows);
    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    // Notify changed filter if there are user filters and at least one of the new rows is accepted by them
    if (this.filterCount() > 0 && newRows.some(row => row.filterAccepted)) {
      this._triggerFilter();
    }

    this._calculateValuesForBackgroundEffect();
    this._markAutoOptimizeWidthColumnsAsDirty();

    // this event should be triggered before the rowOrderChanged event (triggered by the _sort function).
    this._triggerRowsInserted(newRows);
    this._sortAfterInsert(wasEmpty);

    // Update HTML
    if (this._isDataRendered()) {
      if (this.hierarchical) {
        this._renderRowOrderChanges();
      }
      // Remember inserted rows for future events like rowOrderChanged
      if (!this._insertedRows) {
        this._insertedRows = newRows;
        setTimeout(() => {
          this._insertedRows = null;
        }, 0);
      } else {
        arrays.pushAll(this._insertedRows, newRows);
      }

      this.viewRangeDirty = true;
      this._renderViewport();
      this.invalidateLayoutTree();
    }
  }

  protected _sortAfterInsert(wasEmpty: boolean) {
    this._sort();
  }

  protected _sortWhileInit() {
    // Only in Scout JS mode. Modified for Scout Classic (see TableAdapter).
    this._sort();
  }

  /**
   * Replaces all current rows by the given ones and tries to restore the selection afterward (see {@link restoreSelection}).
   */
  replaceRows(rows: ObjectOrModel<TableRow> | ObjectOrModel<TableRow>[]) {
    const selectedKeys = this.getSelectedKeys();
    this.deleteAllRows();
    this.insertRows(rows);
    this.restoreSelection(selectedKeys);
  }

  deleteRow(row: TableRow) {
    this.deleteRows([row]);
  }

  deleteRows(rows: TableRow | TableRow[]) {
    rows = arrays.ensure(rows);
    if (rows.length === 0) {
      return;
    }
    let invalidate: boolean,
      filterChanged: boolean,
      removedRows: TableRow[] = [];

    this.visitRows(row => {
      if (!this.rowsMap[row.id]) {
        return;
      }

      removedRows.push(row);
      // Update HTML
      if (this._isDataRendered()) {
        // Cancel cell editing if cell editor belongs to a cell of the deleted row
        if (this.cellEditorPopup && this.cellEditorPopup.row.id === row.id) {
          this.cellEditorPopup.cancelEdit();
        }

        this._removeRows(row);
        invalidate = true;
      }

      // Update model
      arrays.remove(this.rows, row);
      arrays.remove(this.visibleRows, row);
      if (this.filterCount() > 0 && arrays.remove(this._filteredRows, row)) {
        filterChanged = true;
      }
      delete this.rowsMap[row.id];

      if (this.selectionHandler.lastActionRow === row) {
        this.selectionHandler.clearLastSelectedRowMarker();
      }
    }, rows);

    this.deselectRows(removedRows);

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    if (filterChanged) {
      this._triggerFilter();
    }
    this._group();
    this._updateBackgroundEffect();
    this._markAutoOptimizeWidthColumnsAsDirty();
    this._triggerRowsDeleted(rows);

    if (invalidate) {
      this._renderViewport();
      // Update markers and filler because row may be removed by removeRows. RenderViewport doesn't do it if view range is already correctly rendered.
      this._renderRangeMarkers();
      this._renderFiller();
      this._renderEmptyData();
      this.invalidateLayoutTree();
    }
  }

  deleteAllRows() {
    let filterChanged = this.filterCount() > 0 && this._filteredRows.length > 0,
      rows = this.rows;

    // Update HTML
    if (this._isDataRendered()) {
      // Cancel cell editing
      if (this.cellEditorPopup) {
        this.cellEditorPopup.cancelEdit();
      }

      this.selectionHandler.clearLastSelectedRowMarker();
      this._removeRows();
    }

    // Update model
    this.rows = [];
    this.rowsMap = {};
    this._filteredRows = [];
    this.deselectAll();

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    if (filterChanged) {
      this._triggerFilter();
    }

    this._markAutoOptimizeWidthColumnsAsDirty();
    this._group();
    this._updateBackgroundEffect();
    this._triggerAllRowsDeleted(rows);

    // Update HTML
    if (this._isDataRendered()) {
      this._renderFiller();
      this._renderViewport();
      this._renderEmptyData();
      this.invalidateLayoutTree();
    }
  }

  updateRow(row: ObjectOrModel<TableRow>) {
    this.updateRows([row]);
  }

  updateRows(rows: ObjectOrModel<TableRow> | ObjectOrModel<TableRow>[]) {
    rows = arrays.ensure(rows);
    if (rows.length === 0) {
      return;
    }
    if (this.updateBuffer.isBuffering()) {
      this.updateBuffer.buffer(rows);
      return;
    }
    let filterChanged: boolean, expansionChanged: boolean, autoOptimizeWidthColumnsDirty: boolean;
    let autoOptimizeWidthColumns = this.columns.filter(column => column.autoOptimizeWidth && !column.autoOptimizeWidthRequired);

    let rowsToIndex: Record<string, number> = {};
    this.rows.forEach((row, index) => {
      rowsToIndex[row.id] = index;
    });

    let oldRowsMap: Record<string, TableRow> = {};
    let structureChanged = false;
    let updatedRows = rows.map(rowOrModel => {
      let parentRowId: any = rowOrModel.parentRow;
      let oldRow = this.rowsMap[rowOrModel.id];
      // collect old rows
      oldRowsMap[rowOrModel.id] = oldRow;
      if (!oldRow) {
        throw new Error('Update event received for non existing row. RowId: ' + rowOrModel.id);
      }
      // check structure changes
      if (rowOrModel.parentRow) {
        if (typeof rowOrModel.parentRow === 'string') {
          parentRowId = rowOrModel.parentRow;
        } else if (!objects.isNullOrUndefined(rowOrModel.parentRow.id)) {
          parentRowId = rowOrModel.parentRow.id;
        }
      }
      structureChanged = structureChanged || (scout.nvl(oldRow['_parentRowId'], null) !== scout.nvl(parentRowId, null));
      expansionChanged = expansionChanged || (oldRow.expanded !== scout.nvl(rowOrModel.expanded, false));
      let row = this._initRow(rowOrModel);
      // Check if cell values have changed
      if (row.status === TableRow.Status.NON_CHANGED) {
        row.cells.some((cell, i) => {
          let oldCell = oldRow.cells[i];
          if (!oldCell || oldCell.value !== cell.value) {
            row.status = TableRow.Status.UPDATED;
            return true; // break "some()" loop
          }
          return false;
        });
      }
      // selection
      if (this.selectionHandler.lastActionRow === oldRow) {
        this.selectionHandler.lastActionRow = row;
      }
      arrays.replace(this.selectedRows, oldRow, row);
      // replace row use index lookup for performance reasons
      this.rows[rowsToIndex[row.id]] = row;
      // filter
      row.filterAccepted = oldRow.filterAccepted;
      if (this.filterCount() > 0) {
        filterChanged = this._applyFiltersForRow(row) || filterChanged;
      }
      // Check if cell content changed and if yes mark auto optimize width column as dirty
      autoOptimizeWidthColumnsDirty = this._markAutoOptimizeWidthColumnsAsDirtyIfNeeded(autoOptimizeWidthColumns, oldRow, row);
      return row;
    });

    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    this._updateMenusEnabled();

    this._triggerRowsUpdated(updatedRows);

    if (this._isDataRendered()) {
      this._renderUpdateRows(updatedRows, oldRowsMap);
      if (structureChanged) {
        this._renderRowOrderChanges();
      }
    }

    if (filterChanged) {
      this._triggerFilter();
    }
    if (filterChanged || expansionChanged) {
      this._renderRowDelta();
    }

    this._sortAfterUpdate();
    this._updateBackgroundEffect();
    this.invalidateLayoutTree(); // this will also update the scroll-bars
  }

  protected _renderUpdateRows(rows: TableRow[], oldRowsMap: Record<string, TableRow>) {
    let tooltipSupport = this.$data.data('tooltipSupport') as TooltipSupport;

    // render row and replace div in DOM
    rows.forEach(row => {
      let oldRow = oldRowsMap[row.id];
      if (!oldRow.$row || oldRow.$row.hasClass('hiding')) {
        // If row is not rendered or being removed by an animation, don't try to update it.
        // If it were updated during animated removal, the new row would immediately be inserted again, so the removal would not work.
        return;
      }
      let $updatedRow = $(this._buildRowDiv(row));
      $updatedRow.copyCssClasses(oldRow.$row, Table.SELECTION_CLASSES + ' first last');

      // Check if the cell tooltip is currently pointing to a cell in this row. If yes, update its $anchor to the corresponding cell
      // in the new row. Otherwise, the tooltip position will be wrong, because the old anchor is no longer part of the DOM.
      let $updatedTooltipCell: JQuery = null;
      if (tooltipSupport?.tooltip) {
        let $oldRowCells = oldRow.$row.children('.table-cell');
        let oldTooltipCellIndex = $oldRowCells.index(tooltipSupport.tooltip.$anchor);
        if (oldTooltipCellIndex !== -1) {
          $updatedTooltipCell = $updatedRow.children('.table-cell').eq(oldTooltipCellIndex);
        }
      }

      oldRow.$row.replaceWith($updatedRow);
      Table.linkRowToDiv(row, $updatedRow);
      this._destroyTooltipsForRow(row);
      this._destroyCellEditorForRow(row);
      this._installRow(row);

      if ($updatedTooltipCell?.length) {
        tooltipSupport.update($updatedTooltipCell);
      }
      if (oldRow.$row.hasClass('showing') && oldRow.$row.outerHeight() < row.$row.outerHeight() / 3) {
        // If the row was being shown by an animation, start the animation again for the new row, otherwise row would immediately appear without animation.
        // Do it only, if the current running time of the animation does not exceed 33% (this won't be correct if the height of the new and old rows differ).
        // Goal: if the update happens immediately after the animation started, the new row will be animated nicely. If the update happens later, don't start the animation again from the start.
        this._showRow(row);
      }
    });
  }

  protected _sortAfterUpdate() {
    this._sort();
  }

  /**
   * @deprecated use {@link hierarchical} instead
   */
  isHierarchical(): boolean {
    return this.hierarchical;
  }

  protected _setHierarchical(hierarchical: boolean) {
    if (this.hierarchical === hierarchical) {
      return;
    }

    // Has to be called before the property is set! Otherwise, the grouping will not completely be removed, since isGroupingPossible() will return false.
    if (hierarchical) {
      this.removeAllColumnGroupings();
    }
    if (hierarchical && !(this.accessibilityRenderer instanceof HierarchicalTableAccessibilityRenderer)) {
      this.accessibilityRenderer = new HierarchicalTableAccessibilityRenderer();
    }
    this._setProperty('hierarchical', hierarchical);
  }

  /**
   * The given rows must be rows of this table in desired order.
   */
  updateRowOrder(rows: TableRow | TableRow[]) {
    rows = arrays.ensure(rows);
    if (rows.length !== this.rows.length) {
      throw new Error('Row order may not be updated because lengths of the arrays differ.');
    }

    // update model (make a copy so that original array stays untouched)
    this.rows = rows.slice();
    this._updateRowStructure({
      updateTree: true,
      filteredRows: true,
      applyFilters: false,
      visibleRows: true
    });
    this.clearAggregateRows(this._animateAggregateRows);
    if (this._isDataRendered()) {
      this._renderRowOrderChanges();
    }
    this._triggerRowOrderChanged();

    this._group(this._animateAggregateRows);
    this._animateAggregateRows = false;
  }

  protected _destroyTooltipsForRow(row: TableRow | AggregateTableRow) {
    for (let i = this.tooltips.length - 1; i >= 0; i--) {
      if (this.tooltips[i].row.id === row.id) {
        this.tooltips[i].destroy();
        this.tooltips.splice(i, 1);
      }
    }
  }

  protected _destroyCellEditorForRow(row: TableRow | AggregateTableRow) {
    if (this.cellEditorPopup && this.cellEditorPopup.rendered && this.cellEditorPopup.row.id === row.id) {
      this.cellEditorPopup.destroy();
    }
  }

  startCellEdit<TValue>(column: Column<TValue>, row: TableRow, field: ValueField<TValue>): CellEditorPopup<TValue> {
    if (field.destroyed) {
      // May happen if the action was postponed and the field destroyed in the meantime using endCellEdit.
      return;
    }
    if (!this._isDataRendered()) {
      this._postRenderActions.push(this.startCellEdit.bind(this, column, row, field));
      return;
    }
    if (!this.$container.isAttached()) {
      this._postAttachActions.push(this.startCellEdit.bind(this, column, row, field));
      return;
    }

    this.trigger('startCellEdit', {
      column: column,
      row: row,
      field: field
    });
    this.ensureRowRendered(row);
    let popup = column.startCellEdit(row, field);
    this.cellEditorPopup = popup;
    this.$container.toggleClass('has-cell-editor-popup', !!popup);
    return popup;
  }

  /**
   * @param saveEditorValue when this parameter is set to true, the value of the editor field is set as
   *    new value on the edited cell. In remote case this parameter is always false, because the cell
   *    value is updated by an updateRow event instead. Default is false.
   */
  endCellEdit(field: ValueField<any>, saveEditorValue?: boolean) {
    if (!this.cellEditorPopup) {
      // the cellEditorPopup could already be removed by scrolling (out of view range) or be removed by update rows
      field.destroy();
      return;
    }
    // Remove the cell-editor popup prior to destroying the field, so that the cell-editor-popup's focus context is
    // uninstalled first and the focus can be restored onto the last focused element of the surrounding focus context.
    // Otherwise, if the currently focused field is removed from DOM, the $entryPoint would be focused first, which can
    // be avoided if removing the popup first.
    // Also, Column.setCellValue needs to be called _after_ cellEditorPopup is set to null
    // because in updateRows we check if the popup is still there and start cell editing mode again.
    this._destroyCellEditorPopup(this._updateCellFromEditor.bind(this, this.cellEditorPopup, field, saveEditorValue));
  }

  protected _updateCellFromEditor<TValue>(cellEditorPopup: CellEditorPopup<TValue>, field: ValueField<TValue>, saveEditorValue?: boolean) {
    saveEditorValue = scout.nvl(saveEditorValue, false);
    if (saveEditorValue) {
      let column = cellEditorPopup.column;
      column.updateCellFromEditor(cellEditorPopup.row, field);
    }
    field.destroy();
  }

  completeCellEdit() {
    let field = this.cellEditorPopup.cell.field;
    let event = this.trigger('completeCellEdit', {
      field: field,
      row: this.cellEditorPopup.row,
      column: this.cellEditorPopup.column,
      cell: this.cellEditorPopup.cell
    });

    if (!event.defaultPrevented) {
      return this.endCellEdit(field, true);
    }
  }

  cancelCellEdit() {
    let field = this.cellEditorPopup.cell.field;
    let event = this.trigger('cancelCellEdit', {
      field: field,
      row: this.cellEditorPopup.row,
      column: this.cellEditorPopup.column,
      cell: this.cellEditorPopup.cell
    });

    if (!event.defaultPrevented) {
      this.endCellEdit(field);
    }
  }

  scrollTo(row: TableRow, options?: ScrollToOptions | string) {
    if (this.viewRangeRendered.size() === 0) {
      // Cannot scroll to a row no row is rendered
      return;
    }
    this.ensureRowRendered(row);
    if (!row.$row) {
      // Row may not be visible due to the filter -> don't try to scroll because it would fail
      return;
    }
    scrollbars.scrollTo(this.$data, row.$row, options);
  }

  scrollPageUp() {
    let newScrollTop = Math.max(0, this.$data[0].scrollTop - this.$data.height());
    this.setScrollTop(newScrollTop);
  }

  scrollPageDown() {
    let newScrollTop = Math.min(this.$data[0].scrollHeight, this.$data[0].scrollTop + this.$data.height());
    this.setScrollTop(newScrollTop);
  }

  override setScrollTop(scrollTop: number) {
    this.setProperty('scrollTop', scrollTop);
    // call _renderViewport to make sure rows are rendered immediately. The browser fires the scroll event handled by onDataScroll delayed
    if (this._isDataRendered()) {
      this._renderViewport();
    }
  }

  /** @internal */
  override _renderScrollTop() {
    if (this.rendering) {
      // Not necessary to do it while rendering since it will be done by the layout
      return;
    }
    scrollbars.scrollTop(this.get$Scrollable(), this.scrollTop);
  }

  override get$Scrollable(): JQuery {
    if (this.$data) {
      return this.$data;
    }
    return this.$container;
  }

  /** @see TableModel.scrollToSelection */
  setScrollToSelection(scrollToSelection: boolean) {
    this.setProperty('scrollToSelection', scrollToSelection);
  }

  revealSelection() {
    if (!this._isDataRendered()) {
      // Execute delayed because table may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this.revealSelection.bind(this));
      return;
    }

    if (this.selectedRows.length > 0) {
      this.scrollTo(this.selectedRows[0]);
    }
  }

  revealChecked() {
    let firstCheckedRow = arrays.find(this.rows, row => row.checked);
    if (firstCheckedRow) {
      this.scrollTo(firstCheckedRow);
    }
  }

  rowById(id: string): TableRow {
    return this.rowsMap[id];
  }

  rowsByIds(ids: string[]): TableRow[] {
    return ids.map(this.rowById.bind(this));
  }

  rowsToIds(rows: TableRow[]): string[] {
    return rows.map(row => row.id);
  }

  /**
   * Checks whether the given row is contained in the table. Uses the id of the row for the lookup.
   */
  hasRow(row: TableRow): boolean {
    return Boolean(this.rowsMap[row.id]);
  }

  /**
   * render borders and selection of row. default select if no argument or false is passed in deselect
   * model has to be updated before calling this method.
   */
  protected _renderSelection(rows?: TableRow | TableRow[]) {
    rows = arrays.ensure(rows || this.selectedRows);

    // helper function adds/removes a class for a row only if necessary, return 1 if classes have been changed
    let addOrRemoveClassIfNeededFunc = ($row: JQuery, condition: boolean, classname: string): number => {
      let hasClass = $row.hasClass(classname);
      if (condition && !hasClass) {
        $row.addClass(classname);
        return 1;
      } else if (!condition && hasClass) {
        $row.removeClass(classname);
        return 1;
      }
      return 0;
    };
    this._renderNoRowsSelectedMarker();

    for (let i = 0; i < rows.length; i++) { // traditional for loop, elements might be added during loop
      let row = rows[i];
      if (!row.$row) {
        continue;
      }

      let thisRowSelected = this.selectedRows.indexOf(row) !== -1,
        visibleRows = this.visibleRows,
        previousIndex = visibleRows.indexOf(row) - 1,
        previousRowSelected = previousIndex >= 0 && this.selectedRows.indexOf(visibleRows[previousIndex]) !== -1,
        followingIndex = visibleRows.indexOf(row) + 1,
        followingRowSelected = followingIndex < visibleRows.length && this.selectedRows.indexOf(visibleRows[followingIndex]) !== -1;

      // Don't collapse selection borders if two consecutively selected (real) rows are separated by an aggregation row
      if (thisRowSelected && previousRowSelected && row.aggregateRowBefore) {
        previousRowSelected = false;
      }
      if (thisRowSelected && followingRowSelected && row.aggregateRowAfter) {
        followingRowSelected = false;
      }

      aria.selected(row.$row, thisRowSelected || null);

      let classChanged =
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected, 'selected') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && !previousRowSelected && followingRowSelected, 'select-top') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && previousRowSelected && !followingRowSelected, 'select-bottom') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && !previousRowSelected && !followingRowSelected, 'select-single') +
        addOrRemoveClassIfNeededFunc(row.$row, thisRowSelected && previousRowSelected && followingRowSelected, 'select-middle');

      if (classChanged > 0 && previousRowSelected && rows.indexOf(visibleRows[previousIndex]) === -1) {
        rows.push(visibleRows[previousIndex]);
      }
      if (classChanged > 0 && followingRowSelected && rows.indexOf(visibleRows[followingIndex]) === -1) {
        rows.push(visibleRows[followingIndex]);
      }
    }

    // Make sure the cell editor popup is correctly layouted because selection changes the cell bounds
    if (this.cellEditorPopup && this.cellEditorPopup.rendered && this.selectedRows.indexOf(this.cellEditorPopup.row) > -1) {
      this.cellEditorPopup.position();
      this.cellEditorPopup.pack();
    }
  }

  /** @internal */
  _removeSelection() {
    this.$container.addClass('no-rows-selected');
    this.selectedRows.forEach(row => {
      if (!row.$row) {
        return;
      }
      row.$row.select(false);
      row.$row.toggleClass(Table.SELECTION_CLASSES, false);
      aria.selected(row.$row, null);
    });
  }

  protected _renderNoRowsSelectedMarker() {
    this.$container.toggleClass('no-rows-selected', this.selectedRows.length === 0);
  }

  addRowToSelection(row: TableRow, ongoingSelection?: boolean) {
    if (this.selectedRows.indexOf(row) > -1) {
      return;
    }
    ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
    this.selectedRows.push(row);

    if (row.$row && this._isDataRendered()) {
      row.$row.select(true);
      this._renderSelection(row);
      if (this.scrollToSelection) {
        this.revealSelection();
      }
    }

    this._triggerRowsSelectedPending = true;
    if (!ongoingSelection) {
      this.notifyRowSelectionFinished();
    }
  }

  removeRowFromSelection(row: TableRow, ongoingSelection?: boolean) {
    ongoingSelection = ongoingSelection !== undefined ? ongoingSelection : true;
    if (arrays.remove(this.selectedRows, row)) {
      if (this._isDataRendered()) {
        this._renderSelection(row);
      }
      if (!ongoingSelection) {
        this._triggerRowsSelected();
      } else {
        this._triggerRowsSelectedPending = true;
      }
    }
  }

  selectRow(row: TableRow, debounceSend?: boolean) {
    this.selectRows(row, debounceSend);
  }

  selectRows(rows: TableRow | TableRow[], debounceSend?: boolean) {
    // Exclude rows that are currently not visible because of a filter (they cannot be selected)
    rows = arrays.ensure(rows).filter(row => Boolean(this.visibleRowsMap[row.id]));

    let selectedEqualRows = arrays.equalsIgnoreOrder(rows, this.selectedRows);
    // TODO [7.0] cgu: maybe make sure selectedRows are in correct order, this would make logic in AbstractTableNavigationKeyStroke or renderSelection easier
    // but requires some effort (remember rowIndex, keep array in order after sort, ... see java Table)
    if (selectedEqualRows) {
      return;
    }

    if (this._isDataRendered()) {
      this._removeSelection();
    }

    if (!this.multiSelect && rows.length > 1) {
      rows = [rows[0]];
    }

    this.selectedRows = rows; // (Note: direct assignment is safe because the initial filtering created a copy of the original array)
    this._triggerRowsSelected(debounceSend);

    this._updateMenusEnabled();
    this._updateMenuBar();
    if (this._isDataRendered()) {
      this._renderSelection();
      if (this.scrollToSelection) {
        this.revealSelection();
      }
    }
  }

  deselectRow(row: TableRow) {
    this.deselectRows(row);
  }

  deselectRows(rows: TableRow | TableRow[]) {
    rows = arrays.ensure(rows);
    let selectedRows = this.selectedRows.slice(); // copy
    if (arrays.removeAll(selectedRows, rows)) {
      this.selectRows(selectedRows);
    }
  }

  isRowSelected(row: TableRow): boolean {
    return this.selectedRows.indexOf(row) > -1;
  }

  /**
   * Restores the selection by the given selectedKeys. Rows matching these given values (see {@link getRowsByKey}) will be selected.
   * If the table is {@link hierarchical} all parent rows of selected rows will be expanded.
   *
   * @param selectedKeys array of key values (see {@link TableRow.getKeyValues}) of the rows to select.
   */
  restoreSelection(selectedKeys: any[][]) {
    const rows = this.getRowsByKey(selectedKeys);
    if (this.hierarchical) {
      // collect all parentRows of selectedRows and expand them
      const parentRows = new Set<TableRow>();
      const remaining = new Set(rows);
      for (const r of remaining) {
        const parentRow = r.parentRow;
        if (!parentRow) {
          continue;
        }
        parentRows.add(parentRow);
        // the iterator will iterate over newly added elements
        // therefore new elements added here will be processed
        remaining.add(parentRow);
      }
      this.expandRows([...parentRows]);
    }
    this.selectRows(rows);
  }

  /**
   * Get the key values (see {@link TableRow.getKeyValues}) of all selected rows.
   */
  getSelectedKeys(): any[][] {
    if (!this.selectedRows?.length) {
      return [];
    }
    return this.selectedRows.map(row => row.getKeyValues());
  }

  /**
   * Get rows by comparing the given keys with the key values (see {@link TableRow.getKeyValues}) of the current rows.
   *
   * @param keys array of key values (see {@link TableRow.getKeyValues}).
   */
  getRowsByKey(keys: any[][]): TableRow[] {
    if (!keys?.length) {
      return [];
    }
    keys = [...keys];
    const rows = [];
    for (const row of this.rows) {
      let index = -1;
      try {
        index = keys.findIndex(key => objects.equalsRecursive(key, row.getKeyValues()));
      } catch (e) {
        $.log.warn('Unable to find row.', e);
      }
      if (index !== -1) {
        keys.splice(index, 1);
        rows.push(row);
        if (!keys.length) {
          break;
        }
      }
    }
    return rows;
  }

  /**
   * @see getRowsByKey
   *
   * @param keys key values (see {@link TableRow.getKeyValues}).
   */
  getRowByKey(key: any[]): TableRow {
    if (!key) {
      return null;
    }
    return this.getRowsByKey([key])[0];
  }

  filterCount(): number {
    return this.filters.length;
  }

  filteredRows(): TableRow[] {
    return this._filteredRows;
  }

  $rows(includeAggrRows?: boolean): JQuery {
    let selector = '.table-row';
    if (includeAggrRows) {
      selector += ', .table-aggregate-row';
    }
    return this.$data.find(selector);
  }

  $aggregateRows(): JQuery {
    return this.$data.find('.table-aggregate-row');
  }

  /**
   * @returns the first selected row of this table or null when no row is selected
   */
  selectedRow(): TableRow {
    if (this.selectedRows.length > 0) {
      return this.selectedRows[0];
    }
    return null;
  }

  $selectedRows(): JQuery {
    if (!this.$data) {
      return $();
    }
    return this.$data.find('.selected');
  }

  $cellsForColIndex(colIndex: number, includeAggrRows?: boolean): JQuery {
    let selector = '.table-row > div:nth-of-type(' + colIndex + ')';
    if (includeAggrRows) {
      selector += ', .table-aggregate-row > div:nth-of-type(' + colIndex + ')';
    }
    return this.$data.find(selector);
  }

  $cellsForRow($row: JQuery): JQuery {
    return $row.children('.table-cell');
  }

  /**
   * @param column or columnIndex
   */
  $cell(column: Column<any> | number, $row: JQuery): JQuery {
    let columnIndex: number;
    if (typeof column !== 'number') {
      columnIndex = this.visibleColumns().indexOf(column);
    } else {
      columnIndex = column;
    }
    return $row.children('.table-cell').eq(columnIndex);
  }

  /**
   * Searches for a column with the given ID.
   *
   * @param columnId the ID of the column to look for
   * @param type the type of the column to look for. The return value will be cast to that type. This parameter has no effect at runtime.
   * @returns the column for the requested ID or null if no column has been found.
   */
  columnById<TColumn extends Column>(columnId: string, type: abstract new() => TColumn): TColumn;
  /**
   * Searches for a column with the given ID.
   *
   * If this table has a {@link columnMap}, its type has to contain a mapping for the given ID so the found column can be cast to the correct type.
   * If there is no concrete {@link columnMap}, the return type will be {@link Column}.
   *
   * @param columnId the ID of the column to look for
   * @returns the column for the requested ID or null if no column has been found.
   */
  columnById<TId extends string & keyof ColumnMapOf<this>>(columnId: TId): ColumnMapOf<this>[TId];
  columnById<TId extends string & keyof ColumnMapOf<this>, TColumn extends Column>(columnId: TId, type?: abstract new() => TColumn): ColumnMapOf<this>[TId] | TColumn {
    return arrays.find(this.columns, column => column.id === columnId) as ColumnMapOf<this>[TId];
  }

  /**
   * @param $cell the $cell to get the column for
   * @param $row the $row which contains the $cell. If not passed it will be determined automatically
   * @returns the column for the given $cell
   */
  columnFor$Cell($cell: JQuery, $row?: JQuery): Column<any> {
    $row = $row || $cell.parent();
    let cellIndex = this.$cellsForRow($row).index($cell);
    return this.visibleColumns()[cellIndex];
  }

  columnsByIds<TId extends string & keyof ColumnMapOf<this>>(columnIds: TId[]): ColumnMapOf<this>[TId][] {
    return columnIds.map(id => this.columnById(id));
  }

  getVisibleRows(): TableRow[] {
    return this.visibleRows;
  }

  protected _updateRowStructure(options: UpdateTableRowStructureOptions) {
    let updateTree = scout.nvl(options.updateTree, false),
      updateFilteredRows = scout.nvl(options.filteredRows, updateTree),
      applyFilters = scout.nvl(options.applyFilters, updateFilteredRows),
      filtersChanged = scout.nvl(options.filtersChanged, false),
      updateVisibleRows = scout.nvl(options.visibleRows, updateFilteredRows);
    if (updateTree) {
      this._rebuildTreeStructure();
    }
    if (updateFilteredRows) {
      this._updateFilteredRows(applyFilters, filtersChanged);
    }
    if (updateVisibleRows) {
      this._updateVisibleRows();
    }
  }

  protected _rebuildTreeStructure() {
    let hierarchical = false;
    this.rows.forEach(row => {
      row.childRows = [];
      hierarchical = hierarchical || !objects.isNullOrUndefined(row.parentRow);
    });
    if (!hierarchical) {
      this.rootRows = this.rows;
      this._setHierarchical(hierarchical);
      return;
    }

    this._setHierarchical(hierarchical);
    this.rootRows = [];
    this.rows.forEach(row => {
      let parentRow: TableRow;
      if (objects.isNullOrUndefined(row.parentRow)) {
        // root row
        row.parentRow = null;
        row['_parentRowId'] = null;
        this.rootRows.push(row);
        return;
      }
      if (!objects.isNullOrUndefined(row.parentRow.id)) {
        parentRow = this.rowsMap[row.parentRow.id];
      } else {
        // expect id
        let parentRowId = row.parentRow as unknown as string;
        parentRow = this.rowsMap[parentRowId];
      }
      if (parentRow) {
        row.parentRow = parentRow;
        row['_parentRowId'] = parentRow.id;
        parentRow.childRows.push(row);
      } else {
        // do not allow unresolvable parent rows.
        throw new Error('Parent row of ' + row + ' can not be resolved.');
      }
    });

    // traverse row tree to have minimal order of rows.
    this._maxLevel = 0;
    this.rows = [];
    this.visitRows((row, level) => {
      row.hierarchyLevel = level;
      this._maxLevel = Math.max(level, this._maxLevel);
      this.rows.push(row);
    });

    this._calculateTableNodeColumn();
  }

  protected _updateFilteredRows(applyFilters?: boolean, changed?: boolean) {
    changed = Boolean(changed);
    applyFilters = scout.nvl(applyFilters, true);
    this._filteredRows = this.rows.filter(row => {
      if (applyFilters) {
        changed = this._applyFiltersForRow(row) || changed;
      }
      return row.filterAccepted;
    });

    if (changed) {
      this._triggerFilter();
    }
  }

  protected _updateVisibleRows() {
    this.visibleRows = this._computeVisibleRows();
    // rebuild the rows by id map of visible rows
    this.visibleRowsMap = this.visibleRows.reduce((map, row) => {
      map[row.id] = row;
      return map;
    }, {});

    if (this.initialized) {
      // deselect not visible rows
      let notVisibleRows = this.selectedRows.filter(selectedRow => !this.visibleRowsMap[selectedRow.id]);
      this.deselectRows(notVisibleRows);
    }
  }

  protected _computeVisibleRows(rows?: TableRow[]): TableRow[] {
    let visibleRows: TableRow[] = [];
    rows = rows || this.rootRows;
    rows.forEach(row => {
      let visibleChildRows = this._computeVisibleRows(row.childRows);
      if (row.filterAccepted) {
        visibleRows.push(row);
      } else if (visibleChildRows.length > 0) {
        visibleRows.push(row);
      }
      row.expandable = visibleChildRows.length > 0;
      if (row.expanded) {
        visibleRows = visibleRows.concat(visibleChildRows);
      }
    });
    return visibleRows;
  }

  visibleChildRows(row: TableRow): TableRow[] {
    return row.childRows.filter(child => Boolean(this.visibleRowsMap[child.id]));
  }

  protected _renderRowDelta() {
    if (!this._isDataRendered()) {
      return;
    }
    let renderedRows: TableRow[] = [];
    let rowsToHide: TableRow[] = [];
    this.$rows().each((i, elem) => {
      let $row = $(elem);
      let row = $row.data('row') as TableRow;
      if (this.visibleRows.indexOf(row) < 0) {
        // remember for remove animated
        row.$row.detach();
        rowsToHide.push(row);
      } else {
        if ($row.hasClass('hiding')) {
          // If a row is shown again while the hide animation is still running, complete the hide animation first to ensure the same model row will never be rendered twice.
          // In order to get a show animation for that case, don't add this row to the list of renderedRows.
          $row.stop(false, true);
        } else {
          renderedRows.push(row);
        }
      }
    });

    this._rerenderViewport();
    // insert rows to remove animated
    rowsToHide.forEach(row => row.$row.insertAfter(this.$fillBefore));
    // Rows removed by an animation are still there, new rows were appended -> reset correct row order
    this._order$Rows().insertAfter(this.$fillBefore);
    // Also make sure aggregate rows are at the correct position (_renderAggregateRows does nothing because they are already rendered)
    this._order$AggregateRows();

    // remove animated
    rowsToHide.forEach(row => this._hideRow(row));

    this.$rows().each((i, elem) => {
      let $row = $(elem);
      let row = $row.data('row') as TableRow;
      if ($row.hasClass('hiding')) {
        // Do not remove rows which are removed using an animation
        // row.$row may already point to a new row -> don't call removeRow to not accidentally remove the new row
        return;
      }
      if (renderedRows.indexOf(row) < 0) {
        this._showRow(row);
      }
    });
    this._renderScrollTop();
    this._renderEmptyData();
  }

  /**
   * Sorts the given $rows according to the row index
   */
  protected _order$Rows($rows?: JQuery): JQuery {
    // Find rows using jquery because this.filteredRows() may be empty but there may be $rows which are getting removed by animation
    $rows = $rows || this.$rows();
    return $rows.sort((elem1, elem2) => {
      let $row1 = $(elem1),
        $row2 = $(elem2),
        row1 = $row1.data('row') as TableRow,
        row2 = $row2.data('row') as TableRow;

      return this.rows.indexOf(row1) - this.rows.indexOf(row2);
    });
  }

  protected _order$AggregateRows($rows?: JQuery) {
    // Find aggregate rows using jquery because
    // this._aggregateRows may be empty but there may be $aggregateRows which are getting removed by animation
    $rows = $rows || this.$aggregateRows();
    $rows.each((i, elem) => {
      let $aggrRow = $(elem),
        aggregateRow = $aggrRow.data('aggregateRow') as AggregateTableRow;
      if (!aggregateRow || !aggregateRow.prevRow) {
        return;
      }
      $aggrRow.insertAfter(aggregateRow.prevRow.$row);
    });
  }

  /**
   * @returns true if row state has changed, false if not
   */
  protected _applyFiltersForRow(row: TableRow): boolean {
    return this.filterSupport.applyFiltersForElement(row);
  }

  /**
   * @returns labels of the currently active Filters that provide a createLabel() function
   */
  filteredBy(): string[] {
    let filteredBy: string[] = [];
    this.filters.forEach(filter => {
      // check if filter supports label
      if (filter instanceof TableUserFilter) {
        filteredBy.push(filter.createLabel());
      }
    });
    return filteredBy;
  }

  resetUserFilter(applyFilter = true) {
    this.filters.filter(filter => filter instanceof TableUserFilter)
      .forEach(filter => this.removeFilter(filter, applyFilter));

    this._triggerFilterReset();
  }

  hasUserFilter(): boolean {
    return this.filters
      .filter(filter => filter instanceof TableUserFilter)
      .length > 0;
  }

  resizeToFit(column: Column<any>, maxWidth?: number) {
    if (column.fixedWidth) {
      return;
    }
    let returnValue = column.calculateOptimalWidth();
    if (objects.isPlainObject(returnValue)) {
      // Function returned a promise -> delay resizing
      returnValue.always(this._resizeToFit.bind(this, column, maxWidth));
    } else {
      this._resizeToFit(column, maxWidth, returnValue);
    }
  }

  protected _resizeToFit(column: Column<any>, maxWidth?: number, calculatedSize?: number) {
    if (calculatedSize === -1) {
      // Calculation has been aborted -> don't resize
      return;
    }
    if (maxWidth && maxWidth > 0 && calculatedSize > maxWidth) {
      calculatedSize = maxWidth;
    }
    if (column.width !== calculatedSize) {
      this.resizeColumn(column, calculatedSize);
    }
    column.autoOptimizeWidthRequired = false;
    this._triggerColumnResizedToFit(column);
  }

  /**
   * @param filter The filter to add.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  addFilter(filter: FilterOrFunction<TableRow>, applyFilter = true) {
    if (filter instanceof TableUserFilter) {
      let previousFilter = this.getFilter(filter.createKey());
      this.filterSupport.removeFilter(previousFilter, false);
    }

    let added = this.filterSupport.addFilter(filter, applyFilter);
    if (added && added.length) {
      this.trigger('filterAdded', {
        filter: added[0]
      });
    }
  }

  /**
   * @param filter The filter to remove.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   */
  removeFilter(filter: FilterOrFunction<TableRow>, applyFilter = true) {
    let removed = this.filterSupport.removeFilter(filter, applyFilter);
    if (removed && removed.length) {
      this.trigger('filterRemoved', {
        filter: removed[0]
      });
    }
  }

  removeFilterByKey(key: string, applyFilter = true) {
    this.removeFilter(this.getFilter(key), applyFilter);
  }

  getFilter(key: string): Filter<TableRow> {
    return arrays.find(this.filters, f => {
      if (!(f instanceof TableUserFilter)) {
        return false;
      }
      return objects.equals(f.createKey(), key);
    });
  }

  /**
   * @param filter The new filters.
   * @param applyFilter Whether to apply the filters after modifying the filter list or not. Default is true.
   * @see TableModel.filters
   */
  setFilters(filters: (FilterOrFunction<TableRow> | ObjectOrModel<TableUserFilter>)[], applyFilter = true) {
    this.resetUserFilter(false);
    let tableFilters = filters.map(filter => this._ensureFilter(filter));
    let result = this.filterSupport.setFilters(tableFilters, applyFilter);
    let filtersAdded = result.filtersAdded;
    let filtersRemoved = result.filtersRemoved;
    filtersAdded.forEach(filter => this.trigger('filterAdded', {
      filter: filter
    }));
    filtersRemoved.forEach(filter => this.trigger('filtersRemoved', {
      filter: filter
    }));
  }

  protected _ensureFilter<T extends Filter<TableRow>>(filter: TableUserFilterModel | FilterOrFunction<TableRow>): Filter<TableRow> {
    if (filter instanceof TableUserFilter || !filter['objectType']) {
      return filter as Filter<TableRow>;
    }

    let filterModel = filter as FullModelOf<TableUserFilter>;
    // @ts-expect-error
    if (filterModel.column) {
      // @ts-expect-error
      filterModel.column = this.columnById(filterModel.column as string);
    }
    filterModel.table = this;
    filterModel.session = this.session;
    return scout.create(filterModel);
  }

  filter() {
    this.filterSupport.filter();
  }

  protected _filter(options?: UpdateTableRowStructureOptions) {
    this._updateRowStructure($.extend({}, options, {filteredRows: true}));
    this._renderRowDelta();
    this._group();
    this.revealSelection();
  }

  protected _createFilterSupport(): FilterSupport<TableRow> {
    return new FilterSupport({
      widget: this,
      $container: () => this.$container,
      getElementsForFiltering: () => this.rows,
      createTextFilter: () => scout.create(TableTextUserFilter, {
        session: this.session,
        table: this
      }),
      updateTextFilterText: (filter: TableTextUserFilter, text) => {
        if (objects.equals(filter.text, text)) {
          return false;
        }
        filter.text = text;
        return true;
      }
    });
  }

  /** @see TableModel.textFilterEnabled */
  setTextFilterEnabled(textFilterEnabled: boolean) {
    this.setProperty('textFilterEnabled', textFilterEnabled);
  }

  isTextFilterFieldVisible(): boolean {
    return this.textFilterEnabled && !this.footerVisible;
  }

  protected _renderTextFilterEnabled() {
    this.filterSupport.renderFilterField();
  }

  protected _renderMultiSelect() {
    aria.multiselectable(this.$container, this.multiSelect || this.multiCheck ? true : null);
  }

  protected _renderMultiCheck() {
    aria.multiselectable(this.$container, this.multiSelect || this.multiCheck ? true : null);
  }

  updateFilteredElements(result: FilterResult<TableRow>, opts: UpdateFilteredElementsOptions) {
    if (this.filteredElementsDirty) {
      this._filter({
        filteredRows: true,
        applyFilters: false,
        filtersChanged: true
      });
      this.filteredElementsDirty = false;
    }
  }

  /**
   * Resizes the given column to the new size.
   *
   * @param column column to resize
   * @param width new column size
   */
  resizeColumn(column: Column<any>, width: number) {
    if (column.fixedWidth) {
      return;
    }
    width = Math.floor(width);
    column.setWidth(width);

    if (this._isDataRendered()) {
      this._renderResizeColumn(column, width);
    }

    this._triggerColumnResized(column);
  }

  protected _renderResizeColumn(column: Column<any>, width: number) {
    let visibleColumnIndex = this.visibleColumns().indexOf(column);
    if (visibleColumnIndex !== -1) {
      let colNum = visibleColumnIndex + 1;
      this.$cellsForColIndex(colNum, true)
        .css('min-width', width)
        .css('max-width', width);

      this._updateRealColumnWidth(column);
      this._updateRowWidth();
      this.$rows(true)
        .css('width', this.rowWidth);

      // If resized column contains cells with wrapped text, view port needs to be updated
      // Remove row height for non-rendered rows because it may have changed due to resizing (wrap text)
      this._updateRowHeights();
      this._renderFiller();
      this._renderViewport();
      this.updateScrollbars();
      this._renderEmptyData();
    }

    this._aggregateRows.forEach(aggregateRow => {
      if (aggregateRow.$row) {
        this._resizeAggregateCell(this.$cell(column, aggregateRow.$row));
      }
    });

    this.findDesktop().repositionTooltips();
  }

  /** @internal */
  _resizeAggregateCell($cell: JQuery) {
    // A resize of aggregate columns might also be necessary if the current resize column itself does not contain any content.
    // E.g. when having 3 columns: first and last have content, middle column is empty. While resizing the middle column,
    // it might happen that the overlapping content from the first and the last collide. Therefore, always update aggregate columns
    // closest to $cell.
    let range = this._getAggrCellRange($cell);
    this._updateAggrCell(range[0]);
    if (range.length > 1) {
      this._updateAggrCell(range[range.length - 1]);
    }
  }

  /**
   * @internal
   * Gets the aggregation cell range. this is the range from one aggregation cell with content to the next (or the table end).
   * @param $cell The cell to get the surrounding range
   * @returns All cells of the range to which the given cell belongs
   */
  _getAggrCellRange($cell: JQuery): JQuery[] {
    let $cells: JQuery[] = [],
      $row = $cell.parent(),
      visibleColumns = this.visibleColumns(),
      $start = $cell,
      direction: number;

    let hasContent = ($c: JQuery) => !$c.hasClass('empty');
    if (hasContent($cell)) {
      direction = $cell.hasClass('halign-right') ? -1 : 1; // do not use column.horizontalAlignment here because it might be different in the aggregation row (e.g. when the first column is a number column).
    } else {
      direction = 1; // after start has been found: always walk to the right
      let colIndex = visibleColumns.indexOf(this.columnFor$Cell($cell));
      for (let pos = colIndex - 1; pos >= 0; pos--) {
        $start = this.$cell(visibleColumns[pos], $row);
        if (hasContent($start)) {
          break; // next column with content reached
        }
      }
    }

    $cells.push($start);
    let startIndex = visibleColumns.indexOf(this.columnFor$Cell($start));
    for (let pos = startIndex + direction; pos >= 0 && pos < visibleColumns.length; pos += direction) {
      let $curCell = this.$cell(visibleColumns[pos], $row);
      $cells.push($curCell);
      if (hasContent($curCell)) {
        break; // next column with content reached
      }
    }
    return $cells;
  }

  /**
   * Updates the width and icon visibility of an aggregate cell in groupingStyle=title
   * @param $cell The aggregation cell that should be updated.
   */
  protected _updateAggrCell($cell: JQuery) {
    let $cellText = $cell.children('.text');
    if (!$cellText || !$cellText.length || $cell.hasClass('empty')) {
      return; // nothing to update (empty cell)
    }

    let $icon = $cell.children('.table-cell-icon').first();
    let cellWidth = this._getAggrCellWidth($cell, $icon);
    cellWidth = this._updateAggrIconVisibility($icon, $cellText, cellWidth);
    $cellText.cssMaxWidth(cellWidth);
  }

  protected _getWidthWithMarginCached($element: JQuery): number {
    if (!$element || !$element.length) {
      return 0;
    }

    let widthKey = 'widthWithMargin';
    let precomputed = $element.data(widthKey) as number;
    if (precomputed) {
      return precomputed;
    }

    let originallyVisible = $element.isVisible();
    if (!originallyVisible) {
      $element.setVisible(true); // set visible so that the width can be read
    }
    let width = graphics.size($element, {includeMargin: true}).width;
    if (!originallyVisible) {
      $element.setVisible(false); // restore original visibility
    }
    $element.data(widthKey, width); // cache width
    return width;
  }

  /**
   * Gets the computed space that is available to the content of the cell.
   *
   * @param $cell The aggregation cell for which the info should be computed
   * @param $icon The icon of the cell. May be null.
   * @returns The unused space available into flow direction of the cell til the end of the table or the next cell with content.
   */
  protected _getAggrCellWidth($cell: JQuery, $icon: JQuery): number {
    let cellSpaceForText = $cell.cssMaxWidth() - $cell.cssPaddingX();
    if ($icon && $icon.length && $icon.isVisible()) {
      cellSpaceForText -= this._getWidthWithMarginCached($icon);
    }

    let range = this._getAggrCellRange($cell);
    for (let i = 1; i < range.length; i++) {
      let $aggrCell = range[i];
      let cellIsEmpty = $aggrCell.hasClass('empty');
      if (cellIsEmpty) {
        // consume full space available
        cellSpaceForText += $aggrCell.cssMaxWidth();
      } else {
        // first neighbour with content found
        let growsTowardsEachOthers = $cell.hasClass('halign-right') !== $aggrCell.hasClass('halign-right');
        if (growsTowardsEachOthers) {
          // cells grow towards each other: use the free space of the cell
          let cellWidth = $aggrCell.cssMaxWidth();
          let cellConsumedSpace = $aggrCell.children().toArray()
            .map(item => $(item))
            .filter($item => $item.isVisible())
            .map($item => graphics.size($item, {includeMargin: true}).width)
            .reduce((a, b) => a + b, 0) + $cell.cssPaddingLeft();
          let cellAvailSpace = cellWidth - cellConsumedSpace;
          if ($aggrCell.hasClass('halign-center')) {
            // if the cell uses center-alignment, the available space is consumed by left and right side
            // -> only half of the space is available as overflow
            cellAvailSpace /= 2;
          }
          cellSpaceForText += cellAvailSpace;
        }
        break;
      }
    }

    return Math.max(0, cellSpaceForText);
  }

  /**
   * Sets the aggregation cell icon visible or not depending on the space available.
   * If the available space is big enough to hold the content of the cell text and the icon, the icon will become visible. Otherwise, the icon will be hidden.
   * This way no ellipsis will be shown as long as there is enough space for the text only.
   *
   * @param $icon The icon for which the visibility should be changed
   * @param $cellText The element holding the text of the cell. Required to compute if there is still enough space for text and icon or not
   * @param spaceAvailableForText The newly computed space available for the cell text.
   * @returns The new space available for the text. If the visibility of the icon changed, more or less space might become available.
   */
  protected _updateAggrIconVisibility($icon: JQuery, $cellText: JQuery, spaceAvailableForText: number): number {
    if (!$icon || !$icon.length) {
      return spaceAvailableForText;
    }
    let isIconVisible = $icon.isVisible();
    let iconWidth = this._getWidthWithMarginCached($icon);
    let cellTextWidth = this._getWidthWithMarginCached($cellText);
    let spaceForIcon = spaceAvailableForText - cellTextWidth + (isIconVisible ? iconWidth : 0);
    let newIsIconVisible = spaceForIcon > iconWidth;
    if (newIsIconVisible === isIconVisible) {
      return spaceAvailableForText;
    }

    $icon.setVisible(newIsIconVisible);
    return spaceAvailableForText + (newIsIconVisible ? -iconWidth : iconWidth);
  }

  moveColumn(column: Column<any>, visibleOldPos: number, visibleNewPos: number, dragged?: boolean) {
    // If there are fixed columns, don't allow moving the column onto the other side of the fixed columns
    visibleNewPos = this._considerFixedPositionColumns(visibleOldPos, visibleNewPos);

    // Translate position of 'visible columns' array to position in 'all columns' array
    let visibleColumns = this.visibleColumns();
    let newColumn = visibleColumns[visibleNewPos];
    let newPos = this.columns.indexOf(newColumn);

    arrays.remove(this.columns, column);
    arrays.insert(this.columns, column, newPos);

    visibleColumns = this.visibleColumns();
    visibleNewPos = visibleColumns.indexOf(column); // we must re-evaluate visible columns

    this._calculateTableNodeColumn();
    this._triggerColumnMoved(column, visibleOldPos, visibleNewPos, dragged);

    // move aggregated rows
    this._aggregateRows.forEach(aggregateRow => arrays.move(aggregateRow.contents, visibleOldPos, visibleNewPos));

    // move cells
    if (this._isDataRendered()) {
      this._rerenderViewport();
    }
  }

  /**
   * Ensures the given newPos does not pass a fixed column boundary (necessary when moving columns)
   */
  protected _considerFixedPositionColumns(visibleOldPos: number, visibleNewPos: number): number {
    let fixedColumnIndex = -1;
    if (visibleNewPos > visibleOldPos) {
      // move to right
      fixedColumnIndex = arrays.findIndexFrom(this.visibleColumns(), visibleOldPos, col => col.fixedPosition);
      if (fixedColumnIndex > -1) {
        visibleNewPos = Math.min(visibleNewPos, fixedColumnIndex - 1);
      }
    } else {
      // move to left
      fixedColumnIndex = arrays.findIndexFromReverse(this.visibleColumns(), visibleOldPos, col => col.fixedPosition);
      if (fixedColumnIndex > -1) {
        visibleNewPos = Math.max(visibleNewPos, fixedColumnIndex + 1);
      }
    }
    return visibleNewPos;
  }

  protected _renderColumnOrderChanges(oldColumnOrder: Column<any>[]) {
    let $cell: HTMLElement, that = this;

    if (this.header) {
      this.header.onOrderChanged(oldColumnOrder);
    }

    // move cells
    this.$rows(true).each(function() {
      let $row = $(this);
      let $orderedCells = $();
      let $cells = $row.children();
      for (let i = 0; i < that.columns.length; i++) {
        let column = that.columns[i];

        // Find $cell for given column
        for (let j = 0; j < oldColumnOrder.length; j++) {
          if (oldColumnOrder[j] === column) {
            $cell = $cells[j];
            break;
          }
        }
        $orderedCells.push($cell);
      }
      $row.prepend($orderedCells);
    });
  }

  protected _triggerRowsInserted(rows: TableRow[]) {
    this.trigger('rowsInserted', {
      rows: rows
    });
  }

  protected _triggerRowsDeleted(rows: TableRow[]) {
    this.trigger('rowsDeleted', {
      rows: rows
    });
  }

  protected _triggerRowsUpdated(rows: TableRow[]) {
    this.trigger('rowsUpdated', {
      rows: rows
    });
  }

  protected _triggerAllRowsDeleted(rows: TableRow[]) {
    this.trigger('allRowsDeleted', {
      rows: rows
    });
  }

  protected _triggerRowsSelected(debounce?: boolean) {
    this.trigger('rowsSelected', {
      debounce: debounce
    });
  }

  protected _triggerRowsChecked(rows: TableRow[]) {
    this.trigger('rowsChecked', {
      rows: rows
    });
  }

  protected _triggerRowsExpanded(rows: TableRow[]) {
    this.trigger('rowsExpanded', {
      rows: rows
    });
  }

  protected _triggerFilter() {
    this.trigger('filter');
  }

  protected _triggerFilterReset() {
    this.trigger('filterReset');
  }

  protected _triggerAppLinkAction(column: Column<any>, row: TableRow, ref: string, $appLink: JQuery) {
    this.trigger('appLinkAction', {
      column: column,
      row: row,
      ref: ref,
      $appLink: $appLink
    });
  }

  protected _triggerReload(reloadReason?: TableReloadReason) {
    this.trigger('reload', {
      reloadReason: reloadReason
    });
  }

  protected _triggerClipboardExport() {
    let event = this.trigger('clipboardExport');
    if (!event.defaultPrevented) {
      this._exportToClipboard();
    }
  }

  protected _triggerRowOrderChanged(row?: TableRow, animating?: boolean) {
    let event = {
      row: row,
      animating: animating
    };
    this.trigger('rowOrderChanged', event);
  }

  protected _triggerColumnResized(column: Column<any>) {
    let event = {
      column: column
    };
    this.trigger('columnResized', event);
  }

  protected _triggerColumnResizedToFit(column: Column<any>) {
    let event = {
      column: column
    };
    this.trigger('columnResizedToFit', event);
  }

  protected _triggerColumnMoved(column: Column<any>, oldPos: number, newPos: number, dragged?: boolean) {
    let event = {
      column: column,
      oldPos: oldPos,
      newPos: newPos,
      dragged: dragged
    };
    this.trigger('columnMoved', event);
  }

  protected _triggerAggregationFunctionChanged(column: NumberColumn) {
    let event = {
      column: column
    };
    this.trigger('aggregationFunctionChanged', event);
  }

  /** @see TableModel.headerVisible */
  setHeaderVisible(visible: boolean) {
    this.setProperty('headerVisible', visible);
  }

  protected _renderHeaderVisible() {
    this._renderTableHeader();
  }

  /** @see TableModel.headerEnabled */
  setHeaderEnabled(headerEnabled: boolean) {
    this.setProperty('headerEnabled', headerEnabled);
  }

  protected _renderHeaderEnabled() {
    // Rebuild the table header when this property changes
    this._removeTableHeader();
    this._renderTableHeader();
  }

  /** @see TableModel.headerMenusEnabled */
  setHeaderMenusEnabled(headerMenusEnabled: boolean) {
    this.setProperty('headerMenusEnabled', headerMenusEnabled);
    if (this.header) {
      this.header.setHeaderMenusEnabled(this.headerMenusEnabled);
    }
  }

  hasPermanentHeadOrTailSortColumns(): boolean {
    return this._permanentHeadSortColumns.length !== 0 || this._permanentTailSortColumns.length !== 0;
  }

  protected _setHeadAndTailSortColumns() {
    // find all sort columns (head and tail sort columns should always be included)
    let sortColumns = this.columns.filter(c => c.sortIndex >= 0);
    sortColumns.sort((a, b) => a.sortIndex - b.sortIndex);

    this._permanentHeadSortColumns = [];
    this._permanentTailSortColumns = [];

    sortColumns.forEach(c => {
      if (c.initialAlwaysIncludeSortAtBegin) {
        this._permanentHeadSortColumns.push(c);
      } else if (c.initialAlwaysIncludeSortAtEnd) {
        this._permanentTailSortColumns.push(c);
      }
    });
  }

  /** @see TableModel.tileMode */
  setTileMode(tileMode: boolean) {
    this.setProperty('tileMode', tileMode);
  }

  protected _setTileMode(tileMode: boolean) {
    if (tileMode) {
      this._ensureMediator();
      if (!this.tileTableHeader) {
        this._setTileTableHeader(this._createTileTableHeader());
      }
      this.tableTileGridMediator.loadTiles();
      this.tableTileGridMediator.activate();
    }
    this._setProperty('tileMode', tileMode);

    if (!tileMode && this.tableTileGridMediator) {
      this.tableTileGridMediator.deactivate();
    }
  }

  protected _ensureMediator() {
    if (!this.tableTileGridMediator) {
      this.tableTileGridMediator = scout.create(TableTileGridMediator, {
        parent: this,
        gridColumnCount: 6
      });
    }
  }

  protected _renderTileMode() {
    if (this.tableTileGridMediator) {
      this.tableTileGridMediator.renderTileMode();
    }
  }

  createTiles(rows: TableRow[]): Tile[] {
    return rows.map(row => {
      let tile = this.createTileForRow(row);
      this._adaptTile(tile);
      tile.rowId = row.id;
      return tile;
    });
  }

  protected _adaptTile(tile: Tile) {
    tile.setGridDataHints({
      weightX: 0
    });
  }

  createTileForRow(row: TableRow): Tile {
    if (this.tileProducer) {
      return this.tileProducer(row);
    }
    throw new Error('Cannot create a tile without a producer.');
  }

  /** @see TableModel.tileProducer */
  setTileProducer(tileProducer: (row: TableRow) => Tile) {
    this.setProperty('tileProducer', tileProducer);
  }

  protected _setTileTableHeader(tileTableHeader: TileTableHeaderBox) {
    if (tileTableHeader) {
      tileTableHeader.addCssClass('tile-table-header');
    }
    this._setProperty('tileTableHeader', tileTableHeader);
  }

  protected _createTileTableHeader(): TileTableHeaderBox {
    return scout.create(TileTableHeaderBox, {
      parent: this
    });
  }

  /** @see TableModel.rowIconVisible */
  setRowIconVisible(rowIconVisible: boolean) {
    this.setProperty('rowIconVisible', rowIconVisible);
  }

  protected _setRowIconVisible(rowIconVisible: boolean) {
    this._setProperty('rowIconVisible', rowIconVisible);
    let column = this.rowIconColumn;
    if (this.rowIconVisible && !column) {
      this._insertRowIconColumn();
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    } else if (!this.rowIconVisible && column) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.rowIconColumn = null;
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    }
  }

  protected _renderRowIconVisible() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this.redraw();
  }

  protected _renderRowIconColumnWidth() {
    if (!this.rowIconVisible) {
      return;
    }
    this._renderRowIconVisible();
  }

  /** @see TableModel.rowIconColumnWidth */
  setRowIconColumnWidth(width: number) {
    this.setProperty('rowIconColumnWidth', width);
  }

  protected _setRowIconColumnWidth(width: number) {
    this._setProperty('rowIconColumnWidth', width);
    let column = this.rowIconColumn;
    if (column) {
      column.width = width;
    }
  }

  protected _setSelectedRows(selectedRows: TableRow[] | string[]) {
    if (typeof selectedRows[0] === 'string') {
      selectedRows = this.rowsByIds(selectedRows as string[]);
    }
    this._setProperty('selectedRows', selectedRows);
  }

  /** @see TableModel.menus */
  setMenus(menus: ObjectOrChildModel<Menu>[]) {
    this.setProperty('menus', menus);
  }

  protected _setMenus(menus: Menu[]) {
    this.updateKeyStrokes(menus, this.menus);
    this.menus.forEach(menu => menu.off('propertyChange:inheritAccessibility', this._menuInheritAccessibilityChangeHandler));
    this._setProperty('menus', menus);
    this._updateMenuBar();
    this._updateMenusEnabled();
    this.menus.forEach(menu => menu.on('propertyChange:inheritAccessibility', this._menuInheritAccessibilityChangeHandler));

    if (this.header) {
      this.header.updateMenuBar();
    }
  }

  setMenuBarVisible(visible: boolean) {
    this.setProperty('menuBarVisible', visible);
  }

  protected _setMenuBarVisible(visible: boolean) {
    this._setProperty('menuBarVisible', visible);
    this._updateMenuBar();
  }

  protected _renderMenuBarVisible() {
    if (this.menuBarVisible) {
      this.menuBar.render();
      this._refreshMenuBarPosition();
    } else {
      this.menuBar.remove();
    }
    this._updateMenuBar();
    this.invalidateLayoutTree();
  }

  /** @internal */
  _refreshMenuBarPosition() {
    this.$container.removeClass('menubar-top');
    this.$container.removeClass('menubar-bottom');
    if (this.menuBarVisible && this.menuBar.rendered) {
      if (this.menuBar.position === MenuBar.Position.TOP) {
        this.menuBar.$container.prependTo(this.$container);
        this.$container.addClass('menubar-top');
      } else {
        this.menuBar.$container.appendTo(this.$container);
        this.$container.addClass('menubar-bottom');
      }
    }
  }

  protected _createMenuBar(): MenuBar {
    return scout.create(MenuBar, {
      parent: this,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new MenuItemsOrder(this.session, 'Table', this.defaultMenuTypes),
      menuFilter: this._filterMenusHandler,
      cssClass: 'bounded'
    });
  }

  protected _updateMenuBar() {
    if (this.menuBarVisible) {
      // Do not update menuBar while it is invisible, the menus may now be managed by another widget.
      // -> this makes sure the parent is not accidentally set to the table, the other widget should remain responsible
      let notAllowedTypes = ['Header'];
      let menuItems = this._filterMenus(this.menus, MenuDestinations.MENU_BAR, false, true, notAllowedTypes);
      menuItems = this.staticMenus.concat(menuItems);
      this.menuBar.setMenuItems(menuItems);
    } else {
      this.menuBar.setMenuItems([]);
    }
    this._refreshMenuBarClasses();
    if (this.contextMenu) {
      let contextMenuItems = this._filterMenus(this.menus, MenuDestinations.CONTEXT_MENU, true, false, ['Header']);
      this.contextMenu.updateMenuItems(contextMenuItems);
    }
  }

  protected _refreshMenuBarClasses() {
    if (!this.$container) {
      return;
    }
    this.$container.toggleClass('has-menubar', this.menuBar && this.menuBar.visible);
    this.$container.toggleClass('menubar-top', this.menuBar && this.menuBar.position === MenuBar.Position.TOP);
    this.$container.toggleClass('menubar-bottom', this.menuBar && this.menuBar.position !== MenuBar.Position.TOP);
  }

  /**
   * Updates the enabled state of single- and multi-selection menus based on the enabled state of the selected row(s).
   *
   * To make a menu independent of the enabled state of its ancestors (including the row), {@link Widget.inheritAccessibility} can be set to false.
   * To make a menu only independent of the enabled state of the row, but it should still consider the enabled state of the table and its ancestors, setting the dependent dimension can be prevented as follows:
   * ```
   * menu.on('propertyChange:enabled-dependent', event => event.preventDefault());
   * ```
   */
  protected _updateMenusEnabled() {
    const menus = this._filterMenusForContextMenu();
    let selectedRowsEnabled = this.selectedRows.every(row => row.enabled);
    for (const menu of menus) {
      menu.setPropertyDimension('enabled', 'dependent', !menu.inheritAccessibility || selectedRowsEnabled);
    }
  }

  protected _setKeyStrokes(keyStrokes: Action[]) {
    this.updateKeyStrokes(keyStrokes, this.keyStrokes);
    this._setProperty('keyStrokes', keyStrokes);
  }

  setTableStatus(status: StatusOrModel) {
    this.setProperty('tableStatus', status);
  }

  /**
   * If the rows of this table are limited, the tableStatus is set to the corresponding message.
   * {@link rows.length} and {@link estimatedRowCount} of this table are taken into account to create the status message.
   * @param limitedResult Specifies if the rows of this table are limited.
   */
  setLimitedResultTableStatus(limitedResult: boolean) {
    if (!limitedResult) {
      this.setTableStatus(null);
      return;
    }

    const estimatedRowCount = this.estimatedRowCount;
    const numRows = this.rows.length;
    const decimalFormat = this.session.locale.decimalFormat;
    const showingRowCountText = decimalFormat.format(numRows);
    let message: string;
    if (Device.get().type === Device.Type.MOBILE) {
      if (estimatedRowCount > 0) {
        const estimatedRowCountText = decimalFormat.format(estimatedRowCount);
        message = this.session.text('MaxOutlineRowWarningMobileWithEstimatedRowCount', showingRowCountText, estimatedRowCountText);
      } else {
        message = this.session.text('MaxOutlineRowWarningMobile', showingRowCountText);
      }
    } else {
      if (estimatedRowCount > 0) {
        const estimatedRowCountText = decimalFormat.format(estimatedRowCount);
        message = this.session.text('MaxOutlineRowWarningWithEstimatedRowCount', showingRowCountText, estimatedRowCountText);
      } else {
        message = this.session.text('MaxOutlineRowWarning', showingRowCountText);
      }
    }
    this.setTableStatus(LimitedResultTableStatus.info(message));
  }

  protected _setTableStatus(status: StatusOrModel) {
    status = Status.ensure(status);
    this._setProperty('tableStatus', status);
  }

  /** @see TableModel.tableStatusVisible */
  setTableStatusVisible(visible: boolean) {
    this.setProperty('tableStatusVisible', visible);
    this._updateFooterVisibility();
  }

  protected _updateFooterVisibility() {
    this.setFooterVisible(this.tableStatusVisible || this._hasVisibleTableControls());
  }

  /** @see TableModel.hierarchicalStyle */
  setHierarchicalStyle(style: TableHierarchicalStyle) {
    this.setProperty('hierarchicalStyle', style);
  }

  protected _renderHierarchicalStyle() {
    this.$container.toggleClass('structured', Table.HierarchicalStyle.STRUCTURED === this.hierarchicalStyle);
  }

  /** @see TableModel.footerVisible */
  setFooterVisible(visible: boolean) {
    this._setProperty('footerVisible', visible);
    if (visible && !this.footer) {
      this.footer = this._createFooter();
    }

    // relink table controls to new footer
    this.tableControls.forEach(control => {
      control.tableFooter = this.footer;
    });

    if (this.rendered) {
      this._renderFooterVisible();
    }
    if (!visible && this.footer) {
      this.footer.destroy();
      this.footer = null;
    }
  }

  /**
   * Renders the background effect of every column, if column.backgroundEffect is set
   */
  protected _renderBackgroundEffect() {
    this.columns.forEach((column: NumberColumn) => {
      if (!column.backgroundEffect) {
        return;
      }
      column._renderBackgroundEffect();
    });
  }

  protected _renderRowChecked(row: TableRow) {
    if (!this.checkable) {
      return;
    }
    if (!row.$row) {
      return;
    }
    let $styleElem: JQuery;
    if (this.checkableStyle === Table.CheckableStyle.TABLE_ROW) {
      $styleElem = row.$row;
    } else {
      if (!this.checkableColumn) {
        throw new Error('checkableColumn not set');
      }
      $styleElem = this.checkableColumn.$checkBox(row.$row);
      aria.checked(row.$row, row.checked); // also set the row to aria checked
    }
    $styleElem.toggleClass('checked', row.checked);
    aria.checked($styleElem, row.checked);
  }

  /** @see TableModel.checkable */
  setCheckable(checkable: boolean) {
    this.setProperty('checkable', checkable);
  }

  protected _setCheckable(checkable: boolean) {
    this._setProperty('checkable', checkable);
    this._updateCheckableColumn();
  }

  protected _updateCheckableColumn() {
    let column = this.checkableColumn;
    let showCheckBoxes = this.checkable && scout.isOneOf(this.checkableStyle, Table.CheckableStyle.CHECKBOX, Table.CheckableStyle.CHECKBOX_TABLE_ROW);
    if (showCheckBoxes && !column) {
      this._insertBooleanColumn();
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    } else if (!showCheckBoxes && column && column.guiOnly) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.checkableColumn = null;
      this._calculateTableNodeColumn();
      this.trigger('columnStructureChanged');
    }
  }

  protected _renderCheckable() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this.redraw();
  }

  setCheckableStyle(checkableStyle: TableCheckableStyle) {
    this.setProperty('checkableStyle', checkableStyle);
  }

  protected _setCheckableStyle(checkableStyle: TableCheckableStyle) {
    this._setProperty('checkableStyle', checkableStyle);
    this._updateCheckableColumn();
  }

  protected _renderCheckableStyle() {
    this.$container.toggleClass('checkable', scout.isOneOf(this.checkableStyle, Table.CheckableStyle.TABLE_ROW, Table.CheckableStyle.CHECKBOX_TABLE_ROW));
    this.$container.toggleClass('table-row-check', this.checkableStyle === Table.CheckableStyle.TABLE_ROW);
    if (this._isDataRendered()) {
      this._updateRowWidth();
      this.redraw();
    }
  }

  /** @see TableModel.compact */
  setCompact(compact: boolean) {
    this.setProperty('compact', compact);
  }

  protected _setCompact(compact: boolean) {
    this._setProperty('compact', compact);
    this._updateCompactColumn();
    if (this.compactHandler) {
      this.compactHandler.handle(this.compact);
    }
  }

  protected _updateCompactColumn(): boolean {
    let column = this.compactColumn;
    if (this.compact && !column) {
      this._insertCompactColumn();
      return true;
    }
    if (!this.compact && column) {
      column.destroy();
      arrays.remove(this.columns, column);
      this.compactColumn = null;
      return true;
    }
    return false;
  }

  protected _insertCompactColumn() {
    let column = scout.create(CompactColumn, {
      session: this.session,
      table: this,
      guiOnly: true,
      headerMenuEnabled: false
    });
    this.columns.push(column); // Insert after the other ui columns
    this.compactColumn = column;
  }

  protected _renderCompact() {
    this.columnLayoutDirty = true;
    this._updateRowWidth();
    this.redraw();
  }

  setGroupingStyle(groupingStyle: TableGroupingStyle) {
    this.setProperty('groupingStyle', groupingStyle);
  }

  protected _setGroupingStyle(groupingStyle: TableGroupingStyle) {
    this._setProperty('groupingStyle', groupingStyle);
    this._group();
  }

  protected _renderGroupingStyle() {
    this._rerenderViewport();
  }

  redraw() {
    if (!this._isDataRendered()) {
      return;
    }
    this._rerenderHeaderColumns();
    this._rerenderViewport();
    this.invalidateLayoutTree();
  }

  protected _rerenderHeaderColumns() {
    if (this.header) {
      this.header.rerenderColumns();
      this.invalidateLayoutTree();
    }
  }

  /** @internal */
  _renderTableHeader() {
    if (this.tileMode) {
      return;
    }
    let changed = false;
    if (this.headerVisible && !this.header) {
      this.header = this._createHeader();
      this.header.render();
      this._renderEmptyData();
      changed = true;
    } else if (!this.headerVisible && this.header) {
      this._removeTableHeader();
      this._removeEmptyData();
      changed = true;
    }
    this.$container.toggleClass('header-invisible', !this.header);
    if (changed) {
      this.invalidateLayoutTree();
    }
  }

  protected _removeTableHeader() {
    if (this.header) {
      this.header.destroy();
      this.header = null;
    }
  }

  /**
   * @param width optional width of emptyData, if omitted the width is set to the header's scrollWidth.
   */
  protected _renderEmptyData() {
    if (!this.header || this.visibleRows.length > 0) {
      return;
    }
    if (!this.$emptyData) {
      this.$emptyData = this.$data.appendDiv().html('&nbsp;');
    }
    this.$emptyData
      .css('min-width', this.rowWidth)
      .css('max-width', this.rowWidth);
    this.updateScrollbars();
  }

  protected _removeEmptyData() {
    if (this.header && this.visibleRows.length === 0) {
      return;
    }
    if (this.$emptyData) {
      this.$emptyData.remove();
      this.$emptyData = null;
      this.updateScrollbars();
    }
  }

  protected _renderFooterVisible() {
    if (!this.footer) {
      return;
    }
    if (this.footerVisible) {
      this._renderFooter();
    } else {
      this._removeFooter();
    }
    this.invalidateLayoutTree();

    this.filterSupport.renderFilterField();
  }

  protected _renderFooter() {
    if (this.footer.rendered) {
      return;
    }

    this.footer.render();
  }

  protected _removeFooter() {
    if (!this.footer.rendered) {
      return;
    }
    this.footer.remove();
  }

  protected override _renderEnabled() {
    super._renderEnabled();

    this._installOrUninstallDragAndDropHandler();
    let enabled = this.enabledComputed;
    if (!this.tileMode) {
      this.$data.setEnabled(enabled);
    }

    this.$container.setTabbableOrFocusable(enabled);
  }

  protected override _renderDisabledStyle() {
    super._renderDisabledStyle();
    this._renderDisabledStyleInternal(this.$data);
  }

  /** @see TableModel.autoResizeColumns */
  setAutoResizeColumns(autoResizeColumns: boolean) {
    this.setProperty('autoResizeColumns', autoResizeColumns);
  }

  protected _renderAutoResizeColumns() {
    if (!this.autoResizeColumns && Device.get().hasTableCellZoomBug()) {
      // Clear real width so that row width is updated correctly by the table layout if autoResizeColumns is disabled on the fly
      this.visibleColumns().forEach((column, colIndex) => {
        column._realWidth = null;
      });
    }
    this.columnLayoutDirty = true;
    this.invalidateLayoutTree();
  }

  /** @see TableModel.multilineText */
  setMultilineText(multilineText: boolean) {
    this.setProperty('multilineText', multilineText);
  }

  protected _renderMultilineText() {
    this._markAutoOptimizeWidthColumnsAsDirty();
    this.redraw();
  }

  /** @see TableModel.dropType */
  setDropType(dropType: DropType) {
    this.setProperty('dropType', dropType);
  }

  /** @see TableModel.maxRowCount */
  setMaxRowCount(maxRowCount: number) {
    this.setProperty('maxRowCount', maxRowCount);
  }

  /** @see TableModel.estimatedRowCount */
  setEstimatedRowCount(estimatedRowCount: number) {
    this.setProperty('estimatedRowCount', estimatedRowCount);
  }

  protected _renderDropType() {
    this._installOrUninstallDragAndDropHandler();
  }

  /** @see TableModel.dropMaximumSize */
  setDropMaximumSize(dropMaximumSize: number) {
    this.setProperty('dropMaximumSize', dropMaximumSize);
  }

  protected _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        doInstall: () => this.dropType && this.enabledComputed,
        selector: '.table-data,.table-row',
        onDrop: event => this.trigger('drop', event),
        dropType: () => this.dropType,
        additionalDropProperties: event => {
          let $target = $(event.currentTarget);
          let properties = {
            rowId: ''
          };
          if ($target.hasClass('table-row')) {
            let row = $target.data('row') as TableRow;
            properties.rowId = row.id;
          }
          return properties;
        }
      });
  }

  /**
   * This listener is used to invalidate table layout when an image icon has been loaded (which happens async in the browser).
   */
  protected _installImageListeners() {
    this._imageLoadListener = this._onImageLoadOrError.bind(this);
    // Image events don't bubble -> use capture phase instead
    this.$data[0].addEventListener('load', this._imageLoadListener, true);
    this.$data[0].addEventListener('error', this._imageLoadListener, true);
  }

  protected _uninstallImageListeners() {
    this.$data[0].removeEventListener('load', this._imageLoadListener, true);
    this.$data[0].removeEventListener('error', this._imageLoadListener, true);
  }

  /**
   * Calculates the optimal view range size (number of rows to be rendered).
   * It uses the default row height to estimate how many rows fit in the view port.
   * The view range size is this value * 2.
   */
  calculateViewRangeSize(): number {
    // Make sure row height is up-to-date (row height may be different after zooming)
    this._updateRowHeight();

    if (this.rowHeight === 0) {
      throw new Error('Cannot calculate view range with rowHeight = 0');
    }
    return Math.ceil(this.$data.outerHeight() / this.rowHeight) * 2;
  }

  setViewRangeSize(viewRangeSize: number) {
    if (this.viewRangeSize === viewRangeSize) {
      return;
    }
    this._setProperty('viewRangeSize', viewRangeSize);
    if (this._isDataRendered()) {
      this._renderViewport();
    }
  }

  protected _calculateCurrentViewRange(): Range {
    let rowIndex: number,
      scrollTop = this.$data[0].scrollTop,
      maxScrollTop = this.$data[0].scrollHeight - this.$data[0].clientHeight;

    if (maxScrollTop === 0) {
      // no scrollbars visible
      rowIndex = 0;
    } else {
      rowIndex = this._rowIndexAtScrollTop(scrollTop);
    }

    return this._calculateViewRangeForRowIndex(rowIndex);
  }

  /**
   * Returns the index of the row which is at position scrollTop.
   * @internal
   */
  _rowIndexAtScrollTop(scrollTop: number): number {
    let height = 0,
      index = -1;
    this.visibleRows.some((row, i) => {
      height += this._heightForRow(row);
      if (scrollTop < height) {
        index = i;
        return true;
      }
      return false;
    });
    return index;
  }

  protected _heightForRow(row: TableRow): number {
    let height = 0;
    let aggregateRow = row.aggregateRowBefore;
    if (this.groupingStyle === Table.GroupingStyle.BOTTOM) {
      aggregateRow = row.aggregateRowAfter;
    }

    if (row.height) {
      height = row.height;
    } else {
      height = this.rowHeight;
    }

    // Add height of aggregate row as well
    if (aggregateRow) {
      if (aggregateRow.height) {
        height += aggregateRow.height;
      } else {
        height += this.aggregateRowHeight;
      }
    }

    return height;
  }

  protected _measureRowHeight($row: JQuery): number {
    return graphics.size($row, {includeMargin: true, exact: true}).height;
  }

  /**
   * Returns a range of size this.viewRangeSize. Start of range is rowIndex - viewRangeSize / 4.
   * -> 1/4 of the rows are before the viewport 2/4 in the viewport 1/4 after the viewport,
   * assuming viewRangeSize is 2*number of possible rows in the viewport (see calculateViewRangeSize).
   */
  protected _calculateViewRangeForRowIndex(rowIndex: number): Range {
    // regular / non-virtual scrolling? -> all rows are already rendered in the DOM
    if (!this.virtual) {
      return new Range(0, this.visibleRows.length);
    }

    let viewRange = new Range(),
      quarterRange = Math.floor(this.viewRangeSize / 4);

    viewRange.from = Math.max(rowIndex - quarterRange, 0);
    viewRange.to = Math.min(viewRange.from + this.viewRangeSize, this.visibleRows.length);

    // Try to use the whole viewRangeSize (extend from if necessary)
    let diff = this.viewRangeSize - viewRange.size();
    if (diff > 0) {
      viewRange.from = Math.max(viewRange.to - this.viewRangeSize, 0);
    }
    return viewRange;
  }

  /**
   * Calculates and renders the rows which should be visible in the current viewport based on scroll top.
   * @internal
   */
  _renderViewport() {
    if (!this.isAttachedAndRendered()) {
      // if table is not attached the correct viewPort can not be evaluated. Mark for render after attach.
      this._renderViewPortAfterAttach = true;
      return;
    }
    if (this._renderViewportBlocked) {
      return;
    }
    if (this.visibleColumns().length === 0) {
      return;
    }
    if (!this.$container.isEveryParentVisible()) {
      // If the table is invisible, the height of the rows cannot be determined.
      // In that case, the table won't be layouted either -> as soon as it will be layouted, renderViewport will be called again
      this.invalidateLayoutTree();
      return;
    }
    let viewRange = this._calculateCurrentViewRange();
    this._renderViewRange(viewRange);
    this._renderLastRowAtBottomMarker();
    this._renderNoRowsSelectedMarker(); // Necessary to call it here if there are no rows at all
  }

  protected _rerenderViewport() {
    if (!this.isAttachedAndRendered()) {
      // if table is not attached the correct viewPort can not be evaluated. Mark for rerender after attach.
      this._rerenderViewPortAfterAttach = true;
      return;
    }
    this._removeRows();
    this._removeAggregateRows();
    this._renderFiller();
    this._renderViewport();
  }

  protected _renderViewRangeForRowIndex(rowIndex: number) {
    let viewRange = this._calculateViewRangeForRowIndex(rowIndex);
    this._renderViewRange(viewRange);
  }

  /**
   * Renders the rows visible in the viewport and removes the other rows
   */
  protected _renderViewRange(viewRange: Range) {
    if (viewRange.from === this.viewRangeRendered.from && viewRange.to === this.viewRangeRendered.to && !this.viewRangeDirty) {
      // Range already rendered -> do nothing
      return;
    }
    this._removeRangeMarkers();
    let rangesToRender = viewRange.subtract(this.viewRangeRendered);
    let rangesToRemove = this.viewRangeRendered.subtract(viewRange);
    rangesToRemove.forEach(range => this._removeRowsInRange(range));
    rangesToRender.forEach(range => this._renderRowsInRange(range));

    // check if at least last and first row in range got correctly rendered
    if (this.viewRangeRendered.size() > 0) {
      let rows = this.visibleRows;
      let firstRow = rows[this.viewRangeRendered.from];
      let lastRow = rows[this.viewRangeRendered.to - 1];
      if (!firstRow.$row || !lastRow.$row) {
        throw new Error('Rows not rendered as expected. ' + this.viewRangeRendered +
          '. First: ' + graphics.debugOutput(firstRow.$row) +
          '. Last: ' + graphics.debugOutput(lastRow.$row) +
          '. Length: visibleRows=' + this.visibleRows.length + ' rows=' + this.rows.length);
      }
    }

    this._renderRangeMarkers();
    this._renderAggregateRows();
    this._renderFiller();
    this._renderEmptyData();
    this._renderBackgroundEffect();
    this._renderSelection();
    this.viewRangeDirty = false;
  }

  protected _renderLastRowAtBottomMarker() {
    if (this.$rows().is(':animated')) {
      // Don't change state if rows are animated (e.g. by filtering) because the last row may temporarily be at the end but will be moved up by animation.
      // State will be updated before the animation runs.
      return;
    }
    let lastVisibleRow = this._lastVisibleRow();
    if (lastVisibleRow && lastVisibleRow.$row) {
      // Use ceil because position may be fractional (offsetBounds uses ceil for the height only)
      this.$data.toggleClass('last-row-at-bottom', Math.ceil(graphics.offsetBounds(lastVisibleRow.$row).bottom()) >= graphics.offsetBounds(this.$data).bottom());
    } else {
      this.$data.removeClass('last-row-at-bottom');
    }
  }

  protected _lastVisibleRow(): TableRow {
    return this.visibleRows[this.visibleRows.length - 1];
  }

  protected _removeRangeMarkers() {
    this._modifyRangeMarkers('removeClass');
  }

  protected _renderRangeMarkers() {
    this._modifyRangeMarkers('addClass');
  }

  protected _modifyRangeMarkers(funcName: string) {
    if (this.viewRangeRendered.size() === 0) {
      return;
    }
    let visibleRows = this.visibleRows;
    modifyRangeMarker(visibleRows[this.viewRangeRendered.from], 'first');
    modifyRangeMarker(visibleRows[this.viewRangeRendered.to - 1], 'last');

    function modifyRangeMarker(row: TableRow, cssClass: string) {
      if (row && row.$row) {
        row.$row[funcName](cssClass);
      }
    }
  }

  /**
   * Renders the view range that contains the given row.<br>
   * Does nothing if the row is already rendered or not visible (e.g. due to filtering).
   */
  ensureRowRendered(row: TableRow) {
    if (row.$row) {
      return;
    }
    let rowIndex = this.visibleRows.indexOf(row);
    if (rowIndex < 0) {
      return;
    }
    this._renderViewRangeForRowIndex(rowIndex);
  }

  protected _renderFiller() {
    if (!this.$fillBefore) {
      this.$fillBefore = this.$data.prependDiv('table-data-fill');
      this._applyFillerStyle(this.$fillBefore);
    }

    let fillBeforeHeight = this._calculateFillerHeight(new Range(0, this.viewRangeRendered.from));
    this.$fillBefore.cssHeight(fillBeforeHeight);
    this.$fillBefore.cssWidth(this.rowWidth);
    $.log.isTraceEnabled() && $.log.trace('FillBefore height: ' + fillBeforeHeight);

    if (!this.$fillAfter) {
      this.$fillAfter = this.$data.appendDiv('table-data-fill');
      this._applyFillerStyle(this.$fillAfter);
    }

    let fillAfterHeight = this._calculateFillerHeight(new Range(this.viewRangeRendered.to, this.visibleRows.length));
    this.$fillAfter.cssHeight(fillAfterHeight);
    this.$fillAfter.cssWidth(this.rowWidth);
    $.log.isTraceEnabled() && $.log.trace('FillAfter height: ' + fillAfterHeight);
  }

  protected _applyFillerStyle($filler: JQuery) {
    let lineColor = $filler.css('background-color');
    // In order to get a 1px border we need to get the right value in percentage for the linear gradient
    let lineWidth = ((1 - 1 / this.rowHeight) * 100).toFixed(2) + '%';
    $filler.css({
      background: 'linear-gradient(to bottom, transparent, transparent ' + lineWidth + ', ' + lineColor + ' ' + lineWidth + ', ' + lineColor + ')',
      backgroundSize: '100% ' + this.rowHeight + 'px',
      backgroundColor: 'transparent'
    });
  }

  protected _calculateFillerHeight(range: Range): number {
    let totalHeight = 0;
    for (let i = range.from; i < range.to; i++) {
      let row = this.visibleRows[i];
      totalHeight += this._heightForRow(row);
    }
    return totalHeight;
  }

  containsAggregatedNumberColumn(): boolean {
    if (!this.initialized) {
      return false;
    }
    return this.visibleColumns().some(column => column instanceof NumberColumn && column.aggregationFunction !== 'none');
  }

  /**
   * Rebuilds the header.<br>
   * Does not modify the rows, it expects a deleteAll and insert operation to follow which will do the job.
   */
  updateColumnStructure(columns: Column<any>[]) {
    this._destroyColumns();
    this.columns = columns;
    this._initColumns();
    this.trigger('columnStructureChanged');
    if (this._isDataRendered()) {
      this._updateRowWidth();
      this.$rows(true).css('width', this.rowWidth);
      this._rerenderHeaderColumns();
      this._renderEmptyData();
    }
  }

  updateColumnOrder(columns: (Column<any> | { id: string })[]) {
    if (columns.length !== this.columns.length) {
      throw new Error('Column order may not be updated because lengths of the arrays differ.');
    }

    let oldColumnOrder = this.columns.slice();

    for (let i = 0; i < columns.length; i++) {
      let column = columns[i];
      let currentPosition = arrays.findIndex(this.columns, element => element.id === column.id);
      if (currentPosition < 0) {
        throw new Error('Column with id ' + column.id + 'not found.');
      }

      if (currentPosition !== i) {
        // Update model
        arrays.move(this.columns, currentPosition, i);
      }
    }

    if (this._isDataRendered()) {
      this._renderColumnOrderChanges(oldColumnOrder);
    }
  }

  /**
   * @param columns array of columns which were updated.
   */
  updateColumnHeaders(columns: Column[]) {
    let oldColumnState: ColumnModel;

    // Update model columns
    for (let i = 0; i < columns.length; i++) {
      let column = this.columnById(columns[i].id);
      oldColumnState = $.extend(oldColumnState, column);
      column.text = columns[i].text;
      column.headerTooltipText = columns[i].headerTooltipText;
      column.headerTooltipHtmlEnabled = columns[i].headerTooltipHtmlEnabled;
      column.headerCssClass = columns[i].headerCssClass;
      column.headerHtmlEnabled = columns[i].headerHtmlEnabled;
      column.headerBackgroundColor = columns[i].headerBackgroundColor;
      column.headerForegroundColor = columns[i].headerForegroundColor;
      column.headerFont = columns[i].headerFont;
      column.headerIconId = columns[i].headerIconId;
      column.sortActive = columns[i].sortActive;
      column.sortAscending = columns[i].sortAscending;
      if (column.grouped && !columns[i].grouped) {
        this._removeGroupColumn(column);
      }
      column.grouped = columns[i].grouped;
      if (!column.sortActive && column.sortIndex !== -1) {
        // Adjust indices of other sort columns (if a sort column in the middle got removed, there won't necessarily be an event for the other columns)
        this._removeSortColumn(column);
      } else if (column.grouped && column.sortActive && column.sortIndex === -1) {
        this._addGroupColumn(column);
      } else if (column.sortActive && column.sortIndex === -1) {
        // Necessary if there is a tail sort column (there won't be an event for the tail sort column if another sort column was added before)
        this._addSortColumn(column);
      } else {
        column.sortIndex = columns[i].sortIndex;
      }

      if (this._isDataRendered() && this.header) {
        this.header.updateHeader(column, oldColumnState);
      }
    }
  }

  protected override _attach() {
    this.$parent.append(this.$container);
    super._attach();
  }

  /**
   * Method invoked when this is a 'detailTable' and the outline content is displayed.
   */
  protected override _postAttach() {
    this._rerenderViewportAfterAttach();
    let htmlParent = this.htmlComp.getParent();
    this.htmlComp.setSize(htmlParent.size());
    super._postAttach();
  }

  protected override _renderOnAttach() {
    super._renderOnAttach();
    this._rerenderViewportAfterAttach();
    this._renderViewportAfterAttach();
    let actions = this._postAttachActions;
    this._postAttachActions = [];
    actions.forEach(action => action());
  }

  protected _rerenderViewportAfterAttach() {
    if (this._rerenderViewPortAfterAttach) {
      this._rerenderViewport();
      this._rerenderViewPortAfterAttach = false;
      this._renderViewPortAfterAttach = false;
    }
  }

  protected _renderViewportAfterAttach() {
    if (this._renderViewPortAfterAttach) {
      this._renderViewport();
      this._renderViewPortAfterAttach = false;
    }
  }

  /**
   * Method invoked when this is a 'detailTable' and the outline content is not displayed anymore.
   */
  protected override _detach() {
    this.$container.detach();
    // Detach helper stores the current scroll pos and restores in attach.
    // To make it work scrollTop needs to be reset here otherwise viewport won't be rendered by _onDataScroll
    super._detach();
  }

  /**
   * @param callback function to be called right after the popup is destroyed
   */
  protected _destroyCellEditorPopup(callback?: () => void) {
    // When a cell editor popup is open and table is detached, we close the popup immediately
    // and don't wait for the model event 'endCellEdit'. By doing this we can avoid problems
    // with invalid focus contexts.
    // However: when 'completeCellEdit' is already scheduled, we must wait because Scout classic
    // must send a request to the server first #249385.
    if (!this.cellEditorPopup) {
      return;
    }
    let destroyEditor = () => {
      this.cellEditorPopup.destroy();
      this.cellEditorPopup = null;
      if (this.$container) {
        this.$container.removeClass('has-cell-editor-popup');
      }
      if (callback) {
        callback();
      }
    };
    let promise = this.cellEditorPopup.waitForCompleteCellEdit();
    if (promise.state() === 'resolved') {
      // Do it immediately if promise has already been resolved.
      // This makes sure updateRow does not immediately reopen the editor after closing.
      destroyEditor();
    } else {
      promise.then(destroyEditor);
    }
  }

  /** @see TableModel.virtual */
  setVirtual(virtual: boolean) {
    this._setProperty('virtual', virtual);
  }

  setCellValue<TValue>(column: Column<TValue>, row: TableRow, value: TValue) {
    column.setCellValue(row, value);
  }

  setCellText(column: Column<any>, row: TableRow, displayText: string) {
    column.setCellText(row, displayText);
  }

  setCellErrorStatus(column: Column<any>, row: TableRow, errorStatus: Status) {
    column.setCellErrorStatus(row, errorStatus);
  }

  /**
   * @param includeGuiColumns true to also include columns created by the UI (e.g. {@link rowIconColumn} or {@link checkableColumn}). Default is true.
   * @param includeCompacted true to also include the columns that are invisible because they are compacted. Default is false which means compacted columns are not returned.
   */
  visibleColumns(includeGuiColumns?: boolean, includeCompacted?: boolean): Column<any>[] {
    return this.filterColumns(column => scout.nvl(includeCompacted, false) ? column.visibleIgnoreCompacted : column.visible, includeGuiColumns);
  }

  /**
   * @param includeGuiColumns true to also include columns created by the UI (e.g. {@link rowIconColumn} or {@link checkableColumn}). Default is true.
   */
  displayableColumns(includeGuiColumns?: boolean): Column<any>[] {
    return this.filterColumns(column => column.displayable, includeGuiColumns);
  }

  /**
   * @returns all columns marked as primaryKey column (see {@link Column.primaryKey})
   */
  primaryKeyColumns(): Column[] {
    return this.filterColumns(column => column.primaryKey);
  }

  /**
   * @returns all columns marked as summary column (see {@link Column.summary})
   */
  summaryColumns(): Column[] {
    return this.filterColumns(column => column.summary);
  }

  /**
   * @param includeGuiColumns true to also include columns created by the UI (e.g. {@link rowIconColumn} or {@link checkableColumn}). Default is true.
   */
  filterColumns(filter: Predicate<Column<any>>, includeGuiColumns?: boolean): Column<any>[] {
    includeGuiColumns = scout.nvl(includeGuiColumns, true);
    return this.columns.filter(column => filter(column) && (includeGuiColumns || !column.guiOnly));
  }

  // same as on Tree.prototype._onDesktopPopupOpen
  protected _onDesktopPopupOpen(event: DesktopPopupOpenEvent) {
    let popup = event.popup;
    if (!this.isFocusable(false)) {
      return;
    }
    // Set table style to focused if a context menu or a menu bar popup opens, so that it looks as it still has the focus
    if (this.has(popup) && popup instanceof ContextMenuPopup) {
      this.$container.addClass('focused');
      popup.one('destroy', () => {
        if (this._isDataRendered()) {
          this.$container.removeClass('focused');
        }
      });
    }
  }

  protected _onDesktopPropertyChange(event: PropertyChangeEvent<any, Desktop>) {
    // The height of the menuBar changes by css when switching to or from the dense mode
    if (event.propertyName === 'dense') {
      this.menuBar.invalidateLayoutTree();
    }
  }

  markRowsAsNonChanged(rows?: TableRow | TableRow[]) {
    arrays.ensure(rows || this.rows).forEach(row => {
      row.status = TableRow.Status.NON_CHANGED;
    });
  }

  isColumnAddable(insertAfterColumn?: Column): boolean {
    if (this.organizer) {
      return this.organizer.isColumnAddable(insertAfterColumn);
    }
    return false;
  }

  isColumnRemovable(column: Column): boolean {
    if (this.organizer) {
      return this.organizer.isColumnRemovable(column);
    }
    return false;
  }

  isColumnModifiable(column: Column): boolean {
    if (this.organizer) {
      return this.organizer.isColumnModifiable(column);
    }
    return false;
  }

  isCustomizable(): boolean {
    // TODO bsh [js-table] Delegate to this.customizer
    return false;
  }

  /* --- STATIC HELPERS ------------------------------------------------------------- */

  static parseHorizontalAlignment(alignment: Alignment): string {
    if (alignment > 0) {
      return 'right';
    }
    if (alignment === 0) {
      return 'center';
    }
    return 'left';
  }

  static linkRowToDiv(row: TableRow, $row: JQuery) {
    if (row) {
      row.$row = $row;
    }
    if ($row) {
      $row.data('row', row);
    }
  }
}

export type TableMenuType = EnumObject<typeof Table.MenuType>;
export type TableHierarchicalStyle = EnumObject<typeof Table.HierarchicalStyle>;
export type TableCheckableStyle = EnumObject<typeof Table.CheckableStyle>;
export type TableGroupingStyle = EnumObject<typeof Table.GroupingStyle>;
export type TableReloadReason = EnumObject<typeof Table.ReloadReason>;

export type UpdateTableRowStructureOptions = {
  /**
   * Default is false.
   */
  updateTree?: boolean;
  /**
   * Default is the value of {@link updateTree}.
   */
  filteredRows?: boolean;
  /**
   * Default is the value of {@link filteredRows}.
   */
  applyFilters?: boolean;
  /**
   * Default is false.
   */
  filtersChanged?: boolean;
  /**
   * Default is the value of {@link filteredRows}.
   */
  visibleRows?: boolean;
};

export type TableCellPosition = {
  column: Column<any>;
  row: TableRow;
};

export type TableRowCheckOptions = {
  checked?: boolean;
  checkOnlyEnabled?: boolean;
};

export type AggregateTableRow = {
  id?: string;
  /**
   * List of aggregated values per {@link Table#visibleColumns visible column}. If a column has no aggregated value,
   * the corresponding entry is empty. This array needs to be updated whenever the list of visible columns changes.
   */
  contents: any[];
  prevRow: TableRow;
  nextRow: TableRow;
  $row?: JQuery;
  height?: number;
};

export type ColumnMap = {
  [type: string]: Column<any>;
};

export type ColumnMapOf<T> = T extends { columnMap: infer TMap } ? TMap : object;
