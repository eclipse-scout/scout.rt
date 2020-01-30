/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Column = function() {
  this.autoOptimizeWidth = false;
  this.autoOptimizeWidthRequired = false; // true if content of the column changed and width has to be optimized
  this.autoOptimizeMaxWidth = -1;
  this.cssClass = null;
  this.editable = false;
  this.removable = false;
  this.modifiable = false;
  this.fixedWidth = false;
  this.fixedPosition = false;
  this.grouped = false;
  this.headerHtmlEnabled = false;
  this.horizontalAlignment = -1;
  this.htmlEnabled = false;
  this.index = -1;
  this.initialized = false;
  this.mandatory = false;
  this.optimalWidthMeasurer = new scout.ColumnOptimalWidthMeasurer(this);
  this.sortActive = false;
  this.sortAscending = true;
  this.sortIndex = -1;
  this.summary = false;
  this.type = 'text';
  this.width = 60;
  this.initialWidth = undefined; // the width the column initially has
  this.prefMinWidth = null;
  this.minWidth = scout.Column.DEFAULT_MIN_WIDTH; // the minimal width the column can have
  this.showSeparator = true;
  this.table = null;
  this.tableNodeColumn = false;
  this.maxLength = 4000;
  this.textWrap = false;
  this.filterType = 'TextColumnUserFilter';
  this.comparator = scout.comparators.TEXT;
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
};

scout.Column.DEFAULT_MIN_WIDTH = 60;
scout.Column.NARROW_MIN_WIDTH = 32; // for columns without text (icon, check box)

scout.Column.prototype.init = function(model) {
  this.session = model.session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Initial width is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = scout.nvl(this.width, 0);
  }
  this._init(model);
  this.initialized = true;
};

/**
 * Override this function in order to implement custom init logic.
 */
scout.Column.prototype._init = function(model) {
  scout.texts.resolveTextProperty(this, 'text');
  scout.texts.resolveTextProperty(this, 'headerTooltipText');
  scout.icons.resolveIconProperty(this, 'headerIconId');
  this._setTable(this.table);
  this._setDisplayable(this.displayable);
  this._setAutoOptimizeWidth(this.autoOptimizeWidth);
  // no need to call setEditable here. cell propagation is done in _initCell
};

scout.Column.prototype.destroy = function() {
  this._destroy();
  this._setTable(null);
};

/**
 * Override this function in order to implement custom destroy logic.
 */
scout.Column.prototype._destroy = function(model) {
  // NOP
};

