scout.TableStartCellEditKeyStroke = function(table) {
  scout.TableStartCellEditKeyStroke.parent.call(this);
  this.field = table;
  this.ctrl = true;
  this.which = [scout.keys.ENTER];
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var editPosition = event._editPosition;
    return this.field.$cell(editPosition.column, editPosition.row.$row);
  }.bind(this);
};
scout.inherits(scout.TableStartCellEditKeyStroke, scout.KeyStroke);

scout.TableStartCellEditKeyStroke.prototype._accept = function(event) {
  var accepted = scout.TableStartCellEditKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var $selectedRows = this.field.$selectedRows();
  if (!$selectedRows.length) {
    return false;
  }

  var position = this.field.nextEditableCellPosForRow(0, $selectedRows.first().data('row'));
  if (position) {
    event._editPosition = position;
    return true;
  } else {
    return false;
  }
};

scout.TableStartCellEditKeyStroke.prototype.handle = function(event) {
  var editPosition = event._editPosition;
  this.field.prepareCellEdit(editPosition.row.id, editPosition.column.id, true);
};
