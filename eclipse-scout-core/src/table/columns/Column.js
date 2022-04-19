/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Cell, ColumnOptimalWidthMeasurer, comparators, EventSupport, FormField, GridData, icons, objects, scout, Status, strings, styles, Table, TableRow, texts} from '../../index';
import $ from 'jquery';

export default class Column {

  constructor() {
    this.autoOptimizeWidth = false;
    this.autoOptimizeWidthRequired = false; // true if content of the column changed and width has to be optimized
    this.autoOptimizeMaxWidth = -1;
    this.cssClass = null;
    this.compacted = false;
    this.editable = false;
    this.removable = false;
    this.modifiable = false;
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
    this.initialized = false;
    this.mandatory = false;
    this.optimalWidthMeasurer = new ColumnOptimalWidthMeasurer(this);
    this.sortActive = false;
    this.sortAscending = true;
    this.sortIndex = -1;
    this.summary = false;
    this.type = 'text';
    this.width = 60;
    this.initialWidth = undefined; // the width the column initially has
    this.minWidth = Column.DEFAULT_MIN_WIDTH; // the minimal width the column can have
    this.showSeparator = true;
    this.table = null;
    this.tableNodeColumn = false;
    this.maxLength = 4000;
    this.text = null;
    this.textWrap = false;
    this.filterType = 'TextColumnUserFilter';
    this.comparator = comparators.TEXT;
    this.displayable = true;
    this.visible = true;
    this.textBased = true;
    this.headerMenuEnabled = true;
    this.tableNodeLevel0CellPadding = 28;
    this.expandableIconLevel0CellPadding = 13;
    this.nodeColumnCandidate = true;

    this.events = this._createEventSupport();

    this._tableColumnsChangedHandler = this._onTableColumnsChanged.bind(this);
    // Contains the width the cells of the column really have (only set in Chrome due to a Chrome bug, see Table._updateRealColumnWidths)
    this._realWidth = null;
    this.$header = null; // Set by TableHeader.js
    this.$separator = null;
  }

  static DEFAULT_MIN_WIDTH = 60;
  static SMALL_MIN_WIDTH = 38;
  static NARROW_MIN_WIDTH = 34;

  init(model) {
    this.session = model.session;

    // Copy all properties from model to this
    $.extend(this, model);

    // Initial width is only sent if it differs from width
    if (this.initialWidth === undefined) {
      this.initialWidth = scout.nvl(this.width, 0);
    }
    this._init(model);
    this.initialized = true;
  }

