scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);

  this.registerKeyStroke(new scout.TableFilterControlKeyStrokes(field));
  this.registerKeyStroke(new scout.TableControlKeyStrokes(field));
  this.registerKeyStroke(new scout.TableStartCellEditKeyStroke(field));
  this.registerKeyStroke(new scout.TableSelectAllKeyStroke(field));
  this.registerKeyStroke(new scout.TableRefreshKeyStroke(field));
  this.registerKeyStroke(new scout.ContextMenuKeyStroke(field, field.onContextMenu, field));
};
scout.inherits(scout.TableKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TableKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.TableKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  this.keyStrokes = this.keyStrokes.concat(this._field.tableControls);
  this.keyStrokes = this.keyStrokes.concat(this._field.menus);
};

/* ---- KeyStrokes ---------------------------------------------------------- */

scout.TableStartCellEditKeyStroke = function(table) {
  scout.TableStartCellEditKeyStroke.parent.call(this);
  this._table = table;
  this.keyStroke = 'control-e';
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableStartCellEditKeyStroke, scout.KeyStroke);

/**
 * @Override
 */
scout.TableStartCellEditKeyStroke.prototype.handle = function(event) {
  var pos, $selectedRow;
  //TODO nbu refactor
  $selectedRow = this._table.$selectedRows().first();
  if ($selectedRow.length === 0) {
    return;
  }
  pos = this._table.nextEditableCellPosForRow(0, $selectedRow.data('row'));
  if (pos) {
    this._table.sendPrepareCellEdit(pos.row.id, pos.column.id);

    // Chrome focuses address bar with ctrl-e -> prevent
    event.preventDefault();
  }
};

/**
 * @Override
 */
scout.TableStartCellEditKeyStroke.prototype._drawKeyBox = function($container, drawedKeys) {
  var keyBoxText, $selectedRow, pos;
  if (!this.drawHint || !this.keyStroke) {
    return;
  }
  //TODO nbu refactor
  $selectedRow = this._table.$selectedRows().first();
  if ($selectedRow.length === 0) {
    return;
  }
  pos = this._table.nextEditableCellPosForRow(0, $selectedRow.data('row'));
  if (pos) {
    keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
    scout.keyStrokeBox.drawSingleKeyBoxItem(4, keyBoxText, $selectedRow, this.ctrl, this.alt, this.shift);
  }
};

scout.TableSelectAllKeyStroke = function(table) {
  scout.TableSelectAllKeyStroke.parent.call(this);
  this._table = table;
  this.keyStroke = 'control-a';
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableSelectAllKeyStroke, scout.KeyStroke);

/**
 * @Override
 */
scout.TableSelectAllKeyStroke.prototype.accept = function(event) {
  if (this._table.rows.length === this._table.selectedRows.length) {
    return false;
  }
  return scout.TableSelectAllKeyStroke.parent.prototype.accept.call(this, event);
};

/**
 * @Override
 */
scout.TableSelectAllKeyStroke.prototype.handle = function(event) {
  this._table.selectAll();
  event.preventDefault();
};

/**
 * @Override
 */
scout.TableSelectAllKeyStroke.prototype._drawKeyBox = function($container, drawedKeys) {
  if (this._table.rows.length === this._table.selectedRows.length) {
    return;
  }
  var keyBoxText;
  if (!this.drawHint || !this.keyStroke || !this._table.footer || this._table.footer._$infoSelection > 0) {
    return;
  }
  keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
  scout.keyStrokeBox.drawSingleKeyBoxItem(0, keyBoxText, this._table.footer._$infoSelection.find('.info-button'), this.ctrl, this.alt, this.shift);
};

scout.TableRefreshKeyStroke = function(table) {
  scout.TableRefreshKeyStroke.parent.call(this);
  this._table = table;
  this.keyStroke = 'F5';
  this.initKeyStrokeParts();
};
scout.inherits(scout.TableRefreshKeyStroke, scout.KeyStroke);

/**
 * @Override
 */
scout.TableRefreshKeyStroke.prototype.handle = function(event) {
  this._table.sendReload();
  event.preventDefault();
};

/**
 * @Override
 */
scout.TableRefreshKeyStroke.prototype._drawKeyBox = function($container, drawedKeys) {
  var keyBoxText;
  if (!this.drawHint || !this.keyStroke || !this._table.footer || this._table.footer._$infoSelection > 0) {
    return;
  }
  keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
  scout.keyStrokeBox.drawSingleKeyBoxItem(0, keyBoxText, this._table.footer._$infoLoad.find('.info-button'), this.ctrl, this.alt, this.shift);
};
