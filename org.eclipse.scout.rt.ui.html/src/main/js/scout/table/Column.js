scout.Column = function() {};

scout.Column.prototype.init = function(model, session) {
  this.session = session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);

  // InitialWidth is only sent if it differs from width
  if (this.initialWidth === undefined) {
    this.initialWidth = this.width;
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
  scout.defaultValues.applyTo(cell, 'Cell');
  return cell;
};

scout.Column.prototype.buildCell = function(row) {
  var cell = this.table.cell(this, row);
  var text = this.table.cellText(this, row);
  if (!cell.htmlEnabled) {
    text = scout.strings.encode(text);
    if (this.table.multilineText) {
      text = scout.strings.nl2br(text, false);
    }
  }
  var iconId = cell.iconId;
  var icon = this._icon(row, iconId, !! text) || '';
  var cssClass = this._cssClass(row, cell);
  var tooltipText = this.table.cellTooltipText(this, row);
  var tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');
  var style = this.table.cellStyle(this, cell);

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
    cellHtml += '<div class="width-fix" style="max-width: ' + (this.width - this.table.cellHorizontalPadding - 2 /* unknown IE9 extra space */) + 'px; ' + '">';
    // same calculation in scout.Table.prototype.resizeColumn
  }
  cellHtml += content;
  if (scout.device.tableAdditionalDivRequired) {
    cellHtml += '</div>';
  }
  cellHtml += '</div>';
  return cellHtml;
};

scout.Column.prototype._icon = function(row, iconId, hasText) {
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
  }
  else {
    cssClass += ' image-icon';
    return '<img class="' + cssClass + '" src="' + icon.iconUrl + '">';
  }
};

scout.Column.prototype._cssClass = function(row, cell) {
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

  //TODO CGU cssClass is actually only sent for cells, should we change this in model? discuss with jgu
  if (cell.cssClass) {
    cssClass += ' ' + cell.cssClass;
  } else if (this.cssClass) {
    cssClass += ' ' + this.cssClass;
  }
  return cssClass;
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
  popup = new scout.CellEditorPopup(this, row, cell, this.session);
  popup.$anchor = $cell;
  popup.render(this.table.$data);
  popup.pack();
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
