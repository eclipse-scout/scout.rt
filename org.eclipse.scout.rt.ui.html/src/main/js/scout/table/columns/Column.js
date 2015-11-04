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
  this.minWidth = scout.Column.DEFAULT_MIN_WIDTH;
  this.showSeparator = true; // currently a UI-only property, defaults to true
};

scout.Column.DEFAULT_MIN_WIDTH = 80;
scout.Column.NARROW_MIN_WIDTH = 30; // for columns without text (icon, check box)

scout.Column.prototype.init = function(model) {
  this.session = model.session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // InitialWidth is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = scout.helpers.nvl(this.width, 0);
  }

  if (this.aggregationFunction) {
    this.setAggregationFunction(this.aggregationFunction);
  }

};

/**
 * Converts the cell if it is of type string to an object with
 * a property 'text' with the original value.
 *
 * Example:
 * 'My Company' --> { text: 'MyCompany'; }
 *
 * @see JsonCell.java
 */
scout.Column.prototype.initCell = function(cell) {
  if (typeof cell === 'string') {
    cell = {
      text: cell
    };
  }
  // server sends cell.value only if it differs from text -> make sure cell.value is set and has the right type
  if (cell.value === undefined) {
    // Cell.value may be undefined for other column types -> use table.cellValue to access the value.
    // The only reason is to save some memory (may get obsolete in the future)
    if (this.type === 'number') {
      if (cell.text) { // Number('') would generate 0 -> don't set in that case
        cell.value = Number(cell.text);
      }
    }
  }
  scout.defaultValues.applyTo(cell, 'Cell');
  return cell;
};

scout.Column.prototype.buildCellForRow = function(row) {
  var cell = this.table.cell(this, row);
  return this.buildCell(cell, row);
};

scout.Column.prototype.buildCell = function(cell, row) {
  var text = cell.text || '';
  if (!cell.htmlEnabled) {
    text = scout.strings.encode(text);
    if (this.table.multilineText) {
      text = scout.strings.nl2br(text, false);
    }
  }
  var iconId = cell.iconId;
  var icon = this._icon(iconId, !!text) || '';
  var cssClass = this._cellCssClass(cell);
  var style = this._cellStyle(cell);
  var tooltipText = cell.tooltipText || '';
  var tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

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
  cellHtml += '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>';
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

  //TODO CGU cssClass is actually only sent for cells, should we change this in model? discuss with jgu
  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
  } else if (this.cssClass) {
    cssClass += ' ' + this.cssClass;
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
  style += scout.helpers.legacyStyle(cell);
  return style;
};

scout.Column.prototype.cellTextForGrouping = function(row) {
  return this.table.cellText(this, row);
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row'),
    cell = this.table.cell(this, row);

  if (this.table.enabled && row.enabled && cell.editable && !event.ctrlKey && !event.shiftKey) {
    this.table.prepareCellEdit(row.id, this.id, true);
  }
};

scout.Column.prototype.startCellEdit = function(row, fieldId) {
  var popup,
    $row = row.$row,
    cell = this.table.cell(this, row),
    $cell = this.table.$cell(this, $row);

  cell.field = this.session.getOrCreateModelAdapter(fieldId, this.table);
  // Override field alignment with the cell's alignment
  cell.field.gridData.horizontalAlignment = cell.horizontalAlignment;

  popup = scout.create(scout.CellEditorPopup, {
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
 * Returns the cell value to be used for grouping and filtering (chart, column filter).
 */
scout.Column.prototype.cellValueForGrouping = function(row) {
  var cell = this.table.cell(this, row);
  if (cell.value !== undefined) {
    return cell.value;
  }
  if (!cell.text) {
    return null;
  }
  return this._prepareTextForGrouping(cell.text, cell.htmlEnabled);
};

/**
 * Removes html tags, converts to single line, removes leading and trailing whitespaces.
 */
scout.Column.prototype._prepareTextForGrouping = function(text, htmlEnabled) {
  if (htmlEnabled) {
    // remove html tags
    text = scout.strings.plainText(text);
  }

  // convert to single line
  text = text.replace('\n', ' ');
  text = text.trim();
  return text;
};

scout.Column.prototype.setAggregationFunction = function(func) {
  this.aggregationFunction = func;
  if (func === 'sum') {
    this.aggrStart = scout.aggregation.sumStart;
    this.aggrStep = scout.aggregation.sumStep;
    this.aggrFinish = scout.aggregation.sumFinish;
    this.aggrSymbol = scout.aggregation.sumSymbol;
  } else if (func === 'avg') {
    this.aggrStart = scout.aggregation.avgStart;
    this.aggrStep = scout.aggregation.avgStep;
    this.aggrFinish = scout.aggregation.avgFinish;
    this.aggrSymbol = scout.aggregation.avgSymbol;
  } else if (func === 'min') {
    this.aggrStart = scout.aggregation.minStart;
    this.aggrStep = scout.aggregation.minStep;
    this.aggrFinish = scout.aggregation.minFinish;
    this.aggrSymbol = scout.aggregation.minSymbol;
  } else if (func === 'max') {
    this.aggrStart = scout.aggregation.maxStart;
    this.aggrStep = scout.aggregation.maxStep;
    this.aggrFinish = scout.aggregation.maxFinish;
    this.aggrSymbol = scout.aggregation.maxSymbol;
  }
};
