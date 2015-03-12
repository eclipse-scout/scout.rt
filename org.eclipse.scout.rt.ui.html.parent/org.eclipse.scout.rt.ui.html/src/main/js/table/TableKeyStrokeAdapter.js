scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TableFilterControlKeyStrokes(field));
  this.keyStrokes.push(new scout.TableControlKeyStrokes(field));
};
scout.inherits(scout.TableKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);
