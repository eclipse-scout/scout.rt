scout.ButtonKeyStrokeAdapter = function(field) {
  scout.ButtonKeyStrokeAdapter.parent.call(this, field);
  this.registerKeyStroke(new scout.ButtonKeyStroke(field, 'ENTER'));
  this.registerKeyStroke(new scout.ButtonKeyStroke(field, 'SPACE'));
};

scout.inherits(scout.ButtonKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);
