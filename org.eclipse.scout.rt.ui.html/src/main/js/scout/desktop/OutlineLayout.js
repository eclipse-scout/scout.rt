scout.OutlineLayout = function(outline) {
  scout.OutlineLayout.parent.call(this, outline);
  this.outline = outline;
};
scout.inherits(scout.OutlineLayout, scout.TreeLayout);

scout.OutlineLayout.prototype._setDataHeight = function(heightOffset) {
  // Add title height to heightOffset
  if (this.outline.titleVisible) {
    var titleSize = scout.graphics.getSize(this.outline.$title, true);
    heightOffset += titleSize.height;
  }

  scout.OutlineLayout.parent.prototype._setDataHeight.call(this, heightOffset);
};