scout.Column.prototype._setTable = function(table) {
  if (this.table) {
    this.table.off('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
  }
  this.table = table;
  if (this.table) {
    this.table.on('columnMoved columnStructureChanged', this._tableColumnsChangedHandler);
  }
};

/**
 * Converts the vararg if it is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 * @param {scout.Cell|string|number|object} vararg either a Cell instance or a scalar value
 */
scout.Column.prototype.initCell = function(vararg, row) {
  var cell = this._ensureCell(vararg);
  this._initCell(cell);

  // If a text is provided, use that text instead of using formatValue to generate a text based on the value
  if (scout.objects.isNullOrUndefined(cell.text)) {
    this._updateCellText(row, cell);
  }
  return cell;
};

/**
 * Ensures that a Cell instance is returned. When vararg is a scalar value a new Cell instance is created and
 * the value is set as cell.value property.
 *
 * @param {scout.Cell|string|number|object} vararg either a Cell instance or a scalar value
 * @returns {*}
 * @private
 */
scout.Column.prototype._ensureCell = function(vararg) {
  var cell;

  if (vararg instanceof scout.Cell) {
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
};

/**
 * Override this method to create a value based on the given scalar value.
 */
scout.Column.prototype._parseValue = function(scalar) {
  return scalar;
};

scout.Column.prototype._updateCellText = function(row, cell) {
  var value = cell.value;
  if (!row) {
    // row is omitted when creating aggregate cells
    return;
  }

  var returned = this._formatValue(value);
  if (returned && $.isFunction(returned.promise)) {
    // Promise is returned -> set display text later
    this.setCellTextDeferred(returned, row, cell);
  } else {
    this.setCellText(row, returned, cell);
  }
};

scout.Column.prototype._formatValue = function(value) {
  return scout.nvl(value, '');
};

/**
 * If cell does not define properties, use column values.
 * Override this function to impl. type specific init cell behavior.
 *
 * @param {scout.Cell} cell
 */
scout.Column.prototype._initCell = function(cell) {
  cell.cssClass = scout.nvl(cell.cssClass, this.cssClass);
  cell.editable = scout.nvl(cell.editable, this.editable);
  cell.horizontalAlignment = scout.nvl(cell.horizontalAlignment, this.horizontalAlignment);
  cell.htmlEnabled = scout.nvl(cell.htmlEnabled, this.htmlEnabled);
  cell.mandatory = scout.nvl(cell.mandatory, this.mandatory);
  return cell;
};

scout.Column.prototype.buildCellForRow = function(row) {
  var cell = this.cell(row);
  return this.buildCell(cell, row);
};

scout.Column.prototype.buildCellForAggregateRow = function(aggregateRow) {
  var cell;
  if (this.grouped) {
    var refRow = (this.table.groupingStyle === scout.Table.GroupingStyle.TOP ? aggregateRow.nextRow : aggregateRow.prevRow);
    cell = this.createAggrGroupCell(refRow);
  } else {
    var aggregateValue = aggregateRow.contents[this.table.columns.indexOf(this)];
    cell = this.createAggrValueCell(aggregateValue);
  }
  return this.buildCell(cell, {});
};

scout.Column.prototype.buildCell = function(cell, row) {
  scout.assertParameter('cell', cell, scout.Cell);

  var tableNodeColumn = this.table.isTableNodeColumn(this),
    rowPadding = 0;

  if (tableNodeColumn) {
    rowPadding = this.table._calcRowLevelPadding(row);
  }

  var text = this._text(cell);
  var iconId = cell.iconId;
  var icon = this._icon(iconId, !!text) || '';
  var cssClass = this._cellCssClass(cell, tableNodeColumn);
  var style = this._cellStyle(cell, tableNodeColumn, rowPadding);

  if (cell.errorStatus) {
    row.hasError = true;
  }

  var content;
  if (!text && !icon) {
    // If every cell of a row is empty the row would collapse, using nbsp makes sure the row is as height as the others even if it is empty
    content = '&nbsp;';
    cssClass = scout.strings.join(' ', cssClass, 'empty');
  } else {
    content = icon + text;
  }

  if (tableNodeColumn && row._expandable) {
    this.tableNodeColumn = true;
    content = this._expandIcon(row.expanded, rowPadding) + content;
    if (row.expanded) {
      cssClass += ' expanded';
    }
  }

  return this._buildCell(content, style, cssClass);
};

scout.Column.prototype._buildCell = function(content, style, cssClass) {
  var cellHtml = '';
  cellHtml += '<div class="' + cssClass + '" style="' + style + '"' + scout.device.unselectableAttribute.string + '>';
  if (scout.device.tableAdditionalDivRequired) {
    cellHtml += '<div class="width-fix" style="max-width: ' + (this.width - this.table.cellHorizontalPadding - 2 /* unknown IE9 extra space */ ) + 'px; ' + '">';
    // same calculation in scout.Table.prototype.resizeColumn
  }
  cellHtml += content;
  if (scout.device.tableAdditionalDivRequired) {
    cellHtml += '</div>';
  }
  cellHtml += '</div>';
  return cellHtml;
};

scout.Column.prototype._expandIcon = function(expanded, rowPadding) {
  var style = 'padding-left: ' + (rowPadding + this.expandableIconLevel0CellPadding) + 'px';
  var cssClasses = 'table-row-control';
  if (expanded) {
    cssClasses += ' expanded';
  }
  var html = '<div class="' + cssClasses + '" style="' + style + '"></div>';
  return html;
};

scout.Column.prototype._icon = function(iconId, hasText) {
  var cssClass, icon;
  if (!iconId) {
    return;
  }
  cssClass = 'table-cell-icon';
  if (hasText) {
    cssClass += ' with-text';
  }
  icon = scout.icons.parseIconId(iconId);
  if (icon.isFontIcon()) {
    cssClass += ' font-icon';
    return '<span class="' + icon.appendCssClass(cssClass) + '">' + icon.iconCharacter + '</span>';
  } else {
    cssClass += ' image-icon';
    return '<img class="' + cssClass + '" src="' + icon.iconUrl + '">';
  }
};

scout.Column.prototype._text = function(cell) {
  var text = cell.text || '';

  if (!cell.htmlEnabled) {
    text = cell.encodedText() || '';
    if (this.table.multilineText) {
      text = scout.strings.nl2br(text, false);
    }
  }

  return text;
};

scout.Column.prototype._cellCssClass = function(cell, tableNode) {
  var cssClass = 'table-cell';
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
  cssClass += ' halign-' + scout.Table.parseHorizontalAlignment(cell.horizontalAlignment);
  var visibleColumns = this.table.visibleColumns();
  var overAllColumnPosition = visibleColumns.indexOf(this);
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
};

scout.Column.prototype._cellStyle = function(cell, tableNodeColumn, rowPadding) {
  var style,
    width = this.width;

  if (width === 0) {
    return 'display: none;';
  }
  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
  if (tableNodeColumn) {
    // calculate padding
    style += ' padding-left: ' + (this.tableNodeLevel0CellPadding + rowPadding) + 'px; ';
  }
  style += scout.styles.legacyStyle(cell);
  return style;
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    cell = this.cell(row);

  if (this.isCellEditable(row, cell, event)) {
    this.table.prepareCellEdit(this, row, true);
  }
};

scout.Column.prototype.isCellEditable = function(row, cell, event) {
  return this.table.enabledComputed && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey;
};

scout.Column.prototype.startCellEdit = function(row, field) {
  var popup,
    $row = row.$row,
    cell = this.cell(row),
    $cell = this.table.$cell(this, $row);

  cell.field = field;
  // Override field alignment with the cell's alignment
  cell.field.gridData.horizontalAlignment = cell.horizontalAlignment;

  popup = scout.create('CellEditorPopup', {
    parent: this.table,
    column: this,
    row: row,
    cell: cell
  });
  popup.$anchor = $cell;
  popup.open(this.table.$data);
  return popup;
};

/**
 * @returns the cell object for this column from the given row.
 */
scout.Column.prototype.cell = function(row) {
  return this.table.cell(this, row);
};

/**
 * @returns the cell object for this column from the first selected row in the table.
 */
scout.Column.prototype.selectedCell = function() {
  var selectedRow = this.table.selectedRow();
  return this.table.cell(this, selectedRow);
};

scout.Column.prototype.cellValueOrText = function(row) {
  if (this.textBased) {
    return this.table.cellText(this, row);
  } else {
    return this.table.cellValue(this, row);
  }
};

/**
 * @returns the cell value to be used for grouping and filtering (chart, column filter).
 */
scout.Column.prototype.cellValueOrTextForCalculation = function(row) {
  var cell = this.cell(row);
  var value = this.cellValueOrText(row);
  if (scout.objects.isNullOrUndefined(value)) {
    return null;
  }
  return this._preprocessValueOrTextForCalculation(value, cell);
};

scout.Column.prototype._preprocessValueOrTextForCalculation = function(value, cell) {
  if (typeof value === 'string') {
    // In case of string columns, value and text are equal -> use _preprocessStringForCalculation to handle html tags and new lines correctly
    return this._preprocessTextForCalculation(value, cell.htmlEnabled);
  }
  return value;
};

scout.Column.prototype._preprocessTextForCalculation = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    removeNewlines: true,
    trim: true
  });
};