  /**
   * Override this function in order to implement custom init logic.
   */
  _init(model) {
    texts.resolveTextProperty(this, 'text');
    texts.resolveTextProperty(this, 'headerTooltipText');
    icons.resolveIconProperty(this, 'headerIconId');
    this._setTable(this.table);
    this._setDisplayable(this.displayable);
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
  _destroy() {
    // NOP
  }

  _setTable(table) {
    if (this.table) {
      this.table.off('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
    }
    this.table = table;
    if (this.table) {
      this.table.on('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
    }
  }

  /**
   * Converts the vararg if it is of type string to an object with
   * a property 'text' with the original value.
   *
   * Example:
   * 'My Company' --> { text: 'MyCompany'; }
   *
   * @see JsonCell.java
   * @param {Cell|string|number|object} vararg either a Cell instance or a scalar value
   */
  initCell(vararg, row) {
    let cell = this._ensureCell(vararg);
    this._initCell(cell);

    // If a text is provided, use that text instead of using formatValue to generate a text based on the value
    if (objects.isNullOrUndefined(cell.text)) {
      this._updateCellText(row, cell);
    }
    return cell;
  }

  /**
   * Ensures that a Cell instance is returned. When vararg is a scalar value a new Cell instance is created and
   * the value is set as cell.value property.
   *
   * @param {Cell|string|number|object} vararg either a Cell instance or a scalar value
   * @returns {*}
   * @private
   */
  _ensureCell(vararg) {
    let cell;

    if (vararg instanceof Cell) {
      cell = vararg;

      // value may be set but may have the wrong type (e.g. text instead of date) -> ensure type
      cell.value = this._parseValue(cell.value);
    } else {
      // in this case 'vararg' is only a scalar value, typically a string
      cell = scout.create('Cell', {
        value: this._parseValue(vararg)
      });
    }

    return cell;
  }

  /**
   * Override this method to create a value based on the given scalar value.
   */
  _parseValue(scalar) {
    return scalar;
  }

  _updateCellText(row, cell) {
    let value = cell.value;
    if (!row) {
      // row is omitted when creating aggregate cells
      return;
    }

    let returned = this._formatValue(value, row);
    if (returned && typeof returned.promise === 'function') {
      // Promise is returned -> set display text later
      this.setCellTextDeferred(returned, row, cell);
    } else {
      this.setCellText(row, returned, cell);
    }
  }

  _formatValue(value, row) {
    return scout.nvl(value, '');
  }

  /**
   * If cell does not define properties, use column values.
   * Override this function to implement type specific init cell behavior.
   *
   * @param {Cell} cell
   */
  _initCell(cell) {
    cell.cssClass = scout.nvl(cell.cssClass, this.cssClass);
    cell.editable = scout.nvl(cell.editable, this.editable);
    cell.horizontalAlignment = scout.nvl(cell.horizontalAlignment, this.horizontalAlignment);
    cell.htmlEnabled = scout.nvl(cell.htmlEnabled, this.htmlEnabled);
    cell.mandatory = scout.nvl(cell.mandatory, this.mandatory);
    return cell;
  }

  buildCellForRow(row) {
    let cell = this.cell(row);
    return this.buildCell(cell, row);
  }

  buildCellForAggregateRow(aggregateRow) {
    let cell;
    if (this.grouped) {
      let refRow = (this.table.groupingStyle === Table.GroupingStyle.TOP ? aggregateRow.nextRow : aggregateRow.prevRow);
      cell = this.createAggrGroupCell(refRow);
    } else {
      let aggregateValue = aggregateRow.contents[this.table.columns.indexOf(this)];
      cell = this.createAggrValueCell(aggregateValue);
    }
    return this.buildCell(cell, {});
  }

  buildCell(cell, row) {
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

    let content;
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

    if (tableNodeColumn && row._expandable) {
      this.tableNodeColumn = true;
      content = this._expandIcon(row.expanded, rowPadding) + content;
      if (row.expanded) {
        cssClass += ' expanded';
      }
    }

    return this._buildCell(cell, content, style, cssClass);
  }

  _buildCell(cell, content, style, cssClass) {
    return '<div class="' + cssClass + '" style="' + style + '">' + content + '</div>';
  }

  _expandIcon(expanded, rowPadding) {
    let style = 'padding-left: ' + (rowPadding + this.expandableIconLevel0CellPadding) + 'px';
    let cssClasses = 'table-row-control';
    if (expanded) {
      cssClasses += ' expanded';
    }
    return '<div class="' + cssClasses + '" style="' + style + '"></div>';
  }

  _icon(iconId, hasText) {
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
      return '<span class="' + icon.appendCssClass(cssClass) + '">' + icon.iconCharacter + '</span>';
    }
    cssClass += ' image-icon';
    return '<img class="' + cssClass + '" src="' + icon.iconUrl + '">';
  }

  _text(cell) {
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

  _cellCssClass(cell, tableNode) {
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

  _cellStyle(cell, tableNodeColumn, rowPadding) {
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

  onMouseUp(event, $row) {
    let row = $row.data('row'),
      cell = this.cell(row);

    if (this.isCellEditable(row, cell, event)) {
      this.table.prepareCellEdit(this, row, true);
    }
  }

  isCellEditable(row, cell, event) {
    return this.table.enabledComputed && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey;
  }

  startCellEdit(row, field) {
    let popup,
      $row = row.$row,
      cell = this.cell(row),
      $cell = this.table.$cell(this, $row);

    cell.field = field;
    // Override field alignment with the cell's alignment
    cell.field.gridData.horizontalAlignment = cell.horizontalAlignment;
    popup = this._createEditorPopup(row, cell);
    popup.$anchor = $cell;
    popup.open(this.table.$data);
    return popup;
  }

  _createEditorPopup(row, cell) {
    return scout.create('CellEditorPopup', {
      parent: this.table,
      column: this,
      row: row,
      cell: cell
    });
  }

  /**
   * @returns {Cell} the cell object for this column from the given row.
   */
  cell(row) {
    return this.table.cell(this, row);
  }

  /**
   * Creates an artificial cell from the properties relevant for the column header.
   * @returns {Cell}
   */
  headerCell() {
    return scout.create('Cell', {
      value: this.text,
      text: this.text,
      iconId: this.headerIconId,
      cssClass: this.headerCssClass,
      tooltipText: this.headerTooltipText,
      htmlEnabled: this.headerHtmlEnabled
    });
  }

  /**
   * @returns {Cell} the cell object for this column from the first selected row in the table.
   */
  selectedCell() {
    let selectedRow = this.table.selectedRow();
    return this.table.cell(this, selectedRow);
  }

  /**
   * @param row
   * @returns {string|*} the text of the cell if {@link Column.textBased} is true, otherwise the value of the cell.
   */
  cellValueOrText(row) {
    if (this.textBased) {
      return this.table.cellText(this, row);
    }
    return this.table.cellValue(this, row);
  }

  cellValue(row) {
    return this.table.cellValue(this, row);
  }

  cellText(row) {
    return this.table.cellText(this, row);
  }

  /**
   * @returns {*} the cell value to be used for grouping and filtering (chart, column filter).
   */
  cellValueOrTextForCalculation(row) {
    let cell = this.cell(row);
    let value = this.cellValueOrText(row);
    if (objects.isNullOrUndefined(value)) {
      return null;
    }
    return this._preprocessValueOrTextForCalculation(value, cell);
  }

  _preprocessValueOrTextForCalculation(value, cell) {
    if (typeof value === 'string') {
      // In case of string columns, value and text are equal -> use _preprocessStringForCalculation to handle html tags and new lines correctly
      return this._preprocessTextForCalculation(value, cell.htmlEnabled);
    }
    return value;
  }

  _preprocessTextForCalculation(text, htmlEnabled) {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled,
      removeNewlines: true,
      trim: true
    });
  }

  /**
   * @returns {string} the cell text to be used for table grouping
   */
  cellTextForGrouping(row) {
    let cell = this.cell(row);
    return this._preprocessTextForGrouping(cell.text, cell.htmlEnabled);
  }

  _preprocessTextForGrouping(text, htmlEnabled) {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled,
      trim: true
    });
  }

  /**
   * @returns {string} the cell text to be used for the text filter
   */
  cellTextForTextFilter(row) {
    let cell = this.cell(row);
    return this._preprocessTextForTextFilter(cell.text, cell.htmlEnabled);
  }

  _preprocessTextForTextFilter(text, htmlEnabled) {
    return this._preprocessText(text, {
      removeHtmlTags: htmlEnabled
    });
  }

  /**
   * @returns {string} the cell text to be used for the table row detail.
   */
  cellTextForRowDetail(row) {
    let cell = this.cell(row);

    return this._preprocessText(this._text(cell), {
      removeHtmlTags: cell.htmlEnabled
    });
  }

  /**
   * Removes html tags, converts to single line, removes leading and trailing whitespaces.
   */
  _preprocessText(text, options) {
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

  setCellValue(row, value) {
    let cell = this.cell(row);
    this._setCellValue(row, value, cell);
    this._updateCellText(row, cell);
  }

  _setCellValue(row, value, cell) {
    // value may have the wrong type (e.g. text instead of date) -> ensure type
    value = this._parseValue(value);

    // Only update row status when value changed.
    // Cell text needs to be updated even if value did not change
    // (text may cause an invalid value that won't be saved on the cell, reverting to the valid value needs to update the text again)
    if (cell.value !== value && row.status === TableRow.Status.NON_CHANGED) {
      row.status = TableRow.Status.UPDATED;
    }

    cell.setValue(value);
  }

  setCellTextDeferred(promise, row, cell) {
    promise
      .done(text => {
        this.setCellText(row, text, cell);
      })
      .fail(error => {
        this.setCellText(row, '', cell);
        $.log.error('Could not resolve cell text for value ' + cell.value, error);
      });

    // (then) promises always resolve asynchronously which means the text will always be set later after row is initialized and will generate an update row event.
    // To make sure not every cell update will render the viewport (which is an expensive operation), the update is buffered and done as soon as all promises resolve.
    this.table.updateBuffer.pushPromise(promise);
  }

  setCellText(row, text, cell) {
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

  setCellErrorStatus(row, errorStatus, cell) {
    if (!cell) {
      cell = this.cell(row);
    }
    cell.setErrorStatus(errorStatus);
  }

  setCellIconId(row, iconId) {
    let cell = this.cell(row);
    if (cell.iconId === iconId) {
      return;
    }
    cell.setIconId(iconId);
    if (row.initialized) {
      this.table.updateRow(row);
    }
  }

  setHorizontalAlignment(hAlign) {
    if (this.horizontalAlignment === hAlign) {
      return;
    }
    this.horizontalAlignment = hAlign;

    this.table.rows.forEach(row => {
      this.cell(row).setHorizontalAlignment(hAlign);
    });

    this.table.updateRows(this.table.rows);

    if (this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setEditable(editable) {
    if (this.editable === editable) {
      return;
    }
    this.editable = editable;

    this.table.rows.forEach(row => {
      this.cell(row).setEditable(editable);
    });

    this.table.updateRows(this.table.rows);
  }

  setMandatory(mandatory) {
    if (this.mandatory === mandatory) {
      return;
    }
    this.mandatory = mandatory;

    this.table.rows.forEach(row => {
      this.cell(row).setMandatory(mandatory);
    });

    this.table.updateRows(this.table.rows);
  }

  setCssClass(cssClass) {
    if (this.cssClass === cssClass) {
      return;
    }

    this.cssClass = cssClass;

    this.table.rows.forEach(function(row) {
      this.cell(row).setCssClass(cssClass);
    }, this);

    this.table.updateRows(this.table.rows);
  }

  setWidth(width) {
    if (this.width === width) {
      return;
    }
    this.table.resizeColumn(this, width);
  }

  createAggrGroupCell(row) {
    let cell = this.cell(row);
    return this.initCell(scout.create('Cell', {
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

  createAggrValueCell(value) {
    return this.createAggrEmptyCell();
  }

  createAggrEmptyCell() {
    return this.initCell(scout.create('Cell', {
      empty: true,
      cssClass: 'table-aggregate-cell'
    }));
  }

  calculateOptimalWidth() {
    return this.optimalWidthMeasurer.measure();
  }

  /**
   * Returns a type specific column user-filter. The default impl. returns a ColumnUserFilter.
   * Sub-classes that must return another type, must simply change the value of the 'filterType' property.
   */
  createFilter(model) {
    return scout.create(this.filterType, {
      session: this.session,
      table: this.table,
      column: this
    });
  }

  /**
   * Returns a table header menu. Sub-classes can override this method to create a column specific table header menu.
   */
  createTableHeaderMenu(tableHeader) {
    let $header = this.$header;
    return scout.create('TableHeaderMenu', {
      parent: tableHeader,
      column: $header.data('column'),
      tableHeader: tableHeader,
      $anchor: $header
    });
  }

  /**
   * @returns a field instance used as editor when a cell of this column is in edit mode.
   */
  createEditor(row) {
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
  _initEditorField(field, cell) {
    if (cell.errorStatus) {
      this._updateEditorFromInvalidCell(field, cell);
    } else {
      this._updateEditorFromValidCell(field, cell);
    }
  }

  _updateEditorFromValidCell(field, cell) {
    field.setValue(cell.value);
  }

  _updateEditorFromInvalidCell(field, cell) {
    field.setErrorStatus(cell.errorStatus);
    field.setDisplayText(cell.text);
  }

  _createEditor() {
    return scout.create('StringField', {
      parent: this.table,
      maxLength: this.maxLength,
      multilineText: this.table.multilineText,
      wrapText: this.textWrap
    });
  }

  updateCellFromEditor(row, field) {
    if (field.errorStatus) {
      this._updateCellFromInvalidEditor(row, field);
    } else {
      this._updateCellFromValidEditor(row, field);
    }
  }

  _updateCellFromInvalidEditor(row, field) {
    this.setCellErrorStatus(row, field.errorStatus);
    this.setCellText(row, field.displayText);
  }

  _updateCellFromValidEditor(row, field) {
    this.setCellErrorStatus(row, null);
    this.setCellValue(row, field.value);
  }

  /**
   * Override this function to install a specific compare function on a column instance.
   * The default impl. installs a generic comparator working with less than and greater than.
   *
   * @returns whether or not it was possible to install a compare function. If not, client side sorting is disabled.
   */
  installComparator() {
    return this.comparator.install(this.session);
  }

  /**
   * @returns {boolean} whether or not it is possible to sort this column.
   * As a side effect a comparator is installed.
   */
  isSortingPossible() {
    // If installation fails sorting is still possible (in case of the text comparator just without a collator)
    this.installComparator();
    return true;
  }

  compare(row1, row2) {
    let cell1 = this.table.cell(this, row1),
      cell2 = this.table.cell(this, row2);

    if (cell1.sortCode !== null || cell2.sortCode !== null) {
      return comparators.NUMERIC.compare(cell1.sortCode, cell2.sortCode);
    }

    let valueA = this.cellValueOrText(row1);
    let valueB = this.cellValueOrText(row2);
    return this.comparator.compare(valueA, valueB);
  }

  isVisible() {
    return this.displayable && this.visible && !this.compacted;
  }

  /**
   *
   * @param {boolean} visible
   * @param {boolean} [redraw] true, to redraw the table immediately, false if not.
   * When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}. Default is true.
   */
  setVisible(visible, redraw) {
    if (this.visible === visible) {
      return;
    }
    this._setVisible(visible, redraw);
  }

  _setVisible(visible, redraw) {
    this.visible = visible;
    if (scout.nvl(redraw, this.initialized)) {
      this.table.onColumnVisibilityChanged();
    }
  }

  /**
   *
   * @param {boolean} displayable
   * @param {boolean} [redraw] true, to redraw the table immediately, false if not.
   * When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}. Default is true.
   */
  setDisplayable(displayable, redraw) {
    if (this.displayable === displayable) {
      return;
    }
    this._setDisplayable(displayable, redraw);
  }

  _setDisplayable(displayable, redraw) {
    this.displayable = displayable;
    if (scout.nvl(redraw, this.initialized)) {
      this.table.onColumnVisibilityChanged();
    }
  }

  /**
   *
   * @param {boolean} compacted
   * @param {boolean} [redraw] true, to redraw the table immediately, false if not.
   * When false is used, the redraw needs to be triggered manually using {@link Table.onColumnVisibilityChanged}. Default is true.
   */
  setCompacted(compacted, redraw) {
    if (this.compacted === compacted) {
      return;
    }
    this._setCompacted(compacted, redraw);
  }

  _setCompacted(compacted, redraw) {
    this.compacted = compacted;
    if (scout.nvl(redraw, this.initialized)) {
      this.table.onColumnVisibilityChanged();
    }
  }

  setAutoOptimizeWidth(autoOptimizeWidth) {
    if (this.autoOptimizeWidth === autoOptimizeWidth) {
      return;
    }
    this._setAutoOptimizeWidth(autoOptimizeWidth);
  }

  _setAutoOptimizeWidth(autoOptimizeWidth) {
    this.autoOptimizeWidth = autoOptimizeWidth;
    this.autoOptimizeWidthRequired = autoOptimizeWidth;
    if (this.initialized) {
      this.table.columnLayoutDirty = true;
      this.table.invalidateLayoutTree();
    }
  }

  setMaxLength(maxLength) {
    this.maxLength = maxLength;
  }

  setText(text) {
    if (this.text === text) {
      return;
    }
    this.text = text;
    if (this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderIconId(headerIconId) {
    if (this.headerIconId === headerIconId) {
      return;
    }
    this.headerIconId = headerIconId;
    if (this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderCssClass(headerCssClass) {
    if (this.headerCssClass === headerCssClass) {
      return;
    }
    let oldState = $.extend({}, this);
    this.headerCssClass = headerCssClass;
    if (this.table.header) {
      this.table.header.updateHeader(this, oldState);
    }
  }

  setHeaderHtmlEnabled(headerHtmlEnabled) {
    if (this.headerHtmlEnabled === headerHtmlEnabled) {
      return;
    }
    this.headerHtmlEnabled = headerHtmlEnabled;
    if (this.table.header) {
      this.table.header.updateHeader(this);
    }
  }

  setHeaderTooltipText(headerTooltipText) {
    this.headerTooltipText = headerTooltipText;
  }

  setHeaderTooltipHtmlEnabled(headerTooltipHtmlEnabled) {
    this.headerTooltipHtmlEnabled = headerTooltipHtmlEnabled;
  }

  setTextWrap(textWrap) {
    if (this.textWrap === textWrap) {
      return;
    }
    this.textWrap = textWrap;
    if (this.table.rendered && this.table.multilineText) { // If multilineText is disabled toggling textWrap has no effect
      // See also table._renderMultilineText(), requires similar operations
      this.autoOptimizeWidthRequired = true;
      this.table._redraw();
      this.table.invalidateLayoutTree();
    }
  }

  isContentValid(row) {
    let cell = this.cell(row);
    let validByErrorStatus = !cell.errorStatus || cell.errorStatus.severity !== Status.Severity.ERROR;
    let validByMandatory = !cell.mandatory || this._hasCellValue(cell);
    return {
      valid: validByErrorStatus && validByMandatory,
      validByErrorStatus: validByErrorStatus,
      validByMandatory: validByMandatory
    };
  }

  _hasCellValue(cell) {
    return !!cell.value;
  }

  _onTableColumnsChanged(event) {
    if (this.table.visibleColumns().indexOf(this) === 0) {
      this.tableNodeLevel0CellPadding = 28;
      this.expandableIconLevel0CellPadding = 13;
    } else {
      this.tableNodeLevel0CellPadding = 23;
      this.expandableIconLevel0CellPadding = 8;
    }
  }

  _realWidthIfAvailable() {
    return this._realWidth || this.width;
  }

  // --- Event handling methods ---
  _createEventSupport() {
    return new EventSupport();
  }

  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }

  one(type, func) {
    this.events.one(type, func);
  }

  on(type, func) {
    return this.events.on(type, func);
  }

  off(type, func) {
    this.events.off(type, func);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }

  /**
   * Adds an event handler using {@link #one()} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when(type) {
    return this.events.when(type);
  }
}
