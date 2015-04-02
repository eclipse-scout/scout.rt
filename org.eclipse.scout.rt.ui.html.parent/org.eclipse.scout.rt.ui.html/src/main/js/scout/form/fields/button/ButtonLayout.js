/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.AbstractLayout);

scout.ButtonLayout.prototype.layout = function($container) {
  // button has no children - nothing to do here
  var $button = $container.children('button');
  $button.css('width', '100%');
};

scout.ButtonLayout.prototype.preferredLayoutSize = function($container) {
  var $button = $container.children('button'),
    insets = scout.graphics.getInsets($button),
    label,
    labelSize = new scout.Dimension(0, 0),
    iconSize = new scout.Dimension(0, 0),
    contentSize,
    gap = 0,
    hasLabel = false,
    hasIcon = false;

  if (this.button.label) {
    label = scout.strings.removeAmpersand(this.button.label);
    labelSize = scout.graphics.measureString(label);
    hasLabel = true;
  }
  if (this.button.iconId) {
    // FIXME AWE: (button) read size from image dynamically
    // maybe add event-handler when IMG is loaded and invalidate layout
    iconSize = new scout.Dimension(16, 16);
    hasIcon = true;
  }
  if (hasLabel && hasIcon) {
    gap = 8;
  }
  contentSize = new scout.Dimension(
      labelSize.width + gap + iconSize.width,
      Math.max(labelSize.height, iconSize.height)
  );
  return new scout.Dimension(
      contentSize.width + insets.left + insets.right,
      contentSize.height + insets.top + insets.bottom);
};