/**
 * @returns the cell text to be used for table grouping
 */
scout.Column.prototype.cellTextForGrouping = function(row) {
  var cell = this.cell(row);
  return this._preprocessTextForGrouping(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessTextForGrouping = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled,
    trim: true
  });
};

/**
 * @returns the cell text to be used for the text filter
 */
scout.Column.prototype.cellTextForTextFilter = function(row) {
  var cell = this.cell(row);
  return this._preprocessTextForTextFilter(cell.text, cell.htmlEnabled);
};

scout.Column.prototype._preprocessTextForTextFilter = function(text, htmlEnabled) {
  return this._preprocessText(text, {
    removeHtmlTags: htmlEnabled
  });
};

/**
 * @returns the cell text to be used for the table row detail.
 */
scout.Column.prototype.cellTextForRowDetail = function(row) {
  var cell = this.cell(row);

  return this._preprocessText(this._text(cell), {
    removeHtmlTags: cell.htmlEnabled
  });
};

/**
 * Removes html tags, converts to single line, removes leading and trailing whitespaces.
 */
scout.Column.prototype._preprocessText = function(text, options) {
  if (text === null || text === undefined) {
    return text;
  }
  options = options || {};
  if (options.removeHtmlTags) {
    text = scout.strings.plainText(text);
  }
  if (options.removeNewlines) {
    text = text.replace('\n', ' ');
  }
  if (options.trim) {
    text = text.trim();
  }
  return text;
};

