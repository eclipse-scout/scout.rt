scout.TableControlKeyStrokeAdapter = function(tableControl) {
  scout.TableControlKeyStrokeAdapter.parent.call(this, tableControl);

  this.registerKeyStroke(new scout.TableAdditionalControlsKeyStrokes(tableControl));
};

scout.inherits(scout.TableControlKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
