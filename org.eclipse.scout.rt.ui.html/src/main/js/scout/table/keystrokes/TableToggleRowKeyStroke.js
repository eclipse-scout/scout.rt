scout.TableToggleRowKeyStroke = function(table) {
  scout.TableToggleRowKeyStroke.parent.call(this);
  this.field = table;

  this.which = [scout.keys.SPACE];
  this.stopPropagation = true;
  this.renderingHints.render = false;
};
scout.inherits(scout.TableToggleRowKeyStroke, scout.KeyStroke);

scout.TableToggleRowKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableToggleRowKeyStroke.parent.prototype._accept.call(this, event);
  return accepted &&
    this.field.checkable &&
    this.field.$selectedRows().length;
};

scout.TableToggleRowKeyStroke.prototype.handle = function(event) {
  var $selection = this.field.$selectedRows();

  var checked = !$selection.first().data('row').checked;
  var table = this.field;
  $selection.each(function() {
    table.checkRow($(this).data('row'), checked);
  });
};