scout.Column.prototype.setCellValue = function(row, value) {
  var cell = this.cell(row);

  // value may have the wrong type (e.g. text instead of date) -> ensure type
  value = this._parseValue(value);

  // do not trigger value change when value did not change
  if (cell.value === value) {
    return;
  }

  cell.setValue(value);
  if (row.status === scout.TableRow.Status.NON_CHANGED) {
    row.status = scout.TableRow.Status.UPDATED;
  }
  this._updateCellText(row, cell);
};

scout.Column.prototype.setCellTextDeferred = function(promise, row, cell) {
  promise
    .done(function(text) {
      this.setCellText(row, text, cell);
    }.bind(this))
    .fail(function(error) {
      this.setCellText(row, '', cell);
      $.log.error('Could not resolve cell text for value ' + cell.value, error);
    }.bind(this));

  // (then) promises always resolve asynchronously which means the text will always be set later after row is initialized and will generate an update row event.
  // To make sure not every cell update will render the viewport (which is an expensive operation), the update is buffered and done as soon as all promises resolve.
  this.table.updateBuffer.pushPromise(promise);
};

scout.Column.prototype.setCellText = function(row, text, cell) {
  if (!cell) {
    cell = this.cell(row);
  }
  cell.setText(text);

  // Don't update row while initializing (it is either added to the table later, or being added / updated right now)
  // The check for "this.table" is necessary, because the column could already have been destroyed (method is called
  // asynchronously by setCellTextDeferred).
  if (row.initialized && this.table) {
    this.table.updateRow(row);
  }
};

scout.Column.prototype.setHorizontalAlignment = function(hAlign) {
  if (this.horizontalAlignment === hAlign) {
    return;
  }
  this.horizontalAlignment = hAlign;

  this.table.rows.forEach(function(row) {
    this.cell(row).setHorizontalAlignment(hAlign);
  }.bind(this));

  this.table.updateRows(this.table.rows);
};

scout.Column.prototype.setEditable = function(editable) {
  if (this.editable === editable) {
    return;
  }
  this.editable = editable;

  this.table.rows.forEach(function(row) {
    this.cell(row).setEditable(editable);
  }.bind(this));

  this.table.updateRows(this.table.rows);
};

scout.Column.prototype.setMandatory = function(mandatory) {
  if (this.mandatory === mandatory) {
    return;
  }
  this.mandatory = mandatory;

  this.table.rows.forEach(function(row) {
    this.cell(row).setMandatory(mandatory);
  }.bind(this));

  this.table.updateRows(this.table.rows);
};

scout.Column.prototype.setCssClass = function(cssClass) {
  if (this.cssClass === cssClass) {
    return;
  }

  this.cssClass = cssClass;

  this.table.rows.forEach(function(row) {
    this.cell(row).setCssClass(cssClass);
  }, this);

  this.table.updateRows(this.table.rows);
};

scout.Column.prototype.setWidth = function(width) {
  if (this.width === width) {
    return;
  }
  this.table.resizeColumn(this, width);
};

scout.Column.prototype.createAggrGroupCell = function(row) {
  var cell = this.cell(row);
  return this.initCell(scout.create('Cell', {
    // value necessary for value based columns (e.g. checkbox column)
    value: cell.value,
    text: this.cellTextForGrouping(row),
    iconId: cell.iconId,
    horizontalAlignment: this.horizontalAlignment,
    cssClass: 'table-aggregate-cell'
  }));
};

scout.Column.prototype.createAggrValueCell = function(value) {
  return this.createAggrEmptyCell();
};

scout.Column.prototype.createAggrEmptyCell = function() {
  return this.initCell(scout.create('Cell', {
    empty: true
  }));
};

scout.Column.prototype.calculateOptimalWidth = function() {
  return this.optimalWidthMeasurer.measure();
};

/**
 * Returns a type specific column user-filter. The default impl. returns a ColumnUserFilter.
 * Sub-classes that must return another type, must simply change the value of the 'filterType' property.
 */
scout.Column.prototype.createFilter = function(model) {
  return scout.create(this.filterType, {
    session: this.session,
    table: this.table,
    column: this
  });
};

/**
 * Returns a table header menu. Sub-classes can override this method to create a column specific table header menu.
 */
scout.Column.prototype.createTableHeaderMenu = function(tableHeader) {
  var $header = this.$header;
  return scout.create('TableHeaderMenu', {
    parent: tableHeader,
    column: $header.data('column'),
    tableHeader: tableHeader,
    $anchor: $header,
    focusableContainer: true
  });
};

/**
 * @returns a field instance used as editor when a cell of this column is in edit mode.
 */
