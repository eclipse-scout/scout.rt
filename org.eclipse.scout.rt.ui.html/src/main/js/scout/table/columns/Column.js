/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Column = function() {
  this.cssClass;
  this.editable = false;
  this.removable = false;
  this.modifiable = false;
  this.fixedWidth = false;
  this.horizontalAlignment = -1;
  this.htmlEnabled = false;
  this.mandatory = false;
  this.sortActive = false;
  this.sortAscending = true;
  this.sortIndex = -1;
  this.summary = false;
  this.type = 'text';
  this.width = 60;
  this.minWidth = scout.Column.DEFAULT_MIN_WIDTH;
  this.showSeparator = true; // currently a UI-only property, defaults to true
  this.textWrap = false;
  this.filterType = 'TextColumnUserFilter';
  this.comparator = scout.comparators.TEXT;
  this.displayable = true;
  this.visible = true;
  this.textBased = true;
};

scout.Column.DEFAULT_MIN_WIDTH = 50;
scout.Column.NARROW_MIN_WIDTH = 32; // for columns without text (icon, check box)

scout.Column.prototype.init = function(model) {
  this.session = model.session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // Initial width is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = scout.nvl(this.width, 0);
  }
  scout.texts.resolveTextProperty(this);
  this._init(model);
};

/**
 * Override this function in order to implement custom init logic.
 * The default impl. does nothing.
 */
scout.Column.prototype._init = function(model) {
  // NOP
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

    // If a text but no value is provided, parse the text and set the result as value.
    // Otherwise, the (null) value would be formatted later and the provided text replaced with ''
    if (cell.text && cell.value === undefined) {
      cell.value = this._parseValue(cell.text);
    }
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
    returned
      .then(function(text) {
        this.setCellText(row, text, cell);
      }.bind(this), function(error) {
        this.setCellText(row, '', cell);
        $.log.error('Could not resolve cell text for value ' + value, error);
      }.bind(this));
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

  var text = cell.text || '';
  if (!cell.htmlEnabled) {
    text = cell.encodedText() || '';
    if (this.table.multilineText) {
      text = scout.strings.nl2br(text, false);
    }
  }
  var iconId = cell.iconId;
  var icon = this._icon(iconId, !!text) || '';
  var cssClass = this._cellCssClass(cell);
  var style = this._cellStyle(cell);

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

scout.Column.prototype._cellCssClass = function(cell) {
  var cssClass = 'table-cell';
  if (this.mandatory) {
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
  var columnPosition = visibleColumns.indexOf(this);
  if (columnPosition === 0) {
    cssClass += ' first';
  }
  if (columnPosition === visibleColumns.length - 1) {
    cssClass += ' last';
  }

  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
  }
  return cssClass;
};

scout.Column.prototype._cellStyle = function(cell) {
  var style,
    width = this.width;

  if (width === 0) {
    return 'display: none;';
  }

  style = 'min-width: ' + width + 'px; max-width: ' + width + 'px; ';
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
  return this.table.enabled && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey;
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
  if (!value) {
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

  cell.setValue(value);
  this._updateCellText(row, cell);
};

scout.Column.prototype.setCellText = function(row, text, cell) {
  if (!cell) {
    cell = this.cell(row);
  }
  cell.setText(text);

  // Don't update row while initializing (it is either added to the table later, or being added / updated right now)
  if (row.initialized) {
    this.table.updateRow(row);
  }
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
  // Prepare a temporary container that is not (yet) part of the DOM to prevent
  // expensive "forced reflow" while adding the cell divs. Only after all cells
  // are rendered, the container is added to the DOM.
  var $tmp = this.table.$data.makeDiv('invisible');
  var addDivForMeasurement = function($div) {
    $div.removeAttr('style').appendTo($tmp);
  };

  // Create divs for all relevant cells of the column
  addDivForMeasurement(this.$header.clone()); // header
  this.table.rows.forEach(function(row) {
    addDivForMeasurement($(this.buildCellForRow(row))); // model rows
  }, this);
  this.table._aggregateRows.forEach(function(row) {
    addDivForMeasurement($(this.buildCellForAggregateRow(row))); // aggregate rows
  }, this);

  // Add to DOM and measure optimal width
  this.table.$data.append($tmp);
  var optimalWidth = this.minWidth;
  $tmp.children().each(function() {
    optimalWidth = Math.max(optimalWidth, scout.graphics.getSize($(this)).width);
  });

  // Remove
  $tmp.remove();

  return optimalWidth;
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
 * @returns a field instance used as editor when a cell of this column is in edit mode.
 */
scout.Column.prototype.createDefaultEditor = function(row) {
  var field = this._createDefaultEditor(row);
  field.setLabelVisible(false);
  field.setValue(this.cell(row).value);
  return field;
};

scout.Column.prototype._createDefaultEditor = function() {
  return scout.create('StringField', {
    parent: this.table
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
 * @returns whether or not this column can be used to sort on the client side. In a JS only the flag 'uiSortPossible'
 *     is never set and defaults to true. As a side effect of this function a comparator is installed on each column.
 *     In a remote app the server sets the 'uiSortPossible' flag, which decides if the column must be sorted by the
 *     server or can be sorted by the client.
 */
scout.Column.prototype.isUiSortPossible = function() {
  var uiSortPossible = scout.nvl(this.uiSortPossible, true);
  return uiSortPossible && this.installComparator();
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
  this.table.onColumnVisibilityChanged(this);
};

scout.Column.prototype.setDisplayable = function(displayable) {
  if (this.displayable === displayable) {
    return;
  }
  this._setDisplayable('displayable', displayable);
};

scout.Column.prototype._setDisplayable = function(displayable) {
  this.displayable = displayable;
  this.table.onColumnVisibilityChanged(this);
};

scout.Column.prototype.isContentValid = function(row) {
  return this.cell(row).isContentValid();
};
