scout.CellEditorPopupKeyStrokeAdapter = function(popup) {
  scout.CellEditorPopupKeyStrokeAdapter.parent.call(this, popup);

  this.keyStrokes.push(new scout.CellEditorCompleteEditKeyStroke(popup));
  this.keyStrokes.push(new scout.CellEditorCancelEditKeyStroke(popup));
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
