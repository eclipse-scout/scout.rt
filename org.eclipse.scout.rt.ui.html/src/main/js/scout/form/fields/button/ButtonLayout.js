/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this, button);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.FormFieldLayout);

scout.ButtonLayout.prototype.naturalSize = function() {
  var fieldSize = scout.graphics.prefSize(this.button.$field, true),
    iconSize = new scout.Dimension(0, 0);

  if (this.button.iconId) {
    // FIXME AWE: (button) read size from image dynamically
    // maybe add event-handler when IMG is loaded and invalidate layout
    iconSize = new scout.Dimension(16, 16);
    if (this.button.label) {
      iconSize.width += 8; // add gap between text and icon
    }
  }

  var prefSize = fieldSize;
  prefSize.width += iconSize.width;
  prefSize.height = Math.max(prefSize.height, iconSize.height);

  return prefSize;
};
