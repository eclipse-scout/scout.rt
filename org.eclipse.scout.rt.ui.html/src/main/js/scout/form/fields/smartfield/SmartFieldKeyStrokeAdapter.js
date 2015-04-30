scout.SmartFieldKeyStrokeAdapter = function(field) {
  scout.SmartFieldKeyStrokeAdapter.parent.call(this, field);

  // FIXME AWE: (smart-field) key-strokes hier noch registrieren
  this.keyStrokes.push(new scout.SmartFieldBackspaceKeyStroke());
};
scout.inherits(scout.SmartFieldKeyStrokeAdapter, scout.ValueFieldKeyStrokeAdapter);


scout.SmartFieldBackspaceKeyStroke = function() {
  scout.StringFieldBackspaceKeyStroke.parent.call(this);
  this.keyStroke = 'Backspace';
  this.drawHint = false;
  this.initKeyStrokeParts();
};
scout.inherits(scout.SmartFieldBackspaceKeyStroke, scout.KeyStroke);

scout.SmartFieldBackspaceKeyStroke.prototype.handle = function(event) {};
