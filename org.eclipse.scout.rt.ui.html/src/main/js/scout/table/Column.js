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

scout.Column.prototype.buildCell = function(row) {
  var style, text, tooltipText, tooltip, cssClass, cell, icon, iconId,
    content = '';
  cell = this.table.cell(this, row);
  text = this.table.cellText(this, row);

  if (!cell.htmlEnabled) {
    text = scout.strings.encode(text);
  }
  if (this.table.multilineText) {
    text = scout.strings.nl2br(text, false);
  }
  iconId = cell.iconId;
  // Cell icon has priority over row icon
  // If there is no cell icon, the row icon is displayed if the column is the first one
  if (!iconId && row.iconId && this.table.columns.indexOf(this) === 0) {
    iconId = row.iconId;
    // row icon must always be left aligned (copy cell to not modify the original state)
    cell = $.extend({}, cell);
    cell.horizontalAlignment = -1;
  }
  icon = this._icon(row, iconId, !! text) || '';
  if (!text && !icon) {
    // If every cell of a row is empty the row would collapse, using nbsp makes sure the row is as height as the others even if it is empty
    content = '&nbsp;';
  } else {
    content = icon + text;
  }
  cssClass = this._cssClass(row, cell);
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');
  style = this.table.cellStyle(this, cell);
  if (cell.errorStatus) {
    row.hasError = true;
  }

  return '<div class="' + cssClass + '" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + content + '</div>';
};

scout.Column.prototype._icon = function(row, iconId, hasText) {
  var cssClass, iconChar;
  if (!iconId) {
    return;
  }
  cssClass = 'table-cell-icon';
  if (hasText) {
    cssClass += ' with-text';
  }
  if (scout.strings.startsWith(iconId, "font:")) {
    iconChar = iconId.substr(5);
    cssClass += ' font-icon';
    return '<span class="' + cssClass + '">' + iconChar + '</span>';
  } else {
    cssClass += ' image-icon';
    return '<img class="' + cssClass + '" src="' + iconId + '">';
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
  if (!cell) {
    // gui only columns don't have cells
    return cssClass;
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
    this.table.sendPrepareCellEdit(row.id, this.id);
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
  popup.alignTo();
  popup.pack();
  return popup;
};
