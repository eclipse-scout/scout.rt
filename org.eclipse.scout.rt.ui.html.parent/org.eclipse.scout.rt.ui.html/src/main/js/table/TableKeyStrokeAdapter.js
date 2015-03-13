scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TableFilterControlKeyStrokes(field));
  this.keyStrokes.push(new scout.TableControlKeyStrokes(field));
};
scout.inherits(scout.TableKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TableKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.TableKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  this.keyStrokes = this.keyStrokes.concat(this._field.tableControls);
  this.keyStrokes = this.keyStrokes.concat(this._field.menus);
};
