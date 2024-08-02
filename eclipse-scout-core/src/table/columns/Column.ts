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
  AggregateTableRow, Alignment, Cell, CellEditorPopup, ColumnComparator, ColumnEventMap, ColumnModel, ColumnOptimalWidthMeasurer, ColumnUserFilter, comparators, Event, EventHandler, FormField, GridData, icons, InitModelOf, objects,
  ObjectWithType, PropertyEventEmitter, scout, Session, SomeRequired, Status, StringField, strings, styles, Table, TableColumnMovedEvent, TableHeader, TableHeaderMenu, TableRow, texts, ValueField, widgets
} from '../../index';
import $ from 'jquery';

export class Column<TValue = string> extends PropertyEventEmitter implements ColumnModel<TValue>, ObjectWithType {
  declare model: ColumnModel<TValue>;
  declare initModel: SomeRequired<this['model'], 'session'>;
  declare eventMap: ColumnEventMap;
  declare self: Column<any>;

  objectType: string;
  id: string;
  autoOptimizeWidth: boolean;
  /** true if content of the column changed and width has to be optimized */
  autoOptimizeWidthRequired: boolean;
  session: Session;
  autoOptimizeMaxWidth: number;
  cssClass: string;
  editable: boolean;
  removable: boolean;
  modifiable: boolean;
  fixedWidth: boolean;
  fixedPosition: boolean;
  grouped: boolean;
  headerCssClass: string;
  headerIconId: string;
  headerHtmlEnabled: boolean;
  headerTooltipText: string;
  headerBackgroundColor: string;
  headerForegroundColor: string;
  headerFont: string;
  headerTooltipHtmlEnabled: boolean;
  horizontalAlignment: Alignment;
  htmlEnabled: boolean;
  initialAlwaysIncludeSortAtBegin: boolean;
  initialAlwaysIncludeSortAtEnd: boolean;
  index: number;
  primaryKey: boolean;
  guiOnly: boolean;
  mandatory: boolean;
  optimalWidthMeasurer: ColumnOptimalWidthMeasurer;
  sortActive: boolean;
  checkable: boolean;
  sortAscending: boolean;
  sortIndex: number;
  summary: boolean;
  type: string;
  width: number;
  initialWidth: number;
  minWidth: number;
  showSeparator: boolean;
  table: Table;
  tableNodeColumn: boolean;
  maxLength: number;
  text: string;
  textWrap: boolean;
  filterType: string;
  comparator: ColumnComparator;
  visible: boolean;
  textBased: boolean;
  headerMenuEnabled: boolean;
  tableNodeLevel0CellPadding: number;
  expandableIconLevel0CellPadding: number;
  nodeColumnCandidate: boolean;
  /** Set by TableHeader */
  $header: JQuery;
  $separator: JQuery;

  /**
   * Contains the width the cells of the column really have (only set in Chrome due to a Chrome bug, see Table._updateRealColumnWidths)
   * @internal
   */
  _realWidth: number;

  protected _tableColumnsChangedHandler: EventHandler<TableColumnMovedEvent | Event<Table>>;

  constructor() {
    super();
    this.autoOptimizeWidth = false;
    this.autoOptimizeWidthRequired = false;
    this.autoOptimizeMaxWidth = -1;
    this.removable = true;
    this.cssClass = null;
    this.editable = false;
    this.modifiable = true;
    this.fixedWidth = false;
    this.fixedPosition = false;
    this.grouped = false;
    this.headerCssClass = null;
    this.headerIconId = null;
    this.headerHtmlEnabled = false;
    this.headerTooltipText = null;
    this.headerTooltipHtmlEnabled = false;
    this.horizontalAlignment = -1;
    this.htmlEnabled = false;
    this.index = -1;
    this.primaryKey = false;
    this.mandatory = false;
    this.optimalWidthMeasurer = new ColumnOptimalWidthMeasurer(this);
    this.sortActive = false;
    this.sortAscending = true;
    this.sortIndex = -1;
    this.summary = false;
    this.type = 'text';
    this.width = 60;
    this.initialWidth = undefined;
    this.minWidth = Column.DEFAULT_MIN_WIDTH;
    this.showSeparator = true;
    this.table = null;
    this.tableNodeColumn = false;
    this.maxLength = 4000;
    this.text = null;
    this.textWrap = false;
    this.filterType = 'TextColumnUserFilter';
    this.comparator = comparators.TEXT;
    this.visible = true;
    this.textBased = true;
    this.headerMenuEnabled = true;
    this.tableNodeLevel0CellPadding = 28;
    this.expandableIconLevel0CellPadding = 13;
    this.nodeColumnCandidate = true;

    this._tableColumnsChangedHandler = this._onTableColumnsChanged.bind(this);
    this._realWidth = null;

    this.$header = null;
    this.$separator = null;

    this._addMultiDimensionalProperty('visible', true);
    this._addPropertyDimensionAlias('visible', 'visibleGranted', {dimension: 'granted'});
    this._addPropertyDimensionAlias('visible', 'displayable');
    this._addPropertyDimensionAlias('visible', 'compacted', {inverted: true});
  }

