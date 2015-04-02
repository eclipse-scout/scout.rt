scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TableFilterControlKeyStrokes(field));
  this.keyStrokes.push(new scout.TableControlKeyStrokes(field));
  this.keyStrokes.push(new scout.TableStartCellEditKeyStroke(field));
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
