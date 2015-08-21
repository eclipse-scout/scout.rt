scout.TableSelectAllKeyStroke = function(table) {
  scout.TableSelectAllKeyStroke.parent.call(this);
  this.field = table;
  this.ctrl = true;
  this.which = [scout.keys.A];
  this.renderingHints.offset = 0;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return this.field.footer ? this.field.footer._$infoSelection.find('.info-button') : null;
  }.bind(this);
};
scout.inherits(scout.TableSelectAllKeyStroke, scout.KeyStroke);

scout.TableSelectAllKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableSelectAllKeyStroke.parent.prototype._accept.call(this, event);
  return accepted && this.field.rows.length !== this.field.selectedRows.length;
};

scout.TableSelectAllKeyStroke.prototype.handle = function(event) {
  this.field.selectAll();
};