  static DEFAULT_MIN_WIDTH = 60;
  static SMALL_MIN_WIDTH = 38;
  static NARROW_MIN_WIDTH = 34;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.session = model.session;

    // Initial width is only sent if it differs from width
    if (this.initialWidth === undefined) {
      this.initialWidth = scout.nvl(this.width, 0);
    }

    this.resolveTextKeys(['text', 'headerTooltipText']);
    this.resolveIconIds(['headerIconId']);

    this._setTable(this.table);
    this._setAutoOptimizeWidth(this.autoOptimizeWidth);
    // no need to call setEditable here. cell propagation is done in _initCell
  }

  destroy() {
    this._destroy();
    this._setTable(null);
  }

  /**
   * Override this function in order to implement custom destroy logic.
   */
  protected _destroy() {
    // NOP
  }

  /** @internal */
  _setTable(table: Table) {
    if (this.table) {
      this.table.off('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
    }
    this.table = table;
    if (this.table) {
      this.table.on('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
    }
  }

  resolveTextKeys(properties: string[]) {
    properties.forEach(property => {
      texts.resolveTextProperty(this, property);
    });
  }

  resolveIconIds(properties: string[]) {
    properties.forEach(property => {
      icons.resolveIconProperty(this, property);
    });
  }

  /**
   * Converts the vararg if it is of type string to an object with
   * a property 'text' with the original value.
   *
   * Example:
   * 'My Company' --> { text: 'MyCompany'; }
   *
   * @see JsonCell.java
   * @param vararg either a Cell instance or a scalar value
   */
  initCell(vararg: TValue | Cell<TValue>, row?: TableRow): Cell<TValue> {
    let cell = this._ensureCell(vararg);
    this._initCell(cell);

    // If a text is provided, use that text instead of using formatValue to generate a text based on the value
    if (objects.isNullOrUndefined(cell.text)) {
      this._updateCellText(row, cell);
    }
    return cell;
  }

  /**
   * Ensures that a Cell instance is returned.
   * When vararg is a scalar value a new Cell instance is created and the value is set as {@link cell.value} property.
   *
   * @param vararg either a Cell instance or a scalar value
   */
  private _ensureCell(vararg: Cell<TValue> | TValue): Cell<TValue> {
    let cell: Cell<TValue>;

    if (vararg instanceof Cell) {
      cell = vararg;

      // value may be set but may have the wrong type (e.g. text instead of date) -> ensure type
      cell.value = this._ensureValue(cell.value);
    } else {
      // in this case 'vararg' is only a scalar value, typically a string
      let cellType = Cell<TValue>;
      cell = scout.create(cellType, {
        value: this._ensureValue(vararg)
      });
    }

    return cell;
  }

  /**
   * Override this method to create a value based on the given scalar value.
   */
  protected _ensureValue(scalar: TValue | string): TValue {
    return scalar as TValue;
  }

  protected _updateCellText(row: TableRow, cell: Cell<TValue>) {
    let value = cell.value;
    if (!row) {
      // row is omitted when creating aggregate cells
      return;
    }

    let returned = this._formatValue(value, row);
    if (objects.isPromise(returned)) {
      // Promise is returned -> set display text later
      this.setCellTextDeferred(returned, row, cell);
    } else {
      this.setCellText(row, returned, cell);
    }
  }

  protected _formatValue(value: TValue, row?: TableRow): string | JQuery.Promise<string> {
    return scout.nvl(value, '');
  }

  /**
   * If cell does not define properties, use column values.
   * Override this function to implement type specific init cell behavior.
   *
   */
  protected _initCell(cell: Cell<TValue>): Cell<TValue> {
    cell.cssClass = scout.nvl(cell.cssClass, this.cssClass);
    cell.editable = scout.nvl(cell.editable, this.editable);
    cell.horizontalAlignment = scout.nvl(cell.horizontalAlignment, this.horizontalAlignment);
    cell.htmlEnabled = scout.nvl(cell.htmlEnabled, this.htmlEnabled);
    cell.mandatory = scout.nvl(cell.mandatory, this.mandatory);
    return cell;
  }

  buildCellForRow(row: TableRow): string {
    let cell = this.cell(row);
    return this.buildCell(cell, row);
  }

  buildCellForAggregateRow(aggregateRow: AggregateTableRow): string {
    let cell: Cell<TValue>;
    if (this.grouped) {
      let refRow = (this.table.groupingStyle === Table.GroupingStyle.TOP ? aggregateRow.nextRow : aggregateRow.prevRow);
      cell = this.createAggrGroupCell(refRow);
    } else {
      let aggregateValue = aggregateRow.contents[this.table.visibleColumns().indexOf(this)];
      cell = this.createAggrValueCell(aggregateValue);
    }
    return this.buildCell(cell, {});
  }

  buildCell(cell: Cell<TValue>, row: TableRow | { hasError?: boolean; expanded?: boolean; expandable?: boolean; parentRow?: TableRow }): string {
    scout.assertParameter('cell', cell, Cell);

    let tableNodeColumn = this.table.isTableNodeColumn(this),
      rowPadding = 0;

    if (tableNodeColumn) {
      rowPadding = this.table._calcRowLevelPadding(row);
    }

    let text = this._text(cell);
    let icon = this._icon(cell.iconId, !!text) || '';
    let cssClass = this._cellCssClass(cell, tableNodeColumn);
    let style = this._cellStyle(cell, tableNodeColumn, rowPadding);

    if (cell.errorStatus) {
      row.hasError = true;
    }

    let content: string;
    if (!text && !icon) {
      // If every cell of a row is empty the row would collapse, using nbsp makes sure the row is as height as the others even if it is empty
      content = '&nbsp;';
      cssClass = strings.join(' ', cssClass, 'empty');
    } else {
      if (cell.flowsLeft) {
        content = text + icon;
      } else {
        content = icon + text;
      }
    }

    if (tableNodeColumn && row.expandable) {
      this.tableNodeColumn = true;
      content = this._expandIcon(row.expanded, rowPadding) + content;
      if (row.expanded) {
        cssClass += ' expanded';
      }
    }

    return this._buildCell(cell, content, style, cssClass);
  }

  protected _buildCell(cell: Cell<TValue>, content: string, style: string, cssClass: string): string {
    let ariaAttributes = '';
    if (this.table.accessibilityRenderer && strings.hasText(this.table.accessibilityRenderer.cellRole)) {
      ariaAttributes = ' role="' + this.table.accessibilityRenderer.cellRole + '"';
    }
    // Set the label of the cell to header name + cell content. The reference to cell content is needed, because
    // without it screen readers may only announce the header name without the cell content. If there is no header
    // to reference, we do not need to reference the cell either, because screen readers will announce the cell
    // content naturally if there is no aria-labelledby
    if (this.table.header && strings.hasText(this.table.header.headerLabelId)) {
      let cellLabelId = widgets.createUniqueId('lbl');
      ariaAttributes += ' aria-labelledBy="' + this.table.header.headerLabelId + ' ' + cellLabelId + '" ' + 'id="' + cellLabelId + '"';
    }
    return '<div' + ariaAttributes + ' class="' + cssClass + '" style="' + style + '">' + content + '</div>';
  }

  protected _expandIcon(expanded: boolean, rowPadding: number): string {
    let style = 'padding-left: ' + (rowPadding + this.expandableIconLevel0CellPadding) + 'px';
    let cssClasses = 'table-row-control';
    if (expanded) {
      cssClasses += ' expanded';
    }
    return '<div aria-hidden="true" class="' + cssClasses + '" style="' + style + '"></div>';
  }

  protected _icon(iconId: string, hasText: boolean): string {
    let cssClass, icon;
    if (!iconId) {
      return;
    }
    cssClass = 'table-cell-icon';
    if (hasText) {
      cssClass += ' with-text';
    }
    icon = icons.parseIconId(iconId);
    if (icon.isFontIcon()) {
      cssClass += ' font-icon';
      return '<span aria-hidden="true" class="' + icon.appendCssClass(cssClass) + '">' + icon.iconCharacter + '</span>';
    }
    cssClass += ' image-icon';
    return '<img alt="" class="' + cssClass + '" src="' + icon.iconUrl + '">';
  }

  protected _text(cell: Cell<TValue>): string {
    let text = cell.text || '';

    if (!cell.htmlEnabled) {
      text = cell.encodedText() || '';
      if (this.table.multilineText) {
        text = strings.nl2br(text, false);
      }
      if (text) {
        // Wrap in a span to make customization using css easier.
        // An empty text will be replaced with nbsp later on. To make that work, only wrap it if there is text.
        text = '<span class="text">' + text + '</span>';
      }
    }

    return text;
  }

  protected _cellCssClass(cell: Cell<TValue>, tableNode?: boolean): string {
    let cssClass = 'table-cell';
    if (cell.mandatory) {
      cssClass += ' mandatory';
    }
    if (!this.table.multilineText || !this.textWrap) {
      cssClass += ' white-space-nowrap';
    }
    if (cell.editable) {
      cssClass += ' editable';
    }
    if (cell.errorStatus) {
      cssClass += ' has-error';
    }
    if (cell.iconId && !cell.text) {
      cssClass += ' icon-only';
    }
    cssClass += ' halign-' + Table.parseHorizontalAlignment(cell.horizontalAlignment);
    let visibleColumns = this.table.visibleColumns();
    let overAllColumnPosition = visibleColumns.indexOf(this);
    if (overAllColumnPosition === 0) {
      cssClass += ' first';
    }
    if (overAllColumnPosition === visibleColumns.length - 1) {
      cssClass += ' last';
    }
    if (tableNode) {
      cssClass += ' table-node';
    }

    if (cell.cssClass) {
      cssClass += ' ' + cell.cssClass;
    }
    return cssClass;
  }

  protected _cellStyle(cell: Cell<TValue>, tableNodeColumn?: boolean, rowPadding?: number): string {
    let style,
      width = this.width;

    if (width === 0) {
      return 'display: none;';
    }
    style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
    if (tableNodeColumn) {
      // calculate padding
      style += ' padding-left: ' + (this.tableNodeLevel0CellPadding + rowPadding) + 'px; ';
    }
    style += styles.legacyStyle(cell);
    return style;
  }

  onMouseUp(event: JQuery.MouseUpEvent, $row: JQuery) {
    let row = $row.data('row') as TableRow,
      cell = this.cell(row);

    if (this.isCellEditable(row, cell, event)) {
      this.table.prepareCellEdit(this, row, true);
    }
  }

  isCellEditable(row: TableRow, cell: Cell<TValue>, event: JQuery.MouseEventBase): boolean {
    return this.table.enabledComputed && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey;
  }

  startCellEdit(row: TableRow, field: ValueField<TValue>): CellEditorPopup<TValue> {
    let $row = row.$row,
      cell = this.cell(row),
      $cell = this.table.$cell(this, $row);

    cell.field = field;
    // Override field alignment with the cell's alignment
    cell.field.gridData.horizontalAlignment = cell.horizontalAlignment;
    let popup = this._createEditorPopup(row, cell);
    popup.$anchor = $cell;
    popup.open(this.table.$data);
    return popup;
  }

  protected _createEditorPopup(row: TableRow, cell: Cell<TValue>): CellEditorPopup<TValue> {
    let cellEditorPopup = CellEditorPopup<TValue>;
    return scout.create(cellEditorPopup, {
      parent: this.table,
      column: this,
      row: row,
      cell: cell
    });
  }

  /**
   * @returns the cell object for this column from the given row.
   */
  cell(row: TableRow): Cell<TValue> {
    return this.table.cell(this, row);
  }

  /**
   * Creates an artificial cell from the properties relevant for the column header.
   */
  headerCell(): Cell<string> {
    let cellType = Cell<string>;
    return scout.create(cellType, {
      value: this.text,
      text: this.text,
      iconId: this.headerIconId,
      cssClass: this.headerCssClass,
      tooltipText: this.headerTooltipText,
      htmlEnabled: this.headerHtmlEnabled
    });
  }

  /**
   * @returns the cell object for this column from the first selected row in the table.
   */
  selectedCell(): Cell<TValue> {
    let selectedRow = this.table.selectedRow();
    return this.table.cell(this, selectedRow);
  }

  /**
   * @returns the value of the cell. If it is text based as string otherwise the raw value.
   */
  cellValueOrText(row: TableRow): TValue | string {
    if (this.textBased) {
      return this.table.cellText(this, row);
    }
    return this.table.cellValue(this, row);
  }

  cellValue(row: TableRow): TValue {
    return this.table.cellValue(this, row);
  }

  cellText(row: TableRow): string {
    return this.table.cellText(this, row);
  }

  /**
   * @returns the cell value to be used for grouping and filtering (chart, column filter).
   */
  cellValueOrTextForCalculation(row: TableRow): TValue | string {
    let cell = this.cell(row);
    let value = this.cellValueOrText(row);
    if (objects.isNullOrUndefined(value)) {
      return null;
    }
    return this._preprocessValueOrTextForCalculation(value, cell);
  }

  protected _preprocessValueOrTextForCalculation(value: TValue | string, cell?: Cell<TValue>): TValue | string {
    if (typeof value === 'string') {
      // In case of string columns, value and text are equal -> use _preprocessStringForCalculation to handle html tags and new lines correctly
      return this._preprocessTextForCalculation(value, cell?.htmlEnabled);
    }
    return value;
  }

  protected _preprocessTextForCalculation(text: string, htmlEnabled?: boolean): string {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled,
      removeNewlines: true,
      trim: true
    });
  }

  /**
   * @returns the cell text to be used for table grouping
   */
  cellTextForGrouping(row: TableRow): string {
    let cell = this.cell(row);
    return this._preprocessTextForGrouping(cell.text, cell.htmlEnabled);
  }

  protected _preprocessTextForGrouping(text: string, htmlEnabled?: boolean): string {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled,
      trim: true
    });
  }

  /**
   * @returns the cell text to be used for the text filter
   */
  cellTextForTextFilter(row: TableRow): string {
    let cell = this.cell(row);
    return this._preprocessTextForTextFilter(cell.text, cell.htmlEnabled);
  }

  protected _preprocessTextForTextFilter(text: string, htmlEnabled?: boolean): string {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled
    });
  }

  /**
   * @returns the cell text to be used for the table row detail.
   */
  cellTextForRowDetail(row: TableRow): string {
    let cell = this.cell(row);

    return this._preprocessText(this._text(cell), {
      removeHtmlTags: cell.htmlEnabled
    });
  }

  /**
   * Removes html tags, converts to single line, removes leading and trailing whitespaces.
   */
  protected _preprocessText(text: string, options: { removeHtmlTags?: boolean; removeNewlines?: boolean; trim?: boolean }): string {
    if (text === null || text === undefined) {
      return text;
    }
    options = options || {};
    if (options.removeHtmlTags) {
      text = strings.plainText(text);
    }
    if (options.removeNewlines) {
      text = text.replace('\n', ' ');
    }
    if (options.trim) {
      text = text.trim();
    }
    return text;
  }

  setCellValue(row: TableRow, value: TValue) {
    let cell = this.cell(row);
    this._setCellValue(row, value, cell);
    this._updateCellText(row, cell);
  }

  protected _setCellValue(row: TableRow, value: TValue, cell: Cell<TValue>) {
    // value may have the wrong type (e.g. text instead of date) -> ensure type
    value = this._ensureValue(value);

    // Only update row status when value changed.
    // Cell text needs to be updated even if value did not change
    // (text may cause an invalid value that won't be saved on the cell, reverting to the valid value needs to update the text again)
    if (cell.value !== value && row.status === TableRow.Status.NON_CHANGED) {
      row.status = TableRow.Status.UPDATED;
    }

    cell.setValue(value);
  }

  setCellTextDeferred(promise: JQuery.Promise<string>, row: TableRow, cell: Cell<TValue>) {
    promise
      .done(text => this.setCellText(row, text, cell))
      .fail(error => {
        this.setCellText(row, '', cell);
        $.log.error('Could not resolve cell text for value ' + cell.value, error);
      });

    // (then) promises always resolve asynchronously which means the text will always be set later after row is initialized and will generate an update row event.
    // To make sure not every cell update will render the viewport (which is an expensive operation), the update is buffered and done as soon as all promises resolve.
    this.table.updateBuffer.pushPromise(promise);
  }

  setCellText(row: TableRow, text: string, cell?: Cell<TValue>) {
    if (!cell) {
      cell = this.cell(row);
    }
    if (cell.text === text) {
      // Don't trigger row update if text has not changed
      return;
    }
    cell.setText(text);

    // Don't update row while initializing (it is either added to the table later, or being added / updated right now)
    // The check for "this.table" is necessary, because the column could already have been destroyed (method is called
    // asynchronously by setCellTextDeferred).
    if (row.initialized && this.table) {
      this.table.updateRow(row);
    }
  }

  setCellErrorStatus(row: TableRow, errorStatus: Status, cell?: Cell<TValue>) {
    if (!cell) {
      cell = this.cell(row);
    }
    cell.setErrorStatus(errorStatus);
  }

  setCellIconId(row: TableRow, iconId: string) {
    let cell = this.cell(row);
    if (cell.iconId === iconId) {
      return;
    }
    cell.setIconId(iconId);
    if (row.initialized) {
      this.table.updateRow(row);
    }
  }

  setHorizontalAlignment(horizontalAlignment: Alignment) {
    let changed = this.setProperty('horizontalAlignment', horizontalAlignment);
    if (!changed) {
      return;
    }

    this.table.rows.forEach(row => this.cell(row).setHorizontalAlignment(horizontalAlignment));
    this.table.updateRows(this.table.rows);

    if (this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setEditable(editable: boolean) {
    let changed = this.setProperty('editable', editable);
    if (!changed) {
      return;
    }

    this.table.rows.forEach(row => this.cell(row).setEditable(editable));
    this.table.updateRows(this.table.rows);
  }

  setMandatory(mandatory: boolean) {
    let changed = this.setProperty('mandatory', mandatory);
    if (!changed) {
      return;
    }

    this.table.rows.forEach(row => this.cell(row).setMandatory(mandatory));
    this.table.updateRows(this.table.rows);
  }

  setCssClass(cssClass: string) {
    let changed = this.setProperty('cssClass', cssClass);
    if (!changed) {
      return;
    }

    this.table.rows.forEach(row => this.cell(row).setCssClass(cssClass));
    this.table.updateRows(this.table.rows);
  }

  setSummary(summary: boolean) {
    const changed = this.setProperty('summary', summary);
    if (!changed) {
      return;
    }

    this.table.updateRows(this.table.rows);
  }

  setWidth(width: number) {
    let changed = this.setProperty('width', width);
    if (!changed) {
      return;
    }
    this.table.resizeColumn(this, width);
  }

  createAggrGroupCell(row: TableRow): Cell<TValue> {
    let cell = this.cell(row);
    let cellType = Cell<TValue>;
    return this.initCell(scout.create(cellType, {
      // value necessary for value based columns (e.g. checkbox column)
      value: cell.value,
      text: this.cellTextForGrouping(row),
      iconId: cell.iconId,
      horizontalAlignment: this.horizontalAlignment,
      htmlEnabled: false, // grouping cells need a text <span> to work which will only be created if html is disabled. Tags will be removed anyway by cellTextForGrouping
      cssClass: 'table-aggregate-cell' + (cell.cssClass ? ' ' + cell.cssClass : ''),
      backgroundColor: 'inherit',
      flowsLeft: this.horizontalAlignment > 0
    }));
  }

  createAggrValueCell(value: TValue): Cell<TValue> {
    return this.createAggrEmptyCell();
  }

  createAggrEmptyCell(): Cell<TValue> {
    let cellType = Cell<TValue>;
    return this.initCell(scout.create(cellType, {
      empty: true,
      cssClass: 'table-aggregate-cell'
    }));
  }

  calculateOptimalWidth(): number | JQuery.Promise<number> {
    return this.optimalWidthMeasurer.measure();
  }

  /**
   * Returns a type specific column user-filter. The default impl. returns a ColumnUserFilter.
   * Subclasses that must return another type, must simply change the value of the 'filterType' property.
   */
  createFilter(): ColumnUserFilter {
    return scout.create(this.filterType, {
      session: this.session,
      table: this.table,
      column: this
    });
  }

  /**
   * Returns a table header menu. Subclasses can override this method to create a column specific table header menu.
   */
  createTableHeaderMenu(tableHeader: TableHeader): TableHeaderMenu {
    let $header = this.$header;
    return scout.create(TableHeaderMenu, {
      parent: tableHeader,
      column: $header.data('column'),
      tableHeader: tableHeader,
      $anchor: $header
    });
  }

  /**
   * @returns a field instance used as editor when a cell of this column is in edit mode.
   */
  createEditor(row: TableRow): ValueField<TValue> {
    let field = this._createEditor(row);
    let cell = this.cell(row);
    this._initEditorField(field, cell);
    field.setLabelVisible(false);
    field.setFieldStyle(FormField.FieldStyle.CLASSIC);
    let hints = new GridData(field.gridDataHints);
    hints.horizontalAlignment = cell.horizontalAlignment;
    field.setGridDataHints(hints);
    return field;
  }

  /**
   * Depending on the type of column the editor may need to be initialized differently.
   * The default implementation either copies the value to the field if the field has no error or copies the text and error status if it has an error.
   */
  protected _initEditorField(field: ValueField<TValue>, cell: Cell<TValue>) {
    if (cell.errorStatus) {
      this._updateEditorFromInvalidCell(field, cell);
    } else {
      this._updateEditorFromValidCell(field, cell);
    }
  }

  protected _updateEditorFromValidCell(field: ValueField<TValue>, cell: Cell<TValue>) {
    field.setValue(cell.value);
  }

  protected _updateEditorFromInvalidCell(field: ValueField<TValue>, cell: Cell<TValue>) {
    field.setErrorStatus(cell.errorStatus);
    field.setDisplayText(cell.text);
  }

  protected _createEditor(row: TableRow): ValueField<TValue, any> {
    return scout.create(StringField, {
      parent: this.table,
      maxLength: this.maxLength,
      multilineText: this.table.multilineText,
      wrapText: this.textWrap
    }) as unknown as ValueField<TValue>;
  }

  updateCellFromEditor(row: TableRow, field: ValueField<TValue>) {
    if (field.errorStatus) {
      this._updateCellFromInvalidEditor(row, field);
    } else {
      this._updateCellFromValidEditor(row, field);
    }
  }

  protected _updateCellFromInvalidEditor(row: TableRow, field: ValueField<TValue>) {
    this.setCellErrorStatus(row, field.errorStatus);
    this.setCellText(row, field.displayText);
  }

  protected _updateCellFromValidEditor(row: TableRow, field: ValueField<TValue>) {
    this.setCellErrorStatus(row, null);
    this.setCellValue(row, field.value);
  }

  /**
   * Override this function to install a specific compare function on a column instance.
   * The default impl. installs a generic comparator working with less than and greater than.
   *
   * @returns whether or not it was possible to install a compare function. If not, client side sorting is disabled.
   */
  installComparator(): boolean {
    return this.comparator.install(this.session);
  }

  /**
   * @returns whether or not it is possible to sort this column. As a side effect a comparator is installed.
   */
  isSortingPossible(): boolean {
    // If installation fails sorting is still possible (in case of the text comparator just without a collator)
    this.installComparator();
    return true;
  }

  compare(row1: TableRow, row2: TableRow): number {
    let cell1 = this.table.cell(this, row1),
      cell2 = this.table.cell(this, row2);

    if (cell1.sortCode !== null || cell2.sortCode !== null) {
      return comparators.NUMERIC.compare(cell1.sortCode, cell2.sortCode);
    }

    let valueA = this.cellValueOrText(row1);
    let valueB = this.cellValueOrText(row2);
    return this.comparator.compare(valueA, valueB);
  }

  /**
   * @deprecated use {@link visible} directly. Will be removed in an upcoming release.
   */
  isVisible(): boolean {
    return this.visible;
  }

  /**
   * Computes the visibility of the column ignoring the compacted state.
   *
   * @returns true if all visible dimensions excluding the dimension `compacted` of the column are true.
   *          So even if the column is compacted, it will return true if all other dimensions are true.
   */
  get visibleIgnoreCompacted(): boolean {
    return this.computeMultiDimensionalProperty('visible', ['compacted']);
  }

  /**
   * Sets the 'default' dimension for the {@link Column.visible} property and recomputes its state.
   *
   * @param visible the new visible value for the 'default' dimension, or an object containing the new visible dimensions.
   * @param redraw true, to redraw the table immediately, false if not. Default is {@link initialized}.
   *               When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}.
   * @see ColumnModel.visible
   */
  setVisible(visible: boolean | Record<string, boolean>, redraw?: boolean) {
    let changed = this.setProperty('visible', visible);
    // If the visibility of a column changes while it is compacted, the CompactColumn needs to update its content -> Always trigger structure change handler so that TableCompactHandler can update it.
    if ((changed || this.compacted) && scout.nvl(redraw, this.initialized)) {
      this.table.onColumnVisibilityChanged();
    }
  }

  /**
   * Sets the 'granted' dimension for the {@link Column.visible} property and recomputes its state.
   *
   * @param visibleGranted the new visible value for the 'granted' dimension.
   * @param redraw true, to redraw the table immediately, false if not. Default is {@link initialized}.
   *               When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}.
   * @see ColumnModel.visibleGranted
   */
  setVisibleGranted(visibleGranted: boolean, redraw?: boolean) {
    this.setVisible(this.extendPropertyDimensions('visible', 'granted', visibleGranted), redraw);
  }

  get visibleGranted(): boolean {
    return this.getProperty('visibleGranted');
  }

  /**
   * Sets the 'displayable' dimension for the {@link Column.visible} property and recomputes its state.
   *
   * @param displayable the new visible value for the 'displayable' dimension.
   * @param redraw true, to redraw the table immediately, false if not. Default is {@link initialized}.
   *               When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}.
   * @see ColumnModel.displayable
   */
  setDisplayable(displayable: boolean, redraw?: boolean) {
    this.setVisible(this.extendPropertyDimensions('visible', 'displayable', displayable), redraw);
  }

  get displayable(): boolean {
    return this.getProperty('displayable');
  }

  /**
   * Sets the 'compacted' dimension for the {@link Column.visible} property and recomputes its state.
   *
   * @param displayable the new visible value for the 'compacted' dimension.
   * @param redraw true, to redraw the table immediately, false if not. Default is {@link initialized}.
   *               When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}.
   * @see ColumnModel.visible
   */
  setCompacted(compacted: boolean, redraw?: boolean) {
    this.setVisible(this.extendPropertyDimensions('visible', 'compacted', compacted), redraw);
  }

  get compacted(): boolean {
    return this.getProperty('compacted');
  }

  setAutoOptimizeWidth(autoOptimizeWidth: boolean) {
    this.setProperty('autoOptimizeWidth', autoOptimizeWidth);
  }

  protected _setAutoOptimizeWidth(autoOptimizeWidth: boolean) {
    this._setProperty('autoOptimizeWidth', autoOptimizeWidth);
    this.autoOptimizeWidthRequired = autoOptimizeWidth;
    if (this.initialized) {
      this.table.columnLayoutDirty = true;
      this.table.invalidateLayoutTree();
    }
  }

  setMaxLength(maxLength: number) {
    this.setProperty('maxLength', maxLength);
  }

  setText(text: string) {
    let changed = this.setProperty('text', text);
    if (changed && this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderIconId(headerIconId: string) {
    let changed = this.setProperty('headerIconId', headerIconId);
    if (changed && this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderCssClass(headerCssClass: string) {
    let oldState = $.extend({}, this);
    let changed = this.setProperty('headerCssClass', headerCssClass);
    if (changed && this.table.header) {
      this.table.header.updateHeader(this, oldState);
    }
  }

  setHeaderHtmlEnabled(headerHtmlEnabled: boolean) {
    let changed = this.setProperty('headerHtmlEnabled', headerHtmlEnabled);
    if (changed && this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderTooltipText(headerTooltipText: string) {
    let changed = this.setProperty('headerTooltipText', headerTooltipText);
    if (changed && this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderTooltipHtmlEnabled(headerTooltipHtmlEnabled: boolean) {
    this.setProperty('headerTooltipHtmlEnabled', headerTooltipHtmlEnabled);
  }

  setTextWrap(textWrap: boolean) {
    let changed = this.setProperty('textWrap', textWrap);
    if (changed && this.table.rendered && this.table.multilineText) { // If multilineText is disabled toggling textWrap has no effect
      // See also table._renderMultilineText(), requires similar operations
      this.autoOptimizeWidthRequired = true;
      this.table.redraw();
    }
  }

  isContentValid(row: TableRow): { valid: boolean; validByMandatory: boolean; errorStatus: Status } {
    const cell = this.cell(row),
      validByErrorStatus = !cell.errorStatus || cell.errorStatus.isValid(),
      validByMandatory = !cell.mandatory || this._hasCellValue(cell);
    return {
      valid: validByErrorStatus && validByMandatory,
      validByMandatory,
      errorStatus: cell.errorStatus
    };
  }

  protected _hasCellValue(cell: Cell<TValue>): boolean {
    return !!cell.value;
  }

  protected _onTableColumnsChanged(event: TableColumnMovedEvent | Event<Table>) {
    if (this.table.visibleColumns().indexOf(this) === 0) {
      this.tableNodeLevel0CellPadding = 28;
      this.expandableIconLevel0CellPadding = 13;
    } else {
      this.tableNodeLevel0CellPadding = 23;
      this.expandableIconLevel0CellPadding = 8;
    }
  }

  realWidthIfAvailable(): number {
    return this._realWidth || this.width;
  }
}
