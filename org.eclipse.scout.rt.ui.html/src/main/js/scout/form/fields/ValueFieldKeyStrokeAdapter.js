scout.ValueFieldKeyStrokeAdapter = function(field) {
  scout.ValueFieldKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.ValueFieldAcceptDisplayTextKeyStroke(field));
};
scout.inherits(scout.ValueFieldKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

/* ---- KeyStrokes ---------------------------------------------------------- */

scout.ValueFieldAcceptDisplayTextKeyStroke = function(field) {
  scout.ValueFieldAcceptDisplayTextKeyStroke.parent.call(this);
  this._field = field;
  this.bubbleUp = true;
};
scout.inherits(scout.ValueFieldAcceptDisplayTextKeyStroke, scout.KeyStroke);

/**
 * @Override scout.KeyStroke
 */
scout.ValueFieldAcceptDisplayTextKeyStroke.prototype.handle = function(event) {
  this._field.displayTextChanged();
};

/**
 * @Override scout.KeyStroke
 */
scout.ValueFieldAcceptDisplayTextKeyStroke.prototype.accept = function(event) {
  return event && event.which === scout.keys.ENTER;
};
