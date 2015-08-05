scout.CellEditorPopupKeyStrokeAdapter = function(cellEditorPopup) {
  scout.CellEditorPopupKeyStrokeAdapter.parent.call(this, cellEditorPopup);

  this.registerKeyStroke(new scout.CellEditorCompleteEditKeyStroke(cellEditorPopup));
  this.registerKeyStroke(new scout.CellEditorCancelEditKeyStroke(cellEditorPopup));
  this.registerKeyStroke(new scout.CellEditorTabKeyStroke(cellEditorPopup));
};
scout.inherits(scout.CellEditorPopupKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

/* ---- KeyStrokes ---------------------------------------------------------- */

scout.CellEditorCompleteEditKeyStroke = function(popup) {
  scout.CellEditorCompleteEditKeyStroke.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.CellEditorCompleteEditKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorCompleteEditKeyStroke.prototype.handle = function(event) {
  this._popup.completeEdit();
};

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorCompleteEditKeyStroke.prototype.accept = function(event) {
  return event && event.which === scout.keys.ENTER;
};

scout.CellEditorCancelEditKeyStroke = function(popup) {
  scout.CellEditorCancelEditKeyStroke.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.CellEditorCancelEditKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorCancelEditKeyStroke.prototype.handle = function(event) {
  this._popup.cancelEdit();
};

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorCancelEditKeyStroke.prototype.accept = function(event) {
  return event && event.which === scout.keys.ESC;
};

scout.CellEditorTabKeyStroke = function(popup) {
  scout.CellEditorCompleteEditKeyStroke.parent.call(this);
  this._popup = popup;
};
scout.inherits(scout.CellEditorTabKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorTabKeyStroke.prototype.handle = function(event) {
  if (this._popup.completeEditRequested) {
    // Make sure events (complete, prepare) don't get sent twice since it will lead to exceptions
    // This may happen if user presses and holds the tab key
    return;
  }
  var pos,
    backwards = event.shiftKey,
    table = this._popup.table,
    column = this._popup.column,
    row = this._popup.row;

  this._popup.completeEdit();
  this._popup.completeEditRequested = true;

  pos = table.nextEditableCellPos(column, row, backwards);
  if (pos) {
    table.prepareCellEdit(pos.row.id, pos.column.id);
  }

  // Prevent default tabbing
  event.preventDefault();
};

/**
 * @Override scout.KeyStroke
 */
scout.CellEditorTabKeyStroke.prototype.accept = function(event) {
  return event.which === scout.keys.TAB;
};
