scout.CellEditorPopupLayout = function(cellEditorPopup) {
  scout.CellEditorPopupLayout.parent.call(this);
  this.cellEditorPopup = cellEditorPopup;
  this._field = cellEditorPopup.cell.field;
  this._htmlContainer = cellEditorPopup.htmlComp;
};
scout.inherits(scout.CellEditorPopupLayout, scout.AbstractLayout);

scout.CellEditorPopupLayout.prototype.layout = function($container) {
  var size, prefSize, popupSize,
    htmlField = this._field.htmlComp;

  size = this._htmlContainer.getAvailableSize()
    .subtract(this._htmlContainer.getInsets())
    .subtract(htmlField.getMargins());
  htmlField.setSize(size);
};

scout.CellEditorPopupLayout.prototype.preferredLayoutSize = function($container) {
  var cellBounds, rowBounds, margin,
    $row = this.cellEditorPopup.row.$row,
    $cell = this.cellEditorPopup.$anchor;

  cellBounds = scout.graphics.bounds($cell, false, true);
  rowBounds = scout.graphics.bounds($row, false, true);
  margin = this.cellEditorPopup.$container.cssMarginLeft();  
  if (margin < 0) {
    // extend the width if the popup has a negative margin (used for the first cell)
    cellBounds.width = cellBounds.width + -margin;
  }
  return new scout.Dimension(cellBounds.width, rowBounds.height);
};
