/**
 * Button Layout, for fields with a button.
 */
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.AbstractLayout);

scout.ButtonLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.getSize(),
    containerInsets = htmlContainer.getInsets({includeMargin: true}); // includeMargin=true because of 'margin-left' in Button.css

  var buttonSize = containerSize.subtract(containerInsets);
  scout.graphics.setSize(this.button.$field, buttonSize);
};

scout.ButtonLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    fieldSize = scout.graphics.getSize(this.button.$field),
    containerInsets = htmlContainer.getInsets({includeMargin: true}), // includeMargin=true because of 'margin-left' in Button.css
    iconSize = new scout.Dimension(0, 0);

  if (this.button.iconId) {
    // FIXME AWE: (button) read size from image dynamically
    // maybe add event-handler when IMG is loaded and invalidate layout
    iconSize = new scout.Dimension(16, 16);
    if (this.button.label) {
      iconSize.width += 8; // add gap between text and icon
    }
  }

  var prefSize = fieldSize.add(containerInsets);
  prefSize.width += iconSize.width;
  prefSize.height = Math.max(prefSize.height, iconSize.height);

  return prefSize;
};
