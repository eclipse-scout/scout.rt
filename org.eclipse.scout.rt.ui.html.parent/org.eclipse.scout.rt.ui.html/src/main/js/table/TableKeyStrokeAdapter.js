scout.TableKeyStrokeAdapter = function(field) {
  scout.TableKeyStrokeAdapter.parent.call(this, field);

  this.keyStrokes.push(new scout.TableFilterControlKeyStrokes(field));
  this.keyStrokes.push(new scout.TableControlKeyStrokes(field));
};
scout.inherits(scout.TableKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.TableKeyStrokeAdapter.prototype.installModelKeystrokes = function() {
  scout.TableKeyStrokeAdapter.parent.prototype.installModelKeystrokes.call(this);
  if (this.keyStrokes.length > 0) {
    this.keyStrokes = this.keyStrokes.concat(this._field.tableControls);
  } else if (this._field.tableControls) {
    this.keyStrokes = this._field.tableControls;
  }
  if (this.keyStrokes.length > 0) {
    this.keyStrokes = this.keyStrokes.concat(this._field.menus);
  } else if (this._field.menus) {
    this.keyStrokes = this._field.menus;
  }
};
