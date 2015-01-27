/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function() {
  scout.ButtonLayout.parent.call(this);
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
    textSize = scout.graphics.measureString($button.html());
  return new scout.Dimension(
      textSize.width + insets.left + insets.right,
      textSize.height + insets.top + insets.bottom);
};
