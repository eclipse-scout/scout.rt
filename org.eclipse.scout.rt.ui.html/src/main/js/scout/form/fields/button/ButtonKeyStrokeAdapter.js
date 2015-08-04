scout.ButtonKeyStrokeAdapter = function(button) {
  scout.ButtonKeyStrokeAdapter.parent.call(this, button);
  this.registerKeyStroke(new scout.ButtonKeyStroke(button, 'ENTER'));
  this.registerKeyStroke(new scout.ButtonKeyStroke(button, 'SPACE'));
};

scout.inherits(scout.ButtonKeyStrokeAdapter, scout.FormFieldKeyStrokeAdapter);
