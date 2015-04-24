scout.ButtonKeyStrokeAdapter = function(field) {
  scout.ButtonKeyStrokeAdapter.parent.call(this, field);
  this.keyStrokes.push(new scout.ButtonKeyStroke(field,'ENTER'));
  this.keyStrokes.push(new scout.ButtonKeyStroke(field,'SPACE'));
};

scout.inherits(scout.ButtonKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);