scout.Column.prototype.createEditor = function(row) {
  var field = this._createEditor(row);
  var cell = this.cell(row);
  field.setLabelVisible(false);
  field.setValue(cell.value);
  field.setFieldStyle(scout.FormField.FieldStyle.CLASSIC);
  var hints = new scout.GridData(field.gridDataHints);
  hints.horizontalAlignment = cell.horizontalAlignment;
  field.setGridDataHints(hints);
  return field;
};

scout.Column.prototype._createEditor = function() {
  return scout.create('StringField', {
    parent: this.table,
    maxLength: this.maxLength,
    multilineText: this.table.multilineText,
    wrapText: this.textWrap
  });
};

/**
 * Override this function to install a specific compare function on a column instance.
 * The default impl. installs a generic comparator working with less than and greater than.
 *
 * @returns whether or not it was possible to install a compare function. If not, client side sorting is disabled.
 */
scout.Column.prototype.installComparator = function() {
  return this.comparator.install(this.session);
};

/**
 * @returns whether or not it is possible to sort this column.
 * As a side effect a comparator is installed.
 */
scout.Column.prototype.isSortingPossible = function() {
  // If installation fails sorting is still possible (in case of the text comparator just without a collator)
  this.installComparator();
  return true;
};

scout.Column.prototype.compare = function(row1, row2) {
  var valueA = this.cellValueOrText(row1);
  var valueB = this.cellValueOrText(row2);
  return this.comparator.compare(valueA, valueB);
};

scout.Column.prototype.isVisible = function() {
  return this.displayable && this.visible;
};

scout.Column.prototype.setVisible = function(visible) {
  if (this.visible === visible) {
    return;
  }
  this._setVisible(visible);
};

scout.Column.prototype._setVisible = function(visible) {
  this.visible = visible;
  if (this.initialized) {
    this.table.onColumnVisibilityChanged(this);
  }
};

scout.Column.prototype.setDisplayable = function(displayable) {
  if (this.displayable === displayable) {
    return;
  }
  this._setDisplayable(displayable);
};

scout.Column.prototype._setDisplayable = function(displayable) {
  this.displayable = displayable;
  if (this.initialized) {
    this.table.onColumnVisibilityChanged(this);
  }
};

scout.Column.prototype.setAutoOptimizeWidth = function(autoOptimizeWidth) {
  if (this.autoOptimizeWidth === autoOptimizeWidth) {
    return;
  }
  this._setAutoOptimizeWidth(autoOptimizeWidth);
};

scout.Column.prototype._setAutoOptimizeWidth = function(autoOptimizeWidth) {
  this.autoOptimizeWidth = autoOptimizeWidth;
  this.autoOptimizeWidthRequired = autoOptimizeWidth;
  if (this.initialized) {
    this.table.columnLayoutDirty = true;
    this.table.invalidateLayoutTree();
  }
};

scout.Column.prototype.setMaxLength = function(maxLength) {
  this.maxLength = maxLength;
};

scout.Column.prototype.setTextWrap = function(textWrap) {
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
};

scout.Column.prototype.isContentValid = function(row) {
  return this.cell(row).isContentValid();
};

scout.Column.prototype._onTableColumnsChanged = function(event) {
  if (this.table.visibleColumns().indexOf(this) === 0) {
    this.tableNodeLevel0CellPadding = 28;
    this.expandableIconLevel0CellPadding = 13;
  } else {
    this.tableNodeLevel0CellPadding = 23;
    this.expandableIconLevel0CellPadding = 8;
  }
};

scout.Column.prototype._realWidthIfAvailable = function() {
  return this._realWidth || this.width;
};

//--- Event handling methods ---
scout.Column.prototype._createEventSupport = function() {
  return new scout.EventSupport();
};

scout.Column.prototype.trigger = function(type, event) {
  event = event || {};
  event.source = this;
  this.events.trigger(type, event);
};

scout.Column.prototype.one = function(type, func) {
  this.events.one(type, func);
};

scout.Column.prototype.on = function(type, func) {
  return this.events.on(type, func);
};

scout.Column.prototype.off = function(type, func) {
  this.events.off(type, func);
};

scout.Column.prototype.addListener = function(listener) {
  this.events.addListener(listener);
};

scout.Column.prototype.removeListener = function(listener) {
  this.events.removeListener(listener);
};

/**
 * Adds an event handler using {@link #one()} and returns a promise.
 * The promise is resolved as soon as the event is triggered.
 */
scout.Column.prototype.when = function(type) {
  return this.events.when(type);
};
