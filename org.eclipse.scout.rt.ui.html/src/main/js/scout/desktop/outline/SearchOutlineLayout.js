scout.SearchOutlineLayout = function(outline) {
  scout.SearchOutlineLayout.parent.call(this, outline);
  this.outline = outline;
};
scout.inherits(scout.SearchOutlineLayout, scout.OutlineLayout);

scout.SearchOutlineLayout.prototype._setDataHeight = function(heightOffset) {
  // Add search panel height to heightOffset
  var searchPanelSize = scout.graphics.getSize(this.outline.$searchPanel, true);
  heightOffset += searchPanelSize.height;

  scout.SearchOutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset);
};
