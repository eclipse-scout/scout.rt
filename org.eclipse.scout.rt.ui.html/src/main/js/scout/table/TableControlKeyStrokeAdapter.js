scout.TableControlKeyStrokeAdapter = function(field) {
  scout.TableControlKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TableAdditionalControlsKeyStrokes(field));
};

scout.inherits(scout.TableControlKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
